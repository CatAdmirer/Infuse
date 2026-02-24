package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
import org.jetbrains.annotations.Nullable;

public class Thief extends InfuseEffect {
    private static final Map<UUID, DisguiseData> disguisedPlayers = new HashMap<>();
    private InfuseEffect stolen = null;

    public Thief() {
        super(EffectIds.THIEF, "thief", false);
    }

    public Thief(boolean augmented) {
        super(EffectIds.THIEF, "thief", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_THIEF_NAME.toComponent() : Messages.THIEF_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_THIEF_LORE.getComponentList() : Messages.THIEF_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Thief(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Thief(false);
    }

    // Hiding a thief user from the rest of the players online
    @Override
    public void equip(Infuse plugin, Player player) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            otherPlayer.unlistPlayer(player);
            otherPlayer.hidePlayer(plugin, player);
        }
    }
    
    @Override
    public void unequip(Infuse plugin, Player player) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            otherPlayer.listPlayer(player);
            otherPlayer.showPlayer(plugin, player);
        }
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "thief")) return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown);
    }

    /**
     * Disguises a thief user into another player.
     * Overrides the thief user's name and skin.
     * 
     * @param thiefUser The thief user to disguise
     * @param player The player to disguise the thief as
     */
    private void disguise(Infuse plugin, Player thiefUser, Player player) {
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
        BossBar bossBar = BossBar.bossBar(Component.text("Disguise"), 1, BossBar.Color.PINK, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(thiefUser);

        // Starting the task to update the bossbar and eventually revert the disguise.
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            long timeLeft = disguiseEndTime - System.currentTimeMillis();

            if (timeLeft < 0 || timeLeft / 3600.0 < 0) {
                removeDisguise(thiefUser);
                bossBar.removeViewer(thiefUser);
                task.cancel();
                return;
            }

            bossBar.progress(timeLeft / 3600.0f);
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


    @Nullable
    public InfuseEffect getStolenEffect() {
        return stolen;
    }

    private void activateEffect(Infuse plugin, Player player, @NotNull InfuseEffect stolen, Entity victim) {
        String msg = Messages.THIEF_STEAL.getMessage();
        msg = msg.replace("%player%", victim.getName());
        msg = msg.replace("%effect_name%", stolen.getName());
        player.sendMessage(Messages.toComponent(msg));

        // Saving the stolen effect
        this.stolen = stolen;

        // Activating the stolen spark.
        stolen.activateSpark(plugin, player);

        UUID playerUUID = player.getUniqueId();

        // Removing cooldowns from the stolen spark
        CooldownManager.clearSpecificCooldown(playerUUID, stolen.getRegularForm().getKey());
        CooldownManager.clearSpecificDuration(playerUUID, stolen.getRegularForm().getKey());

        // Applying cooldowns for the thief effect
        long cooldown = plugin.getConfigFile().cooldown(stolen);
        long duration = plugin.getConfigFile().duration(stolen);

        CooldownManager.setDuration(playerUUID, "thief_stolen", duration);
        CooldownManager.setCooldown(playerUUID, "thief_stolen", cooldown * 2);

        // Clearing the stolen effect after the spark is done
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            this.stolen = null;
        }, duration * 20);
    }

    private record DisguiseData(Component customName, Component displayName, boolean customNameVisible, PlayerTextures skin) {}

    public static class Listeners implements Listener {
        private final Infuse plugin;
        private final Thief effect = new Thief();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }

        // Hiding thief effect users from players who recently joined
        @EventHandler
        public void hideThievesOnJoin(PlayerJoinEvent event) {
            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!plugin.getDataManager().hasEffect(otherPlayer, effect)) continue;
                
                event.getPlayer().unlistPlayer(otherPlayer);
            }
        }

        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            Player deadPlayer = event.getEntity();
            
            // If a disguised player dies, revert their disguise
            if (disguisedPlayers.containsKey(deadPlayer.getUniqueId())) effect.removeDisguise(deadPlayer);

            if (!(event.getDamageSource().getCausingEntity() instanceof Player killer)) return;

            // If a player with the thief effect kills someone, they should disguise themselves as the player they kill
            if (plugin.getDataManager().hasEffect(killer, effect)) {
                effect.disguise(plugin, killer, deadPlayer);
            }
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
                effect.removeDisguise(player);
            }
        }

        @EventHandler
        public void onPlayerHit(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player victim)) return;
            if (!(event.getDamager() instanceof Player player)) return;
            if (!plugin.getDataManager().hasEffect(player, effect)) return;

            UUID playerUUID = player.getUniqueId();
            if (!CooldownManager.isEffectActive(playerUUID, "thief")) return;

            InfuseEffect leftEffect = plugin.getDataManager().getEffect(victim.getUniqueId(), "1");
            InfuseEffect rightEffect = plugin.getDataManager().getEffect(victim.getUniqueId(), "2");

            if (leftEffect != null && rightEffect != null) {
                effect.activateEffect(plugin, player, Math.random() > 0.5 ? leftEffect : rightEffect, victim);
            } else if (leftEffect != null) {
                effect.activateEffect(plugin, player, leftEffect, victim);
            } else if (rightEffect != null) {
                effect.activateEffect(plugin, player, rightEffect, victim);
            } else return;

            CooldownManager.setDuration(playerUUID, "thief", 0);
        }
    }
}