package com.catadmirer.infuseSMP.Inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class EffectLevelInventory implements InventoryHolder {
    private final Inventory inventory;

    public EffectLevelInventory(ItemStack regularEffect, ItemStack augmentedEffect, Material backgroundItem) {
        inventory = Bukkit.createInventory(this, 27, Component.text("Choose", NamedTextColor.YELLOW));
        fillChoiceGUI(inventory, backgroundItem);
        inventory.setItem(11, regularEffect);
        inventory.setItem(15, augmentedEffect);
    }

    private void fillChoiceGUI(Inventory gui, Material color) {
        ItemStack filler = new ItemStack(color);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.text(" "));
        filler.setItemMeta(meta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}