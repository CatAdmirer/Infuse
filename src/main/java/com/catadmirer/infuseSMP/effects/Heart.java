package com.catadmirer.infuseSMP.effects;

import java.util.List;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;

import net.kyori.adventure.text.Component;

public class Heart extends InfuseEffect {
    public Heart() {
        super(EffectIds.HEART, "heart", false);
    }

    public Heart(boolean augmented) {
        super(EffectIds.HEART, "heart", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_HEART_NAME.toComponent() : Messages.HEART_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_HEART_LORE.getComponentList() : Messages.HEART_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Heart(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Heart(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffect.INFINITE_DURATION, 2, false, false));
    }

    @Override
    public void unequip(Infuse plugin, Player player) {
        player.removePotionEffect(PotionEffectType.LUCK);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "heart")) return;

        // Applying effects for the heart spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "heart", duration);
        CooldownManager.setCooldown(playerUUID, "heart", cooldown);
    }
}