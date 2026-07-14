package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.inventories.RecipeGUI;
import com.catadmirer.infuseSMP.inventories.RecipeListGUI;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecipesCommand implements Listener {
    private final Infuse plugin;

    public static LiteralCommandNode<CommandSourceStack> build(Infuse plugin) {
        RecipesCommand cmd = new RecipesCommand(plugin);

        return Commands.literal("recipes")
            .executes(cmd::openRecipeGUI)
            .build();
    }

    public RecipesCommand(Infuse plugin) {
        this.plugin = plugin;
    }

    public int openRecipeGUI(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getSender() instanceof Player player) {
            player.openInventory(new RecipeListGUI().getInventory());
        }
        return 1;
    }

    /**
     * Preventing players from clicking items in a {@link RecipeGUI} inventory.
     * 
     * @param event The {@link InventoryClickEvent} to listen for.
     */
    @EventHandler
    public void recipeGUIHandler(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getHolder() instanceof RecipeGUI) {
            event.setCancelled(true);
        }
    }

    /**
     * Inventory click handler for the RecipeListGUI inventory
     * 
     * @param event an InventoryClickEvent
     */
    @EventHandler
    public void recipeListGUIHandler(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof RecipeListGUI)) return;

        event.setCancelled(true);

        // Getting the clicked item and opening the recipe menu for the item.
        ItemStack clickedItem = event.getCurrentItem();
        InfuseEffect effect = InfuseEffect.fromItem(clickedItem);
        if (effect == null) return;

        HumanEntity player = event.getWhoClicked();

        // Erroring out if the recipe is not enabled
        if (!plugin.getRecipeManager().isRecipeEnabled(effect)) {
            player.sendMessage(new Message(MessageType.RECIPE_DISABLED).toComponent());
            return;
        }

        if (plugin.getRecipeManager().getRecipe(effect).getChoiceMap().isEmpty()) {
            player.sendMessage(new Message(MessageType.RECIPE_NOT_FOUND).toComponent());
            return;
        }

        // Opening the recipe gui
        Inventory recipeGui = new RecipeGUI(plugin.getRecipeManager(), effect).getInventory();
        player.closeInventory();
        player.openInventory(recipeGui);
    }
}