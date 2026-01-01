package com.catadmirer.infuseSMP.inventories;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.catadmirer.infuseSMP.util.InventoryUtils;

public class AugOrRegChooser implements InventoryHolder {
    private final Inventory inventory;

    public AugOrRegChooser(ItemStack regularEffect, ItemStack augmentedEffect, Material backgroundItem) {
        inventory = Bukkit.createInventory(this, 27, Component.text("Choose", NamedTextColor.YELLOW));
        
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