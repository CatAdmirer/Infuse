package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.commands.Recipes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RecipeListGUI implements InventoryHolder {
    private final Inventory inventory;

    public RecipeListGUI() {
        inventory = Bukkit.createInventory(this, 36, Component.text("Potion Crafting"));

        // Loading the potions into the inventory
        int[] customSlots = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33};
        int index = 0;
        for (String potionName : Recipes.recipeKeys) {
            if (index >= customSlots.length) break;
            ItemStack potion = Recipes.createPotionWithModifiedLore(potionName);
            inventory.setItem(customSlots[index] - 1, potion);
            index++;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}