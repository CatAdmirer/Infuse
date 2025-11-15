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
            if (Augmented.isFrost(clicked)) {
                augmentedOrRegular(player, Augmented.createFrost(), Frost.createEffect(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (Augmented.isSpeed(clicked)) {
                augmentedOrRegular(player, Augmented.createSpeed(), Speed.createEffect(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (Augmented.isStrength(clicked)) {
                augmentedOrRegular(player, Augmented.createStrength(), Strength.createEffect(), Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.isThunder(clicked)) {
                augmentedOrRegular(player, Augmented.createThunder(), Thunder.createEffect(), Material.YELLOW_STAINED_GLASS_PANE);
            } else if (Augmented.isThief(clicked)) {
                augmentedOrRegular(player, Augmented.createThief(), Thief.createEffect(), Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.isHeart(clicked)) {
                augmentedOrRegular(player, Augmented.createHeart(), Heart.createEffect(), Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.isEmerald(clicked)) {
                augmentedOrRegular(player, Augmented.createEmerald(), Emerald.createEffect(), Material.LIME_STAINED_GLASS_PANE);
            } else if (Augmented.isEnder(clicked)) {
                augmentedOrRegular(player, Augmented.createEnder(), Ender.createEffect(), Material.PURPLE_STAINED_GLASS_PANE);
            } else if (Augmented.isApophis(clicked)) {
                augmentedOrRegular(player, Augmented.createApophis(), Apophis.createEffect(), Material.MAGENTA_STAINED_GLASS_PANE);
            } else if (Augmented.isFeather(clicked)) {
                augmentedOrRegular(player, Augmented.createFeather(), Feather.createEffect(), Material.WHITE_STAINED_GLASS_PANE);
            } else if (Augmented.isFire(clicked)) {
                augmentedOrRegular(player, Augmented.createFire(), Fire.createEffect(), Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Augmented.isHaste(clicked)) {
                augmentedOrRegular(player, Augmented.createHaste(), Haste.createEffect(), Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Augmented.isInvis(clicked)) {
                augmentedOrRegular(player, Augmented.createInvis(), Invisibility.createEffect(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            } else if (Augmented.isOcean(clicked)) {
                augmentedOrRegular(player, Augmented.createOcean(), Ocean.createEffect(), Material.BLUE_STAINED_GLASS_PANE);
            } else if (Augmented.isRegen(clicked)) {
                augmentedOrRegular(player, Augmented.createRegen(), Regen.createEffect(), Material.RED_STAINED_GLASS_PANE);
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