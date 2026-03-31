package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Thunder implements Listener {
    private final Map<UUID, Long> entityLightningCooldowns = new HashMap<>();

    private static Infuse plugin;

    public Thunder(Infuse plugin) {
        Thunder.plugin = plugin;
    }

    @EventHandler
    public void thunderAutoChanneling(EntityDamageByEntityEvent event) {
        // Ignoring non-trident damage
        if (!(event.getDamager() instanceof Trident trident)) return;

        // Making sure the shooter has the thunder effect
        if (!(trident.getShooter() instanceof Player attacker)) return;
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.THUNDER)) return;

        // Only summoning lightning if the target is a living entity
        if (event.getEntity() instanceof LivingEntity target) {
            // TODO: Talk with cat about just striking lightning normally
            //target.getWorld().strikeLightning(target.getLocation());
            target.getWorld().strikeLightningEffect(target.getLocation());
            target.damage(4, attacker);
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
        }
    }

    public static void activateSpark(Boolean isAugmented, Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "thunder")) return;
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_THUNDER : EffectMapping.THUNDER);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_THUNDER : EffectMapping.THUNDER);

        CooldownManager.setTimes(playerUUID, "thunder", cooldown, duration);

        long durationTicks = duration * 20;
        World world = caster.getWorld();

        // Future configs
        double radius = 10;

        // Starting the lightning storm
        new BukkitRunnable() {
            int ticksElapsed = 0;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    return;
                }

                Location center = caster.getLocation();
                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(caster)) continue;

                    if (target instanceof Player p) {
                        if (plugin.getDataManager().isTrusted(p, caster)) continue;
                    }

                    target.getWorld().strikeLightningEffect(target.getLocation());
                    target.damage(4, caster);
                    world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                }

                this.ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void thunderChainLightning(EntityDamageByEntityEvent event) {
        // Making sure the attacker has the thunder effect
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.THUNDER)) return;

        // Making sure the target is a living entity
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Adding the target to the chain lightning cooldown
        UUID targetUUID = target.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (this.entityLightningCooldowns.containsKey(targetUUID)) {
            long lastStrikeTime = this.entityLightningCooldowns.get(targetUUID);
            if (currentTime - lastStrikeTime < 2000L) {
                return;
            }
        }

        this.entityLightningCooldowns.put(targetUUID, currentTime);

        // Finding the next target of the lightning chain
        List<Entity> nearbyEntities = target.getNearbyEntities(3, 3, 3);
        Optional<Entity> nextChainTarget = nearbyEntities.stream().filter((e) -> {
            return e instanceof LivingEntity && !e.equals(attacker);
        }).findFirst();

        // Only striking if there is another target?
        if (nextChainTarget.isPresent()) {
            target.getWorld().strikeLightningEffect(target.getLocation());
            target.damage(4, attacker);
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
            this.chainLightning(target, attacker);
        }
    }

    private void chainLightning(LivingEntity startEntity, Player attacker) {
        List<LivingEntity> lightningTargets = new ArrayList<>();
        lightningTargets.add(startEntity);

        BukkitTask loop = new BukkitRunnable() {
            @Override
            public void run() {
                // Stopping once enough people have been hit
                if (lightningTargets.size() > 5) {
                    cancel();
                    return;
                }

                if (lightningTargets.isEmpty()) {
                    cancel();
                    return;
                }

                // Getting the current target
                LivingEntity livingEntity = lightningTargets.get(0);

                // Damaging the current target
                livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());
                livingEntity.damage(4, attacker);
                livingEntity.getWorld().spawnParticle(Particle.DUST, livingEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));

                lightningTargets.removeIf(LivingEntity::isDead);

                // Finding the next target
                for (Entity entity : livingEntity.getNearbyEntities(3, 3, 3)) {
                    if (!(entity instanceof LivingEntity living)) continue;
                    if (entity.equals(attacker)) continue;
                    if (lightningTargets.contains(entity)) continue;

                    lightningTargets.add(living);
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
        Bukkit.getScheduler().runTaskLater(plugin, othertask -> {
            loop.cancel();
        }, 60L);
    }
}