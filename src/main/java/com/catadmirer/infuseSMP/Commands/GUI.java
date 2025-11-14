package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import java.util.Map;
import java.util.function.Supplier;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import org.bukkit.inventory.meta.ItemMeta;

public class GUI implements Listener, CommandExecutor {
    public static void openSwordSelectionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§lInfuses");
        ItemStack magenta = createPane(Material.MAGENTA_STAINED_GLASS_PANE);
        ItemStack purple = createPane(Material.PURPLE_STAINED_GLASS_PANE);
        ItemStack blue = createPane(Material.BLUE_STAINED_GLASS_PANE);
        ItemStack lightBlue = createPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        int[] magentaSlots = {0, 1, 2, 6, 7, 8, 9, 10, 16, 17, 18, 26, 27, 35};
        int[] purpleSlots = {36, 37, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        int[] blueSlots = {49};
        int[] lightBlueSlots = {3, 4, 5, 11, 13, 15, 19, 25, 28, 34, 38, 42};
        setItems(gui, magentaSlots, magenta);
        setItems(gui, purpleSlots, purple);
        setItems(gui, blueSlots, blue);
        setItems(gui, lightBlueSlots, lightBlue);
        Map<Integer, ItemStack> infusedItems = Map.ofEntries(
                Map.entry(12, Augmented.createFrost()),
                Map.entry(14, Augmented.createSpeed()),
                Map.entry(20, Augmented.createFeather()),
                Map.entry(21, Augmented.createOcean()),
                Map.entry(22, Augmented.createInvis()),
                Map.entry(23, Augmented.createEnder()),
                Map.entry(24, Augmented.createEmerald()),
                Map.entry(29, Augmented.createHeart()),
                Map.entry(30, Augmented.createRegen()),
                Map.entry(31, Augmented.createStrength()),
                Map.entry(32, Augmented.createFire()),
                Map.entry(33, Augmented.createHaste()),
                Map.entry(40, Augmented.createThunder())
        );

        infusedItems.forEach(gui::setItem);
        if ((Boolean) Infuse.getInstance().getCanfig("extra_effects.Thief")) {
            gui.setItem(39, Augmented.createThief());
        }
        if ((Boolean) Infuse.getInstance().getCanfig("extra_effects.Apophis")) {
            gui.setItem(41, Augmented.createApophis());
        }
        player.openInventory(gui);
    }

    private static ItemStack createPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }

    private static void setItems(Inventory gui, int[] slots, ItemStack item) {
        for (int slot : slots) {
            gui.setItem(slot, item);
        }
    }

    private void openDaGui(Player player,
                               Supplier<ItemStack> augmentedSupplier,
                               Supplier<ItemStack> regularSupplier,
                               Material backgroundColor) {

        Inventory gui = Bukkit.createInventory(null, 27, "§eChoose");
        fillChoiceGUI(gui, backgroundColor);
        gui.setItem(11, regularSupplier.get());
        gui.setItem(15, augmentedSupplier.get());

        player.openInventory(gui);
    }

    private void fillChoiceGUI(Inventory gui, Material color) {
        ItemStack filler = new ItemStack(color);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }
    }

    @EventHandler
    public void onClicky(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (title.equals("§lInfuses")) {
            event.setCancelled(true);
            if (Augmented.isFrost(clicked)) {
                openDaGui(player, Augmented::createFrost, Frost::createEffect, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (Augmented.isSpeed(clicked)) {
                openDaGui(player, Augmented::createSpeed, Speed::createEffect, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (Augmented.isStrength(clicked)) {
                openDaGui(player, Augmented::createStrength, Strength::createEffect, Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.isThunder(clicked)) {
                openDaGui(player, Augmented::createThunder, Thunder::createEffect, Material.YELLOW_STAINED_GLASS_PANE);
            } else if (Augmented.isThief(clicked)) {
                openDaGui(player, Augmented::createThief, Thief::createEffect, Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.isHeart(clicked)) {
                openDaGui(player, Augmented::createHeart, Heart::createEffect, Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.isEmerald(clicked)) {
                openDaGui(player, Augmented::createEmerald, Emerald::createEffect, Material.LIME_STAINED_GLASS_PANE);
            } else if (Augmented.isEnder(clicked)) {
                openDaGui(player, Augmented::createEnder, Ender::createEffect, Material.PURPLE_STAINED_GLASS_PANE);
            } else if (Augmented.isApophis(clicked)) {
                openDaGui(player, Augmented::createApophis, Apophis::createEffect, Material.MAGENTA_STAINED_GLASS_PANE);
            } else if (Augmented.isFeather(clicked)) {
                openDaGui(player, Augmented::createFeather, Feather::createEffect, Material.WHITE_STAINED_GLASS_PANE);
            } else if (Augmented.isFire(clicked)) {
                openDaGui(player, Augmented::createFire, Fire::createEffect, Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Augmented.isHaste(clicked)) {
                openDaGui(player, Augmented::createHaste, Haste::createEffect, Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Augmented.isInvis(clicked)) {
                openDaGui(player, Augmented::createInvis, Invisibility::createEffect, Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            } else if (Augmented.isOcean(clicked)) {
                openDaGui(player, Augmented::createOcean, Ocean::createEffect, Material.BLUE_STAINED_GLASS_PANE);
            } else if (Augmented.isRegen(clicked)) {
                openDaGui(player, Augmented::createRegen, Regen::createEffect, Material.RED_STAINED_GLASS_PANE);
            }
            return;
        }
        if (title.equals("§eChoose")) {
            if (clicked.getType() != Material.POTION) {
                event.setCancelled(true);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("infuses")) {
            if (sender instanceof Player player) {
                this.openSwordSelectionGUI(player);
            } else {
                sender.sendMessage(String.valueOf(ChatColor.RED) + "Only players can use this command.");
            }

            return true;
        } else {
            return false;
        }
    }
}