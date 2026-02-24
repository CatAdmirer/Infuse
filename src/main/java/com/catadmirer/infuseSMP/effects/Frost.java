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

public class Frost extends InfuseEffect {
    public Frost() {
        super(EffectIds.FROST, "frost", false);
    }

    public Frost(boolean augmented) {
        super(EffectIds.FROST, "frost", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_FROST_NAME.toComponent() : Messages.FROST_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_FROST_LORE.getComponentList() : Messages.FROST_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Frost(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Frost(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "frost")) return;

        // Applying effects for the frost spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "frost", duration);
        CooldownManager.setCooldown(playerUUID, "frost", cooldown);
    }

    public static class Listeners implements Listener {
        private final Infuse plugin;
        private final Frost effect = new Frost();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }
    }
}