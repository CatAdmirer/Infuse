package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.Augmented;
import com.catadmirer.infuseSMP.Effects.Ender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class Recipes implements CommandExecutor, Listener {

    private final Infuse plugin;

    private Map<String, Object[]> potionRecipes = new HashMap<>();

    public Recipes(Infuse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            openGUI(player);
            return true;
        }
        return false;
    }

    private static ItemStack createPotionWithModifiedLore(String potionName, int augmentedLimit, int regularLimit) {
        Augmented augmented = new Augmented();
        ItemStack potionItem = null;
        switch (potionName) {
            case "emerald":
                potionItem = augmented.createEME();
                break;
            case "feather":
                potionItem = augmented.createFEATHER();
                break;
            case "fire":
                potionItem = augmented.createFIRE();
                break;
            case "end_first":
                potionItem = Augmented.createENDER();
                break;
            case "end_second":
                potionItem = Ender.createEnderGem();
                break;
            case "frost":
                potionItem = augmented.createFROST();
                break;
            case "haste":
                potionItem = augmented.createHASTE();
                break;
            case "heart":
                potionItem = augmented.createHEART();
                break;
            case "invis":
                potionItem = augmented.createINVIS();
                break;
            case "ocean":
                potionItem = augmented.createOCEAN();
                break;
            case "regen":
                potionItem = augmented.createREGEN();
                break;
            case "speed":
                potionItem = augmented.createSPEED();
                break;
            case "strength":
                potionItem = augmented.createST();
                break;
            case "thunder":
                potionItem = augmented.createTHUNDER();
                break;
            case "apophis":
                potionItem = augmented.createAPH();
                break;
            case "thief":
                potionItem = augmented.createTHF();
                break;
        }

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

    private ItemStack createPotion(String potionName) {
        Augmented augmented = new Augmented();
        ItemStack potionItem = null;
        switch (potionName) {
            case "emerald":
                potionItem = augmented.createEME();
                break;
            case "feather":
                potionItem = augmented.createFEATHER();
                break;
            case "fire":
                potionItem = augmented.createFIRE();
                break;
            case "end_first":
                potionItem = Augmented.createENDER();
                break;
            case "end_second":
                potionItem = Ender.createEnderGem();
                break;
            case "frost":
                potionItem = augmented.createFROST();
                break;
            case "haste":
                potionItem = augmented.createHASTE();
                break;
            case "heart":
                potionItem = augmented.createHEART();
                break;
            case "invis":
                potionItem = augmented.createINVIS();
                break;
            case "ocean":
                potionItem = augmented.createOCEAN();
                break;
            case "regen":
                potionItem = augmented.createREGEN();
                break;
            case "speed":
                potionItem = augmented.createSPEED();
                break;
            case "strength":
                potionItem = augmented.createST();
                break;
            case "thunder":
                potionItem = augmented.createTHUNDER();
                break;
            case "apophis":
                potionItem = augmented.createAPH();
                break;
            case "thief":
                potionItem = augmented.createTHF();
                break;
        }

        return potionItem;
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
            limits.put("augmented_limit", (Integer) Infuse.getInstance().getCanfig("craft_limits." + itemName + ".augmented_limit"));
            limits.put("regular_limit", (Integer) Infuse.getInstance().getCanfig("craft_limits." + itemName + ".regular_limit"));
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
            Player player = (Player) event.getWhoClicked();
            if (clickedItem.getItemMeta() != null && clickedItem.getItemMeta().hasDisplayName()) {
                String clickedItemName = clickedItem.getItemMeta().getDisplayName()
                        .replace("§6", "")
                        .toLowerCase()
                        .replace(" potion", "")
                        .replace(" ", "_");
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
                potionRecipes.put(potionName, new Object[] { shape, ingredients });
            }
        }
    }


    public void openRecipeGUI(Player player, ItemStack clickedItem) {
        String potionKey = getPotionKeyFromItem(clickedItem);
        if (potionKey == null) {
            player.sendMessage("§cNo recipe found for this potion.");
            return;
        }
        loadPotionRecipes();
        Object[] recipeData = potionRecipes.get(potionKey);
        if (recipeData == null) {
            player.sendMessage("Recipe is disabled/broken");
            return;
        }
        List<String> shape = (List<String>) recipeData[0];
        Map<Character, String> ingredients = (Map<Character, String>) recipeData[1];
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
        Augmented augmented = new Augmented();
        if (augmented.ISST(item)) {
            return "strength";
        } else if (Augmented.ISHEART(item)) {
            return "heart";
        } else if (augmented.ISREGEN(item)) {
            return "regen";
        } else if (augmented.ISINVIS(item)) {
            return "invis";
        } else if (augmented.ISEME(item)) {
            return "emerald";
        } else if (augmented.ISEND(item)) {
            return "end_first";
        } else if (Ender.ISENDER(item)) {
            return "end_second";
        } else if (augmented.ISSPEED(item)) {
            return "speed";
        } else if (augmented.ISHASTE(item)) {
            return "haste";
        } else if (augmented.ISFEATHER(item)) {
            return "feather";
        } else if (augmented.ISOCEAN(item)) {
            return "ocean";
        } else if (augmented.ISFROST(item)) {
            return "frost";
        } else if (augmented.ISFIRE(item)) {
            return "fire";
        } else if (augmented.ISTHUNDER(item)) {
            return "thunder";
        } else if (augmented.ISAUGAPH(item)) {
            return "apophis";
        } else if (augmented.ISTHIEF(item)) {
            return "thief";
        } else {
            return null;
        }
    }
}
