package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.commands.Recipes;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RecipeGUI implements InventoryHolder {
    private final Inventory inventory;

    public RecipeGUI(String potionKey, List<String> shape, Map<Character,String> ingredients) {
        inventory = Bukkit.createInventory(null, 45, "Recipes");
        
        // Loading the ingredients into the gui
        int[] ingredientSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        int slotIndex = 0;
        for (String row : shape) {
            for (char ch : row.toCharArray()) {
                String ingredientName = ingredients.get(ch);
                if (ingredientName != null) {
                    Material material = Material.getMaterial(ingredientName.toUpperCase());
                    if (material != null) {
                        ItemStack ingredientItem = new ItemStack(material);
                        inventory.setItem(ingredientSlots[slotIndex], ingredientItem);
                    }
                }
                slotIndex++;
            }
        }

        // Loading the result of the recipe into the output slot.
        inventory.setItem(25, Recipes.createPotion(potionKey));

        // Filling the rest of the slots with red glass panes
        Recipes.fillRemainingSlots(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}