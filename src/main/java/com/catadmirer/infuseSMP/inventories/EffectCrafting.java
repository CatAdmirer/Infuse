package com.catadmirer.infuseSMP.inventories;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class EffectCrafting implements InventoryHolder {
    private final Inventory inventory;
    private final Location brewerLocation;

    public EffectCrafting(Location brewerLocation) {
        this.inventory = Bukkit.createInventory(this, InventoryType.WORKBENCH, MiniMessage.miniMessage().deserialize("Effect Crafting"));
        this.brewerLocation = brewerLocation;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public @NotNull Location getBrewerLocation() {
        return brewerLocation;
    }
}
