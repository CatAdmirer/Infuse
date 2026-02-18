package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.events.TenHitEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HitTracker implements Listener {
    private final Infuse plugin;
    private final Map<UUID,Integer> hitTracker = new HashMap<>();
    Queue<Runnable> decayQueue = new ConcurrentLinkedQueue<>();

    public HitTracker(Infuse plugin) {
        this.plugin = plugin;
    }

    /**
     * Tracking the number of hits a player has.
     * 
     * @param event A {@link EntityDamageByEntityEvent}
     */
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        // Making sure both entities are players
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;

        Infuse.debug(attacker.getName() + " has hit " + target.getName());

        // Making sure it counts as a normal hit
        // Vanilla attack cooldown needs to be at 84.8% to be a normal hit.
        if (attacker.getAttackCooldown() < 0.85) {
            Infuse.debug("Hit ignored due to being under attack cooldown threshold.");
            return;
        }

        // Incrementing the hit counter
        int hits = hitTracker.getOrDefault(attacker.getUniqueId(), 0) + 1;
        Infuse.debug(attacker.getName() + "'s hit counter is " + hits + ".");

        if (hits == 10) {
            // Calling the TenHitEvent
            TenHitEvent tenHit = new TenHitEvent(attacker, target);
            tenHit.callEvent();
            Infuse.debug("Called TenHitEvent");

            hits -= 10;

            // Removing 10 objects from the queue
            for (int i = 0; i < 10; i++) {
                if (decayQueue.isEmpty()) continue;
                decayQueue.remove();
            }
            Infuse.debug("Removed items from queue.");
        }

        // Saving the hit count
        hitTracker.put(attacker.getUniqueId(), hits);

        // Having the hit counter decay over time
        // TODO: make this a config (0 or below disables it)
        int hitCounterDecaySeconds = 10;
        if (hitCounterDecaySeconds < 1) return;

        Infuse.debug("Adding item to decay queue");
        decayQueue.add(() -> {
            // Skipping if the attacker has left the game
            if (!attacker.isConnected()) return;

            Infuse.debug("Decrementing hit counter");
            int curHits = hitTracker.get(attacker.getUniqueId());

            Infuse.debug(attacker.getName() + "'s hit counter is " + (curHits - 1) + ".");
            hitTracker.put(attacker.getUniqueId(), curHits - 1);
        });
        
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
}
