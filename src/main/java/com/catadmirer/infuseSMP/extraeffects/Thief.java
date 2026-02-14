package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Thief extends InfuseEffect {
    private static final Map<UUID, DisguiseData> disguisedPlayers = new HashMap<>();

    private InfuseEffect stolenEffect;

    public Thief() {
        super(15, "thief", false);
    }

    public Thief(boolean augmented) {
        super(15, "thief", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_THIEF_NAME.toComponent() : Messages.THIEF_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_THIEF_LORE.getComponentList() : Messages.THIEF_LORE.getComponentList();
    }

    // Hiding a thief user from the rest of the players online
    @Override
    public void equip(Infuse plugin, Player thiefUser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.unlistPlayer(thiefUser);
            player.hidePlayer(plugin, thiefUser);
        }

        owner = thiefUser;
    }
    
    @Override
    public void unequip(Infuse plugin, Player thiefUser) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.listPlayer(thiefUser);
            player.showPlayer(plugin, thiefUser);
        }

        owner = thiefUser;
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Thief(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Thief(false);
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

    public void disguise(Infuse plugin, Player player) {
        // Handling if this effect's owner is null
        if (owner == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot apply a disguise with an unowned thief effect.", new IllegalStateException());
        }

        // Handling if this effect's owner is offline
        if (!owner.isOnline()) {
            Bukkit.getLogger().log(Level.SEVERE, new IllegalStateException(), () -> String.format("Cannot disguise %s because they are offline.", owner.getName()));
            return;
        }

        Player thiefUser = owner.getPlayer();

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
        BossBar bar = BossBar.bossBar(Component.text("Disguise"), 1, BossBar.Color.PINK, BossBar.Overlay.PROGRESS);
        bar.progress(1);
        bar.addViewer(thiefUser);

        // Starting the task to update the bossbar and eventually revert the disguise.
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            long timeLeft = disguiseEndTime - System.currentTimeMillis();

            if (timeLeft < 0 || timeLeft / 3600.0 < 0) {
                removeDisguise();
                bar.removeViewer(thiefUser);
                task.cancel();
                return;
            }

            bar.progress(timeLeft / 3600.0f);
        }, 0, 20);
    }

    /**
     * Removes a disguise from a player.
     * Sets a player's skin and name to what they were before they disguised.
     * 
     * @param player The player to remove the disguise from
     */
    public void removeDisguise() {
        // Handling if this effect's owner is null
        if (owner == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot steal an effect with an unowned thief effect.", new IllegalStateException());
        }

        // Handling if this effect's owner is offline
        if (!owner.isOnline()) {
            Bukkit.getLogger().log(Level.SEVERE, new IllegalStateException(), () -> String.format("%s cannot steal an effect because they are offline.", owner.getName()));
            return;
        }

        Player player = owner.getPlayer();

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

    public void stealEffect(Infuse plugin, @NotNull InfuseEffect toSteal) {
        // Handling if this effect's owner is null
        if (owner == null) {
            plugin.getLogger().log(Level.SEVERE, "Cannot steal an effect with an unowned thief effect.", new IllegalStateException());
        }

        // Handling if this effect's owner is offline
        if (!owner.isOnline()) {
            plugin.getLogger().log(Level.SEVERE, new IllegalStateException(), () -> String.format("%s cannot steal an effect because they are offline.", owner.getName()));
            return;
        }

        Player thiefUser = owner.getPlayer();

        // Handling if the stolen effect doesn't have an owner
        if (toSteal.getOwner() == null) {
            plugin.getLogger().log(Level.SEVERE, new IllegalStateException(), () -> String.format("%s cannot steal %s as it does not have an owner!", owner.getName(), toSteal.getKey()));
            return;
        }

        stolenEffect = toSteal;

        String msg = Messages.THIEF_STEAL.getMessage();
        msg = msg.replace("%player%", toSteal.getOwner().getName());
        msg = msg.replace("%effect_name%", toSteal.getName());
        thiefUser.sendMessage(Messages.toComponent(msg));

        // Activating the stolen spark.
        toSteal.activateSpark(plugin, thiefUser);

        UUID playerUUID = thiefUser.getUniqueId();

        // Removing cooldowns from the stolen spark
        CooldownManager.clearSpecificCooldown(playerUUID, toSteal.getName());
        CooldownManager.clearSpecificDuration(playerUUID, toSteal.getName());

        // Applying cooldowns for the thief effect
        long cooldown = plugin.getConfigFile().cooldown(toSteal);
        long duration = plugin.getConfigFile().duration(toSteal);

        CooldownManager.setDuration(playerUUID, "thief_stolen", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        // Removing the stolen effect when the effect is over
        Bukkit.getScheduler().runTaskLater(plugin, () -> setStolenEffect(null), duration * 20);
    }

    @Nullable
    public InfuseEffect getStolenEffect() {
        return stolenEffect;
    }

    private void setStolenEffect(InfuseEffect effect) {
        this.stolenEffect = effect;
    }

    public void activateEffect(Infuse plugin, Player player, @NotNull InfuseEffect effect, Entity victim) {
        String msg = Messages.THIEF_STEAL.getMessage();
        msg = msg.replace("%player%", victim.getName());
        msg = msg.replace("%effect_name%", effect.getName());
        player.sendMessage(Messages.toComponent(msg));

        // Activating the stolen spark.
        effect.activateSpark(plugin, player);

        UUID playerUUID = player.getUniqueId();

        // Removing cooldowns from the stolen spark
        CooldownManager.clearSpecificCooldown(playerUUID, effect.getName());
        CooldownManager.clearSpecificDuration(playerUUID, effect.getName());

        // Applying cooldowns for the thief effect
        long cooldown = plugin.getConfigFile().cooldown(effect);
        long duration = plugin.getConfigFile().duration(effect);

        CooldownManager.setDuration(playerUUID, "thief_stolen", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);
    }

    public record DisguiseData(Component customName, Component displayName, boolean customNameVisible, PlayerTextures skin) {}
}