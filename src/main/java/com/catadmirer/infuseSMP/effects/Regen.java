package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Regen implements Listener {
    private static Infuse plugin;

    public Regen(Infuse plugin) {
        Regen.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (EffectMapping.REGEN.hasEffect(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1, false, false));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityHeal(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (EffectMapping.REGEN.hasEffect(player)) {
                if (event.getFinalDamage() <= 0) {
                    player.setSaturation(Math.min(player.getSaturation() + 6, 20));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager) {
            if (CooldownManager.isEffectActive(damager.getUniqueId(), "regen")) {
                double damage = event.getFinalDamage();
                damager.setHealth(Math.min(damager.getHealth() + damage / 2, damager.getAttribute(Attribute.MAX_HEALTH).getValue()));
            }
        }
    }

    public static void activateSpark(final Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "regen")) {
            // Applying cooldowns and durations for the effect
            boolean isAugmented = plugin.getDataManager().getEffect(playerUUID, "1") == EffectMapping.AUG_REGEN || plugin.getDataManager().getEffect(playerUUID, "2") == EffectMapping.AUG_REGEN;
            long cooldown = plugin.getConfig("regen.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = plugin.getConfig("regen.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "regen", duration);
            CooldownManager.setCooldown(playerUUID, "regen", cooldown);

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        }
    }
}