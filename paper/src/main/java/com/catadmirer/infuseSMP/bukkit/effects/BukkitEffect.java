package com.catadmirer.infuseSMP.bukkit.effects;

import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.effects.BaseEffect;
import com.catadmirer.infuseSMP.platform.BossBarColor;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.awt.Color;

public abstract class BukkitEffect extends BaseEffect implements Listener {
    public static final NamespacedKey EFFECT_KEY = new NamespacedKey("infuse", "effect_key");
    public static final NamespacedKey AUG_KEY = new NamespacedKey("infuse", "aug");

    protected final InfusePlugin plugin = InfusePlugin.getInstance();

    protected BukkitEffect(int id, String plainKey, boolean augmented, Color potionColor, BossBarColor ritualColor) {
        super(id, plainKey, augmented, potionColor, ritualColor);
    }
    /**
     * Creates an {@link ItemStack} representation of the effect for a player to consume.
     *
     * @return The corresponding {@link ItemStack}
     */
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.POTION);

        // Adjusting item data
        item.setData(DataComponentTypes.CUSTOM_NAME, getName().toComponent());
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore().toComponentList()));
        item.editPersistentDataContainer(c -> {
            c.set(EFFECT_KEY, PersistentDataType.STRING, toString());
        });

        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.POTION_CONTENTS));
        item.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().customColor(org.bukkit.Color.fromARGB(potionColor.getRGB())));

        if (augmented) {
            item.setData(DataComponentTypes.ITEM_MODEL, AUG_KEY);
        }

        return item;
    }

    /**
     * Checks if an {@link ItemStack} was created by this effect.
     *
     * @param item The item to check.
     *
     * @return Whether or not the item was created by this effect.
     */
    public boolean itemMatches(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.POTION) return false;

        return key().equals(item.getPersistentDataContainer().get(EFFECT_KEY, PersistentDataType.STRING));
    }
}
