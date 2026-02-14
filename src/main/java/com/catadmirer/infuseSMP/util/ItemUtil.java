package com.catadmirer.infuseSMP.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import com.destroystokyo.paper.MaterialSetTag;

public class ItemUtil {
    public static boolean isSword(ItemStack item) {
        if (item == null) return false;

        return MaterialSetTag.ITEMS_SWORDS.isTagged(item.getType());
    }

    public static boolean isPickaxe(ItemStack item) {
        if (item == null) return false;

        return MaterialSetTag.ITEMS_PICKAXES.isTagged(item.getType());
    }

    public static boolean isAxe(ItemStack item) {
        if (item == null) return false;

        return MaterialSetTag.ITEMS_AXES.isTagged(item.getType());
    }

    public static boolean isShovel(ItemStack item) {
        if (item == null) return false;

        return MaterialSetTag.ITEMS_SHOVELS.isTagged(item.getType());
    }

    public static boolean isHoe(ItemStack item) {
        if (item == null) return false;

        return MaterialSetTag.ITEMS_HOES.isTagged(item.getType());
    }

    public static void giveOrDropItem(HumanEntity player, ItemStack... items) {
        player.getInventory().addItem(items).forEach((i, extra) -> player.getWorld().dropItem(player.getLocation(), extra));
    }
}