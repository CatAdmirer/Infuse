package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.HashMap;
import java.util.Map;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Fire extends InfuseEffect {
    private static Infuse plugin;

    public Fire(Infuse plugin) {
        Fire.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FIRE)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false));
        if (player.isInLava()) {
            player.setGliding(true);
        } else if (player.isInPowderedSnow()) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (event.isGliding()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, new Fire())) return;
        
        if (player.isInLava() || player.isInPowderedSnow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inLava = player.isInLava();
        Vector direction = player.getLocation().getDirection().normalize();
        if (inLava && plugin.getDataManager().hasEffect(player, new Fire())) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            double boostStrength = 0.6;
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FIRE)) return;

        if (event.getForce() >= 1 && event.getProjectile() instanceof Projectile projectile) {
            projectile.setFireTicks(100);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != DamageCause.FALL) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FIRE)) return;
        Material blockType = player.getLocation().getBlock().getType();
        if (blockType == Material.LAVA || blockType == Material.LAVA_CAULDRON) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void fireCombustTarget(TenHitEvent event) {
        Player attacker = event.getAttacker();
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        event.getTarget().setFireTicks(100);
    }

    public void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "fire")) return;

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);

        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setFireTicks(100);
            }
        }

        spawnSparkEffect(player);
        new BukkitRunnable() {
            public void run() {
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
            }
        }.runTaskLater(plugin, 20L);
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "fire", duration);
        CooldownManager.setCooldown(playerUUID, "fire", cooldown);
    }

    private static void spawnSparkEffect(final Player caster) {
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 100) {
                    startDarkRedDustEffect(caster.getLocation(), caster);
                    this.cancel();
                    return;
                }
                
                Location center = caster.getLocation();
                World world = center.getWorld();
                if (this.tick > 0 && this.tick % 20 == 0) {
                    world.playSound(center, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);

                    for(int angle = 0; angle < 360; angle += 20) {
                        double rad = Math.toRadians(angle);
                        double offsetX = 5 * Math.cos(rad);
                        double offsetZ = 5 * Math.sin(rad);
                        Location particleLoc = center.clone().add(offsetX, 0.1, offsetZ);
                        world.spawnParticle(Particle.LAVA, particleLoc, 10, 0.05, 0.05, 0.05, 0.01);
                    }

                    for (Player target : world.getPlayers()) {
                        if (!target.equals(caster) && target.getLocation().distance(center) <= 5) {
                            target.damage(8, caster);
                        }
                    }
                }

                ++this.tick;
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }

    private static void startDarkRedDustEffect(final Location startLoc, Player caster) {
        final World world = startLoc.getWorld();
        double explosionRadius = 5;
        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && target.getLocation().distance(startLoc) <= explosionRadius) {
                target.setVelocity(new Vector(0, 2, 0));
            }
        }

        world.playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 60) {
                    this.cancel();
                    return;
                }
                
                double baseRadius = 5;
                double spreadFactor = this.tick * 0.1;
                double circleRadius = baseRadius + spreadFactor;
                double particleHeightOffset = this.tick * 3;
                if (particleHeightOffset > 30) {
                    this.cancel();
                    return;
                }
                
                for(int angle = 0; angle < 360; ++angle) {
                    double rad = Math.toRadians(angle);
                    double offsetX = circleRadius * Math.cos(rad);
                    double offsetZ = circleRadius * Math.sin(rad);
                    Location particleLoc = startLoc.clone().add(offsetX, particleHeightOffset, offsetZ);
                    world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());
                }

                ++this.tick;
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }
}
