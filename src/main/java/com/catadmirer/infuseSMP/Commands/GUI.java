package com.catadmirer.infuseSMP.Commands;

import java.util.*;
import java.util.function.Supplier;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        int[] lightBlueSlots = {
                3, 4, 5, 11, 13, 15, 19, 25, 28, 34, 38, 42
        };
        setItems(gui, magentaSlots, magenta);
        setItems(gui, purpleSlots, purple);
        setItems(gui, blueSlots, blue);
        setItems(gui, lightBlueSlots, lightBlue);
        Augmented augmented = new Augmented();
        Map<Integer, ItemStack> infusedItems = Map.ofEntries(
                Map.entry(12, Augmented.createFROST()),
                Map.entry(14, augmented.createSPEED()),
                Map.entry(20, Augmented.createFEATHER()),
                Map.entry(21, Augmented.createOCEAN()),
                Map.entry(22, Augmented.createINVIS()),
                Map.entry(23, Augmented.createENDER()),
                Map.entry(24, Augmented.createEME()),
                Map.entry(29, Augmented.createHEART()),
                Map.entry(30, Augmented.createREGEN()),
                Map.entry(31, augmented.createST()),
                Map.entry(32, Augmented.createFIRE()),
                Map.entry(33, Augmented.createHASTE()),
                Map.entry(40, augmented.createTHUNDER())
        );

        infusedItems.forEach(gui::setItem);
        if ((Boolean) Infuse.getInstance().getCanfig("extra_effects.Thief")) {
            gui.setItem(39, Augmented.createTHF());
        }
        if ((Boolean) Infuse.getInstance().getCanfig("extra_effects.Apophis")) {
            gui.setItem(41, Augmented.createAPH());
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
        Augmented augmented = new Augmented();
        if (title.equals("§lInfuses")) {
            event.setCancelled(true);
            if (Augmented.ISFROST(clicked)) {
                openDaGui(player, Augmented::createFROST, Frost::createFrost, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (augmented.ISSPEED(clicked)) {
                openDaGui(player, Augmented::createSPEED, Speed::createSPEED, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            } else if (augmented.ISST(clicked)) {
                openDaGui(player, Augmented::createST, Strength::createStealthGem, Material.RED_STAINED_GLASS_PANE);
            } else if (augmented.ISTHUNDER(clicked)) {
                openDaGui(player, Augmented::createTHUNDER, Thunder::createTHUNDER, Material.YELLOW_STAINED_GLASS_PANE);
            } else if (Augmented.ISTHIEF(clicked)) {
                openDaGui(player, Augmented::createTHF, Thief::createTHF, Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.ISHEART(clicked)) {
                openDaGui(player, Augmented::createHEART, Heart::createHeart, Material.RED_STAINED_GLASS_PANE);
            } else if (Augmented.ISEME(clicked)) {
                openDaGui(player, Augmented::createEME, Emerald::createInvincibilityGem, Material.LIME_STAINED_GLASS_PANE);
            } else if (Augmented.ISEND(clicked)) {
                openDaGui(player, Augmented::createENDER, Ender::createEnderGem, Material.PURPLE_STAINED_GLASS_PANE);
            } else if (Augmented.ISAUGAPH(clicked)) {
                openDaGui(player, Augmented::createAPH, Apophis::createAPH, Material.MAGENTA_STAINED_GLASS_PANE);
            } else if (Augmented.ISFEATHER(clicked)) {
                openDaGui(player, Augmented::createFEATHER, Feather::createGlide, Material.WHITE_STAINED_GLASS_PANE);
            } else if (Augmented.ISFIRE(clicked)) {
                openDaGui(player, Augmented::createFIRE, Fire::createFIRE, Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Augmented.ISHASTE(clicked)) {
                openDaGui(player, Augmented::createHASTE, Haste::createFake, Material.ORANGE_STAINED_GLASS_PANE);
            } else if (Augmented.ISINVIS(clicked)) {
                openDaGui(player, Augmented::createINVIS, Invisibility::createStealthGem, Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            } else if (Augmented.ISOCEAN(clicked)) {
                openDaGui(player, Augmented::createOCEAN, Ocean::createOcean, Material.BLUE_STAINED_GLASS_PANE);
            } else if (Augmented.ISREGEN(clicked)) {
                openDaGui(player, Augmented::createREGEN, Regen::createFake, Material.RED_STAINED_GLASS_PANE);
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
            if (sender instanceof Player) {
                Player player = (Player)sender;
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
