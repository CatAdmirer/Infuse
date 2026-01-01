package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class Thunder implements Listener {
    private final static Set<UUID> activeSparks = new HashSet<>();
    private final Map<UUID, Long> entityLightningCooldowns = new HashMap<>();

    private static Infuse plugin;

    public Thunder(Infuse plugin) {
        Thunder.plugin = plugin;
    }

    @EventHandler
    public void onTridentHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Trident trident) {
            if (!trident.hasMetadata("thunderProcessed")) {
                trident.setMetadata("thunderProcessed", new FixedMetadataValue(plugin, true));
                if (trident.getShooter() instanceof Player attacker) {
                    if (EffectMapping.THUNDER.hasEffect(attacker)) {
                        if (event.getEntity() instanceof LivingEntity target) {
                            target.getWorld().strikeLightningEffect(target.getLocation());
                            target.damage(4, attacker);
                            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                        }
                    }
                }
            }
        }
    }

    public static void activateSpark(final Player caster) {
        final UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "thunder") && !activeSparks.contains(playerUUID)) {
            activeSparks.add(playerUUID);
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            
            // Applying cooldowns and durations for the effect
            boolean isAugmented = plugin.getEffectManager().getEffect(playerUUID, "1") == EffectMapping.AUG_THUNDER || plugin.getEffectManager().getEffect(playerUUID, "2") == EffectMapping.AUG_THUNDER;
            long cooldown = plugin.getConfig("thunder.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = plugin.getConfig("thunder.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "thunder", duration);
            CooldownManager.setCooldown(playerUUID, "thunder", cooldown);

            final long effectDuration = duration * 20;

            final double radius = 10;
            final World world = caster.getWorld();

            new BukkitRunnable() {
                int ticksElapsed = 0;

                public void run() {
                    if (this.ticksElapsed >= effectDuration) {
                        activeSparks.remove(playerUUID);
                        this.cancel();
                        return;
                    }

                    Location center = caster.getLocation();
                    for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                        if (!(entity instanceof LivingEntity target)) continue;
                        if (target.equals(caster)) continue;

                        if (target instanceof Player p) {
                            if (isTeammate(p, caster)) continue;
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


    private static boolean isTeammate(Player player, Player caster) {
        return plugin.getEffectManager().isTrusted(player, caster);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (EffectMapping.THUNDER.hasEffect(attacker)) {
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