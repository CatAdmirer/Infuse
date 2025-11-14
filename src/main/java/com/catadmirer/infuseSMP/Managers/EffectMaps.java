package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import net.md_5.bungee.api.ChatColor;
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
        activeEffects.put("emerald", Emerald.createEffect());
        activeEffects.put("haste", Haste.createEffect());
        activeEffects.put("heart", Heart.createEffect());
        activeEffects.put("invis", Invisibility.createEffect());
        activeEffects.put("frost", Frost.createEffect());
        activeEffects.put("feather", Feather.createEffect());
        activeEffects.put("thunder", Thunder.createEffect());
        activeEffects.put("speed", Speed.createEffect());
        activeEffects.put("regen", Regen.createEffect());
        activeEffects.put("ocean", Ocean.createEffect());
        activeEffects.put("fire", Fire.createEffect());
        activeEffects.put("strength", Strength.createEffect());
        activeEffects.put("ender", Ender.createEffect());
        activeEffects.put("apophis", Apophis.createEffect());
        activeEffects.put("thief", Thief.createEffect());
        activeEffects.put("aug_strength", Augmented.createStrength());
        activeEffects.put("aug_thunder", Augmented.createThunder());
        activeEffects.put("aug_speed", Augmented.createSpeed());
        activeEffects.put("aug_regen", Augmented.createRegen());
        activeEffects.put("aug_ocean", Augmented.createOcean());
        activeEffects.put("aug_emerald", Augmented.createEmerald());
        activeEffects.put("aug_fire", Fire.createEffect());
        activeEffects.put("aug_invis", Augmented.createInvis());
        activeEffects.put("aug_frost", Augmented.createFrost());
        activeEffects.put("aug_haste", Augmented.createHaste());
        activeEffects.put("aug_heart", Augmented.createHeart());
        activeEffects.put("aug_feather", Augmented.createFeather());
        activeEffects.put("aug_ender", Augmented.createEnder());
        activeEffects.put("aug_apophis", Augmented.createApophis());
        activeEffects.put("aug_thief", Augmented.createThief());
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

    public static net.md_5.bungee.api.ChatColor getColorEffect(String key) {
        return color.getOrDefault(key, color.getOrDefault(key, ChatColor.GRAY));
    }
}
