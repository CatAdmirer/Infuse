package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.managers.EffectManager;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

public class ClearEffectsCommand {
    private final EffectManager effectManager;

    public static LiteralCommandNode<CommandSourceStack> build(EffectManager effectManager) {
        ClearEffectsCommand cmd = new ClearEffectsCommand(effectManager);
        return Commands.literal("cleareffects").then(Commands.argument("target", ArgumentTypes.player()).executes(cmd::clearEffects)).build();
    }

    public ClearEffectsCommand(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public int clearEffects(CommandContext<CommandSourceStack> ctx) {
        try {
            Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            effectManager.unequipEffect(target, "1");
            effectManager.unequipEffect(target, "2");
        } catch (CommandSyntaxException e) {
            ctx.getSource().getSender().sendMessage(e.componentMessage());
        }

        return 1;
    }
}