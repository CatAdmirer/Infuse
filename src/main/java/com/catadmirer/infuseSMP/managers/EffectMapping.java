package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    APOPHIS(Infuse.getInstance().getEffectName("apophis"), Apophis::isEffect, Apophis::createEffect),
    EMERALD(Infuse.getInstance().getEffectName("emerald"), Emerald::isEffect, Emerald::createEffect),
    ENDER(Infuse.getInstance().getEffectName("ender"), Ender::isEffect, Ender::createEffect),
    FEATHER(Infuse.getInstance().getEffectName("feather"), Feather::isEffect, Feather::createEffect),
    FIRE(Infuse.getInstance().getEffectName("fire"), Fire::isEffect, Fire::createEffect),
    FROST(Infuse.getInstance().getEffectName("frost"), Frost::isEffect, Frost::createEffect),
    HASTE(Infuse.getInstance().getEffectName("haste"), Haste::isEffect, Haste::createEffect),
    HEART(Infuse.getInstance().getEffectName("heart"), Heart::isEffect, Heart::createEffect),
    INVISIBILITY(Infuse.getInstance().getEffectName("invis"), Invisibility::isEffect, Invisibility::createEffect),
    OCEAN(Infuse.getInstance().getEffectName("ocean"), Ocean::isEffect, Ocean::createEffect),
    REGEN(Infuse.getInstance().getEffectName("regen"), Regen::isEffect, Regen::createEffect),
    SPEED(Infuse.getInstance().getEffectName("speed"), Speed::isEffect, Speed::createEffect),
    STRENGTH(Infuse.getInstance().getEffectName("strength"), Strength::isEffect, Strength::createEffect),
    THIEF(Infuse.getInstance().getEffectName("thief"), Thief::isEffect, Thief::createEffect),
    THUNDER(Infuse.getInstance().getEffectName("thunder"), Thunder::isEffect, Thunder::createEffect),

    AUG_APOPHIS(Infuse.getInstance().getEffectName("aug_apophis"), Augmented::isApophis, Augmented::createApophis),
    AUG_EMERALD(Infuse.getInstance().getEffectName("aug_emerald"), Augmented::isEmerald, Augmented::createEmerald),
    AUG_ENDER(Infuse.getInstance().getEffectName("aug_ender"), Augmented::isEnder, Augmented::createEnder),
    AUG_FEATHER(Infuse.getInstance().getEffectName("aug_feather"), Augmented::isFeather, Augmented::createFeather),
    AUG_FIRE(Infuse.getInstance().getEffectName("aug_fire"), Augmented::isFire, Augmented::createFire),
    AUG_FROST(Infuse.getInstance().getEffectName("aug_frost"), Augmented::isFrost, Augmented::createFrost),
    AUG_HASTE(Infuse.getInstance().getEffectName("aug_haste"), Augmented::isHaste, Augmented::createHaste),
    AUG_HEART(Infuse.getInstance().getEffectName("aug_heart"), Augmented::isHeart, Augmented::createHeart),
    AUG_INVISIBILITY(Infuse.getInstance().getEffectName("aug_invis"), Augmented::isInvis, Augmented::createInvis),
    AUG_OCEAN(Infuse.getInstance().getEffectName("aug_ocean"), Augmented::isOcean, Augmented::createOcean),
    AUG_REGEN(Infuse.getInstance().getEffectName("aug_regen"), Augmented::isRegen, Augmented::createRegen),
    AUG_SPEED(Infuse.getInstance().getEffectName("aug_speed"), Augmented::isSpeed, Augmented::createSpeed),
    AUG_STRENGTH(Infuse.getInstance().getEffectName("aug_strength"), Augmented::isStrength, Augmented::createStrength),
    AUG_THIEF(Infuse.getInstance().getEffectName("aug_thief"), Augmented::isThief, Augmented::createThief),
    AUG_THUNDER(Infuse.getInstance().getEffectName("aug_thunder"), Augmented::isThunder, Augmented::createThunder);

    private final String effectName;
    private final Function<ItemStack,Boolean> matchesItem;
    private final Supplier<ItemStack> createItem;

    private EffectMapping(String effectName, Function<ItemStack,Boolean> matchesItem, Supplier<ItemStack> createItem) {
        this.effectName = effectName;
        this.matchesItem = matchesItem;
        this.createItem = createItem;
    }

    public String getEffectName() {
        return this.effectName;
    }

    public boolean matchesItem(ItemStack item) {
        return matchesItem.apply(item);
    }

    public ItemStack createItem() {
        return createItem.get();
    }

    public static EffectMapping fromItem(ItemStack item) {
        for (EffectMapping mapping : values()) {
            if (mapping.matchesItem(item)) return mapping;
        }

        return null;
    }

    public static EffectMapping fromEffectName(String name) {
        for (EffectMapping mapping : values()) {
            if (mapping.getEffectName().equals(name)) return mapping;
        }

        return null;
    }
}