package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RecipeManager {
    private static final Infuse plugin = Infuse.getInstance();
    private static final File file = new File(plugin.getDataFolder(), "recipes.yml");
    private static final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    private static final Set<NamespacedKey> registered = new HashSet<>();

    static {
        load();
    }

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully.
     */
    public static boolean load() {
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }

        // Loading the config
        try {
            config.load(file);
            Infuse.LOGGER.info("Successfully loaded {}", file.getName());
            return true;
        } catch (InvalidConfigurationException e) {
            Infuse.LOGGER.warn("{} contains an invalid YAML configuration.  Verify the contents of the file.", file.getName());
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not find {}.  Check that it exists.", file.getName());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Writes the config to the file.
     *
     * @return Whether or not the config was successfully written.
     */
    public static boolean save() {
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }

        // Saving the config
        try {
            config.save(file);
            Infuse.LOGGER.info("Saved {}", file.getName());
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.warn("Could not save {}.  Make sure the user has write permissions.", file.getName());
        }

        return false;
    }

    public static void unregisterRecipes() {
        registered.forEach(key -> Bukkit.removeRecipe(key));
        registered.clear();
    }

    public static void registerRecipes() {
        InfuseEffect.getRegisteredEffects().values()
            .stream()
            .map(RecipeManager::getNextCraftable)
            .map(e -> {
                CraftingRecipe recipe = RecipeManager.getRecipe(e);

                if (recipe == null) {
                    Infuse.LOGGER.error("Recipe for effect {} returned null.  Skipping recipe registration.", e.getKey());
                }

                return recipe;
            })
            .filter(Objects::nonNull)
            .forEach(RecipeManager::registerRecipe);
    }

    public static void unregisterRecipe(NamespacedKey recipe) {
        Bukkit.removeRecipe(recipe);
        registered.remove(recipe);
    }

    public static void registerRecipe(CraftingRecipe recipe) {
        if (Bukkit.addRecipe(recipe)) {
            registered.add(recipe.getKey());
        } else {
            Infuse.LOGGER.info("Failed to register the recipe for the {} effect.", recipe.getKey().asString());
        }
    }

    public static @UnmodifiableView Set<NamespacedKey> getRegisteredRecipes() {
        return Set.copyOf(registered);
    }

    /**
     * Handles updating the recipe book when a player crafts an effect.
     * Adds and removes recipes as needed.
     * 
     * @param crafted The effect that was just crafted.
     */
    public static void updateRecipes(InfuseEffect crafted) {
        InfuseEffect next = getNextCraftable(crafted);

        // Removing the recipe if no more effects can be crafted
        if (next == null) {
            unregisterRecipe(getRecipeKey(crafted));
            return;
        }

        // Next limit not reached, skipping updates
        if (next.equals(crafted)) return;

        // Unregistering the old recipe
        unregisterRecipe(getRecipeKey(crafted));

        // Registering the new recipe
        CraftingRecipe recipe = getRecipe(next);
        if (recipe == null) {
            Infuse.LOGGER.error("Recipe for effect {} returned null.  Skipping recipe registration.", next.getKey());
            return;
        }

        registerRecipe(recipe);
    }

    public static NamespacedKey getRecipeKey(InfuseEffect effect) {
        return new NamespacedKey(plugin, effect.getKey());
    }

    /**
     * Checks the DataManager to see if the augmented or regular form of an effect should be crafted.
     * 
     * @param effect The effect to check.
     * @return Null if no more of the effect can be crafted.  Otherwise, the augmented or regular form of the effect.
     */
    public static InfuseEffect getNextCraftable(InfuseEffect effect) {
        effect = effect.getAugmentedVersion();

        int limit = plugin.getMainConfig().getCraftLimit(effect);
        int existing = plugin.getDataManager().getExistingCount(effect);
        if (existing < limit) return effect;

        effect = effect.getRegularVersion();

        limit = plugin.getMainConfig().getCraftLimit(effect);
        existing = plugin.getDataManager().getExistingCount(effect);
        if (existing < limit) return effect;

        return null;
    }

    /**
     * Gets the recipe for the specified {@link InfuseEffect}.
     * 
     * @param effect An effect.
     * @return The recipe for the effect or `null` if the recipe could not be read or is not specified.
     */
    @Nullable
    public static CraftingRecipe getRecipe(InfuseEffect effect) {
        // Making sure the effect's default recipe exists
        if (!config.contains(effect.getPlainKey())) {
            Infuse.LOGGER.warn("No recipe defined for effect {}.", effect.getKey());
            return null;
        }

        // If the augmented key is present, get that recipe
        String key = config.contains(effect.getKey()) ? effect.getKey() : effect.getPlainKey();

        String type = config.getString(key + ".type");

        if (type == null) {
            Infuse.LOGGER.warn("Invalid recipe '{}'.  Missing 'type' key.  Allowed values are 'shaped' or 'shapeless'.", key);
            return null;
        }

        // Creating recipes
        if (type.equals("shaped")) return getShapedRecipe(key, effect);
        if (type.equals("shapeless")) return getShapelessRecipe(key, effect);

        Infuse.LOGGER.warn("Invalid recipe '{}'.  Invalid 'type' key.  Allowed values are 'shaped' or 'shapeless'.", key);
        return null;
    }

    private static ShapedRecipe getShapedRecipe(String key, InfuseEffect effect) {
        // Making sure the required sections exist
        if (!config.contains(key + ".shape")) {
            Infuse.LOGGER.warn("Invalid recipe '{}'.  Missing the 'shape' key.", key);
            return null;
        }
        
        if (!config.contains(key + ".ingredients")) {
            Infuse.LOGGER.warn("Invalid recipe '{}'.  Missing the 'ingredients' key.", key);
            return null;
        }

        // Parsing shape
        String[] shape = config.getStringList(key + ".shape").stream().toArray(String[]::new);
        if (shape.length != 3 || shape[0].length() != 3 || shape[1].length() != 3 || shape[2].length() != 3) {
            Infuse.LOGGER.warn("Invalid shape config for recipe {}.  It needs to be a list of three strings that are each 3 characters long.", key);
            return null;
        }

        ShapedRecipe recipe = new ShapedRecipe(getRecipeKey(effect), effect.createItem());
        recipe.shape(shape);

        // Parsing ingredients
        ConfigurationSection ingredients = config.getConfigurationSection(key + ".ingredients");
        for (String k : ingredients.getKeys(false)) {
            char ingredientLabel = k.charAt(0);
            String ingredientKey = ingredients.getString(k);

            Material material = matFromString(ingredientKey);
            if (material == null) {
                Infuse.LOGGER.error("Invalid material '{}' for recipe '{}'.", ingredientKey, key);
                return null;
            }

            recipe.setIngredient(ingredientLabel, material);
        }

        return recipe;
    }

    private static ShapelessRecipe getShapelessRecipe(String key, InfuseEffect effect) {
        // Making sure the required sections exist
        if (!config.contains(key + ".ingredients")) {
            Infuse.LOGGER.warn("Shapeless recipe '{}' is missing the 'ingredients' key.", key);
            return null;
        }

        // Creating the recipe object
        ShapelessRecipe recipe = new ShapelessRecipe(getRecipeKey(effect), effect.createItem());

        // Parsing ingredients
        List<String> ingredients = config.getStringList(key + ".ingredients");
        for (String ingredient : ingredients) {
            Material material = matFromString(ingredient);
            if (material == null) {
                Infuse.LOGGER.warn("Invalid material '{}' for recipe '{}'", ingredient, key);
                return null;
            }

            recipe.addIngredient(material);
        }

        return recipe;
    }

    /**
     * Converts a string to a Material.
     * 
     * @param key A string representation of a {@link Material}.
     * @return Returns the represented material or null if no material was found in the registry.
     * @throws IllegalArgumentException Throws if the key could not be converted to a {@link NamespacedKey}.
     */
    @Nullable
    public static Material matFromString(String key) throws IllegalArgumentException {
        return Registry.MATERIAL.get(NamespacedKey.fromString(key));
    }
}
