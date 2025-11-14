package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.Augmented;
import com.catadmirer.infuseSMP.Effects.Ender;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
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

    private final Map<String,List<String>> potionShapes = new HashMap<>();
    private final Map<String,Map<Character,String>> potionIngredients = new HashMap<>();

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

    private static ItemStack createPotionWithModifiedLore(String potionName, int augmentedLimit, int regularLimit) {
        ItemStack potionItem = createPotion(potionName);

        if (potionItem != null) {
            ItemMeta meta = potionItem.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Augmented Limit: §b" + augmentedLimit);
                lore.add("§7Regular Limit: §b" + regularLimit);
                meta.setLore(lore);
                potionItem.setItemMeta(meta);
            }
        }

        return potionItem;
    }

    private static ItemStack createPotion(String potionName) {
        return switch (potionName) {
            case "emerald" -> Augmented.createEME();
            case "feather" -> Augmented.createFEATHER();
            case "fire" -> Augmented.createFIRE();
            case "end_first" -> Augmented.createENDER();
            case "end_second" -> Ender.createEnderGem();
            case "frost" -> Augmented.createFROST();
            case "haste" -> Augmented.createHASTE();
            case "heart" -> Augmented.createHEART();
            case "invis" -> Augmented.createINVIS();
            case "ocean" -> Augmented.createOCEAN();
            case "regen" -> Augmented.createREGEN();
            case "speed" -> Augmented.createSPEED();
            case "strength" -> Augmented.createST();
            case "thunder" -> Augmented.createTHUNDER();
            case "apophis" -> Augmented.createAPH();
            case "thief" -> Augmented.createTHF();
            default -> null;
        };
    }

    public static void openGUI(Player player) {
        Map<String, Map<String, Integer>> craftLimits = loadCraftLimitsFromConfig();
        Inventory gui = Bukkit.createInventory(null, 36, "Potion Crafting");
        int[] customSlots = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33};
        int index = 0;
        for (String potionName : craftLimits.keySet()) {
            if (index >= customSlots.length) break;

            Map<String, Integer> limits = craftLimits.get(potionName);
            int augmentedLimit = limits.get("augmented_limit");
            int regularLimit = limits.get("regular_limit");
            ItemStack potion = createPotionWithModifiedLore(potionName, augmentedLimit, regularLimit);
            gui.setItem(customSlots[index] - 1, potion);

            index++;
        }
        fillRemainingSlots(gui);
        player.openInventory(gui);
    }

    private static Map<String, Map<String, Integer>> loadCraftLimitsFromConfig() {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        for (String itemName : Arrays.asList(
                "emerald","feather","fire","end_first","end_second","frost",
                "haste","heart","invis","ocean","regen","speed","strength","thunder","apophis","thief"
        )) {
            Map<String, Integer> limits = new HashMap<>();
            limits.put("augmented_limit", Infuse.getInstance().getCanfig("craft_limits." + itemName + ".augmented_limit"));
            limits.put("regular_limit", Infuse.getInstance().getCanfig("craft_limits." + itemName + ".regular_limit"));
            result.put(itemName, limits);
        }

        return result;
    }

    private static void fillRemainingSlots(Inventory inventory) {
        ItemStack stainedGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta meta = stainedGlassPane.getItemMeta();
        meta.setDisplayName("§7");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, stainedGlassPane);
            }
        }
    }

    @EventHandler
    public void onInventoryClick2(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Recipes")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Potion Crafting") && event.getCurrentItem() != null) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            HumanEntity player = event.getWhoClicked();
            if (clickedItem.getItemMeta() != null && clickedItem.getItemMeta().hasDisplayName()) {
                openRecipeGUI(player, clickedItem);
            }
        }
    }
    
    private void loadPotionRecipes() {
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

    public void openRecipeGUI(HumanEntity player, ItemStack clickedItem) {
        String potionKey = getPotionKeyFromItem(clickedItem);
        if (potionKey == null) {
            player.sendMessage("§cNo recipe found for this potion.");
            return;
        }

        loadPotionRecipes();
        List<String> shape = potionShapes.get(potionKey);
        Map<Character, String> ingredients = potionIngredients.get(potionKey);

        if (shape == null) {
            player.sendMessage("Recipe is disabled/broken");
            return;
        }

        Inventory recipeGui = Bukkit.createInventory(null, 45, "Recipes");
        int[] ingredientSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        int slotIndex = 0;
        for (String row : shape) {
            for (char ch : row.toCharArray()) {
                String ingredientName = ingredients.get(ch);
                if (ingredientName != null) {
                    Material material = Material.getMaterial(ingredientName.toUpperCase());
                    if (material != null) {
                        ItemStack ingredientItem = new ItemStack(material);
                        recipeGui.setItem(ingredientSlots[slotIndex], ingredientItem);
                    }
                }
                slotIndex++;
            }
        }
        recipeGui.setItem(25, createPotion(potionKey));
        fillRemainingSlots(recipeGui);
        player.closeInventory();
        player.openInventory(recipeGui);
    }

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
