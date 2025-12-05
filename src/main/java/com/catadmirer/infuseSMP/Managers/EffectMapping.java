package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    APOPHIS(Infuse.getInstance().getEffectName("apophis"), Apophis::isEffect, Apophis::createRegular),
    EMERALD(Infuse.getInstance().getEffectName("emerald"), Emerald::isRegular, Emerald::createRegular),
    ENDER(Infuse.getInstance().getEffectName("ender"), Ender::isEffect, Ender::createRegular),
    FEATHER(Infuse.getInstance().getEffectName("feather"), Feather::isEffect, Feather::createRegular),
    FIRE(Infuse.getInstance().getEffectName("fire"), Fire::isEffect, Fire::createRegular),
    FROST(Infuse.getInstance().getEffectName("frost"), Frost::isEffect, Frost::createRegular),
    HASTE(Infuse.getInstance().getEffectName("haste"), Haste::isEffect, Haste::createRegular),
    HEART(Infuse.getInstance().getEffectName("heart"), Heart::isEffect, Heart::createRegular),
    INVISIBILITY(Infuse.getInstance().getEffectName("invis"), Invisibility::isEffect, Invisibility::createRegular),
    OCEAN(Infuse.getInstance().getEffectName("ocean"), Ocean::isEffect, Ocean::createRegular),
    REGEN(Infuse.getInstance().getEffectName("regen"), Regen::isEffect, Regen::createRegular),
    SPEED(Infuse.getInstance().getEffectName("speed"), Speed::isEffect, Speed::createRegular),
    STRENGTH(Infuse.getInstance().getEffectName("strength"), Strength::isEffect, Strength::createRegular),
    THIEF(Infuse.getInstance().getEffectName("thief"), Thief::isEffect, Thief::createRegular),
    THUNDER(Infuse.getInstance().getEffectName("thunder"), Thunder::isEffect, Thunder::createRegular),

    AUG_APOPHIS(Infuse.getInstance().getEffectName("aug_apophis"), Apophis::isAugmented, Apophis::createAugmented),
    AUG_EMERALD(Infuse.getInstance().getEffectName("aug_emerald"), Emerald::isAugmented, Emerald::createAugmented),
    AUG_ENDER(Infuse.getInstance().getEffectName("aug_ender"), Ender::isAugmented, Ender::createAugmented),
    AUG_FEATHER(Infuse.getInstance().getEffectName("aug_feather"), Feather::isAugmented, Feather::createAugmented),
    AUG_FIRE(Infuse.getInstance().getEffectName("aug_fire"), Fire::isAugmented, Fire::createAugmented),
    AUG_FROST(Infuse.getInstance().getEffectName("aug_frost"), Frost::isAugmented, Frost::createAugmented),
    AUG_HASTE(Infuse.getInstance().getEffectName("aug_haste"), Haste::isAugmented, Haste::createAugmented),
    AUG_HEART(Infuse.getInstance().getEffectName("aug_heart"), Heart::isAugmented, Heart::createAugmented),
    AUG_INVISIBILITY(Infuse.getInstance().getEffectName("aug_invis"), Invisibility::isAugmented, Invisibility::createAugmented),
    AUG_OCEAN(Infuse.getInstance().getEffectName("aug_ocean"), Ocean::isAugmented, Ocean::createAugmented),
    AUG_REGEN(Infuse.getInstance().getEffectName("aug_regen"), Regen::isAugmented, Regen::createAugmented),
    AUG_SPEED(Infuse.getInstance().getEffectName("aug_speed"), Speed::isAugmented, Speed::createAugmented),
    AUG_STRENGTH(Infuse.getInstance().getEffectName("aug_strength"), Strength::isAugmented, Strength::createAugmented),
    AUG_THIEF(Infuse.getInstance().getEffectName("aug_thief"), Thief::isAugmented, Thief::createAugmented),
    AUG_THUNDER(Infuse.getInstance().getEffectName("aug_thunder"), Thunder::isAugmented, Thunder::createAugmented);

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