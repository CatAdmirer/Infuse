package com.catadmirer.infuseSMP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.catadmirer.infuseSMP.events.TenHitEvent;

public class HitTracker {
    private final Infuse plugin;
    private final Map<UUID,Integer> hitTracker = new HashMap<>();

    public HitTracker(Infuse plugin) {
        this.plugin = plugin;
    }

    /**
     * Tracking the number of hits a player has.
     * 
     * @param event A {@link EntityDamageByEntityEvent}
     */
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        // Making sure both entities are players
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;

        // Making sure it counts as a normal hit
        // Vanilla attack cooldown needs to be at 84.8% to be a normal hit.
        if (attacker.getAttackCooldown() < 0.85) return;

        // Incrementing the hit counter
        int hits = hitTracker.getOrDefault(attacker, 0) + 1;
        hitTracker.put(attacker.getUniqueId(), hits);

        // Calling the TenHitEvent
        TenHitEvent tenHit = new TenHitEvent(attacker, target);
        tenHit.callEvent();

        // Having the hit counter decay over time
        // TODO: make this a config (0 or below disables it)
        int hitCounterDecaySeconds = 10;
        if (hitCounterDecaySeconds < 1) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!attacker.isConnected()) return;

            hitTracker.put(attacker.getUniqueId(), hits - 1);
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
