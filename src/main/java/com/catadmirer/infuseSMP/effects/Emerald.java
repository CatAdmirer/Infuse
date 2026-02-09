package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.kyori.adventure.text.Component;
import java.util.List;
import java.util.UUID;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Emerald extends InfuseEffect {
    public Emerald() {
        super(1, "emerald", false);
    }

    public Emerald(boolean augmented) {
        super(1, "emerald", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_EMERALD_NAME.toComponent() : Messages.EMERALD_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_EMERALD_LORE.getComponentList() : Messages.EMERALD_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Emerald(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Emerald(false);
    }

    @Override
    public void equip(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffect.INFINITE_DURATION, 2, false, false));
    }

    @Override
    public void unequip(Player player) {
        player.removePotionEffect(PotionEffectType.LUCK);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "emerald")) return;

        // Applying effects for the emerald spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "emerald", duration);
        CooldownManager.setCooldown(playerUUID, "emerald", cooldown);
    }
}