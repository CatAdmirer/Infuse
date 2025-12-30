package com.catadmirer.infuseSMP.Inventories;

import java.util.Map;

import com.catadmirer.infuseSMP.Commands.Recipes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RecipeListGUI implements InventoryHolder {
    private final Inventory inventory;

    public RecipeListGUI(Map<String, Map<String, Integer>> craftLimits) {
        inventory = Bukkit.createInventory(null, 36, Component.text("Potion Crafting"));

        int[] customSlots = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33};
        int index = 0;
        for (String potionName : craftLimits.keySet()) {
            if (index >= customSlots.length) break;

            Map<String, Integer> limits = craftLimits.get(potionName);
            int augmentedLimit = limits.get("augmented_limit");
            int regularLimit = limits.get("regular_limit");
            ItemStack potion = Recipes.createPotionWithModifiedLore(potionName, augmentedLimit, regularLimit);
            inventory.setItem(customSlots[index] - 1, potion);

            index++;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}