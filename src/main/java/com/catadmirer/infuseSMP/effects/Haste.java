package com.catadmirer.infuseSMP.effects;

import java.util.List;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;

import net.kyori.adventure.text.Component;

public class Haste extends InfuseEffect {
    public Haste() {
        super(EffectIds.HASTE, "haste", false);
    }

    public Haste(boolean augmented) {
        super(EffectIds.HASTE, "haste", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_HASTE_NAME.toComponent() : Messages.HASTE_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_HASTE_LORE.getComponentList() : Messages.HASTE_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Haste(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Haste(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "haste")) return;

        // Applying effects for the haste spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "haste", duration);
        CooldownManager.setCooldown(playerUUID, "haste", cooldown);
    }

    public static class Listeners implements Listener {
        private final Infuse plugin;
        private final Haste effect = new Haste();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }
    }
}