package com.catadmirer.infuseSMP.util;

import com.catadmirer.infuseSMP.Infuse;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class EffectUtil {
    public static int getIdFromItem(ItemStack item) {
        if (item == null) return -1;
        if (item.getType() != Material.POTION) return -1;
        if (!item.hasItemMeta()) return -1;

        return item.getItemMeta().getPersistentDataContainer().get(Infuse.EFFECT_ID, PersistentDataType.INTEGER);
    }
}
