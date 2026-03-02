package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class EffectChooser implements InventoryHolder {
    private final Inventory inventory;

    public EffectChooser(Infuse plugin) {
        inventory = Bukkit.createInventory(this, 54, Messages.toComponent("<b>Infuses"));

        // Filling the inventory with decorative glass panes
        int[] magentaSlots = {0, 1, 2, 6, 7, 8, 9, 10, 16, 17, 18, 26, 27, 35};
        int[] purpleSlots = {36, 37, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        int[] lightBlueSlots = {3, 4, 5, 11, 13, 15, 19, 25, 28, 34, 38, 42};
        InventoryUtils.setItems(inventory, magentaSlots, InventoryUtils.createNoName(Material.MAGENTA_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, purpleSlots, InventoryUtils.createNoName(Material.PURPLE_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, lightBlueSlots, InventoryUtils.createNoName(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

        inventory.setItem(12, EffectMapping.AUG_FROST.createItem());
        inventory.setItem(14, EffectMapping.AUG_SPEED.createItem());
        inventory.setItem(20, EffectMapping.AUG_FEATHER.createItem());
        inventory.setItem(21, EffectMapping.AUG_OCEAN.createItem());
        inventory.setItem(22, EffectMapping.AUG_INVIS.createItem());
        inventory.setItem(23, EffectMapping.AUG_ENDER.createItem());
        inventory.setItem(24, EffectMapping.AUG_EMERALD.createItem());
        inventory.setItem(29, EffectMapping.AUG_HEART.createItem());
        inventory.setItem(30, EffectMapping.AUG_REGEN.createItem());
        inventory.setItem(31, EffectMapping.AUG_STRENGTH.createItem());
        inventory.setItem(32, EffectMapping.AUG_FIRE.createItem());
        inventory.setItem(33, EffectMapping.AUG_HASTE.createItem());
        inventory.setItem(40, EffectMapping.AUG_THUNDER.createItem());

        if (plugin.getMainConfig().enableThief()) {
            inventory.setItem(39, EffectMapping.AUG_THIEF.createItem());
        }
        if (plugin.getMainConfig().enableApophis()) {
            inventory.setItem(41, EffectMapping.AUG_APOPHIS.createItem());
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}