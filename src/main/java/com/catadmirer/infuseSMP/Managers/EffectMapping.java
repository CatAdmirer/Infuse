package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    APOPHIS(Infuse.getInstance().getEffect("apophis"), Apophis::ISPAPH, Apophis::createAPH),
    EMERALD(Infuse.getInstance().getEffect("emerald"), Emerald::isInvincibilityGem, Emerald::createInvincibilityGem),
    ENDER(Infuse.getInstance().getEffect("ender"), Ender::ISENDER, Ender::createEnderGem),
    FEATHER(Infuse.getInstance().getEffect("feather"), Feather::isInvincibilityGem, Feather::createGlide),
    FIRE(Infuse.getInstance().getEffect("fire"), Fire::isStrengthGem, Fire::createFIRE),
    FROST(Infuse.getInstance().getEffect("frost"), Frost::isStealthGem, Frost::createFrost),
    HASTE(Infuse.getInstance().getEffect("haste"), Haste::isInvincibilityGem, Haste::createFake),
    HEART(Infuse.getInstance().getEffect("heart"), Heart::isHeartEffect, Heart::createHeart),
    INVISIBILITY(Infuse.getInstance().getEffect("invis"), Invisibility::isStealthGem, Invisibility::createStealthGem),
    OCEAN(Infuse.getInstance().getEffect("ocean"), Ocean::isInventoryGlitchGem, Ocean::createOcean),
    REGEN(Infuse.getInstance().getEffect("regen"), Regen::isInvincibilityGem, Regen::createFake),
    SPEED(Infuse.getInstance().getEffect("speed"), Speed::isInvincibilityGem, Speed::createSPEED),
    STRENGTH(Infuse.getInstance().getEffect("strength"), Strength::isStealthGem, Strength::createStealthGem),
    THIEF(Infuse.getInstance().getEffect("thief"), Thief::ISTHF, Thief::createTHF),
    THUNDER(Infuse.getInstance().getEffect("thunder"), Thunder::isInvincibilityGem, Thunder::createTHUNDER),
    AUG_APOPHIS(Infuse.getInstance().getEffect("aug_apophis"), Augmented::ISAUGAPH, Augmented::createAPH),
    AUG_EMERALD(Infuse.getInstance().getEffect("aug_emerald"), Augmented::ISEME, Augmented::createEME),
    AUG_ENDER(Infuse.getInstance().getEffect("aug_ender"), Augmented::ISEND, Augmented::createENDER),
    AUG_FEATHER(Infuse.getInstance().getEffect("aug_feather"), Augmented::ISFEATHER, Augmented::createFEATHER),
    AUG_FIRE(Infuse.getInstance().getEffect("aug_fire"), Augmented::ISFIRE, Augmented::createFIRE),
    AUG_FROST(Infuse.getInstance().getEffect("aug_frost"), Augmented::ISFROST, Augmented::createFROST),
    AUG_HASTE(Infuse.getInstance().getEffect("aug_haste"), Augmented::ISHASTE, Augmented::createHASTE),
    AUG_HEART(Infuse.getInstance().getEffect("aug_heart"), Augmented::ISHEART, Augmented::createHEART),
    AUG_INVISIBILITY(Infuse.getInstance().getEffect("aug_invis"), Augmented::ISINVIS, Augmented::createINVIS),
    AUG_OCEAN(Infuse.getInstance().getEffect("aug_ocean"), Augmented::ISOCEAN, Augmented::createOCEAN),
    AUG_REGEN(Infuse.getInstance().getEffect("aug_regen"), Augmented::ISREGEN, Augmented::createREGEN),
    AUG_SPEED(Infuse.getInstance().getEffect("aug_speed"), Augmented::ISSPEED, Augmented::createSPEED),
    AUG_STRENGTH(Infuse.getInstance().getEffect("aug_strength"), Augmented::ISST, Augmented::createST),
    AUG_THIEF(Infuse.getInstance().getEffect("aug_thief"), Augmented::ISTHIEF, Augmented::createTHF),
    AUG_THUNDER(Infuse.getInstance().getEffect("aug_thunder"), Augmented::ISTHUNDER, Augmented::createTHUNDER);

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