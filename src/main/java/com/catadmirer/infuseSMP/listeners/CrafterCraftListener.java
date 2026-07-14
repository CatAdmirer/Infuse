package com.catadmirer.infuseSMP.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;

public class CrafterCraftListener implements Listener {
    /** Prevents infuse effects from being crafted in a crafter. */
    @EventHandler
    public void onCrafterCraft(CrafterCraftEvent event) {
        if (event.getResult().getType() != Material.POTION) return;

        event.setCancelled(true);
    }
}
