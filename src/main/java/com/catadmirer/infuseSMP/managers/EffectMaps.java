package com.catadmirer.infuseSMP.managers;

import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;

public class EffectMaps {
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

    public static final Map<String,ChatColor> color = new HashMap<>();

    static {
        color.put("emerald", ChatColor.GREEN);
        color.put("haste", ChatColor.GOLD);
        color.put("heart", ChatColor.RED);
        color.put("invis", ChatColor.DARK_PURPLE);
        color.put("frost", ChatColor.AQUA);
        color.put("feather", ChatColor.of("#BEA3CA"));
        color.put("thunder", ChatColor.YELLOW);
        color.put("speed", ChatColor.of("#E8BD74"));
        color.put("regen", ChatColor.RED);
        color.put("ocean", ChatColor.BLUE);
        color.put("fire", ChatColor.of("#E85720"));
        color.put("strength", ChatColor.DARK_RED);
        color.put("ender", ChatColor.DARK_PURPLE);
        color.put("apophis", ChatColor.DARK_PURPLE);
        color.put("thief", ChatColor.DARK_RED);
        color.put("aug_strength", ChatColor.DARK_RED);
        color.put("aug_thunder", ChatColor.YELLOW);
        color.put("aug_speed", ChatColor.of("#E8BD74"));
        color.put("aug_regen", ChatColor.RED);
        color.put("aug_ocean", ChatColor.BLUE);
        color.put("aug_emerald", ChatColor.GREEN);
        color.put("aug_fire", ChatColor.of("#E85720"));
        color.put("aug_invis", ChatColor.DARK_PURPLE);
        color.put("aug_frost", ChatColor.AQUA);
        color.put("aug_haste", ChatColor.GOLD);
        color.put("aug_heart", ChatColor.RED);
        color.put("aug_feather", ChatColor.of("#BEA3CA"));
        color.put("aug_ender", ChatColor.DARK_PURPLE);
        color.put("aug_apophis", ChatColor.DARK_PURPLE);
        color.put("aug_thief", ChatColor.DARK_RED);
    }

    public static ChatColor getColorEffect(String key) {
        return color.getOrDefault(key, color.getOrDefault(key, ChatColor.GRAY));
    }
}
