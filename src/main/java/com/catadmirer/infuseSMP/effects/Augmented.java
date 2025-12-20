package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class Augmented {
    public static String applyHexColors(String input) {
        String regex = "(#(?:[0-9a-fA-F]{6}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String colorCode = ChatColor.of(hexCode).toString();
            matcher.appendReplacement(result, colorCode);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static ItemStack createEmerald() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_emerald");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_emerald");
            meta.setColor(Color.LIME);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createThief() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_thief");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_thief");
            meta.setColor(Color.RED);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createFeather() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_feather");
            effectName = applyHexColors(effectName);
            meta.setDisplayName(effectName);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("aug_feather"));
            meta.setColor(Color.WHITE);
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createFire() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_fire");
            effectName = applyHexColors(effectName);
            meta.setDisplayName(effectName);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("aug_fire"));
            meta.setColor(Color.fromRGB(0xFFA500));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }


    public static ItemStack createEnder() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_ender");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_ender");
            meta.setColor(Color.fromRGB(0x871277));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createApophis() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName2 = Infuse.getInstance().getEffectName("aug_apophis");
            meta.setDisplayName(effectName2);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_apophis");
            meta.setColor(Color.fromRGB(0x45033E));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createFrost() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_frost");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_frost");
            meta.setColor(Color.AQUA);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createHaste() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_haste");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_haste");
            meta.setColor(Color.fromRGB(0xFFCC33));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createHeart() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_heart");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_heart");
            meta.setColor(Color.RED);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createInvis() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_invis");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_invis");
            meta.setColor(Color.fromRGB(0xCC33FF));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createOcean() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_ocean");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_ocean");
            meta.setColor(Color.BLUE);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createRegen() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_regen");
            meta.setDisplayName(effectName);
            meta.setColor(Color.RED);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_regen");
            meta.setLore(lore);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createSpeed() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_speed");
            effectName = applyHexColors(effectName);
            meta.setDisplayName(effectName);
            meta.setColor(Color.AQUA);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("aug_speed"));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createStrength() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_strength");
            meta.setDisplayName(effectName);
            meta.setColor(Color.fromRGB(0x8B0000));
            List<String> lore = Infuse.getInstance().getEffectLore("aug_strength");
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createThunder() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffectName("aug_thunder");
            meta.setDisplayName(effectName);
            meta.setColor(Color.YELLOW);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_thunder");
            meta.setLore(lore);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isStrength(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_strength");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isHeart(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_heart");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isRegen(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_regen");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isInvis(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_invis");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isEmerald(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_emerald");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isEnder(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_ender");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isSpeed(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_speed");
        effectName = applyHexColors(effectName);
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isHaste(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_haste");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isFeather(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_feather");
        effectName = applyHexColors(effectName);
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isOcean(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_ocean");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isFrost(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_frost");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isFire(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_fire");
        effectName = applyHexColors(effectName);
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isThunder(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_thunder");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isApophis(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_apophis");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static boolean isThief(ItemStack item) {
        String effectName = Infuse.getInstance().getEffectName("aug_thief");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }
}