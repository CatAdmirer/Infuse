package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    APOPHIS(Infuse.getInstance().getEffectName("apophis"), Apophis::ISPAPH, Apophis::createAPH),
    EMERALD(Infuse.getInstance().getEffectName("emerald"), Emerald::isInvincibilityGem, Emerald::createInvincibilityGem),
    ENDER(Infuse.getInstance().getEffectName("ender"), Ender::ISENDER, Ender::createEnderGem),
    FEATHER(Infuse.getInstance().getEffectName("feather"), Feather::isInvincibilityGem, Feather::createGlide),
    FIRE(Infuse.getInstance().getEffectName("fire"), Fire::isStrengthGem, Fire::createFIRE),
    FROST(Infuse.getInstance().getEffectName("frost"), Frost::isStealthGem, Frost::createFrost),
    HASTE(Infuse.getInstance().getEffectName("haste"), Haste::isInvincibilityGem, Haste::createFake),
    HEART(Infuse.getInstance().getEffectName("heart"), Heart::isHeartEffect, Heart::createHeart),
    INVISIBILITY(Infuse.getInstance().getEffectName("invis"), Invisibility::isStealthGem, Invisibility::createStealthGem),
    OCEAN(Infuse.getInstance().getEffectName("ocean"), Ocean::isInventoryGlitchGem, Ocean::createOcean),
    REGEN(Infuse.getInstance().getEffectName("regen"), Regen::isInvincibilityGem, Regen::createFake),
    SPEED(Infuse.getInstance().getEffectName("speed"), Speed::isInvincibilityGem, Speed::createSPEED),
    STRENGTH(Infuse.getInstance().getEffectName("strength"), Strength::isEffect, Strength::createEffect),
    THIEF(Infuse.getInstance().getEffectName("thief"), Thief::ISTHF, Thief::createTHF),
    THUNDER(Infuse.getInstance().getEffectName("thunder"), Thunder::isInvincibilityGem, Thunder::createTHUNDER),
    AUG_APOPHIS(Infuse.getInstance().getEffectName("aug_apophis"), Augmented::ISAUGAPH, Augmented::createAPH),
    AUG_EMERALD(Infuse.getInstance().getEffectName("aug_emerald"), Augmented::ISEME, Augmented::createEME),
    AUG_ENDER(Infuse.getInstance().getEffectName("aug_ender"), Augmented::ISEND, Augmented::createENDER),
    AUG_FEATHER(Infuse.getInstance().getEffectName("aug_feather"), Augmented::ISFEATHER, Augmented::createFEATHER),
    AUG_FIRE(Infuse.getInstance().getEffectName("aug_fire"), Augmented::ISFIRE, Augmented::createFIRE),
    AUG_FROST(Infuse.getInstance().getEffectName("aug_frost"), Augmented::ISFROST, Augmented::createFROST),
    AUG_HASTE(Infuse.getInstance().getEffectName("aug_haste"), Augmented::ISHASTE, Augmented::createHASTE),
    AUG_HEART(Infuse.getInstance().getEffectName("aug_heart"), Augmented::ISHEART, Augmented::createHEART),
    AUG_INVISIBILITY(Infuse.getInstance().getEffectName("aug_invis"), Augmented::ISINVIS, Augmented::createINVIS),
    AUG_OCEAN(Infuse.getInstance().getEffectName("aug_ocean"), Augmented::ISOCEAN, Augmented::createOCEAN),
    AUG_REGEN(Infuse.getInstance().getEffectName("aug_regen"), Augmented::ISREGEN, Augmented::createREGEN),
    AUG_SPEED(Infuse.getInstance().getEffectName("aug_speed"), Augmented::ISSPEED, Augmented::createSPEED),
    AUG_STRENGTH(Infuse.getInstance().getEffectName("aug_strength"), Augmented::ISST, Augmented::createST),
    AUG_THIEF(Infuse.getInstance().getEffectName("aug_thief"), Augmented::ISTHIEF, Augmented::createTHF),
    AUG_THUNDER(Infuse.getInstance().getEffectName("aug_thunder"), Augmented::ISTHUNDER, Augmented::createTHUNDER);

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