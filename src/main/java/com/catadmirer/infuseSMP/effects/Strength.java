package com.catadmirer.infuseSMP.effects;

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
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.ItemUtil;

public class Strength implements Listener {
    private static Infuse plugin;
    
    public Strength(Infuse plugin) {
        Strength.plugin = plugin;
    }

    public static void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Skipping players on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "strength")) return;

        // Playing sounds
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = plugin.getEffectManager().getEffect(playerUUID, "1") == EffectMapping.AUG_STRENGTH || plugin.getEffectManager().getEffect(playerUUID, "2") == EffectMapping.AUG_STRENGTH;
        long cooldown = plugin.getConfig("strength.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = plugin.getConfig("strength.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "strength", duration);
        CooldownManager.setCooldown(playerUUID, "strength", cooldown);
    }

    @EventHandler
    public void extraDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Skipping players without the strength effect
        if (!EffectMapping.STRENGTH.hasEffect(attacker)) return;

        // Boosting damage based on the attacker's health.
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

    /** Automatically crits while the strength spark is active */
    @EventHandler
    public void strengthSparkAutoCrit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        // Fabrictating crits if the spark is active and it wasn't already a critical hit.
        if (CooldownManager.isEffectActive(player.getUniqueId(), "strength") && !event.isCritical()) {
            // Changing the damage to that of a crit.
            double originalDamage = event.getDamage();
            double critDamage = originalDamage * 1.35;
            event.setDamage(critDamage);
            
            // Playing the crit noise and spawing crit particles.
            Entity hitEntity = event.getEntity();
            hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            hitEntity.getWorld().spawnParticle(Particle.CRIT, hitEntity.getLocation().add(0, hitEntity.getHeight() / 2, 0), 10);
        }
    }

    /** Boosts the piercing level of any arrow to 100 for players with the strength effect. */
    @EventHandler
    public void strengthHighPiercing(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Making sure the shooter has the strength effect
        if (!EffectMapping.STRENGTH.hasEffect(player)) return;

        // Increasing the piercing level of the shot arrow.
        if (event.getProjectile() instanceof Arrow arrow) {
            arrow.setPierceLevel(100);
        }
    }

    /** Doubles damage against mobs for players with the strength effect. */
    @EventHandler
    public void strengthDoubleDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // Not dealing double damage to other players
        if (entity instanceof Player) return;

        // Making sure the attacker has the strength effect
        if (!EffectMapping.STRENGTH.hasEffect(attacker)) return;

        // Doubling the damage of the attack
        event.setDamage(event.getDamage() * 2);
    }

    /** Lengthens the shield cooldown of opponents when their shield is disabled by someone with the strength effect. */
    @EventHandler
    public void strengthLengthenShieldCooldown(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Making sure the player was blocking
        if (!player.isBlocking()) return;
        
        // Making sure the attacker is using an axe
        if (!ItemUtil.isAxe(attacker.getInventory().getItemInMainHand())) return;

        // Making sure the attacker has the strength effect
        if (!EffectMapping.STRENGTH.hasEffect(attacker)) return;

        // Playing noise and stunning the opponent
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
        
        // TODO: Test if this can be removed
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setCooldown(Material.SHIELD, 200);
        }, 20L);
    }
}