package com.catadmirer.infuseSMP.bukkit.listeners;

import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final InfusePlugin plugin;

    public PlayerQuitListener(InfusePlugin plugin) {
        this.plugin = plugin;
    }

    /** Unequips a player's effects when they leave the game. */
    @EventHandler
    public void deactivateEffects(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Deactivating the player's effects
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        if (effect != null) effect.unequip(player);

        effect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        if (effect != null) effect.unequip(player);
    }
}
