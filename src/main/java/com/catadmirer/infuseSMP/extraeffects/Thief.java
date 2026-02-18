package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

public class Thief implements Listener {
    private static Infuse plugin;

    private final Map<UUID, DisguiseData> disguisedPlayers = new HashMap<>();

    public Thief(Infuse plugin) {
        Thief.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (otherPlayer.canSee(player)) {
                otherPlayer.listPlayer(player);
            }
        }
    }

    // Hiding a thief user from the rest of the players online
    public static void equipThief(Player thiefUser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.unlistPlayer(thiefUser.getPlayer());
        }
    }

    // Hiding thief effect users from players who recently joined
    @EventHandler
    public void hideThievesOnJoin(PlayerJoinEvent event) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (!plugin.getDataManager().hasEffect(otherPlayer, EffectMapping.THIEF)) continue;
            
            event.getPlayer().unlistPlayer(otherPlayer);
        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "thief")) return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_THIEF : EffectMapping.THIEF);
        long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_THIEF : EffectMapping.THIEF);

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        
        // If a disguised player dies, revert their disguise
        if (disguisedPlayers.containsKey(deadPlayer.getUniqueId())) removeDisguise(deadPlayer);

        if (!(event.getDamageSource().getCausingEntity() instanceof Player killer)) return;

        // If a player with the thief effect kills someone, they should disguise themselves as the player they kill
        if (plugin.getDataManager().hasEffect(killer, EffectMapping.THIEF)) {
            disguise(killer, deadPlayer);
        }
    }

    /**
     * Disguises a thief user into another player.
     * Overrides the thief user's name and skin.
     * 
     * @param thiefUser The thief user to disguise
     * @param player The player to disguise the thief as
     */
    private void disguise(Player thiefUser, Player player) {
        // Storing the killer's original skin
        disguisedPlayers.put(thiefUser.getUniqueId(),
                new DisguiseData(thiefUser.customName(),
                    thiefUser.displayName(),
                    thiefUser.isCustomNameVisible(),
                    thiefUser.getPlayerProfile().getTextures()));

        // Taking the dead player's name
        thiefUser.customName(player.customName());
        thiefUser.displayName(player.displayName());
        thiefUser.setCustomNameVisible(player.isCustomNameVisible());

        // Taking the dead player's skin
        PlayerProfile profile = thiefUser.getPlayerProfile();
        profile.setTextures(player.getPlayerProfile().getTextures());
        thiefUser.setPlayerProfile(profile);

        long disguiseEndTime = System.currentTimeMillis() + 3600 * 1000; // 1 hour

        // Showing the disguise timer bossbar
        BossBar bossBar = Bukkit.createBossBar("Disguise", BarColor.PINK, BarStyle.SOLID);
        bossBar.setProgress(1);
        bossBar.addPlayer(thiefUser);

        // Starting the task to update the bossbar and eventually revert the disguise.
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            long timeLeft = disguiseEndTime - System.currentTimeMillis();

            if (timeLeft < 0 || timeLeft / 3600.0 < 0) {
                removeDisguise(thiefUser);
                bossBar.removePlayer(thiefUser);
                task.cancel();
                return;
            }

            bossBar.setProgress(timeLeft / 3600.0);
        }, 0, 20);
    }

    /**
     * Removes a disguise from a player.
     * Sets a player's skin and name to what they were before they disguised.
     * 
     * @param player The player to remove the disguise from
     */
    private void removeDisguise(Player player) {
        if (!disguisedPlayers.containsKey(player.getUniqueId())) return;

        // Getting the original data for the player
        DisguiseData originalData = disguisedPlayers.remove(player.getUniqueId());

        // Resetting the player's name
        player.customName(originalData.customName);
        player.displayName(originalData.displayName);
        player.setCustomNameVisible(originalData.customNameVisible);

        // Resetting the player's skin
        PlayerProfile profile = player.getPlayerProfile();
        profile.setTextures(originalData.skin);
        player.setPlayerProfile(profile);
    }

    /**
     * Removing an active disguise if a disguised player leaves.
     * 
     * @param event The {@link PlayerQuitEvent} to handle
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (disguisedPlayers.containsKey(player.getUniqueId())) {
            removeDisguise(player);
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.THIEF)) return;

        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isEffectActive(playerUUID, "thief")) return;

        EffectMapping leftEffect = plugin.getDataManager().getEffect(victim.getUniqueId(), "1");
        EffectMapping rightEffect = plugin.getDataManager().getEffect(victim.getUniqueId(), "2");

        if (leftEffect != null && rightEffect != null) {
            activateEffect(player, Math.random() > 0.5 ? leftEffect : rightEffect, victim);
        } else if (leftEffect != null) {
            activateEffect(player, leftEffect, victim);
        } else if (rightEffect != null) {
            activateEffect(player, rightEffect, victim);
        } else return;

        CooldownManager.setDuration(playerUUID, "thief", 0);
    }

    private void activateEffect(Player player, @NotNull EffectMapping effect, Entity victim) {
        String msg = Messages.THIEF_STEAL.getMessage();
        msg = msg.replace("%player%", victim.getName());
        msg = msg.replace("%effect_name%", effect.getName());
        player.sendMessage(Messages.toComponent(msg));

        // Activating the stolen spark.
        effect.activateSpark(player);

        UUID playerUUID = player.getUniqueId();

        // Removing cooldowns from the stolen spark
        CooldownManager.clearSpecificCooldown(playerUUID, effect.regular().getKey());
        CooldownManager.clearSpecificDuration(playerUUID, effect.regular().getKey());

        // Applying cooldowns for the thief effect
        long cooldown = plugin.getConfigFile().cooldown(effect);
        long duration = plugin.getConfigFile().duration(effect);

        CooldownManager.setDuration(playerUUID, "thief_stolen", duration);
        CooldownManager.setCooldown(playerUUID, "thief_stolen", cooldown * 2);
    }

    private record DisguiseData(Component customName, Component displayName, boolean customNameVisible, PlayerTextures skin) {}
}