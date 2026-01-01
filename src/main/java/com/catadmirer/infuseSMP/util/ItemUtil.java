package com.catadmirer.infuseSMP.util;

import org.bukkit.inventory.ItemStack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;

public class ItemUtil {
    public static boolean isAxe(ItemStack item) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTagValues(ItemTypeTagKeys.AXES).contains(item.getType().asItemType());
    }
}
