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
    APOPHIS("apophis", Apophis::isEffect, Apophis::createEffect),
    EMERALD("emerald", Emerald::isEffect, Emerald::createEffect),
    ENDER("ender", Ender::isEffect, Ender::createEffect),
    FEATHER("feather", Feather::isEffect, Feather::createEffect),
    FIRE("fire", Fire::isEffect, Fire::createEffect),
    FROST("frost", Frost::isEffect, Frost::createEffect),
    HASTE("haste", Haste::isEffect, Haste::createEffect),
    HEART("heart", Heart::isEffect, Heart::createEffect),
    INVISIBILITY("invis", Invisibility::isEffect, Invisibility::createEffect),
    OCEAN("ocean", Ocean::isEffect, Ocean::createEffect),
    REGEN("regen", Regen::isEffect, Regen::createEffect),
    SPEED("speed", Speed::isEffect, Speed::createEffect),
    STRENGTH("strength", Strength::isEffect, Strength::createEffect),
    THIEF("thief", Thief::isEffect, Thief::createEffect),
    THUNDER("thunder", Thunder::isEffect, Thunder::createEffect),

    // Defining augmented effects
    AUG_APOPHIS(APOPHIS, Augmented::isApophis, Augmented::createApophis),
    AUG_EMERALD(EMERALD, Augmented::isEmerald, Augmented::createEmerald),
    AUG_ENDER(ENDER, Augmented::isEnder, Augmented::createEnder),
    AUG_FEATHER(FEATHER, Augmented::isFeather, Augmented::createFeather),
    AUG_FIRE(FIRE, Augmented::isFire, Augmented::createFire),
    AUG_FROST(FROST, Augmented::isFrost, Augmented::createFrost),
    AUG_HASTE(HASTE, Augmented::isHaste, Augmented::createHaste),
    AUG_HEART(HEART, Augmented::isHeart, Augmented::createHeart),
    AUG_INVISIBILITY(INVISIBILITY, Augmented::isInvis, Augmented::createInvis),
    AUG_OCEAN(OCEAN, Augmented::isOcean, Augmented::createOcean),
    AUG_REGEN(REGEN, Augmented::isRegen, Augmented::createRegen),
    AUG_SPEED(SPEED, Augmented::isSpeed, Augmented::createSpeed),
    AUG_STRENGTH(STRENGTH, Augmented::isStrength, Augmented::createStrength),
    AUG_THIEF(THIEF, Augmented::isThief, Augmented::createThief),
    AUG_THUNDER(THUNDER, Augmented::isThunder, Augmented::createThunder);

    private final String effectKey;
    private final Function<ItemStack,Boolean> matchesItem;
    private final Supplier<ItemStack> createItem;

    private EffectMapping regular;
    private EffectMapping augmented;

    /**
     * EffectMapping constructor for regular effects
     * 
     * @param effectKey The base string key for the effect
     * @param matchesItem A function used to check if the provided {@link ItemStack} matches the new EffectMapping.
     * @param createItem A function used to create an {@link ItemStack} from this EffectMapping.
     */
    private EffectMapping(String effectKey, Function<ItemStack,Boolean> matchesItem, Supplier<ItemStack> createItem) {
        this.effectKey = effectKey;
        this.matchesItem = matchesItem;
        this.createItem = createItem;

        regular = this;
    }

    private EffectMapping(EffectMapping regular, Function<ItemStack,Boolean> matchesItem, Supplier<ItemStack> createItem) {
        this.effectKey = "aug_" + regular.effectKey;
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
}