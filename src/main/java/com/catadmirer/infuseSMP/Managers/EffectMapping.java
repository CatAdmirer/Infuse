package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    APOPHIS(Infuse.getInstance().getEffect("apophis"), Apophis::isEffect, Apophis::createRegular),
    EMERALD(Infuse.getInstance().getEffect("emerald"), Emerald::isRegular, Emerald::createRegular),
    ENDER(Infuse.getInstance().getEffect("ender"), Ender::isEffect, Ender::createRegular),
    FEATHER(Infuse.getInstance().getEffect("feather"), Feather::isEffect, Feather::createRegular),
    FIRE(Infuse.getInstance().getEffect("fire"), Fire::isEffect, Fire::createRegular),
    FROST(Infuse.getInstance().getEffect("frost"), Frost::isEffect, Frost::createRegular),
    HASTE(Infuse.getInstance().getEffect("haste"), Haste::isEffect, Haste::createRegular),
    HEART(Infuse.getInstance().getEffect("heart"), Heart::isEffect, Heart::createRegular),
    INVISIBILITY(Infuse.getInstance().getEffect("invis"), Invisibility::isEffect, Invisibility::createRegular),
    OCEAN(Infuse.getInstance().getEffect("ocean"), Ocean::isEffect, Ocean::createRegular),
    REGEN(Infuse.getInstance().getEffect("regen"), Regen::isEffect, Regen::createRegular),
    SPEED(Infuse.getInstance().getEffect("speed"), Speed::isEffect, Speed::createRegular),
    STRENGTH(Infuse.getInstance().getEffect("strength"), Strength::isEffect, Strength::createRegular),
    THIEF(Infuse.getInstance().getEffect("thief"), Thief::isEffect, Thief::createRegular),
    THUNDER(Infuse.getInstance().getEffect("thunder"), Thunder::isEffect, Thunder::createRegular),

    AUG_APOPHIS(Infuse.getInstance().getEffect("aug_apophis"), Apophis::isAugmented, Apophis::createAugmented),
    AUG_EMERALD(Infuse.getInstance().getEffect("aug_emerald"), Emerald::isAugmented, Emerald::createAugmented),
    AUG_ENDER(Infuse.getInstance().getEffect("aug_ender"), Ender::isAugmented, Ender::createAugmented),
    AUG_FEATHER(Infuse.getInstance().getEffect("aug_feather"), Feather::isAugmented, Feather::createAugmented),
    AUG_FIRE(Infuse.getInstance().getEffect("aug_fire"), Fire::isAugmented, Fire::createAugmented),
    AUG_FROST(Infuse.getInstance().getEffect("aug_frost"), Frost::isAugmented, Frost::createAugmented),
    AUG_HASTE(Infuse.getInstance().getEffect("aug_haste"), Haste::isAugmented, Haste::createAugmented),
    AUG_HEART(Infuse.getInstance().getEffect("aug_heart"), Heart::isAugmented, Heart::createAugmented),
    AUG_INVISIBILITY(Infuse.getInstance().getEffect("aug_invis"), Invisibility::isAugmented, Invisibility::createAugmented),
    AUG_OCEAN(Infuse.getInstance().getEffect("aug_ocean"), Ocean::isAugmented, Ocean::createAugmented),
    AUG_REGEN(Infuse.getInstance().getEffect("aug_regen"), Regen::isAugmented, Regen::createAugmented),
    AUG_SPEED(Infuse.getInstance().getEffect("aug_speed"), Speed::isAugmented, Speed::createAugmented),
    AUG_STRENGTH(Infuse.getInstance().getEffect("aug_strength"), Strength::isAugmented, Strength::createAugmented),
    AUG_THIEF(Infuse.getInstance().getEffect("aug_thief"), Thief::isAugmented, Thief::createAugmented),
    AUG_THUNDER(Infuse.getInstance().getEffect("aug_thunder"), Thunder::isAugmented, Thunder::createAugmented);

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