package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EffectMaps {

    private static final Map<String, Integer> effectNumber = new HashMap<>();

    static {
        effectNumber.put("emerald", 0);
        effectNumber.put("haste", 8);
        effectNumber.put("heart", 10);
        effectNumber.put("invis", 12);
        effectNumber.put("aug_frost", 7);
        effectNumber.put("aug_feather", 3);
        effectNumber.put("thunder", 22);
        effectNumber.put("speed", 19);
        effectNumber.put("aug_regen", 17);
        effectNumber.put("aug_ocean", 15);
        effectNumber.put("aug_emerald", 1);
        effectNumber.put("fire", 4);
        effectNumber.put("aug_invis", 13);
        effectNumber.put("frost", 6);
        effectNumber.put("aug_thunder", 23);
        effectNumber.put("aug_haste", 9);
        effectNumber.put("aug_speed", 18);
        effectNumber.put("aug_fire", 5);
        effectNumber.put("ocean", 14);
        effectNumber.put("aug_heart", 11);
        effectNumber.put("aug_strength", 21);
        effectNumber.put("feather", 2);
        effectNumber.put("regen", 16);
        effectNumber.put("strength", 20);
        effectNumber.put("ender", 24);
        effectNumber.put("apophis", 25);
        effectNumber.put("aug_ender", 26);
        effectNumber.put("aug_apophis", 27);
        effectNumber.put("thief", 28);
        effectNumber.put("aug_thief", 29);
    }

    public static Integer getEffectNumber(String key) {
        return effectNumber.getOrDefault(key, effectNumber.getOrDefault(key, -1));
    }

    public static final Map<String, ItemStack> activeEffects = new HashMap<>();

    static {
        activeEffects.put("emerald", Emerald.createInvincibilityGem());
        activeEffects.put("haste", Haste.createFake());
        activeEffects.put("heart", Heart.createHeart());
        activeEffects.put("invis", Invisibility.createStealthGem());
        activeEffects.put("frost", Frost.createFrost());
        activeEffects.put("feather", Feather.createGlide());
        activeEffects.put("thunder", Thunder.createTHUNDER());
        activeEffects.put("speed", Speed.createSPEED());
        activeEffects.put("regen", Regen.createFake());
        activeEffects.put("ocean", Ocean.createOcean());
        activeEffects.put("fire", Fire.createFIRE());
        activeEffects.put("strength", Strength.createStealthGem());
        activeEffects.put("ender", Ender.createEnderGem());
        activeEffects.put("apophis", Apophis.createAPH());
        activeEffects.put("thief", Thief.createTHF());
        activeEffects.put("aug_strength", Augmented.createST());
        activeEffects.put("aug_thunder", Augmented.createTHUNDER());
        activeEffects.put("aug_speed", Augmented.createSPEED());
        activeEffects.put("aug_regen", Augmented.createREGEN());
        activeEffects.put("aug_ocean", Augmented.createOCEAN());
        activeEffects.put("aug_emerald", Augmented.createEME());
        activeEffects.put("aug_fire", Fire.createFIRE());
        activeEffects.put("aug_invis", Augmented.createINVIS());
        activeEffects.put("aug_frost", Augmented.createFROST());
        activeEffects.put("aug_haste", Augmented.createHASTE());
        activeEffects.put("aug_heart", Augmented.createHEART());
        activeEffects.put("aug_feather", Augmented.createFEATHER());
        activeEffects.put("aug_ender", Augmented.createENDER());
        activeEffects.put("aug_apophis", Augmented.createAPH());
        activeEffects.put("aug_thief", Augmented.createTHF());
    }

    public static ItemStack getEffectItem(String key) {
        return activeEffects.get(key);
    }

    public static final Map<String, String> cooldownEffect = new HashMap<>();

    static {
        cooldownEffect.put("emerald", "\ue012");
        cooldownEffect.put("haste", "\ue004");
        cooldownEffect.put("heart", "\ue003");
        cooldownEffect.put("invis", "\ue005");
        cooldownEffect.put("frost", "\ue007");
        cooldownEffect.put("feather", "\ue006");
        cooldownEffect.put("thunder", "\ue008");
        cooldownEffect.put("speed", "\ue013");
        cooldownEffect.put("regen", "\ue009");
        cooldownEffect.put("ocean", "\ue010");
        cooldownEffect.put("fire", "\ue011");
        cooldownEffect.put("strength", "\ue002");
        cooldownEffect.put("ender", "\ue051");
        cooldownEffect.put("apophis", "\ue055");
        cooldownEffect.put("thief", "\ue059");
        cooldownEffect.put("aug_strength", "\ue026");
        cooldownEffect.put("aug_thunder", "\ue032");
        cooldownEffect.put("aug_speed", "\ue037");
        cooldownEffect.put("aug_regen", "\ue033");
        cooldownEffect.put("aug_ocean", "\ue034");
        cooldownEffect.put("aug_emerald", "\ue036");
        cooldownEffect.put("aug_fire", "\ue035");
        cooldownEffect.put("aug_invis", "\ue029");
        cooldownEffect.put("aug_frost", "\ue031");
        cooldownEffect.put("aug_haste", "\ue028");
        cooldownEffect.put("aug_heart", "\ue027");
        cooldownEffect.put("aug_feather", "\ue030");
        cooldownEffect.put("aug_ender", "\ue053");
        cooldownEffect.put("aug_apophis", "\ue057");
        cooldownEffect.put("aug_thief", "\ue061");
    }

    public static String getCooldownEffect(String key) {
        return cooldownEffect.getOrDefault(key, cooldownEffect.getOrDefault(key, ""));
    }

    public static final Map<String, String> activeEffect = new HashMap<>();

    static {
        activeEffect.put("emerald", "\ue024");
        activeEffect.put("haste", "\ue016");
        activeEffect.put("heart", "\ue015");
        activeEffect.put("invis", "\ue017");
        activeEffect.put("frost", "\ue019");
        activeEffect.put("feather", "\ue018");
        activeEffect.put("thunder", "\ue020");
        activeEffect.put("speed", "\ue025");
        activeEffect.put("regen", "\ue021");
        activeEffect.put("ocean", "\ue022");
        activeEffect.put("fire", "\ue023");
        activeEffect.put("strength", "\ue014");
        activeEffect.put("ender", "\ue050");
        activeEffect.put("apophis", "\ue054");
        activeEffect.put("thief", "\ue060");
        activeEffect.put("aug_strength", "\ue038");
        activeEffect.put("aug_thunder", "\ue044");
        activeEffect.put("aug_speed", "\ue049");
        activeEffect.put("aug_regen", "\ue045");
        activeEffect.put("aug_ocean", "\ue046");
        activeEffect.put("aug_emerald", "\ue048");
        activeEffect.put("aug_fire", "\ue047");
        activeEffect.put("aug_invis", "\ue041");
        activeEffect.put("aug_frost", "\ue043");
        activeEffect.put("aug_haste", "\ue040");
        activeEffect.put("aug_heart", "\ue039");
        activeEffect.put("aug_feather", "\ue042");
        activeEffect.put("aug_ender", "\ue052");
        activeEffect.put("aug_apophis", "\uE056");
        activeEffect.put("aug_thief", "\ue062");
    }

    public static String getActiveEffect(String key) {
        return activeEffect.getOrDefault(key, activeEffect.getOrDefault(key, ""));
    }

    public static final Map<String, net.md_5.bungee.api.ChatColor> color = new HashMap<>();

    static {
        color.put("emerald", net.md_5.bungee.api.ChatColor.GREEN);
        color.put("haste", net.md_5.bungee.api.ChatColor.GOLD);
        color.put("heart", net.md_5.bungee.api.ChatColor.RED);
        color.put("invis", net.md_5.bungee.api.ChatColor.DARK_PURPLE);
        color.put("frost", net.md_5.bungee.api.ChatColor.AQUA);
        color.put("feather", net.md_5.bungee.api.ChatColor.of("#BEA3CA"));
        color.put("thunder", net.md_5.bungee.api.ChatColor.YELLOW);
        color.put("speed", net.md_5.bungee.api.ChatColor.of("#E8BD74"));
        color.put("regen", net.md_5.bungee.api.ChatColor.RED);
        color.put("ocean", net.md_5.bungee.api.ChatColor.BLUE);
        color.put("fire", net.md_5.bungee.api.ChatColor.of("#E85720"));
        color.put("strength", net.md_5.bungee.api.ChatColor.DARK_RED);
        color.put("ender", net.md_5.bungee.api.ChatColor.DARK_PURPLE);
        color.put("apophis", net.md_5.bungee.api.ChatColor.DARK_PURPLE);
        color.put("thief", net.md_5.bungee.api.ChatColor.DARK_RED);
        color.put("aug_strength", net.md_5.bungee.api.ChatColor.DARK_RED);
        color.put("aug_thunder", net.md_5.bungee.api.ChatColor.YELLOW);
        color.put("aug_speed", net.md_5.bungee.api.ChatColor.of("#E8BD74"));
        color.put("aug_regen", net.md_5.bungee.api.ChatColor.RED);
        color.put("aug_ocean", net.md_5.bungee.api.ChatColor.BLUE);
        color.put("aug_emerald", net.md_5.bungee.api.ChatColor.GREEN);
        color.put("aug_fire", net.md_5.bungee.api.ChatColor.of("#E85720"));
        color.put("aug_invis", net.md_5.bungee.api.ChatColor.DARK_PURPLE);
        color.put("aug_frost", net.md_5.bungee.api.ChatColor.AQUA);
        color.put("aug_haste", net.md_5.bungee.api.ChatColor.GOLD);
        color.put("aug_heart", net.md_5.bungee.api.ChatColor.RED);
        color.put("aug_feather", net.md_5.bungee.api.ChatColor.of("#BEA3CA"));
        color.put("aug_ender", net.md_5.bungee.api.ChatColor.DARK_PURPLE);
        color.put("aug_apophis", net.md_5.bungee.api.ChatColor.DARK_PURPLE);
        color.put("aug_thief", net.md_5.bungee.api.ChatColor.DARK_RED);
    }

    public static net.md_5.bungee.api.ChatColor getColorEffect(String key) {
        return color.getOrDefault(key, color.getOrDefault(key, net.md_5.bungee.api.ChatColor.GRAY));
    }
}
