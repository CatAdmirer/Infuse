package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.HitTracker;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InfuseDebug;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Thunder implements Listener {
    private static final Map<UUID,Integer> hitTracker = new HashMap<>();
    private static final Queue<Runnable> decayQueue = new ConcurrentLinkedQueue<>();

    private static Infuse plugin;

    public Thunder(Infuse plugin) {
        Thunder.plugin = plugin;
    }

    public static void activateSpark(Boolean isAugmented, Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "thunder")) return;
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_THUNDER : EffectMapping.THUNDER);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_THUNDER : EffectMapping.THUNDER);

        CooldownManager.setTimes(playerUUID, "thunder", duration, cooldown);

        long durationTicks = duration * 20;
        World world = caster.getWorld();

        // TODO: make configs
        double baseRadius = 10;
        double radiusBoostPerPlayer = 0.3;

        // Starting the lightning storm
        new BukkitRunnable() {
            int ticksElapsed = 0;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    return;
                }

                // Calculating the radius
                double radius = baseRadius;
                while (true) {
                    long nearbyPlayers = world.getNearbyEntities(caster.getLocation(), radius, radius, radius).stream().filter(p -> p instanceof Player).count();
                    double tmp = baseRadius + radiusBoostPerPlayer * nearbyPlayers;
                    if (tmp == radius) break;
                }

                // Striking all players within the radius
                for (Entity entity : world.getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
                    if (!(entity instanceof Player target)) continue;
                    if (plugin.getDataManager().isTrusted(target, caster)) continue;

                    strikeLighting(target, caster);
                }

                this.ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Custom lightning bolt for the thunder effect.
     * 
     * @param target The entity to hit with a lightning bolt.
     * @param attacker The entity to attribute the damage to.
     */
    public static void strikeLighting(LivingEntity target, LivingEntity attacker) {
        target.getWorld().strikeLightningEffect(target.getLocation());
        target.damage(2, DamageSource.builder(DamageType.LIGHTNING_BOLT).withDirectEntity(attacker).build());
        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
    }

    /**
     * Chain lightning functionality.
     * This is a recursive function that runs up to 10 times to strike nearby entities with lightning.
     * The function should be called with a list containing only the attacking entity.
     * 
     * @param targets The list of targets that have been hit by the lightning bolt, with the exception of the first entry which is the attacker.
     * 
     * @throws InvalidParameterException If the <code>targets</code> parameter is null or empty.
     */
    private void chainLightning(List<Player> targets) {
        if (targets == null) throw new InvalidParameterException("targets cannot be null");
        if (targets.size() == 11) return;
        if (targets.size() < 1) throw new InvalidParameterException("targets list needs to have the attacker in the front");

        Player attacker = targets.get(0);

        // TODO: make config
        double radius = 3;

        for (Entity entity : targets.getLast().getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player target)) continue;
            if (targets.contains(target)) continue;

            // Scheduling the lightning to strike the target 1 second after the next
            Bukkit.getScheduler().runTaskLater(plugin, () -> strikeLighting(target, attacker), 20 * (targets.size() - 1));

            // Adding the target to the list
            targets.add(target);

            // Recursion babyyy
            chainLightning(targets);
            return;
        }
    }

    /**
     * Tracking the number of hits a player has.
     * Yes, this is a copy of the stuff in {@link HitTracker}.  I can't figure out a good way to make it count every 5 hits.
     * 
     * @param event A {@link EntityDamageByEntityEvent}
     */
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        // Making sure both entities are players
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.THUNDER)) return;

        // Making sure it wasn't a lightning bolt
        if (event.getDamageSource().getDamageType().equals(DamageType.LIGHTNING_BOLT)) return;

        // Making sure it counts as a normal hit
        // Vanilla attack cooldown needs to be at 84.8% to be a normal hit.
        if (attacker.getAttackCooldown() < 0.85) {
            InfuseDebug.log("[Thunder] Hit ignored due to being under attack cooldown threshold.");
            return;
        }

        // Incrementing the hit counter
        int hits = hitTracker.getOrDefault(attacker.getUniqueId(), 0) + 1;
        InfuseDebug.log("[Thunder] {}'s thunder hit counter is {}.", attacker.getName(), hits);

        // In stormy weather, the player only needs 5 hits to activate chain lightning
        int hitGoal = attacker.getWorld().isClearWeather() ? 10 : 5;
        if (hits >= hitGoal) {
            hitTracker.put(attacker.getUniqueId(), 0);

            // Removing x objects from the queue
            for (int i = 0; i < hitGoal; i++) {
                if (decayQueue.isEmpty()) continue;
                decayQueue.remove();
            }

            chainLightning(new ArrayList<>(List.of(attacker)));
            
            return;
        }

        // Saving the hit count
        hitTracker.put(attacker.getUniqueId(), hits);

        // Having the hit counter decay over time
        int hitCounterDecaySeconds = plugin.getMainConfig().hitCounterDecaySeconds();
        if (hitCounterDecaySeconds < 1) return;

        InfuseDebug.log("[Thunder] Adding item to decay queue");
        decayQueue.add(() -> {
            // Skipping if the attacker has left the game
            if (!attacker.isConnected()) return;

            InfuseDebug.log("[Thunder] Decrementing hit counter");
            int curHits = hitTracker.get(attacker.getUniqueId());

            InfuseDebug.log("[Thunder] {}'s hit counter is {}.", attacker.getName(), curHits - 1);
            hitTracker.put(attacker.getUniqueId(), curHits - 1);
        });
        InfuseDebug.log("{} items in queue", decayQueue.size());
        
        // Running the decay task if it is still around
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Runnable decayTask = decayQueue.peek();
            if (decayTask != null) {
                decayQueue.remove();
                decayTask.run();
            }
        }, hitCounterDecaySeconds * 20);
    }

    /**
     * Removes players from the hit tracker when they leave.
     * 
     * @param event A {@link PlayerQuitEvent}
     */
    public void onPlayerLeave(PlayerQuitEvent event) {
        hitTracker.remove(event.getPlayer().getUniqueId());
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
            strikeLighting(target, attacker);
        }
    }
}