package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
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
        if (!plugin.getDataManager().hasEffect(attacker, new Thunder())) return;

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
        final UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "thunder")) {
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getConfigFile().cooldown(this);
            long duration = plugin.getConfigFile().duration(this);

            CooldownManager.setDuration(playerUUID, "thunder", duration);
            CooldownManager.setCooldown(playerUUID, "thunder", cooldown);

            final long effectDuration = duration * 20;

            final double radius = 10;
            final World world = caster.getWorld();

            new BukkitRunnable() {
                int ticksElapsed = 0;

                public void run() {
                    if (this.ticksElapsed >= effectDuration) {
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
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (plugin.getDataManager().hasEffect(attacker, new Thunder())) {
                if (event.getEntity() instanceof LivingEntity target) {
                    UUID targetUUID = target.getUniqueId();
                    long currentTime = System.currentTimeMillis();
                    if (this.entityLightningCooldowns.containsKey(targetUUID)) {
                        long lastStrikeTime = this.entityLightningCooldowns.get(targetUUID);
                        if (currentTime - lastStrikeTime < 2000L) {
                            return;
                        }
                    }

                    this.entityLightningCooldowns.put(targetUUID, currentTime);
                    List<Entity> nearbyEntities = target.getNearbyEntities(3, 3, 3);
                    Optional<Entity> nextChainTarget = nearbyEntities.stream().filter((e) -> {
                        return e instanceof LivingEntity && !e.equals(attacker);
                    }).findFirst();
                    if (nextChainTarget.isPresent()) {
                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(4, attacker);
                        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                        this.chainLightning(target, attacker);
                    }

                }
            }
        }
    }

    private void chainLightning(Entity startEntity, final Player attacker) {
        final Set<Entity> processedEntities = new HashSet<>();
        final Queue<Entity> queue = new LinkedList<>();
        queue.add(startEntity);
        (new BukkitRunnable() {
            int strikes = 0;

            public void run() {
                if (!queue.isEmpty() && this.strikes < 5) {
                    Entity currentEntity = null;

                    while(!queue.isEmpty()) {
                        Entity candidate = queue.poll();
                        if (candidate instanceof LivingEntity && !processedEntities.contains(candidate)) {
                            currentEntity = candidate;
                            break;
                        }
                    }

                    if (currentEntity != null) {
                        processedEntities.add(currentEntity);
                        LivingEntity livingEntity = (LivingEntity)currentEntity;
                        if (!livingEntity.equals(attacker)) {
                            livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());
                            livingEntity.damage(4, attacker);
                            livingEntity.getWorld().spawnParticle(Particle.DUST, livingEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                            ++this.strikes;
                            for (Entity entity : livingEntity.getNearbyEntities(3, 3, 3)) {
                                if (entity instanceof LivingEntity && !processedEntities.contains(entity)) {
                                    queue.add(entity);
                                }
                            }
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
    }
}