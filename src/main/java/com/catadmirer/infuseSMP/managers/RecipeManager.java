package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
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
        for (InfuseEffect effect : InfuseEffect.getAllEffects()) {
            if (effect.isAugmented()) continue;
            Bukkit.removeRecipe(getRecipeKey(effect), true);
        }

        // Adding back the infuse recipes
        registerRecipes();
    }

    /** Registers the recipe for each effect. */
    public void registerRecipes() {
        for (InfuseEffect effect : InfuseEffect.getAllEffects()) {
            if (effect.isAugmented()) continue;
            if (!isRecipeEnabled(effect)) continue;
            ShapedRecipe recipe = getRecipe(effect);
            
            Bukkit.addRecipe(recipe);
        }
    }

    public boolean isRecipeEnabled(InfuseEffect effect) {
        return recipesConfig.getBoolean(effect.getName() + ".enabled", false);
    }

    public ShapedRecipe getRecipe(InfuseEffect effect) {
        String baseKey = effect.getName();
        NamespacedKey recipeKey = new NamespacedKey(plugin, baseKey);
        ShapedRecipe effectRecipe = new ShapedRecipe(recipeKey, effect.getRegularForm().createItem());

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

    public void updateEnderRecipe() {
        Ender effect = new Ender(true);
        if (plugin.getDataManager().getExistingCount(effect) > 0) {
            ShapedRecipe enderRecipe = getRecipe(effect);
            Bukkit.removeRecipe(enderRecipe.getKey(), true);

            String matName = recipesConfig.getString("ender.egg_replacement");
            Material eggReplacement = Material.valueOf(matName.toUpperCase());

            ConfigurationSection ingredientsConfig = recipesConfig.getConfigurationSection("ender.ingredients");
            for (String key : ingredientsConfig.getKeys(false)) {
                char ingredientLabel = key.charAt(0);
                if (!ingredientsConfig.getString(key).equals("DRAGON_EGG")) continue;

                enderRecipe.setIngredient(ingredientLabel, eggReplacement);
            }

            Bukkit.addRecipe(enderRecipe);
        }
    }

    public NamespacedKey getRecipeKey(InfuseEffect effect) {
        return new NamespacedKey(plugin, effect.getName());
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
        InfuseEffect effect = InfuseEffect.fromItem(item);
        if (effect == null) return null;
        if (effect.isAugmented()) return null;

        // Checking if the augmented limit has been reached
        InfuseEffect augEffect = effect.getAugmentedForm();
        if (plugin.getMainConfig().getCraftLimit(augEffect) > plugin.getDataManager().getExistingCount(augEffect)) {
            return augEffect.createItem();
        }

        // Checking if the regular limit has been reached
        if (plugin.getMainConfig().getCraftLimit(effect) > plugin.getDataManager().getExistingCount(effect)) {
            return effect.createItem();
        }

        // Craft limits have been reached, return null
        return null;
    }
}