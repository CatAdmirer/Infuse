package com.catadmirer.infuseSMP.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.managers.CooldownManager;

public class ThiefListeners implements Listener {
    private final Map<UUID, Thief.DisguiseData> disguisedPlayers = new HashMap<>();
    private final Infuse plugin;

    public ThiefListeners(Infuse plugin) {
        this.plugin = plugin;
    }


    // Hiding thief effect users from players who recently joined
    @EventHandler
    public void hideThievesOnJoin(PlayerJoinEvent event) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (!plugin.getDataManager().hasEffect(otherPlayer, new Thief())) continue;
            
            event.getPlayer().unlistPlayer(otherPlayer);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        
        // If a disguised player dies, revert their disguise
        if (disguisedPlayers.containsKey(deadPlayer.getUniqueId())) {
            removeDisguise(deadPlayer);
        }

        if (!(event.getDamageSource().getCausingEntity() instanceof Player)) return;

        // If a player with the thief effect kills someone, they should disguise themselves as the player they kill
        InfuseEffect lEffect = plugin.getDataManager().getEffect(deadPlayer.getUniqueId(), "1");
        InfuseEffect rEffect = plugin.getDataManager().getEffect(deadPlayer.getUniqueId(), "1");

        if (lEffect instanceof Thief thiefEffect) {
            thiefEffect.disguise(plugin, deadPlayer);
        } else if (rEffect instanceof Thief thiefEffect) {
            thiefEffect.disguise(plugin, deadPlayer);
        }
    }

    /**
     * Removing an active disguise if a disguised player leaves.
     * 
     * @param event The {@link PlayerQuitEvent} to handle
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (disguisedPlayers.containsKey(player.getUniqueId())) {
            removeDisguise(player);
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, new Thief())) return;

        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isEffectActive(playerUUID, "thief")) return;

        InfuseEffect leftEffect = plugin.getDataManager().getEffect(victim.getUniqueId(), "1");
        InfuseEffect rightEffect = plugin.getDataManager().getEffect(victim.getUniqueId(), "2");

        if (leftEffect != null && rightEffect != null) {
            activateEffect(player, Math.random() > 0.5 ? leftEffect : rightEffect);
        } else if (leftEffect != null) {
            activateEffect(player, leftEffect);
        } else if (rightEffect != null) {
            activateEffect(player, rightEffect);
        } else return;

        CooldownManager.setDuration(playerUUID, "thief", 0);
    }

    private void activateEffect(Player player, InfuseEffect effect) {
        InfuseEffect lEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        InfuseEffect rEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");

        if (lEffect instanceof Thief thiefEffect) {
            thiefEffect.stealEffect(plugin, thiefEffect);
        } else if (rEffect instanceof Thief thiefEffect) {
            thiefEffect.stealEffect(plugin, thiefEffect);
        }
    }

    private void removeDisguise(Player player) {
        InfuseEffect lEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        InfuseEffect rEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");

        if (lEffect instanceof Thief thiefEffect) {
            thiefEffect.removeDisguise();
        } else if (rEffect instanceof Thief thiefEffect) {
            thiefEffect.removeDisguise();
        }
    }
}
