package com.catadmirer.infuseSMP.managers;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import com.catadmirer.infuseSMP.Infuse;

public class RecipeManager {
    private final Infuse plugin;
    private final File recipesFile;
    private final FileConfiguration recipesConfig;

    public RecipeManager(Infuse plugin) {
        this.plugin = plugin;

        recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false);
        }

        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);
    }

    /**
     * Manager functionality for when the plugin is reloaded.
     * 
     * In this case, it unregisters all the recipes then adds them back.
     */
    public void reload() {
        // Removing all the infuse recipes
        for (EffectMapping effect : EffectMapping.values()) {
            if (effect.isAugmented()) continue;
            Bukkit.removeRecipe(getRecipeKey(effect), true);
        }

        // Adding back the infuse recipes
        registerRecipes();
    }

    /** Registers the recipe for each effect. */
    public void registerRecipes() {
        for (EffectMapping effect : EffectMapping.values()) {
            if (effect.isAugmented()) continue;
            if (!isRecipeEnabled(effect)) continue;
            ShapedRecipe recipe = getRecipe(effect);
            
            Bukkit.addRecipe(recipe);
        }
    }

    public boolean isRecipeEnabled(EffectMapping mapping) {
        return recipesConfig.getBoolean(mapping.regular().getKey() + ".enabled", false);
    }

    public ShapedRecipe getRecipe(EffectMapping mapping) {
        String baseKey = mapping.regular().getKey();
        NamespacedKey recipeKey = new NamespacedKey(plugin, baseKey);
        ShapedRecipe effectRecipe = new ShapedRecipe(recipeKey, mapping.regular().createItem());

        effectRecipe.shape(recipesConfig.getStringList(baseKey + ".shape").toArray(String[]::new));
        ConfigurationSection ingredientsConfig = recipesConfig.getConfigurationSection(baseKey + ".ingredients");
        for (String key : ingredientsConfig.getKeys(false)) {
            char ingredientLabel = key.charAt(0);
            String materialName = ingredientsConfig.getString(key);
            Material ingredientMaterial = Material.valueOf(materialName.toUpperCase());
            effectRecipe.setIngredient(ingredientLabel, ingredientMaterial);
        }

        return effectRecipe;
    }

    public NamespacedKey getRecipeKey(EffectMapping effect) {
        return new NamespacedKey(plugin, effect.regular().getKey());
    }

    /**
     * Gets the item to craft from an official Infuse recipe.
     * This makes it easier to determine whether an infuse recipe should craft an augmented or regular effect.
     * 
     * @param recipe The infuse {@link Recipe} to determine the result for.
     * 
     * @return The corresponding {@link ItemStack} for the recipe, or null if the craft limit has been reached or the recipe is not an infuse recipe.
     */
    public ItemStack getItemToCraft(Recipe recipe) {
        ItemStack item = recipe.getResult();
        
        // The returned EffectMapping should always be the regular form
        EffectMapping effect = EffectMapping.fromItem(item);
        if (effect == null) return null;
        if (effect.isAugmented()) return null;

        // Checking if the augmented limit has been reached
        EffectMapping augEffect = effect.augmented();
        if (plugin.getMainConfig().getCraftLimit(augEffect) > plugin.getDataManager().getCrafted(augEffect)) {
            return augEffect.createItem();
        }

        // Checking if the regular limit has been reached
        if (plugin.getMainConfig().getCraftLimit(effect) > plugin.getDataManager().getCrafted(effect)) {
            return effect.createItem();
        }

        // Craft limits have been reached, return null
        return null;
    }
}