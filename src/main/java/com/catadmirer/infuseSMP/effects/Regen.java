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

public class Regen extends InfuseEffect {
    public Regen() {
        super(EffectIds.REGEN, "regen", false);
    }

    public Regen(boolean augmented) {
        super(EffectIds.REGEN, "regen", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_REGEN_NAME.toComponent() : Messages.REGEN_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_REGEN_LORE.getComponentList() : Messages.REGEN_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Regen(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Regen(false);
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
        if (CooldownManager.isOnCooldown(playerUUID, "regen")) return;

        // Applying effects for the regen spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "regen", duration);
        CooldownManager.setCooldown(playerUUID, "regen", cooldown);
    }
}