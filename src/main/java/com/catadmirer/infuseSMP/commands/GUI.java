package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.inventories.EffectChooser;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.inventories.AugOrRegChooser;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.*;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUI implements Listener, CommandExecutor {
    private final Infuse plugin;
    
    public GUI(Infuse plugin) {
        this.plugin = plugin;
    }

    private void augmentedOrRegular(HumanEntity player, ItemStack augmented, ItemStack regular, Material backgroundColor) {
        player.openInventory(new AugOrRegChooser(augmented, regular, backgroundColor).getInventory());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        // Ignoring if the player clicked on an empty slot.
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Only running if the inventory is an EffectInventory
        if (clickedInventory.getHolder() instanceof EffectChooser) {
            // Cancelling the click event to prevent the player from getting the item.
            event.setCancelled(true);

            // Determining the next menu to open
            switch (InfuseEffect.fromItem(clicked).getId()) {
                case EffectIds.APOPHIS -> augmentedOrRegular(player, new Apophis(true).createItem(), new Apophis().createItem(), Material.MAGENTA_STAINED_GLASS_PANE);
                case EffectIds.EMERALD -> augmentedOrRegular(player, new Emerald(true).createItem(), new Emerald().createItem(), Material.LIME_STAINED_GLASS_PANE);
                case EffectIds.ENDER -> augmentedOrRegular(player, new Ender(true).createItem(), new Ender().createItem(), Material.PURPLE_STAINED_GLASS_PANE);
                case EffectIds.FEATHER -> augmentedOrRegular(player, new Feather(true).createItem(), new Feather().createItem(), Material.WHITE_STAINED_GLASS_PANE);
                case EffectIds.FIRE -> augmentedOrRegular(player, new Fire(true).createItem(), new Fire().createItem(), Material.ORANGE_STAINED_GLASS_PANE);
                case EffectIds.FROST -> augmentedOrRegular(player, new Frost(true).createItem(), new Frost().createItem(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                case EffectIds.HASTE -> augmentedOrRegular(player, new Haste(true).createItem(), new Haste().createItem(), Material.ORANGE_STAINED_GLASS_PANE);
                case EffectIds.HEART -> augmentedOrRegular(player, new Heart(true).createItem(), new Heart().createItem(), Material.RED_STAINED_GLASS_PANE);
                case EffectIds.INVIS -> augmentedOrRegular(player, new Invis(true).createItem(), new Invis().createItem(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                case EffectIds.OCEAN -> augmentedOrRegular(player, new Ocean(true).createItem(), new Ocean().createItem(), Material.BLUE_STAINED_GLASS_PANE);
                case EffectIds.REGEN -> augmentedOrRegular(player, new Regen(true).createItem(), new Regen().createItem(), Material.RED_STAINED_GLASS_PANE);
                case EffectIds.SPEED -> augmentedOrRegular(player, new Speed(true).createItem(), new Speed().createItem(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                case EffectIds.STRENGTH -> augmentedOrRegular(player, new Strength(true).createItem(), new Strength().createItem(), Material.RED_STAINED_GLASS_PANE);
                case EffectIds.THIEF -> augmentedOrRegular(player, new Thief(true).createItem(), new Thief().createItem(), Material.RED_STAINED_GLASS_PANE);
                case EffectIds.THUNDER -> augmentedOrRegular(player, new Thunder(true).createItem(), new Thunder().createItem(), Material.YELLOW_STAINED_GLASS_PANE);
                default -> {}
            }
        }

        if (clickedInventory instanceof AugOrRegChooser) {
            if (clicked.getType() != Material.POTION) {
                event.setCancelled(true);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("infuses")) {
            // Opening the gui for players only.
            if (sender instanceof Player player) {
                player.openInventory(new EffectChooser(plugin).getInventory());
            } else {
                sender.sendMessage(Messages.ERROR_NOT_PLAYER.toComponent());
            }

            return true;
        }
        
        return false;
    }
}