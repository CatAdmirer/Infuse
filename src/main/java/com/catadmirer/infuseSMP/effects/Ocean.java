package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Ocean implements Listener {
    private static Infuse plugin;

    public Ocean(Infuse plugin) {
        Ocean.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        if (plugin.getDataManager().hasEffect(player, EffectMapping.OCEAN)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
        }

        // Boosting the strength and damage of the passive drowning if the spark is active
        int drownStrength = 5;
        int drownDamage = 1;
        if (CooldownManager.isEffectActive(player.getUniqueId(), "ocean"))  {
            drownStrength = 20;
            drownDamage = 2;
        }
        
        for (Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer.equals(player)) continue;
            if (otherPlayer.getLocation().distance(player.getLocation()) <= 5) {
                int newAir = Math.max(otherPlayer.getRemainingAir() - drownStrength, -20);
                otherPlayer.setRemainingAir(newAir);
                if (newAir <= 0) {
                    otherPlayer.damage(drownDamage);
                }
            }
        }
    }

    public static void activateSpark(Boolean isAugmented, Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "ocean")) return;

        caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final double radius = 5;
        final World world = caster.getWorld();
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_OCEAN : EffectMapping.OCEAN);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_OCEAN : EffectMapping.OCEAN);

        CooldownManager.setTimes(playerUUID, "ocean", duration, cooldown);

        final long durationTicks = duration * 20L;

        new BukkitRunnable() {
            long ticksElapsed = 0L;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    return;
                }

                for (int angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    double x = caster.getLocation().getX() + radius * Math.cos(rad);
                    double z = caster.getLocation().getZ() + radius * Math.sin(rad);
                    Location particleLoc = new Location(world, x, caster.getLocation().getY(), z);
                    world.spawnParticle(Particle.FALLING_WATER, particleLoc, 1);
                }

                this.ticksElapsed += 10L;
            }
        }.runTaskTimer(plugin, 0L, 10L);

        // Ocean pull runnable
        new BukkitRunnable() {
            @Override
            public void run() {
                // Stopping when the spark has run out
                if (!CooldownManager.isEffectActive(caster.getUniqueId(), "ocean")) {
                    cancel();
                    return;
                }

                World world = caster.getWorld();
                Location holderLoc = caster.getLocation();
                double radius = plugin.getMainConfig().oceanPullRadius();
                double strength = plugin.getMainConfig().oceanPullStrength();

                for (Player p : world.getPlayers()) {
                    if (p.equals(caster)) continue;
                    if (plugin.getDataManager().isTrusted(caster, p)) continue;
                    if (p.getLocation().distance(holderLoc) > radius) continue;

                    Vector direction = holderLoc.toVector().subtract(p.getLocation().toVector());
                    if (direction.lengthSquared() > 0.0001) {
                        Vector pullVector = direction.normalize().multiply(strength);
                        if (Double.isFinite(pullVector.getX()) && Double.isFinite(pullVector.getY()) && Double.isFinite(pullVector.getZ())) {
                            p.setVelocity(pullVector);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, plugin.getMainConfig().oceanPullInterval());
    }
}
