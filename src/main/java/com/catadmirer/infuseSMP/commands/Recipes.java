package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Augmented;
import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.inventories.RecipeGUI;
import com.catadmirer.infuseSMP.inventories.RecipeListGUI;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static ItemStack createPotionWithModifiedLore(String potionName, int augmentedLimit, int regularLimit) {
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

    public static void openGUI(Player player) {
        Inventory gui = new RecipeListGUI(loadCraftLimitsFromConfig()).getInventory();
        
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

    @EventHandler
    public void onInventoryClick2(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeListGUI && event.getCurrentItem() != null) {
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

        Inventory recipeGui = new RecipeGUI(potionKey, shape, ingredients).getInventory();
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