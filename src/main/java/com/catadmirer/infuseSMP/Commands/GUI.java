package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.inventories.EffectInventory;
import com.catadmirer.infuseSMP.inventories.EffectLevelInventory;
import com.catadmirer.infuseSMP.managers.EffectMapping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUI implements Listener, CommandExecutor {
    public static void openSwordSelectionGUI(Player player) {
        player.openInventory(new EffectInventory().getInventory());
    }

    private void augmentedOrRegular(Player player, ItemStack augmented, ItemStack regular, Material backgroundColor) {
        player.openInventory(new EffectLevelInventory(augmented, regular, backgroundColor).getInventory());
    }

    @EventHandler
    public void onClicky(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory clickedInventory = event.getClickedInventory();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clickedInventory instanceof EffectInventory) {
            event.setCancelled(true);
            EffectMapping effect = EffectMapping.fromItem(clicked);
            Material background;
            background = switch (effect) {
                case HEART, AUG_HEART, REGEN, AUG_REGEN, STRENGTH, AUG_STRENGTH, THIEF, AUG_THIEF -> Material.RED_STAINED_GLASS_PANE;
                case FIRE, AUG_FIRE, HASTE, AUG_HASTE -> Material.ORANGE_STAINED_GLASS_PANE;
                case THUNDER, AUG_THUNDER -> Material.YELLOW_STAINED_GLASS_PANE;
                case EMERALD, AUG_EMERALD -> Material.LIME_STAINED_GLASS_PANE;
                case OCEAN, AUG_OCEAN -> Material.BLUE_STAINED_GLASS_PANE;
                case FROST, AUG_FROST, SPEED, AUG_SPEED -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
                case ENDER, AUG_ENDER -> Material.PURPLE_STAINED_GLASS_PANE;
                case APOPHIS, AUG_APOPHIS -> Material.MAGENTA_STAINED_GLASS_PANE;
                case FEATHER, AUG_FEATHER -> Material.WHITE_STAINED_GLASS_PANE;
                case INVIS, AUG_INVIS -> Material.LIGHT_GRAY_STAINED_GLASS_PANE;
            };

            augmentedOrRegular(player, effect.augmented().createItem(), effect.regular().createItem(), background);
        }

        if (clickedInventory instanceof EffectLevelInventory) {
            if (clicked.getType() != Material.POTION) {
                event.setCancelled(true);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("infuses")) return false;

        if (sender instanceof Player player) {
            openSwordSelectionGUI(player);
        } else {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
        }

        return true;
    }
}