package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.*;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EffectChooser implements InventoryHolder, Listener {
    private final Inventory inventory;

    public EffectChooser(Infuse plugin) {
        inventory = Bukkit.createInventory(this, 54, Message.toComponent("<b>Infuses"));

        // Filling the inventory with decorative glass panes
        int[] magentaSlots = {0, 1, 2, 6, 7, 8, 9, 10, 16, 17, 18, 26, 27, 35};
        int[] purpleSlots = {36, 37, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        int[] lightBlueSlots = {3, 4, 5, 11, 13, 15, 19, 25, 28, 34, 38, 42};
        InventoryUtils.setItems(inventory, magentaSlots, InventoryUtils.createNoName(Material.MAGENTA_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, purpleSlots, InventoryUtils.createNoName(Material.PURPLE_STAINED_GLASS_PANE));
        InventoryUtils.setItems(inventory, lightBlueSlots, InventoryUtils.createNoName(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

        inventory.setItem(12, new Frost(true).createItem());
        inventory.setItem(14, new Speed(true).createItem());
        inventory.setItem(20, new Feather(true).createItem());
        inventory.setItem(21, new Ocean(true).createItem());
        inventory.setItem(22, new Invis(true).createItem());
        inventory.setItem(23, new Ender(true).createItem());
        inventory.setItem(24, new Emerald(true).createItem());
        inventory.setItem(29, new Heart(true).createItem());
        inventory.setItem(30, new Regen(true).createItem());
        inventory.setItem(31, new Strength(true).createItem());
        inventory.setItem(32, new Fire(true).createItem());
        inventory.setItem(33, new Haste(true).createItem());
        inventory.setItem(40, new Thunder(true).createItem());

        if (plugin.getMainConfig().enableThief()) {
            inventory.setItem(39, new Thief(true).createItem());
        }
        if (plugin.getMainConfig().enableApophis()) {
            inventory.setItem(41, new Apophis(true).createItem());
        }

        // Locking the inventory
        InventoryUtils.lockInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Only running if the inventory is an EffectChooser
        if (!(event.getClickedInventory().getHolder() instanceof EffectChooser)) return;

        // Cancelling the click event
        event.setCancelled(true);
        
        // Getting the effect the player clicked
        InfuseEffect effect = InfuseEffect.fromItem(event.getCurrentItem());

        // Ignoring if the player clicked on something other than an effect.
        if (effect == null) return;

        // Opening the AugOrRegChooser menu for the effect the player clicked
        event.getWhoClicked().openInventory(new AugOrRegChooser(effect).getInventory());
    }
}