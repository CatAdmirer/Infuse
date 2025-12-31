package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Invisibility implements Listener {
    private static Infuse plugin;

    private final Map<UUID, Integer> meleeHitCounter = new HashMap<>();

    public Invisibility(Infuse plugin) {
        Invisibility.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (EffectMapping.INVIS.hasEffect(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player shooter) {
            if (EffectMapping.INVIS.hasEffect(shooter)) {
                if (event.getEntity() instanceof Arrow) {
                    if (event.getHitEntity() instanceof Player target) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
                        this.spawnBlackParticles(target, 4);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (event.getEntity() instanceof Player target) {
                if (EffectMapping.INVIS.hasEffect(attacker)) {
                    int count = this.meleeHitCounter.getOrDefault(attacker.getUniqueId(), 0) + 1;
                    this.meleeHitCounter.put(attacker.getUniqueId(), count);
                    if (count >= 20) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
                        this.spawnBlackParticles(target, 4);
                        this.meleeHitCounter.put(attacker.getUniqueId(), 0);
                    }
                }
            }
        }
    }

    private void spawnBlackParticles(final Player target, final int durationInSeconds) {
        (new BukkitRunnable() {
            int ticksElapsed = 0;
            final int maxTicks = durationInSeconds * 20;

            public void run() {
                if (this.ticksElapsed >= this.maxTicks) {
                    this.cancel();
                } else {
                    target.getWorld().spawnParticle(Particle.SQUID_INK, target.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0);
                    this.ticksElapsed += 5;
                }
            }
        }).runTaskTimer(plugin, 0L, 5L);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player target) {
            if (EffectMapping.INVIS.hasEffect(target)) {
                event.setCancelled(true);
            }
        }

    }

    public static void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "invis")) {
            caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            
            // Applying cooldowns and durations for the effect
            boolean isAugmented = plugin.getEffectManager().getEffect(playerUUID, "1").isAugmented() || plugin.getEffectManager().getEffect(playerUUID, "2").isAugmented();
            long cooldown = plugin.getConfig("invis.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = plugin.getConfig("invis.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "invis", duration);
            CooldownManager.setCooldown(playerUUID, "invis", cooldown);

            final double radius = 10;
            final long durationTicks = duration * 20;
            final World world = caster.getWorld();
            final Set<Player> vanishedPlayers = new HashSet<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(world) && player.getLocation().distance(caster.getLocation()) <= radius && isTeammate(caster, player)) {
                    vanishedPlayers.add(player);
                }
            }

            for (Player vanished : vanishedPlayers) {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!other.equals(vanished) && !isTeammate(other, vanished)) {
                        other.hidePlayer(plugin, vanished);
                    }
                }
            }

            (new BukkitRunnable() {
                long ticksElapsed = 0L;

                public void run() {
                    if (this.ticksElapsed >= durationTicks) {
                        this.cancel();
                        for (Player vanished : vanishedPlayers) {
                            for (Player other : Bukkit.getOnlinePlayers()) {
                                other.showPlayer(plugin, vanished);
                            }
                        }

                    } else {
                        Location center = caster.getLocation();

                        for(int angle = 0; angle < 360; angle += 2) {
                            double rad = Math.toRadians(angle);
                            double baseX = center.getX() + radius * Math.cos(rad);
                            double baseZ = center.getZ() + radius * Math.sin(rad);
                            DustOptions dustOptions = new DustOptions(Color.BLACK, 15);

                            for(int i = 0; i < 1; ++i) {
                                double offsetX = (Math.random() - 0.5) * 0.3;
                                double offsetZ = (Math.random() - 0.5) * 0.3;
                                Location particleLoc = new Location(world, baseX + offsetX, center.getY(), baseZ + offsetZ);
                                world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
                            }
                        }

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getWorld().equals(world) && p.getLocation().distance(center) <= radius && !isTeammate(p, caster)) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                            }
                        }

                        this.ticksElapsed += 10L;
                    }
                }
            }).runTaskTimer(plugin, 0L, 10L);
        }
    }

    private static boolean isTeammate(Player player, Player caster) {
        return plugin.getEffectManager().isTrusted(player, caster);
    }
}
