package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.InventoryUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class StationSelectionMenu implements InventoryHolder {
    private final Inventory inventory;

    public StationSelectionMenu() {
        inventory = Bukkit.createInventory(this, 27, Component.text("Station Selection"));

        // Filling the inventory with a filler item.
        InventoryUtils.fillInventory(inventory, InventoryUtils.createNoName(Material.GRAY_STAINED_GLASS_PANE));

        // Creating the crafting table option
        ItemStack craftingTable = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftingMeta = craftingTable.getItemMeta();
        craftingMeta.displayName(Component.text("Crafting Table", NamedTextColor.DARK_RED));
        craftingTable.setItemMeta(craftingMeta);

        // Creating the brewing stand option
        ItemStack brewingStand = new ItemStack(Material.BREWING_STAND);
        ItemMeta brewingMeta = brewingStand.getItemMeta();
        brewingMeta.displayName(Component.text("Brewing Stand", NamedTextColor.DARK_RED));
        brewingStand.setItemMeta(brewingMeta);

        // Putting the options into the inventory
        inventory.setItem(11, craftingTable);
        inventory.setItem(15, brewingStand);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}