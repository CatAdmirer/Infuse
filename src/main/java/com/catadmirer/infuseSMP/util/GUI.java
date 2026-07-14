package com.catadmirer.infuseSMP.util;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.inventories.EffectChooser;
import com.catadmirer.infuseSMP.inventories.AugOrRegChooser;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUI implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        // Ignoring if the player clicked on an empty slot.
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clickedInventory == null) return;
        // Only running if the inventory is an EffectInventory
        if (clickedInventory.getHolder() instanceof EffectChooser) {
            // Cancelling the click event to prevent the player from getting the item.
            event.setCancelled(true);

            InfuseEffect effect = InfuseEffect.fromItem(clicked);

            // Ignoring if the player clicked on something other than an effect.
            if (effect == null) return;

            player.openInventory(new AugOrRegChooser(effect).getInventory());
        }

        if (clickedInventory instanceof AugOrRegChooser) {
            if (clicked.getType() != Material.POTION) {
                event.setCancelled(true);
            }
        }
    }
}