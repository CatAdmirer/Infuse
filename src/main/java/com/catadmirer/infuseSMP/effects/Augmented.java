package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
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
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_emerald"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_emerald"));
            meta.setColor(Color.LIME);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createThief() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_thief"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_thief"));
            meta.setColor(Color.RED);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createFeather() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(applyHexColors(Infuse.getInstance().getEffectName("aug_feather")));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_feather").stream().map(Augmented::applyHexColors).toList());
            meta.setColor(Color.WHITE);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createFire() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(applyHexColors(Infuse.getInstance().getEffectName("aug_fire")));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_fire").stream().map(Augmented::applyHexColors).toList());
            meta.setColor(Color.fromRGB(0xFFA500));
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
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_ender"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_ender"));
            meta.setColor(Color.fromRGB(0x871277));
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createApophis() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_apophis"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_apophis"));
            meta.setColor(Color.fromRGB(0x45033E));
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createFrost() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_frost"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_frost"));
            meta.setColor(Color.AQUA);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createHaste() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_haste"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_haste"));
            meta.setColor(Color.fromRGB(0xFFCC33));
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createHeart() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_heart"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_heart"));
            meta.setColor(Color.RED);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createInvis() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_invis"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_invis"));
            meta.setColor(Color.fromRGB(0xCC33FF));
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createOcean() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_ocean"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_ocean"));
            meta.setColor(Color.BLUE);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createRegen() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_regen"));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_regen"));
            meta.setColor(Color.RED);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createSpeed() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(applyHexColors(Infuse.getInstance().getEffectName("aug_speed")));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_speed").stream().map(Augmented::applyHexColors).toList());
            meta.setColor(Color.AQUA);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createStrength() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_strength"));
            meta.setColor(Color.fromRGB(0x8B0000));
            meta.setLore(Infuse.getInstance().getEffectLore("aug_strength"));
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static ItemStack createThunder() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("aug_thunder"));
            meta.setColor(Color.YELLOW);
            meta.setLore(Infuse.getInstance().getEffectLore("aug_thunder"));
            meta.setCustomModelData(999);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isStrength(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_strength"));
    }

    public static boolean isHeart(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_heart"));
    }

    public static boolean isRegen(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_regen"));
    }

    public static boolean isInvis(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_invis"));
    }

    public static boolean isEmerald(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_emerald"));
    }

    public static boolean isEnder(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_ender"));
    }

    public static boolean isSpeed(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(applyHexColors(Infuse.getInstance().getEffectName("aug_speed")));
    }

    public static boolean isHaste(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_haste"));
    }

    public static boolean isFeather(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(applyHexColors(Infuse.getInstance().getEffectName("aug_feather")));
    }

    public static boolean isOcean(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_ocean"));
    }

    public static boolean isFrost(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_frost"));
    }

    public static boolean isFire(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(applyHexColors(Infuse.getInstance().getEffectName("aug_fire")));
    }

    public static boolean isThunder(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_thunder"));
    }

    public static boolean isApophis(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_apophis"));
    }

    public static boolean isThief(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(Infuse.getInstance().getEffectName("aug_thief"));
    }
}