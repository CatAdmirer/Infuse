package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InfuseDebug;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.inventories.RecipeGUI;
import com.catadmirer.infuseSMP.inventories.RecipeListGUI;
import com.catadmirer.infuseSMP.util.InventoryUtils;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.*;
import net.kyori.adventure.text.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class Recipes implements CommandExecutor, Listener {
    private static Infuse plugin;

    private final Map<String, List<String>> potionShapes = new HashMap<>();
    private final Map<String, Map<Character, String>> potionIngredients = new HashMap<>();

    public static final List<String> recipeKeys = List.of("emerald", "feather", "fire", "aug_ender",
            "ender", "frost", "haste", "heart", "invis", "ocean", "regen", "speed", "strength",
            "thunder", "apophis", "thief");

    public Recipes(Infuse plugin) {
        Recipes.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            openGUI(player);
            return true;
        }

        return false;
    }

    /**
     * Create a potion effect with the effect limits for lore rather than the default lore.
     * 
     * @param potionName The name of the potion to create.
     * 
     * @return The effect item with modified lore.
     */
    public static ItemStack createPotionWithModifiedLore(String potionName) {
        // Getting the InfuseEffect from the recipe key
        InfuseEffect effect = InfuseEffect.fromEffectKey(potionName);
        if (effect == null) return null;

        // Creating the potion from the effect
        ItemStack potionItem = effect.createItem();

        int augLeft = plugin.getConfigFile().getCraftLimit(effect.getAugmentedForm()) - plugin.getDataManager().getCrafted(effect.getAugmentedForm());
        int regLeft = plugin.getConfigFile().getCraftLimit(effect) - plugin.getDataManager().getCrafted(effect);

        // Ender effect is *special*
        if (effect.getId() == EffectIds.ENDER) {
            if (effect.isAugmented()) {
                regLeft = 0;
            } else {
                augLeft = 0;
            }
        }

        if (augLeft == 0 && regLeft == 0) {
            return InventoryUtils.createNoName(Material.RED_STAINED_GLASS_PANE);
        }

        ItemMeta meta = potionItem.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            lore.add(Messages.toComponent("<gray>Augmented Limit: <aqua>" + augLeft));
            lore.add(Messages.toComponent("<gray>Regular Limit: <aqua>" + regLeft));
            meta.lore(lore);
            potionItem.setItemMeta(meta);
        }

        return potionItem;
    }

    /**
     * Creates a potion item based on a recipe key.
     * 
     * @param potionName The recipe key to identify
     * 
     * @return An effect as a potion.
     */
    @Nullable
    public static ItemStack createPotion(String potionName) {
        return switch (potionName) {
            case "emerald" -> new Emerald(true).createItem();
            case "aug_ender" -> new Ender(true).createItem();
            case "ender" -> new Ender().createItem();
            case "feather" -> new Feather(true).createItem();
            case "fire" -> new Fire(true).createItem();
            case "frost" -> new Frost(true).createItem();
            case "haste" -> new Haste(true).createItem();
            case "heart" -> new Heart(true).createItem();
            case "invis" -> new Invis(true).createItem();
            case "ocean" -> new Ocean(true).createItem();
            case "regen" -> new Regen(true).createItem();
            case "speed" -> new Speed(true).createItem();
            case "strength" -> new Strength(true).createItem();
            case "thunder" -> new Thunder(true).createItem();
            case "apophis" -> new Apophis(true).createItem();
            case "thief" -> new Thief(true).createItem();
            default -> null;
        };
    }

    /**
     * Opens the recipe list gui for a player.
     * 
     * @param player The player to open the gui for.
     */
    public static void openGUI(Player player) {
        Inventory gui = new RecipeListGUI().getInventory();
        player.openInventory(gui);
    }

    /**
     * Utility function that fills all empty slots of an inventory with red stained glass panes with
     * empty names.
     * 
     * @param inventory The inventory to fill with panes.
     */
    public static void fillRemainingSlots(Inventory inventory) {
        ItemStack stainedGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta meta = stainedGlassPane.getItemMeta();
        meta.displayName(Component.empty());
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, stainedGlassPane);
            }
        }
    }

    /**
     * Inventory click handler for the RecipeGUI inventory
     * 
     * @param event an InventoryClickEvent
     */
    @EventHandler
    public void recipeGUIHandler(InventoryClickEvent event) {
        if (event.getClickedInventory().getHolder() == null) return;
        if (event.getClickedInventory().getHolder() instanceof RecipeGUI) {
            event.setCancelled(true);
        }
    }

    /**
     * Inventory click handler for the RecipeListGUI inventory
     * 
     * @param event an InventoryClickEvent
     */
    @EventHandler
    public void recipeListGUIHandler(InventoryClickEvent event) {
        if (event.getClickedInventory().getHolder() == null) return;
        if (event.getClickedInventory().getHolder() instanceof RecipeListGUI
                && event.getCurrentItem() != null) {
            event.setCancelled(true);

            // Getting the clicked item and opening the recipe menu for the item.
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem.getItemMeta() != null && clickedItem.getItemMeta().hasDisplayName()) {
                openRecipeGUI(event.getWhoClicked(), clickedItem);
            }
        }
    }

    /**
     * Loads the potion recipes from the recipes.yml file.
     */
    private void loadPotionRecipes() {
        // Loading the recipes file
        File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(recipesFile);
        for (String potionName : config.getConfigurationSection("recipes").getKeys(false)) {
            if (config.getBoolean("recipes." + potionName + ".enabled")) {
                List<String> shape = config.getStringList("recipes." + potionName + ".shape");
                Map<Character, String> ingredients = new HashMap<>();
                for (String key : config.getConfigurationSection("recipes." + potionName + ".ingredients").getKeys(false)) {
                    ingredients.put(key.charAt(0), config.getString("recipes." + potionName + ".ingredients." + key));
                }

                potionShapes.put(potionName, shape);
                potionIngredients.put(potionName, ingredients);
            }
        }
    }

    /**
     * Opens the recipe gui for a player based on the item they clicked.
     * 
     * @param player The player to open the inventory for.
     * @param clickedItem The item to get the recipe for.
     */
    public void openRecipeGUI(HumanEntity player, ItemStack clickedItem) {
        // Getting the potion key from the clicked item
        String potionKey = getPotionKeyFromItem(clickedItem);
        if (potionKey == null) {
            player.sendMessage(Messages.RECIPE_NOT_FOUND.toComponent());
            return;
        }

        // Loading the recipes
        loadPotionRecipes();

        // Getting the recipe info for the effect
        List<String> shape = potionShapes.get(potionKey);
        Map<Character, String> ingredients = potionIngredients.get(potionKey);

        // Erroring out if the recipe is not found
        if (shape == null) {
            player.sendMessage(Messages.RECIPE_DISABLED.toComponent());
            return;
        }

        // Opening the recipe gui
        Inventory recipeGui = new RecipeGUI(potionKey, shape, ingredients).getInventory();
        player.closeInventory();
        player.openInventory(recipeGui);
    }

    /**
     * Gets a string recipe key from an ItemStack. These keys are NOT reversible and cannot be used
     * to 100% identify an effect. This is an identifier for the recipes.
     */
    @Nullable
    private String getPotionKeyFromItem(@Nullable ItemStack item) {
        InfuseEffect effect = InfuseEffect.fromItem(item);

        if (effect == null) return null;

        if (effect.getId() == EffectIds.ENDER) {
            return effect.isAugmented() ? "aug_ender" : "ender";
        }

        return effect.getName();
    }
}