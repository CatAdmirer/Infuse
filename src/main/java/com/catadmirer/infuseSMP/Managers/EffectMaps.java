package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.ExtraEffects.*;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;

public class EffectMaps {
    public static final Map<String, Integer> effectNumber = new HashMap<>();
    public static final Map<String, ItemStack> activeEffects = new HashMap<>();
    public static final Map<String, Character> cooldownEffect = new HashMap<>();
    public static final Map<String, Character> activeEffect = new HashMap<>();
    public static final Map<String, ChatColor> color = new HashMap<>();

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
        effectNumber.put("aug_speed", 18);
        effectNumber.put("speed", 19);
        effectNumber.put("strength", 20);
        effectNumber.put("aug_strength", 21);
        effectNumber.put("thunder", 22);
        effectNumber.put("aug_thunder", 23);
        effectNumber.put("ender", 24);
        effectNumber.put("apophis", 25);
        effectNumber.put("aug_ender", 26);
        effectNumber.put("aug_apophis", 27);
        effectNumber.put("thief", 28);
        effectNumber.put("aug_thief", 29);

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

    public static Integer getEffectNumber(String key) {
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

    public static ChatColor getColorEffect(String key) {
        return color.getOrDefault(key, ChatColor.GRAY);
    }
}
