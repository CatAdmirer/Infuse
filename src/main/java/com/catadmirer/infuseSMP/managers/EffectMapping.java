package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    // Defining regular effects
    APOPHIS("apophis", 25, Apophis::isEffect, Apophis::createEffect),
    EMERALD("emerald", 0, Emerald::isEffect, Emerald::createEffect),
    ENDER("ender", 24, Ender::isEffect, Ender::createEffect),
    FEATHER("feather", 2, Feather::isEffect, Feather::createEffect),
    FIRE("fire", 4, Fire::isEffect, Fire::createEffect),
    FROST("frost", 6, Frost::isEffect, Frost::createEffect),
    HASTE("haste", 8, Haste::isEffect, Haste::createEffect),
    HEART("heart", 10, Heart::isEffect, Heart::createEffect),
    INVISIBILITY("invis", 12, Invisibility::isEffect, Invisibility::createEffect),
    OCEAN("ocean", 14, Ocean::isEffect, Ocean::createEffect),
    REGEN("regen", 16, Regen::isEffect, Regen::createEffect),
    SPEED("speed", 19, Speed::isEffect, Speed::createEffect),
    STRENGTH("strength", 20, Strength::isEffect, Strength::createEffect),
    THIEF("thief", 28, Thief::isEffect, Thief::createEffect),
    THUNDER("thunder", 22, Thunder::isEffect, Thunder::createEffect),

    // Defining augmented effects
    AUG_APOPHIS(APOPHIS, 27, Augmented::isApophis, Augmented::createApophis),
    AUG_EMERALD(EMERALD, 1, Augmented::isEmerald, Augmented::createEmerald),
    AUG_ENDER(ENDER, 26, Augmented::isEnder, Augmented::createEnder),
    AUG_FEATHER(FEATHER, 3, Augmented::isFeather, Augmented::createFeather),
    AUG_FIRE(FIRE, 5, Augmented::isFire, Augmented::createFire),
    AUG_FROST(FROST, 7, Augmented::isFrost, Augmented::createFrost),
    AUG_HASTE(HASTE, 9, Augmented::isHaste, Augmented::createHaste),
    AUG_HEART(HEART, 11, Augmented::isHeart, Augmented::createHeart),
    AUG_INVISIBILITY(INVISIBILITY, 13, Augmented::isInvis, Augmented::createInvis),
    AUG_OCEAN(OCEAN, 15, Augmented::isOcean, Augmented::createOcean),
    AUG_REGEN(REGEN, 17, Augmented::isRegen, Augmented::createRegen),
    AUG_SPEED(SPEED, 18, Augmented::isSpeed, Augmented::createSpeed),
    AUG_STRENGTH(STRENGTH, 21, Augmented::isStrength, Augmented::createStrength),
    AUG_THIEF(THIEF, 29, Augmented::isThief, Augmented::createThief),
    AUG_THUNDER(THUNDER, 23, Augmented::isThunder, Augmented::createThunder);

    private final String effectKey;
    private final int effectId;
    private final Function<ItemStack,Boolean> matchesItem;
    private final Supplier<ItemStack> createItem;

    private EffectMapping regular;
    private EffectMapping augmented;

    /**
     * EffectMapping constructor for regular effects
     * 
     * @param effectKey The base string key for this effect.
     * @param effectId The numerical ID for this effect.
     * @param matchesItem A function used to check if the provided {@link ItemStack} matches the new EffectMapping.
     * @param createItem A function used to create an {@link ItemStack} from this EffectMapping.
     */
    private EffectMapping(String effectKey, int effectId, Function<ItemStack,Boolean> matchesItem, Supplier<ItemStack> createItem) {
        this.effectKey = effectKey;
        this.effectId = effectId;
        this.matchesItem = matchesItem;
        this.createItem = createItem;

        regular = this;
    }

    /**
     * EffectMapping constructor for augmented effects
     * 
     * @param regular The regular effect to use as a base for this effect.
     * @param effectId The numerical ID for this effect.
     * @param matchesItem A function used to check if the provided {@link ItemStack} matches the new EffectMapping.
     * @param createItem A function used to create an {@link ItemStack} from this EffectMapping.
     */
    private EffectMapping(EffectMapping regular, int effectId, Function<ItemStack,Boolean> matchesItem, Supplier<ItemStack> createItem) {
        this.effectKey = "aug_" + regular.effectKey;
        this.effectId = effectId;
        this.matchesItem = matchesItem;
        this.createItem = createItem;

        this.regular = regular;
        augmented = this;
        regular.augmented = this;
    }

    /**
     * Gets the string key of the effect.
     * 
     * @return The string key of the effect.
     */
    public String getEffectKey() {
        return effectKey;
    }

    /**
     * Gets the numerical id of the effect.
     * 
     * @return The string key of the effect.
     */
    public int getEffectId() {
        return effectId;
    }

    /**
     * Gets the name of the effect.
     * 
     * @return The name of the effect with colors.
     */
    public String getEffectName() {
        return Infuse.getInstance().getEffectName(effectKey);
    }

    /**
     * Gets the lore of the effect.
     * 
     * @return The lore of the effect with colors.
     */
    public List<String> getEffectLore() {
        return Infuse.getInstance().getEffectLore(effectKey);
    }

    /**
     * Checks if the provided item is an instance of this effect.
     * 
     * @param item The {@link ItemStack} to check.
     * 
     * @return Whether or not the item is an instance of this effect.
     */
    public boolean matchesItem(ItemStack item) {
        return matchesItem.apply(item);
    }

    /**
     * Creates a potion item for an effect.
     * 
     * @return An {@link ItemStack} potion item representing the effect.
     */
    public ItemStack createItem() {
        return createItem.get();
    }

    /**
     * Gets the regular form of this effect.
     * If the mapping is already the regular form, it returns itself.
     * 
     * @return The regular form of this effect.
     */
    public EffectMapping getRegular() {
        return regular;
    }

    /**
     * Gets the augmented form of this effect.
     * If the mapping is already the augmented form, it returns itself.
     * 
     * @return The augmented form of this effect.
     */
    public EffectMapping getAugmented() {
        return augmented;
    }

    /**
     * Gets an EffectMapping from an ItemStack
     * If the plugin cannot find a valid EffectMapping for the item, it returns null.
     * 
     * @param item The {@link ItemStack} to inspect.
     * 
     * @return An effect mapping based on the item stack provided
     */
    public static EffectMapping fromItem(ItemStack item) {
        for (EffectMapping mapping : values()) {
            if (mapping.matchesItem(item)) return mapping;
        }

        return null;
    }

    /**
     * Gets an EffectMapping from the name of an effect
     * If the plugin cannot find a valid EffectMapping for the name, it returns null.
     * 
     * @param name The name to check.
     * 
     * @return An effect mapping based on the name provided
     */
    public static EffectMapping fromEffectName(String name) {
        for (EffectMapping mapping : values()) {
            if (mapping.getEffectName().equals(name)) return mapping;
        }

        return null;
    }

    /**
     * Gets an EffectMapping from the key of an effect
     * If the plugin cannot find a valid EffectMapping for the key, it returns null.
     * 
     * @param key The key to check.
     * 
     * @return An effect mapping based on the key provided
     */
    public static EffectMapping fromEffectKey(String key) {
        for (EffectMapping mapping : values()) {
            if (mapping.effectKey.equals(key)) return mapping;
        }

        return null;
    }

    /**
     * Gets an EffectMapping from the id of an effect
     * If the plugin cannot find a valid EffectMapping for the id, it returns null.
     * 
     * @param id The id to check.
     * 
     * @return An effect mapping based on the id provided.
     */
    public static EffectMapping fromEffectId(int id) {
        for (EffectMapping mapping : values()) {
            if (mapping.effectId == id) return mapping;
        }

        return null;
    }
}