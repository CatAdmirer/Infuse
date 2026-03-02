package com.catadmirer.infuseSMP.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryUtils {
    /**
     * Creates a decorative item with no name.
     * 
     * @param material The material to make the item with.
     * 
     * @return A decorative item with no name.
     */
    public static ItemStack createNoName(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    /**
     * Fills an inventory with a certain item.
     * 
     * @param inventory The inventory to fill.
     * @param item The item to fill the inventory with.
     */
    public static void fillInventory(Inventory inventory, ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, item);
        }
    }

    /**
     * Putting an item into multiple slots of an inventory.
     * 
     * @param inventory The inventory to place the item into.
     * @param slots The list of slots to place the item.
     * @param item The item to put into the inventory
     */
    public static void setItems(Inventory inventory, int[] slots, ItemStack item) {
        for (int slot : slots) inventory.setItem(slot, item);
    }

    /**
     * Utility function that fills all empty slots of an inventory with red stained glass panes with
     * empty names.
     * 
     * @param inventory The inventory to fill with panes.
     */
    public static void fillRemainingSlots(Inventory inventory) {
        ItemStack stainedGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta meta = stainedGlassPane.getItemMeta();
        meta.displayName(Component.empty());
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, stainedGlassPane);
            }
        }
    }
}