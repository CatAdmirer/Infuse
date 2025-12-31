package com.catadmirer.infuseSMP.Inventories;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class BrewingStandGUI implements InventoryHolder {
    private final Inventory inventory;

    public BrewingStandGUI() {
        inventory = Bukkit.createInventory(this, 27, Component.text("Choose GUI"));

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        ItemStack craftingItem = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftingMeta = craftingItem.getItemMeta();
        craftingMeta.setDisplayName("§4Crafting Table");
        craftingItem.setItemMeta(craftingMeta);
        ItemStack brewingItem = new ItemStack(Material.BREWING_STAND);
        ItemMeta brewingMeta = brewingItem.getItemMeta();
        brewingMeta.setDisplayName("§4Brewing Stand");
        brewingItem.setItemMeta(brewingMeta);
        inventory.setItem(11, craftingItem);
        inventory.setItem(15, brewingItem);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}