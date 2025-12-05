package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Inventories.EffectInventory;
import com.catadmirer.infuseSMP.Inventories.EffectLevelInventory;
import com.catadmirer.infuseSMP.Effects.*;
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
            if (Frost.isAugmented(clicked)) {
                augmentedOrRegular(player, Frost.createAugmented(), Frost.createRegular(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (Speed.isAugmented(clicked)) {
                augmentedOrRegular(player, Speed.createAugmented(), Speed.createRegular(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (Strength.isAugmented(clicked)) {
                augmentedOrRegular(player, Strength.createAugmented(), Strength.createRegular(), Material.RED_STAINED_GLASS_PANE);
            } else if (Thunder.isAugmented(clicked)) {
                augmentedOrRegular(player, Thunder.createAugmented(), Thunder.createRegular(), Material.YELLOW_STAINED_GLASS_PANE);
            } else if (Thief.isAugmented(clicked)) {
                augmentedOrRegular(player, Thief.createAugmented(), Thief.createRegular(), Material.RED_STAINED_GLASS_PANE);
            } else if (Heart.isAugmented(clicked)) {
                augmentedOrRegular(player, Heart.createAugmented(), Heart.createRegular(), Material.RED_STAINED_GLASS_PANE);
            } else if (Emerald.isAugmented(clicked)) {
                augmentedOrRegular(player, Emerald.createAugmented(), Emerald.createRegular(), Material.LIME_STAINED_GLASS_PANE);
            } else if (Ender.isAugmented(clicked)) {
                augmentedOrRegular(player, Ender.createAugmented(), Ender.createRegular(), Material.PURPLE_STAINED_GLASS_PANE);
            } else if (Apophis.isAugmented(clicked)) {
                augmentedOrRegular(player, Apophis.createAugmented(), Apophis.createRegular(), Material.MAGENTA_STAINED_GLASS_PANE);
            } else if (Feather.isAugmented(clicked)) {
                augmentedOrRegular(player, Feather.createAugmented(), Feather.createRegular(), Material.WHITE_STAINED_GLASS_PANE);
            } else if (Fire.isAugmented(clicked)) {
                augmentedOrRegular(player, Fire.createAugmented(), Fire.createRegular(), Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Haste.isAugmented(clicked)) {
                augmentedOrRegular(player, Haste.createAugmented(), Haste.createRegular(), Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Invisibility.isAugmented(clicked)) {
                augmentedOrRegular(player, Invisibility.createAugmented(), Invisibility.createRegular(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            } else if (Ocean.isAugmented(clicked)) {
                augmentedOrRegular(player, Ocean.createAugmented(), Ocean.createRegular(), Material.BLUE_STAINED_GLASS_PANE);
            } else if (Regen.isAugmented(clicked)) {
                augmentedOrRegular(player, Regen.createAugmented(), Regen.createRegular(), Material.RED_STAINED_GLASS_PANE);
            }
        }

        if (clickedInventory instanceof EffectLevelInventory) {
            if (clicked.getType() != Material.POTION) {
                event.setCancelled(true);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("infuses")) {
            if (sender instanceof Player player) {
                openSwordSelectionGUI(player);
            } else {
                sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            }

            return true;
        }

        return false;
    }
}