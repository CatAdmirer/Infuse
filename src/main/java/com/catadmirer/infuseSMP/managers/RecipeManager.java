package com.catadmirer.infuseSMP.managers;

import java.io.File;

import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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

    public RecipeManager() {
        this.plugin = Infuse.getInstance();

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
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects()) {
            Bukkit.removeRecipe(getRecipeKey(effect), true);
        }

        // Adding back the infuse recipes
        registerRecipes();
    }

    /** Registers the recipe for each effect. */
    public void registerRecipes() {
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects()) {
            ShapedRecipe recipe = getRecipe(effect.getRegularVersion());

            Bukkit.addRecipe(recipe);
        }
    }

    public boolean isRecipeEnabled(InfuseEffect mapping) {
        NamespacedKey key = getRecipeKey(mapping);
        return Bukkit.getRecipe(key) != null;
    }

    public ShapedRecipe getRecipe(InfuseEffect mapping) {
        String baseKey = mapping.getPlainKey();
        NamespacedKey recipeKey = new NamespacedKey(plugin, baseKey);
        ShapedRecipe effectRecipe = new ShapedRecipe(recipeKey, mapping.getRegularVersion().createItem());

        effectRecipe.shape(recipesConfig.getStringList(baseKey + ".shape").toArray(String[]::new));

        ConfigurationSection ingredientsConfig = recipesConfig.getConfigurationSection(baseKey + ".ingredients");
        for (String key : ingredientsConfig.getKeys(false)) {
            char ingredientLabel = key.charAt(0);

            String materialName = ingredientsConfig.getString(key);
            if (materialName == null) {
                Infuse.LOGGER.error("Failed to get a recipe for the '{}' effect.  An ingredient key has no value.", baseKey);
                continue;
            }

            NamespacedKey matKey = NamespacedKey.fromString(materialName.toLowerCase());
            if (matKey == null) {
                Infuse.LOGGER.error("Failed to get a recipe for the '{}' effect.  '{}' is an invalid material.", baseKey, materialName);
                continue;
            }

            Material ingredientMaterial = Registry.MATERIAL.get(matKey);
            if (ingredientMaterial == null) {
                Infuse.LOGGER.error("Failed to get a recipe for the '{}' effect.  The material '{}' could not be found.", baseKey, materialName);
                continue;
            }

            effectRecipe.setIngredient(ingredientLabel, ingredientMaterial);
        }

        return effectRecipe;
    }

    public void updateEnderRecipe() {
        if (plugin.getDataManager().getExistingCount(new Ender(true)) > 0) {
            ShapedRecipe enderRecipe = getRecipe(new Ender(false));
            Bukkit.removeRecipe(enderRecipe.getKey(), true);

            String matName = recipesConfig.getString("ender.egg_replacement");
            if (matName == null) {
                Infuse.LOGGER.info("Did not find a replacement for the dragon egg.  Skipping recipe update.");
                return;
            }

            NamespacedKey matKey = NamespacedKey.fromString(matName);
            if (matKey == null) {
                Infuse.LOGGER.error("Failed to get the dragon egg replacement for the ender effect.  '{}' is an invalid material.", matName);
                return;
            }

            Material eggReplacement = Registry.MATERIAL.get(matKey);
            if (eggReplacement == null) {
                Infuse.LOGGER.error("Failed to get the dragon egg replacement for the ender effect.  '{}' is not a registered material.", matName);
                return;
            }

            ItemStack egg = new ItemStack(Material.DRAGON_EGG);
            enderRecipe.getChoiceMap().forEach((key, value) -> {
                if (value.test(egg)) {
                    enderRecipe.setIngredient(key, eggReplacement);
                }
            });

            Bukkit.addRecipe(enderRecipe);
        }
    }

    public NamespacedKey getRecipeKey(InfuseEffect effect) {
        return new NamespacedKey(plugin, effect.getPlainKey());
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
        InfuseEffect effect = InfuseEffect.getEffect(item);
        if (effect == null) return null;
        if (effect.isAugmented()) return null;

        // Checking if the augmented limit has been reached
        InfuseEffect augEffect = effect.getAugmentedVersion();
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
