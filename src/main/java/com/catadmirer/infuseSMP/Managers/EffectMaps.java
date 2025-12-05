package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.ExtraEffects.*;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public class EffectMaps {
    public static final Map<String, Integer> effectNumber = new HashMap<>();
    public static final Map<String, ItemStack> activeEffects = new HashMap<>();
    public static final Map<String, Character> cooldownEffect = new HashMap<>();
    public static final Map<String, Character> activeEffect = new HashMap<>();
    public static final Map<String, String> color = new HashMap<>();

    static {
        effectNumber.put("emerald", 0);
        effectNumber.put("aug_emerald", 1);
        effectNumber.put("feather", 2);
        effectNumber.put("aug_feather", 3);
        effectNumber.put("fire", 4);
        effectNumber.put("aug_fire", 5);
        effectNumber.put("frost", 6);
        effectNumber.put("aug_frost", 7);
        effectNumber.put("haste", 8);
        effectNumber.put("aug_haste", 9);
        effectNumber.put("heart", 10);
        effectNumber.put("aug_heart", 11);
        effectNumber.put("invis", 12);
        effectNumber.put("aug_invis", 13);
        effectNumber.put("ocean", 14);
        effectNumber.put("aug_ocean", 15);
        effectNumber.put("regen", 16);
        effectNumber.put("aug_regen", 17);
        effectNumber.put("speed", 18);
        effectNumber.put("aug_speed", 19);
        effectNumber.put("strength", 20);
        effectNumber.put("aug_strength", 21);
        effectNumber.put("thunder", 22);
        effectNumber.put("aug_thunder", 23);
        effectNumber.put("ender", 24);
        effectNumber.put("aug_ender", 25);
        effectNumber.put("apophis", 26);
        effectNumber.put("aug_apophis", 27);
        effectNumber.put("thief", 28);
        effectNumber.put("aug_thief", 29);

        activeEffects.put("emerald", Emerald.createRegular());
        activeEffects.put("haste", Haste.createRegular());
        activeEffects.put("heart", Heart.createRegular());
        activeEffects.put("invis", Invisibility.createRegular());
        activeEffects.put("frost", Frost.createRegular());
        activeEffects.put("feather", Feather.createRegular());
        activeEffects.put("thunder", Thunder.createRegular());
        activeEffects.put("speed", Speed.createRegular());
        activeEffects.put("regen", Regen.createRegular());
        activeEffects.put("ocean", Ocean.createRegular());
        activeEffects.put("fire", Fire.createRegular());
        activeEffects.put("strength", Strength.createRegular());
        activeEffects.put("ender", Ender.createRegular());
        activeEffects.put("apophis", Apophis.createRegular());
        activeEffects.put("thief", Thief.createRegular());
        activeEffects.put("aug_strength", Strength.createAugmented());
        activeEffects.put("aug_thunder", Thunder.createAugmented());
        activeEffects.put("aug_speed", Speed.createAugmented());
        activeEffects.put("aug_regen", Regen.createAugmented());
        activeEffects.put("aug_ocean", Ocean.createAugmented());
        activeEffects.put("aug_emerald", Emerald.createAugmented());
        activeEffects.put("aug_fire", Fire.createRegular());
        activeEffects.put("aug_invis", Invisibility.createAugmented());
        activeEffects.put("aug_frost", Frost.createAugmented());
        activeEffects.put("aug_haste", Haste.createAugmented());
        activeEffects.put("aug_heart", Heart.createAugmented());
        activeEffects.put("aug_feather", Feather.createAugmented());
        activeEffects.put("aug_ender", Ender.createAugmented());
        activeEffects.put("aug_apophis", Apophis.createAugmented());
        activeEffects.put("aug_thief", Thief.createAugmented());

        cooldownEffect.put("emerald", (char) 0xe012);
        cooldownEffect.put("haste", (char) 0xe004);
        cooldownEffect.put("heart", (char) 0xe003);
        cooldownEffect.put("invis", (char) 0xe005);
        cooldownEffect.put("frost", (char) 0xe007);
        cooldownEffect.put("feather", (char) 0xe006);
        cooldownEffect.put("thunder", (char) 0xe008);
        cooldownEffect.put("speed", (char) 0xe013);
        cooldownEffect.put("regen", (char) 0xe009);
        cooldownEffect.put("ocean", (char) 0xe010);
        cooldownEffect.put("fire", (char) 0xe011);
        cooldownEffect.put("strength", (char) 0xe002);
        cooldownEffect.put("ender", (char) 0xe051);
        cooldownEffect.put("apophis", (char) 0xe055);
        cooldownEffect.put("thief", (char) 0xe059);
        cooldownEffect.put("aug_strength", (char) 0xe026);
        cooldownEffect.put("aug_thunder", (char) 0xe032);
        cooldownEffect.put("aug_speed", (char) 0xe037);
        cooldownEffect.put("aug_regen", (char) 0xe033);
        cooldownEffect.put("aug_ocean", (char) 0xe034);
        cooldownEffect.put("aug_emerald", (char) 0xe036);
        cooldownEffect.put("aug_fire", (char) 0xe035);
        cooldownEffect.put("aug_invis", (char) 0xe029);
        cooldownEffect.put("aug_frost", (char) 0xe031);
        cooldownEffect.put("aug_haste", (char) 0xe028);
        cooldownEffect.put("aug_heart", (char) 0xe027);
        cooldownEffect.put("aug_feather", (char) 0xe030);
        cooldownEffect.put("aug_ender", (char) 0xe053);
        cooldownEffect.put("aug_apophis", (char) 0xe057);
        cooldownEffect.put("aug_thief", (char) 0xe061);

        activeEffect.put("emerald", (char) 0xe024);
        activeEffect.put("haste", (char) 0xe016);
        activeEffect.put("heart", (char) 0xe015);
        activeEffect.put("invis", (char) 0xe017);
        activeEffect.put("frost", (char) 0xe019);
        activeEffect.put("feather", (char) 0xe018);
        activeEffect.put("thunder", (char) 0xe020);
        activeEffect.put("speed", (char) 0xe025);
        activeEffect.put("regen", (char) 0xe021);
        activeEffect.put("ocean", (char) 0xe022);
        activeEffect.put("fire", (char) 0xe023);
        activeEffect.put("strength", (char) 0xe014);
        activeEffect.put("ender", (char) 0xe050);
        activeEffect.put("apophis", (char) 0xe054);
        activeEffect.put("thief", (char) 0xe060);
        activeEffect.put("aug_strength", (char) 0xe038);
        activeEffect.put("aug_thunder", (char) 0xe044);
        activeEffect.put("aug_speed", (char) 0xe049);
        activeEffect.put("aug_regen", (char) 0xe045);
        activeEffect.put("aug_ocean", (char) 0xe046);
        activeEffect.put("aug_emerald", (char) 0xe048);
        activeEffect.put("aug_fire", (char) 0xe047);
        activeEffect.put("aug_invis", (char) 0xe041);
        activeEffect.put("aug_frost", (char) 0xe043);
        activeEffect.put("aug_haste", (char) 0xe040);
        activeEffect.put("aug_heart", (char) 0xe039);
        activeEffect.put("aug_feather", (char) 0xe042);
        activeEffect.put("aug_ender", (char) 0xe052);
        activeEffect.put("aug_apophis", (char) 0xe056);
        activeEffect.put("aug_thief", (char) 0xe062);

        color.put("emerald", "§a");
        color.put("haste", "§6");
        color.put("heart", "§c");
        color.put("invis", "§5");
        color.put("frost", "§b");
        color.put("feather", "§x§B§E§A§3§C§A");
        color.put("thunder", "§e");
        color.put("speed", "§x§E§8§B§D§7§4");
        color.put("regen", "§c");
        color.put("ocean", "§9");
        color.put("fire", "§x§E§8§5§7§2§0");
        color.put("strength", "§4");
        color.put("ender", "§5");
        color.put("apophis", "§5");
        color.put("thief", "§4");
        color.put("aug_strength", "§4");
        color.put("aug_thunder", "§e");
        color.put("aug_speed", "§x§E§8§B§D§7§4");
        color.put("aug_regen", "§c");
        color.put("aug_ocean", "§9");
        color.put("aug_emerald", "§a");
        color.put("aug_fire", "§x§E§8§5§7§2§0");
        color.put("aug_invis", "§5");
        color.put("aug_frost", "§b");
        color.put("aug_haste", "§6");
        color.put("aug_heart", "§c");
        color.put("aug_feather", "§x§B§E§A§3§C§A");
        color.put("aug_ender", "§5");
        color.put("aug_apophis", "§5");
        color.put("aug_thief", "§4");
    }

    public static Integer getEffectId(String key) {
        return effectNumber.getOrDefault(key, -1);
    }

    public static ItemStack getEffectItem(String key) {
        return activeEffects.get(key);
    }

    public static char getCooldownEffect(String key) {
        return cooldownEffect.getOrDefault(key, (char) 0);
    }

    public static char getActiveEffect(String key) {
        return activeEffect.getOrDefault(key, (char) 0);
    }

    public static String getColorEffect(String key) {
        return color.getOrDefault(key, "§7");
    }
}
