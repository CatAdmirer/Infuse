package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.playerdata.DataManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import com.catadmirer.infuseSMP.managers.EffectMapping;
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

public class Ocean implements Listener {
    private static Infuse plugin;

    private final DataManager dataManager;

    public Ocean(Infuse plugin, DataManager dataManager) {
        Ocean.plugin = plugin;
        this.dataManager = dataManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (EffectMapping.OCEAN.hasEffect(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
        (new BukkitRunnable() {
            public void run() {
                for (Player effectHolder : Bukkit.getOnlinePlayers()) {
                    if (!EffectMapping.OCEAN.hasEffect(effectHolder)) continue;

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
                    if (!EffectMapping.OCEAN.hasEffect(effectHolder)) continue;
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

    private boolean isTrusted(Player player, Player caster) {
        return dataManager.isTrusted(caster, player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (plugin.getConfigFile().invisDeaths()) {
            if (killer != null && killer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                String msg = Messages.INVIS_KILL.getMessage();
                msg = msg.replace("%victim%", victim.getName());
                msg = msg.replace("%killer%", "<gray><obf>Someone");
                event.deathMessage(Messages.toComponent(msg));
            } else if (victim.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                if (killer != null) {
                    String msg = Messages.INVIS_DEATH.getMessage();
                    msg = msg.replace("%victim%", "<gray><obf>Someone");
                    msg = msg.replace("%killer%", killer.getName());
                    event.deathMessage(Messages.toComponent(msg));
                }
            }
        }
    }

    public static void activateSpark(Boolean isAugmented, Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "ocean")) {
            caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

            final double radius = 5;
            final World world = caster.getWorld();
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_OCEAN : EffectMapping.OCEAN);
            long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_OCEAN : EffectMapping.OCEAN);

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
