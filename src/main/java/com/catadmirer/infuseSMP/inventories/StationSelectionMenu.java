package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.util.InventoryUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class StationSelectionMenu implements InventoryHolder {
    private final Inventory inventory;
    private final Location standLocation;

    public StationSelectionMenu(Location standLocation) {
        inventory = Bukkit.createInventory(this, 27, Component.text("Station Selection"));
        this.standLocation = standLocation;

        // Filling the inventory with a filler item.
        InventoryUtils.fillInventory(inventory, InventoryUtils.createNoName(Material.GRAY_STAINED_GLASS_PANE));

        // Creating the crafting table option
        ItemStack craftingTable = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftingMeta = craftingTable.getItemMeta();
        craftingMeta.displayName(Message.toComponent("<dark_red>Crafting Table"));
        craftingTable.setItemMeta(craftingMeta);

        // Creating the brewing stand option
        ItemStack brewingStand = new ItemStack(Material.BREWING_STAND);
        ItemMeta brewingMeta = brewingStand.getItemMeta();
        brewingMeta.displayName(Message.toComponent("<dark_red>Brewing Stand"));
        brewingStand.setItemMeta(brewingMeta);

        // Putting the options into the inventory
        inventory.setItem(11, craftingTable);
        inventory.setItem(15, brewingStand);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Location getStandLocation() {
        return standLocation;
    }
}