package com.catadmirer.infuseSMP.util;

import org.bukkit.inventory.ItemStack;
import com.destroystokyo.paper.MaterialSetTag;

public class ItemUtil {
    public static boolean isAxe(ItemStack item) {
        if (item == null) return false;

        return MaterialSetTag.ITEMS_AXES.isTagged(item.getType());
    }

    public static boolean isSword(ItemStack item) {
        if (item == null) return false;

        return MaterialSetTag.ITEMS_SWORDS.isTagged(item.getType());
    }
}
