package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

public class Strength implements Listener {
    public static void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "strength")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            
            // Applying cooldowns and durations for the effect
            boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
            long cooldown = Infuse.getInstance().getConfig("strength.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = Infuse.getInstance().getConfig("strength.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "strength", duration);
            CooldownManager.setCooldown(playerUUID, "strength", cooldown);
        }
    }


    @EventHandler
    public void extraDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (EffectMapping.STRENGTH.hasEffect(attacker)) {
                double damage = event.getDamage();
                double health = attacker.getHealth();
                if (health < 2) {
                    event.setDamage(damage + 3);
                } else if (health < 4) {
                    event.setDamage(damage + 2);
                } else if (health < 6) {
                    event.setDamage(damage + 1);
                }
            }
        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (CooldownManager.isEffectActive(player.getUniqueId(), "strength") && !event.isCritical()) {
                double originalDamage = event.getDamage();
                double critDamage = originalDamage * 1.35;
                event.setDamage(critDamage);
                Entity hitEntity = event.getEntity();
                hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                hitEntity.getWorld().spawnParticle(Particle.CRIT, hitEntity.getLocation().add(0, hitEntity.getHeight() / 2, 0), 10);
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getBow() != null && event.getBow().getType() == Material.BOW && EffectMapping.STRENGTH.hasEffect(player)) {
                if (event.getProjectile() instanceof Arrow arrow) {
                    arrow.setPierceLevel(100);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamaeffectob(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (event.getEntity() instanceof LivingEntity entity) {
                if (entity instanceof Player) return;
                if (EffectMapping.STRENGTH.hasEffect(attacker)) {
                    double originalDamage = event.getDamage();
                    event.setDamage(originalDamage * 2);
                }
            }
        }

    }

    @EventHandler
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() == Material.SHIELD && player.isBlocking()) {
                if (event.getDamager() instanceof Player attacker) {
                    if (attacker.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE") && EffectMapping.STRENGTH.hasEffect(attacker)) {
                        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
                        Bukkit.getScheduler().runTaskLater(Infuse.getInstance(), () -> {
                            this.stunShield(player);
                        }, 20L);
                    }
                }
            }
        }

    }

    private void stunShield(Player player) {
        player.setCooldown(Material.SHIELD, 200);
    }
}
