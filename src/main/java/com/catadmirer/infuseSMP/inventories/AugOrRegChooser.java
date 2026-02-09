package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import com.catadmirer.infuseSMP.util.InventoryUtils;

public class AugOrRegChooser implements InventoryHolder {
    private final Inventory inventory;

    public AugOrRegChooser(InfuseEffect effect) {
        inventory = Bukkit.createInventory(this, 27, Component.text("Choose", NamedTextColor.YELLOW));
        
        // Filling the inventory with a filler item.
        InventoryUtils.fillInventory(inventory, InventoryUtils.createNoName(EffectConstants.menuBackgroundColor(effect.getId())));

        // Adding the effects to the inventory
        inventory.setItem(11, effect.getRegularForm().createItem());
        inventory.setItem(15, effect.getAugmentedForm().createItem());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}