package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.inventories.EffectChooser;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.inventories.AugOrRegChooser;
import com.catadmirer.infuseSMP.managers.EffectMapping;
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
            switch (EffectMapping.fromItem(clicked)) {
                case AUG_APOPHIS -> augmentedOrRegular(player, EffectMapping.AUG_APOPHIS.createItem(), EffectMapping.APOPHIS.createItem(), Material.MAGENTA_STAINED_GLASS_PANE);
                case AUG_EMERALD -> augmentedOrRegular(player, EffectMapping.AUG_EMERALD.createItem(), EffectMapping.EMERALD.createItem(), Material.LIME_STAINED_GLASS_PANE);
                case AUG_ENDER -> augmentedOrRegular(player, EffectMapping.AUG_ENDER.createItem(), EffectMapping.ENDER.createItem(), Material.PURPLE_STAINED_GLASS_PANE);
                case AUG_FEATHER -> augmentedOrRegular(player, EffectMapping.AUG_FEATHER.createItem(), EffectMapping.FEATHER.createItem(), Material.WHITE_STAINED_GLASS_PANE);
                case AUG_FIRE -> augmentedOrRegular(player, EffectMapping.AUG_FIRE.createItem(), EffectMapping.FIRE.createItem(), Material.ORANGE_STAINED_GLASS_PANE);
                case AUG_FROST -> augmentedOrRegular(player, EffectMapping.AUG_FROST.createItem(), EffectMapping.FROST.createItem(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                case AUG_HASTE -> augmentedOrRegular(player, EffectMapping.AUG_HASTE.createItem(), EffectMapping.HASTE.createItem(), Material.ORANGE_STAINED_GLASS_PANE);
                case AUG_HEART -> augmentedOrRegular(player, EffectMapping.AUG_HEART.createItem(), EffectMapping.HEART.createItem(), Material.RED_STAINED_GLASS_PANE);
                case AUG_INVIS -> augmentedOrRegular(player, EffectMapping.AUG_INVIS.createItem(), EffectMapping.INVIS.createItem(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                case AUG_OCEAN -> augmentedOrRegular(player, EffectMapping.AUG_OCEAN.createItem(), EffectMapping.OCEAN.createItem(), Material.BLUE_STAINED_GLASS_PANE);
                case AUG_REGEN -> augmentedOrRegular(player, EffectMapping.AUG_REGEN.createItem(), EffectMapping.REGEN.createItem(), Material.RED_STAINED_GLASS_PANE);
                case AUG_SPEED -> augmentedOrRegular(player, EffectMapping.AUG_SPEED.createItem(), EffectMapping.SPEED.createItem(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                case AUG_STRENGTH -> augmentedOrRegular(player, EffectMapping.AUG_STRENGTH.createItem(), EffectMapping.STRENGTH.createItem(), Material.RED_STAINED_GLASS_PANE);
                case AUG_THIEF -> augmentedOrRegular(player, EffectMapping.AUG_THIEF.createItem(), EffectMapping.THIEF.createItem(), Material.RED_STAINED_GLASS_PANE);
                case AUG_THUNDER -> augmentedOrRegular(player, EffectMapping.AUG_THUNDER.createItem(), EffectMapping.THUNDER.createItem(), Material.YELLOW_STAINED_GLASS_PANE);
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