package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InventoryUtils;
import com.catadmirer.infuseSMP.effects.Augmented;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class EffectInventory implements InventoryHolder {
    private final Inventory inventory;

    public EffectInventory() {
        inventory = Bukkit.createInventory(this, 54, "§lInfuses");

        // Filling the inventory with decorative glass panes
        int[] magentaSlots = {0, 1, 2, 6, 7, 8, 9, 10, 16, 17, 18, 26, 27, 35};
        int[] purpleSlots = {36, 37, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        int[] lightBlueSlots = {3, 4, 5, 11, 13, 15, 19, 25, 28, 34, 38, 42};
        InventoryUtils.setItems(inventory, magentaSlots, InventoryUtils.createNoName(Material.MAGENTA_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, purpleSlots, InventoryUtils.createNoName(Material.PURPLE_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, lightBlueSlots, InventoryUtils.createNoName(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

        // Putting a single blue glass pane in the inventory.
        inventory.setItem(49, InventoryUtils.createNoName(Material.BLUE_STAINED_GLASS_PANE));

        // Filling the inventory with effects.
        inventory.setItem(12, Augmented.createFrost());
        inventory.setItem(14, Augmented.createSpeed());
        inventory.setItem(20, Augmented.createFeather());
        inventory.setItem(21, Augmented.createOcean());
        inventory.setItem(22, Augmented.createInvis());
        inventory.setItem(23, Augmented.createEnder());
        inventory.setItem(24, Augmented.createEmerald());
        inventory.setItem(29, Augmented.createHeart());
        inventory.setItem(30, Augmented.createRegen());
        inventory.setItem(31, Augmented.createStrength());
        inventory.setItem(32, Augmented.createFire());
        inventory.setItem(33, Augmented.createHaste());
        inventory.setItem(40, Augmented.createThunder());

        // Adding the extra effects if they are enabled.
        if (Infuse.getInstance().<Boolean>getConfig("extra_effects.Thief")) {
            inventory.setItem(39, Augmented.createThief());
        }
        if (Infuse.getInstance().<Boolean>getConfig("extra_effects.Apophis")) {
            inventory.setItem(41, Augmented.createApophis());
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}