package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.InventoryUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class EffectChooser implements InventoryHolder {
    private final Inventory inventory;

    public EffectChooser(Infuse plugin) {
        inventory = Bukkit.createInventory(this, 54, Component.text("Infuses").decorate(TextDecoration.BOLD));

        // Filling the inventory with decorative glass panes
        int[] magentaSlots = {0, 1, 2, 6, 7, 8, 9, 10, 16, 17, 18, 26, 27, 35};
        int[] purpleSlots = {36, 37, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        int[] lightBlueSlots = {3, 4, 5, 11, 13, 15, 19, 25, 28, 34, 38, 42};
        InventoryUtils.setItems(inventory, magentaSlots, InventoryUtils.createNoName(Material.MAGENTA_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, purpleSlots, InventoryUtils.createNoName(Material.PURPLE_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, lightBlueSlots, InventoryUtils.createNoName(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

        inventory.setItem(12, EffectMapping.FROST.createItem());
        inventory.setItem(14, EffectMapping.SPEED.createItem());
        inventory.setItem(20, EffectMapping.FEATHER.createItem());
        inventory.setItem(21, EffectMapping.OCEAN.createItem());
        inventory.setItem(22, EffectMapping.INVIS.createItem());
        inventory.setItem(23, EffectMapping.ENDER.createItem());
        inventory.setItem(24, EffectMapping.EMERALD.createItem());
        inventory.setItem(29, EffectMapping.HEART.createItem());
        inventory.setItem(30, EffectMapping.REGEN.createItem());
        inventory.setItem(31, EffectMapping.STRENGTH.createItem());
        inventory.setItem(32, EffectMapping.FIRE.createItem());
        inventory.setItem(33, EffectMapping.HASTE.createItem());
        inventory.setItem(40, EffectMapping.THUNDER.createItem());

        if (plugin.<Boolean>getConfig("extra_effects.Thief")) {
            inventory.setItem(39, EffectMapping.THIEF.createItem());
        }
        if (plugin.<Boolean>getConfig("extra_effects.Apophis")) {
            inventory.setItem(41, EffectMapping.APOPHIS.createItem());
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}