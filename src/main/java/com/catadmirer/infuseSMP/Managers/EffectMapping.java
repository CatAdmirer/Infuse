package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    APOPHIS(Infuse.getInstance().getEffect("apophis"), Apophis::isEffect, Apophis::createEffect),
    EMERALD(Infuse.getInstance().getEffect("emerald"), Emerald::isEffect, Emerald::createEffect),
    ENDER(Infuse.getInstance().getEffect("ender"), Ender::isEffect, Ender::createEffect),
    FEATHER(Infuse.getInstance().getEffect("feather"), Feather::isEffect, Feather::createEffect),
    FIRE(Infuse.getInstance().getEffect("fire"), Fire::isEffect, Fire::createEffect),
    FROST(Infuse.getInstance().getEffect("frost"), Frost::isEffect, Frost::createEffect),
    HASTE(Infuse.getInstance().getEffect("haste"), Haste::isEffect, Haste::createEffect),
    HEART(Infuse.getInstance().getEffect("heart"), Heart::isEffect, Heart::createEffect),
    INVISIBILITY(Infuse.getInstance().getEffect("invis"), Invisibility::isStealthGem, Invisibility::createEffect),
    OCEAN(Infuse.getInstance().getEffect("ocean"), Ocean::isEffect, Ocean::createEffect),
    REGEN(Infuse.getInstance().getEffect("regen"), Regen::isEffect, Regen::createEffect),
    SPEED(Infuse.getInstance().getEffect("speed"), Speed::isEffect, Speed::createEffect),
    STRENGTH(Infuse.getInstance().getEffect("strength"), Strength::isEffect, Strength::createEffect),
    THIEF(Infuse.getInstance().getEffect("thief"), Thief::isEffect, Thief::createEffect),
    THUNDER(Infuse.getInstance().getEffect("thunder"), Thunder::isEffect, Thunder::createEffect),

    AUG_APOPHIS(Infuse.getInstance().getEffect("aug_apophis"), Augmented::isApophis, Augmented::createApophis),
    AUG_EMERALD(Infuse.getInstance().getEffect("aug_emerald"), Augmented::isEmerald, Augmented::createEmerald),
    AUG_ENDER(Infuse.getInstance().getEffect("aug_ender"), Augmented::isEnder, Augmented::createEnder),
    AUG_FEATHER(Infuse.getInstance().getEffect("aug_feather"), Augmented::isFeather, Augmented::createFeather),
    AUG_FIRE(Infuse.getInstance().getEffect("aug_fire"), Augmented::isFire, Augmented::createFire),
    AUG_FROST(Infuse.getInstance().getEffect("aug_frost"), Augmented::isFrost, Augmented::createFrost),
    AUG_HASTE(Infuse.getInstance().getEffect("aug_haste"), Augmented::isHaste, Augmented::createHaste),
    AUG_HEART(Infuse.getInstance().getEffect("aug_heart"), Augmented::isHeart, Augmented::createHeart),
    AUG_INVISIBILITY(Infuse.getInstance().getEffect("aug_invis"), Augmented::isInvis, Augmented::createInvis),
    AUG_OCEAN(Infuse.getInstance().getEffect("aug_ocean"), Augmented::isOcean, Augmented::createOcean),
    AUG_REGEN(Infuse.getInstance().getEffect("aug_regen"), Augmented::isRegen, Augmented::createRegen),
    AUG_SPEED(Infuse.getInstance().getEffect("aug_speed"), Augmented::isSpeed, Augmented::createSpeed),
    AUG_STRENGTH(Infuse.getInstance().getEffect("aug_strength"), Augmented::isStrength, Augmented::createStrength),
    AUG_THIEF(Infuse.getInstance().getEffect("aug_thief"), Augmented::isThief, Augmented::createThief),
    AUG_THUNDER(Infuse.getInstance().getEffect("aug_thunder"), Augmented::isThunder, Augmented::createThunder);

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