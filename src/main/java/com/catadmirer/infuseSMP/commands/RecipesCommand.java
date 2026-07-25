package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.inventories.RecipeListGUI;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import org.bukkit.entity.Player;

public class RecipesCommand {
    public static LiteralCommandNode<CommandSourceStack> build(Infuse plugin) {
        return Commands.literal("recipes")
            .executes(RecipesCommand::openRecipeGUI)
            .build();
    }

    public static int openRecipeGUI(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getSender() instanceof Player player) {
            player.openInventory(new RecipeListGUI().getInventory());
        }
        return 1;
    }
}
