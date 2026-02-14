package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.Infuse;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class EffectCrafting implements InventoryHolder {
    private final Inventory inventory;

    public EffectCrafting(Infuse plugin) {
        inventory = Bukkit.createInventory(this, InventoryType.WORKBENCH, MiniMessage.miniMessage().deserialize("Effect Crafting"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
