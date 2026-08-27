package com.catadmirer.infuseSMP.managers;

import java.io.File;

import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
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
     * <p>
     * In this case, it unregisters all the recipes then adds them back.
     */
    public void reload() {
        try {
            recipesConfig.load(recipesFile);
        } catch (Exception e) {
            Infuse.LOGGER.error("Could not reload recipes.yml", e);
        }

        // Removing all the infuse recipes
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            Bukkit.removeRecipe(getRecipeKey(effect));
        }

        // Adding back the infuse recipes
        registerRecipes();
    }

    /** Registers the recipe for each effect. */
    public void registerRecipes() {
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            if (plugin.getMainConfig().allowInfiniteEffects()) {
                Bukkit.addRecipe(effect.getAugmentedVersion());
                return;
            }

            effect = effect.getAugmentedVersion();
            int craftLimit = plugin.getMainConfig().getCraftLimit(effect);
            int crafted = plugin.getDataManager().getExistingCount(effect);

            // If augmented limit is reached, check regular limit.
            if (craftLimit == crafted) {
                effect = effect.getRegularVersion();

                craftLimit = plugin.getMainConfig().getCraftLimit(effect);
                crafted = plugin.getDataManager().getExistingCount(effect);

                // If regular limit is reached, don't register the recipe.
                if (craftLimit == crafted) continue;
            }
            
            ShapedRecipe recipe = getRecipe(effect);

            Bukkit.addRecipe(recipe);
        }
    }

    public ShapedRecipe getRecipe(InfuseEffect mapping) {
        String baseKey = mapping.getKey();
        NamespacedKey recipeKey = getRecipeKey(mapping);
        ShapedRecipe effectRecipe = new ShapedRecipe(recipeKey, mapping.createItem());

        effectRecipe.shape(recipesConfig.getStringList(baseKey + ".shape").toArray(String[]::new));

        ConfigurationSection ingredientsConfig = recipesConfig.getConfigurationSection(baseKey + ".ingredients");
        for (String key : ingredientsConfig.getKeys(false)) {
            char ingredientLabel = key.charAt(0);

            String materialName = ingredientsConfig.getString(key);
            if (materialName == null) {
                Infuse.LOGGER.error("The infuse effect '{}' has failed to register its recipe, A ingredient has not been defined properly.", baseKey);
            }

            Material ingredientMaterial = Material.valueOf(materialName.toUpperCase());
            effectRecipe.setIngredient(ingredientLabel, ingredientMaterial);
        }

        return effectRecipe;
    }

    public NamespacedKey getRecipeKey(InfuseEffect effect) {
        return new NamespacedKey(plugin, effect.getPlainKey());
    }
}
