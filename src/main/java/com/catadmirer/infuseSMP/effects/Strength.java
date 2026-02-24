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

public class Strength extends InfuseEffect {
    public Strength() {
        super(EffectIds.STRENGTH, "strength", false);
    }

    public Strength(boolean augmented) {
        super(EffectIds.STRENGTH, "strength", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_STRENGTH_NAME.toComponent() : Messages.STRENGTH_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_STRENGTH_LORE.getComponentList() : Messages.STRENGTH_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Strength(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Strength(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "strength")) return;

        // Applying effects for the strength spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "strength", duration);
        CooldownManager.setCooldown(playerUUID, "strength", cooldown);
    }

    public class Listeners implements Listener {
        private final Infuse plugin;
        private final Strength effect = new Strength();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }
    }
}