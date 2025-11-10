package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import org.bukkit.inventory.ItemStack;

public enum EffectMapping {
    IMMORTAL(Infuse.getInstance().getEffect("strength")) {
        Strength strength = new Strength(Infuse.getInstance());
        public boolean matchesItem(ItemStack item) {
            return strength.isStealthGem(item);
        }
        public ItemStack createItem() {
            return strength.createStealthGem();
        }
    },
    VIRUS(Infuse.getInstance().getEffect("heart")) {
        public boolean matchesItem(ItemStack item) {
            return Heart.isHeartEffect(item);
        }
        public ItemStack createItem() {
            return Heart.createHeart();
        }
    },
    POT(Infuse.getInstance().getEffect("regen")) {
        public boolean matchesItem(ItemStack item) {
            return Regen.isInvincibilityGem(item);
        }
        public ItemStack createItem() {
            return Regen.createFake();
        }
    },
    FAKE(Infuse.getInstance().getEffect("invis")) {
        public boolean matchesItem(ItemStack item) {
            return Invisibility.isStealthGem(item);
        }
        public ItemStack createItem() {
            return Invisibility.createStealthGem();
        }
    },
    THUNDER2(Infuse.getInstance().getEffect("thunder")) {
        EffectManager trustManager = new EffectManager(Infuse.getInstance().getDataFolder());
        Thunder thunder = new Thunder(Infuse.getInstance(), trustManager);
        public boolean matchesItem(ItemStack item) {
            return thunder.isInvincibilityGem(item);
        }
        public ItemStack createItem() {
            return thunder.createTHUNDER();
        }
    },
    GLIDE(Infuse.getInstance().getEffect("emerald")) {
        public boolean matchesItem(ItemStack item) {
            return Emerald.isInvincibilityGem(item);
        }
        public ItemStack createItem() {
            return Emerald.createInvincibilityGem();
        }
    },
    TELE(Infuse.getInstance().getEffect("speed")) {
        Speed speed = new Speed(Infuse.getInstance());
        public boolean matchesItem(ItemStack item) {
            return speed.isInvincibilityGem(item);
        }
        public ItemStack createItem() {
            return speed.createSPEED();
        }
    },
    HEART(Infuse.getInstance().getEffect("haste")) {
        public boolean matchesItem(ItemStack item) {
            return Haste.isInvincibilityGem(item);
        }
        public ItemStack createItem() {
            return Haste.createFake();
        }
    },
    TNT(Infuse.getInstance().getEffect("feather")) {
        public boolean matchesItem(ItemStack item) {
            return Feather.isInvincibilityGem(item);
        }
        public ItemStack createItem() {
            return Feather.createGlide();
        }
    },
    re(Infuse.getInstance().getEffect("ocean")) {
        public boolean matchesItem(ItemStack item) {
            return Ocean.isInventoryGlitchGem(item);
        }
        public ItemStack createItem() {
            return Ocean.createOcean();
        }
    },
    FROST(Infuse.getInstance().getEffect("frost")) {
        public boolean matchesItem(ItemStack item) {
            return Frost.isStealthGem(item);
        }
        public ItemStack createItem() {
            return Frost.createFrost();
        }
    },
    DREAM(Infuse.getInstance().getEffect("fire")) {
        public boolean matchesItem(ItemStack item) {
            return Fire.isStrengthGem(item);
        }
        public ItemStack createItem() {
            return Fire.createFIRE();
        }
    },
    IMMORTAL1(Infuse.getInstance().getEffect("aug_strength")) {
        Augmented augmented = new Augmented(Infuse.getInstance());
        public boolean matchesItem(ItemStack item) {
            return augmented.ISST(item);
        }
        public ItemStack createItem() {
            return augmented.createST();
        }
    },
    VIRUS1(Infuse.getInstance().getEffect("aug_heart")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISHEART(item);
        }
        public ItemStack createItem() {
            return Augmented.createHEART();
        }
    },
    POT1(Infuse.getInstance().getEffect("aug_regen")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISREGEN(item);
        }
        public ItemStack createItem() {
            return Augmented.createREGEN();
        }
    },
    FAKE1(Infuse.getInstance().getEffect("aug_invis")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISINVIS(item);
        }
        public ItemStack createItem() {
            return Augmented.createINVIS();
        }
    },
    GLIDE1(Infuse.getInstance().getEffect("aug_emerald")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISEME(item);
        }
        public ItemStack createItem() {
            return Augmented.createEME();
        }
    },
    TELE1(Infuse.getInstance().getEffect("aug_speed")) {
        Augmented augmented = new Augmented(Infuse.getInstance());
        public boolean matchesItem(ItemStack item) {
            return augmented.ISSPEED(item);
        }
        public ItemStack createItem() {
            return augmented.createSPEED();
        }
    },
    HEART1(Infuse.getInstance().getEffect("aug_haste")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISHASTE(item);
        }
        public ItemStack createItem() {
            return Augmented.createHASTE();
        }
    },
    TNT1(Infuse.getInstance().getEffect("aug_feather")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISFEATHER(item);
        }
        public ItemStack createItem() {
            return Augmented.createFEATHER();
        }
    },
    OCEAN1(Infuse.getInstance().getEffect("aug_ocean")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISOCEAN(item);
        }
        public ItemStack createItem() {
            return Augmented.createOCEAN();
        }
    },
    FROST1(Infuse.getInstance().getEffect("aug_frost")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISFROST(item);
        }
        public ItemStack createItem() {
            return Augmented.createFROST();
        }
    },
    THUNDER(Infuse.getInstance().getEffect("aug_thunder")) {
        Augmented augmented = new Augmented(Infuse.getInstance());
        public boolean matchesItem(ItemStack item) {
            return augmented.ISTHUNDER(item);
        }
        public ItemStack createItem() {
            return augmented.createTHUNDER();
        }
    },
    DREAM1(Infuse.getInstance().getEffect("aug_fire")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISFIRE(item);
        }
        public ItemStack createItem() {
            return Augmented.createFIRE();
        }
    },
    END(Infuse.getInstance().getEffect("aug_ender")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISEND(item);
        }
        public ItemStack createItem() {
            return Augmented.createENDER();
        }
    },
    APH(Infuse.getInstance().getEffect("aug_apophis")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISAUGAPH(item);
        }
        public ItemStack createItem() {
            return Augmented.createAPH();
        }
    },
    END2(Infuse.getInstance().getEffect("ender")) {
        public boolean matchesItem(ItemStack item) {
            return Ender.ISENDER(item);
        }
        public ItemStack createItem() {
            return Ender.createEnderGem();
        }
    },
    THF(Infuse.getInstance().getEffect("thief")) {
        public boolean matchesItem(ItemStack item) {
            return Thief.ISTHF(item);
        }
        public ItemStack createItem() {
            return Thief.createTHF();
        }
    },
    THF2(Infuse.getInstance().getEffect("aug_thief")) {
        public boolean matchesItem(ItemStack item) {
            return Augmented.ISTHIEF(item);
        }
        public ItemStack createItem() {
            return Augmented.createTHF();
        }
    },
    APH_NAME(Infuse.getInstance().getEffect("apophis")) {
        public boolean matchesItem(ItemStack item) {
            return Apophis.ISPAPH(item);
        }
        public ItemStack createItem() {
            return Apophis.createAPH();
        }
    };

    private final String hackName;

    private EffectMapping(String param3) {
        this.hackName = param3;
    }

    public String getHackName() {
        return this.hackName;
    }

    public abstract boolean matchesItem(ItemStack var1);

    public abstract ItemStack createItem();

    public static EffectMapping fromItem(ItemStack item) {
        EffectMapping[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            EffectMapping mapping = var1[var3];
            if (mapping.matchesItem(item)) {
                return mapping;
            }
        }

        return null;
    }

    public static EffectMapping fromHackName(String name) {
        EffectMapping[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            EffectMapping mapping = var1[var3];
            if (mapping.getHackName().equals(name)) {
                return mapping;
            }
        }

        return null;
    }
    private static EffectMapping[] $values() {
        return new EffectMapping[]{IMMORTAL, VIRUS, POT, FAKE, THUNDER2, GLIDE, TELE, HEART, TNT, re, FROST, DREAM, IMMORTAL1, VIRUS1, POT1, FAKE1, GLIDE1, TELE1, HEART1, TNT1, OCEAN1, FROST1, THUNDER, DREAM1, END, APH, END2, APH_NAME, THF, THF2};
    }
}
