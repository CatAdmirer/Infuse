package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Thunder extends InfuseEffect {
    public Thunder() {
        super(EffectIds.THUNDER, "thunder", false);
    }

    public Thunder(boolean augmented) {
        super(EffectIds.THUNDER, "thunder", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_THUNDER_NAME.toComponent() : Messages.THUNDER_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_THUNDER_LORE.getComponentList() : Messages.THUNDER_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Thunder(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Thunder(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "thunder")) return;

        // Applying effects for the thunder spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "thunder", duration);
        CooldownManager.setCooldown(playerUUID, "thunder", cooldown);
    }

    public class Listeners implements Listener {
        private final Infuse plugin;
        private final Thunder effect = new Thunder();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }
    }
}