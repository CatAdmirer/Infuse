package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.particles.Particles;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Speed implements Listener {
    private static Infuse plugin;

    private final Map<UUID, Integer> speedLevels = new HashMap<>();
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private final Map<UUID, Long> bowPullStartTime = new HashMap<>();

    public Speed(Infuse plugin) {
        Speed.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!EffectMapping.SPEED.hasEffect(p)) continue;

                    UUID uuid = p.getUniqueId();
                    long lastHit = Speed.this.lastHitTime.getOrDefault(uuid, 0L);
                    if (System.currentTimeMillis() - lastHit > 1000L) {
                        Speed.this.speedLevels.put(uuid, 1);
                    }

                    int currentLevel = Speed.this.speedLevels.getOrDefault(uuid, 1);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, Math.max(0, currentLevel - 1), false, false, false));
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (EffectMapping.SPEED.hasEffect(player)) {
                long startTime = this.bowPullStartTime.getOrDefault(player.getUniqueId(), 0L);
                long pullTimeMs = System.currentTimeMillis() - startTime;
                double adjustedPullTimeMs = pullTimeMs * 1.8;
                float pullFraction = (float)Math.min(adjustedPullTimeMs / 1000, 1);
                event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(pullFraction));
                this.bowPullStartTime.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (EffectMapping.SPEED.hasEffect(player)) {
                UUID uuid = player.getUniqueId();
                long currentTime = System.currentTimeMillis();
                long lastHit = this.lastHitTime.getOrDefault(uuid, 0L);
                if (currentTime - lastHit >= 50L) {
                    this.lastHitTime.put(uuid, currentTime);
                    this.speedLevels.put(uuid, this.speedLevels.getOrDefault(uuid, 1) + 1);
                    if (event.getEntity() instanceof LivingEntity target) {
                        int currentNoDamageTicks = target.getNoDamageTicks();
                        target.setNoDamageTicks(currentNoDamageTicks / 2);
                    }

                }
            }
        }
    }

    public static void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "speed")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            Particles.spawnEffectCloud(player, Color.fromRGB(0xD1A44B));
            final Vector direction = player.getEyeLocation().getDirection().normalize();
            Object obj = plugin.getConfig("speed.playerVelocityMultiplier");
            double playerVelocityMultiplier = ((Number) obj).doubleValue();
            player.setVelocity(direction.clone().multiply(playerVelocityMultiplier));
            final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0xE6DCAA), 1.5F);
            final Location[] previousLocation = new Location[]{player.getLocation().clone()};
            final int[] ticksPassed = new int[]{0};
            final Location anchor = player.getLocation();
            Bukkit.getRegionScheduler().runAtFixedRate(plugin, anchor, (task) -> {
                if (!player.isOnline()) {
                    task.cancel();
                    return;
                }

                Location currentLocation = player.getLocation();
                double distance = previousLocation[0].distance(currentLocation);

                if (distance > 0.1) {
                    Vector step = currentLocation.toVector().subtract(previousLocation[0].toVector()).normalize().multiply(0.3);
                    Location particleLocation = previousLocation[0].clone();

                    for (double d = 0; d <= distance; d += step.length()) {
                        particleLocation.add(step);
                        player.getWorld().spawnParticle(Particle.DUST, particleLocation, 5, 0.1, 0.05, 0.1, 0.05, dustOptions);
                    }

                    previousLocation[0] = currentLocation.clone();
                }

                if (ticksPassed[0] >= 3 && player.isOnGround()) {
                    task.cancel();
                }

                ticksPassed[0]++;
            }, 1L, 1L);

            // Applying cooldowns and durations for the effect
            boolean isAugmented = plugin.getEffectManager().getEffect(playerUUID, "1") == EffectMapping.AUG_SPEED || plugin.getEffectManager().getEffect(playerUUID, "2") == EffectMapping.AUG_SPEED;
            long cooldown = plugin.getConfig("speed.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = plugin.getConfig("speed.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "speed", duration);
            CooldownManager.setCooldown(playerUUID, "speed", cooldown);
        }
    }
}