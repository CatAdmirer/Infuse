package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Augmented;
import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.inventories.RecipeGUI;
import com.catadmirer.infuseSMP.inventories.RecipeListGUI;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.io.File;
import java.util.ArrayList;
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
    private final Infuse plugin;

    private final Map<String, List<String>> potionShapes = new HashMap<>();
    private final Map<String, Map<Character, String>> potionIngredients = new HashMap<>();

    public static final List<String> recipeKeys = List.of("emerald", "feather", "fire", "end_first",
            "end_second", "frost", "haste", "heart", "invis", "ocean", "regen", "speed", "strength",
            "thunder", "apophis", "thief");

    public Recipes(Infuse plugin) {
        this.plugin = plugin;
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
        // Creating the potion from the key
        ItemStack potionItem = createPotion(potionName);
        if (potionItem == null) return null;

        // Getting the craft limits from the config
        List<Integer> limits = loadCraftLimitsFromConfig(potionName);

        ItemMeta meta = potionItem.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Augmented Limit: §b" + limits.get(0));
            lore.add("§7Regular Limit: §b" + limits.get(1));
            meta.setLore(lore);
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
    public static ItemStack createPotion(String potionName) {
        return switch (potionName) {
            case "emerald" -> Augmented.createEmerald();
            case "feather" -> Augmented.createFeather();
            case "fire" -> Augmented.createFire();
            case "end_first" -> Augmented.createEnder();
            case "end_second" -> Ender.createEffect();
            case "frost" -> Augmented.createFrost();
            case "haste" -> Augmented.createHaste();
            case "heart" -> Augmented.createHeart();
            case "invis" -> Augmented.createInvis();
            case "ocean" -> Augmented.createOcean();
            case "regen" -> Augmented.createRegen();
            case "speed" -> Augmented.createSpeed();
            case "strength" -> Augmented.createStrength();
            case "thunder" -> Augmented.createThunder();
            case "apophis" -> Augmented.createApophis();
            case "thief" -> Augmented.createThief();
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

        fillRemainingSlots(gui);
        player.openInventory(gui);
    }

    /**
     * Loads the craft limits from the config.
     * 
     * @return A map of the craft limits for each effect.
     */
    private static List<Integer> loadCraftLimitsFromConfig(String recipeKey) {
        return List.of(
            Infuse.getInstance().getConfig("craft_limits." + recipeKey + ".augmented_limit"),
            Infuse.getInstance().getConfig("craft_limits." + recipeKey + ".regular_limit"));
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
        meta.setDisplayName("§7");
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
        if (event.getInventory().getHolder() instanceof RecipeGUI) {
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
        if (event.getInventory().getHolder() instanceof RecipeListGUI
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
            player.sendMessage("§cNo recipe found for this potion.");
            return;
        }

        // Loading the recipes
        loadPotionRecipes();

        // Getting the recipe info for the effect
        List<String> shape = potionShapes.get(potionKey);
        Map<Character, String> ingredients = potionIngredients.get(potionKey);

        // Erroring out if the recipe is not found
        if (shape == null) {
            player.sendMessage("Recipe is disabled/broken");
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
    private String getPotionKeyFromItem(ItemStack item) {
        return switch (EffectMapping.fromItem(item)) {
            case APOPHIS, AUG_APOPHIS -> "apophis";
            case EMERALD, AUG_EMERALD -> "emerald";
            case ENDER -> "end_second";
            case AUG_ENDER -> "end_first";
            case FEATHER, AUG_FEATHER -> "feather";
            case FIRE, AUG_FIRE -> "fire";
            case FROST, AUG_FROST -> "frost";
            case HASTE, AUG_HASTE -> "haste";
            case HEART, AUG_HEART -> "heart";
            case INVISIBILITY, AUG_INVISIBILITY -> "invis";
            case OCEAN, AUG_OCEAN -> "ocean";
            case REGEN, AUG_REGEN -> "regen";
            case SPEED, AUG_SPEED -> "speed";
            case STRENGTH, AUG_STRENGTH -> "strength";
            case THIEF, AUG_THIEF -> "thief";
            case THUNDER, AUG_THUNDER -> "thunder";
            default -> null;
        };
    }
}