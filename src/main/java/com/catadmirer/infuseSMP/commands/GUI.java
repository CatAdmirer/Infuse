package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.inventories.EffectChooser;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.inventories.AugOrRegChooser;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("infuses")) {
            // Opening the gui for players only.
            if (sender instanceof Player player) {
                player.openInventory(new EffectChooser().getInventory());
            } else {
                sender.sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
            }

            return true;
        }
        
        return false;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Only running if the inventory is an EffectInventory
        if (clickedInventory.getHolder() instanceof EffectChooser) {
            // Cancelling the click event to prevent the player from getting the item.
            event.setCancelled(true);

            InfuseEffect effect = InfuseEffect.fromItem(clicked);

            // Ignoring if the player clicked on something other than an effect.
            if (effect == null) return;

            player.openInventory(new AugOrRegChooser(effect).getInventory());
        }

        if (clickedInventory instanceof AugOrRegChooser) {
            if (clicked.getType() != Material.POTION) {
                event.setCancelled(true);
            }
        }
    }
}