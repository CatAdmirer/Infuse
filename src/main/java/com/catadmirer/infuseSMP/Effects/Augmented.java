package com.catadmirer.infuseSMP.Effects;

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

    public static ItemStack createEME() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_emerald");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_emerald");
            meta.setColor(Color.fromRGB(0, 255, 0));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createTHF() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_thief");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_thief");
            meta.setColor(Color.fromRGB(255, 0, 0));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createFEATHER() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_feather");
            gemName = applyHexColors(gemName);
            meta.setDisplayName(gemName);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("aug_feather"));
            meta.setColor(Color.fromRGB(255, 255, 255));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createFIRE() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_fire");
            gemName = applyHexColors(gemName);
            meta.setDisplayName(gemName);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("aug_fire"));
            meta.setColor(Color.fromRGB(255, 165, 0));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }


    public static ItemStack createENDER() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_ender");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_ender");
            meta.setColor(Color.fromRGB(135, 18, 119));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createAPH() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName2 = Infuse.getInstance().getEffect("aug_apophis");
            meta.setDisplayName(gemName2);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_apophis");
            meta.setColor(Color.fromRGB(69, 3, 62));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createFROST() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_frost");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_frost");
            meta.setColor(Color.fromRGB(0, 255, 255));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createHASTE() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_haste");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_haste");
            meta.setColor(Color.fromRGB(255, 204, 51));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createHEART() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_heart");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_heart");
            meta.setColor(Color.RED);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createINVIS() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_invis");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_invis");
            meta.setColor(Color.fromRGB(204, 51, 255));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createOCEAN() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_ocean");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("aug_ocean");
            meta.setColor(Color.fromRGB(0, 0, 255));
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createREGEN() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_regen");
            meta.setDisplayName(gemName);
            meta.setColor(Color.fromRGB(255, 0, 0));
            List<String> lore = Infuse.getInstance().getEffectLore("aug_regen");
            meta.setLore(lore);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createSPEED() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_speed");
            gemName = applyHexColors(gemName);
            meta.setDisplayName(gemName);
            meta.setColor(Color.AQUA);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("aug_speed"));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createST() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_strength");
            meta.setDisplayName(gemName);
            meta.setColor(Color.fromRGB(139, 0, 0));
            List<String> lore = Infuse.getInstance().getEffectLore("aug_strength");
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static ItemStack createTHUNDER() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("aug_thunder");
            meta.setDisplayName(gemName);
            meta.setColor(Color.fromRGB(255, 255, 0));
            List<String> lore = Infuse.getInstance().getEffectLore("aug_thunder");
            meta.setLore(lore);
            meta.setCustomModelData(999);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static boolean ISST(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_strength");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISHEART(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_heart");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISREGEN(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_regen");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISINVIS(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_invis");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISEME(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_emerald");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISEND(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_ender");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISSPEED(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_speed");
        gemName = applyHexColors(gemName);
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISHASTE(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_haste");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISFEATHER(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_feather");
        gemName = applyHexColors(gemName);
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISOCEAN(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_ocean");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISFROST(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_frost");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISFIRE(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_fire");
        gemName = applyHexColors(gemName);
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISTHUNDER(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("aug_thunder");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName);
    }

    public static boolean ISAUGAPH(ItemStack item) {
        String gemName2 = Infuse.getInstance().getEffect("aug_apophis");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName2);
    }

    public static boolean ISTHIEF(ItemStack item) {
        String gemName2 = Infuse.getInstance().getEffect("aug_thief");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(gemName2);
    }
}