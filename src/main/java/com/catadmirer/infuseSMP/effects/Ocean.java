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

public class Ocean extends InfuseEffect {
    public Ocean() {
        super(EffectIds.OCEAN, "ocean", false);
    }

    public Ocean(boolean augmented) {
        super(EffectIds.OCEAN, "ocean", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_OCEAN_NAME.toComponent() : Messages.OCEAN_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_OCEAN_LORE.getComponentList() : Messages.OCEAN_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Ocean(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Ocean(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "ocean")) return;

        // Applying effects for the ocean spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "ocean", duration);
        CooldownManager.setCooldown(playerUUID, "ocean", cooldown);
    }

    public class Listeners implements Listener {
        private final Infuse plugin;
        private final Ocean effect = new Ocean();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }
    }
}