package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Ocean extends InfuseEffect {
    private static Infuse plugin;

    public Ocean(Infuse plugin) {
        Ocean.plugin = plugin;
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getDataManager().hasEffect(p, new Ocean())) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
        (new BukkitRunnable() {
            public void run() {
                for (Player effectHolder : Bukkit.getOnlinePlayers()) {
                    if (!plugin.getDataManager().hasEffect(effectHolder, new Ocean())) continue;

                    for (Player p : effectHolder.getWorld().getPlayers()) {
                        if (!p.equals(effectHolder) && p.getLocation().distance(effectHolder.getLocation()) <= 5 && p.getLocation().getBlock().isLiquid()) {
                            int currentAir = p.getRemainingAir();
                            int newAir = Math.max(currentAir - 5, -20);
                            p.setRemainingAir(newAir);
                            if (newAir <= 0) {
                                p.damage(1);
                            }
                        }
                    }
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player effectHolder : Bukkit.getOnlinePlayers()) {
                    if (!CooldownManager.isEffectActive(effectHolder.getUniqueId(), "ocean")) {
                        continue;
                    }
                    if (!plugin.getDataManager().hasEffect(effectHolder, new Ocean())) continue;

                    World world = effectHolder.getWorld();
                    Location holderLoc = effectHolder.getLocation();
                    double radius = plugin.getConfigFile().oceanPullRadius();
                    double strength = plugin.getConfigFile().oceanPullStrength();

                    for (Player p : world.getPlayers()) {
                        if (p.equals(effectHolder)) continue;
                        if (isTrusted(effectHolder, p)) continue;
                        if (p.getLocation().distance(holderLoc) <= radius) {
                            Vector direction = holderLoc.toVector().subtract(p.getLocation().toVector());
                            if (direction.lengthSquared() > 0.0001) {
                                Vector pullVector = direction.normalize().multiply(strength);
                                if (Double.isFinite(pullVector.getX()) && Double.isFinite(pullVector.getY()) && Double.isFinite(pullVector.getZ())) {
                                    p.setVelocity(pullVector);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, plugin.getConfigFile().oceanPullInterval());
    }

    @Override
    public void equip(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, PotionEffect.INFINITE_DURATION, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PotionEffect.INFINITE_DURATION, 0, false, false));
    }

    @Override
    public void unequip(Player player) {
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        // TODO Auto-generated method stub
        
    }

    private boolean isTrusted(Player player, Player caster) {
        return plugin.getDataManager().isTrusted(caster, player);
    }

    public static void activateSpark(Boolean isAugmented, Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "ocean")) {
            caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

            final double radius = 5;
            final World world = caster.getWorld();
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getConfigFile().cooldown(this);
            long duration = plugin.getConfigFile().duration(this);

            CooldownManager.setDuration(playerUUID, "ocean", duration);
            CooldownManager.setCooldown(playerUUID, "ocean", cooldown);

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

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.equals(caster) &&
                                p.getWorld().equals(world) &&
                                p.getLocation().distance(caster.getLocation()) <= radius) {

                            Vector direction = caster.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                            p.setVelocity(direction.multiply(0.5));

                            if (p.getLocation().getBlock().isLiquid()) {
                                int newOxygen = Math.max(p.getRemainingAir() - 20, -20);
                                p.setRemainingAir(newOxygen);
                                if (newOxygen <= 0) {
                                    p.damage(2);
                                }
                            }
                        }
                    }

                    this.ticksElapsed += 10L;
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }
    }
}
