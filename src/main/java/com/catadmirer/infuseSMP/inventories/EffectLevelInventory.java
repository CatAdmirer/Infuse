package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EffectLevelInventory implements InventoryHolder {
    private final Inventory inventory;

    public EffectLevelInventory(ItemStack regularEffect, ItemStack augmentedEffect, Material backgroundItem) {
        inventory = Bukkit.createInventory(null, 27, "§eChoose");
        
        // Filling the inventory with a filler item.
        InventoryUtils.fillInventory(inventory, InventoryUtils.createNoName(backgroundItem));

        // Adding the effects to the inventory
        inventory.setItem(11, regularEffect);
        inventory.setItem(15, augmentedEffect);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}