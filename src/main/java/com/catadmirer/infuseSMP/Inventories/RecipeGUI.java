package com.catadmirer.infuseSMP.Inventories;

import com.catadmirer.infuseSMP.Commands.Recipes;
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
        inventory.setItem(25, Recipes.createPotion(potionKey));
        Recipes.fillRemainingSlots(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
