package com.catadmirer.infuseSMP.Inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.Augmented;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class EffectInventory implements InventoryHolder {
    private final Inventory inventory;

    public EffectInventory() {
        inventory = Bukkit.createInventory(this, 54, Component.text("Infuses").decorate(TextDecoration.BOLD));

        int[] magentaSlots = {0, 1, 2, 6, 7, 8, 9, 10, 16, 17, 18, 26, 27, 35};
        int[] purpleSlots = {36, 37, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        int[] lightBlueSlots = {3, 4, 5, 11, 13, 15, 19, 25, 28, 34, 38, 42};
        setItems(inventory, magentaSlots, createPane(Material.MAGENTA_STAINED_GLASS_PANE));
        setItems(inventory, purpleSlots, createPane(Material.PURPLE_STAINED_GLASS_PANE));
        setItems(inventory, lightBlueSlots, createPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inventory.setItem(49, createPane(Material.BLUE_STAINED_GLASS_PANE));

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

        if (Infuse.getInstance().<Boolean>getCanfig("extra_effects.Thief")) {
            inventory.setItem(39, Augmented.createThief());
        }
        if (Infuse.getInstance().<Boolean>getCanfig("extra_effects.Apophis")) {
            inventory.setItem(41, Augmented.createApophis());
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private static ItemStack createPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.text(" "));
        pane.setItemMeta(meta);
        return pane;
    }

    private static void setItems(Inventory gui, int[] slots, ItemStack item) {
        for (int slot : slots) {
            gui.setItem(slot, item);
        }
    }
}