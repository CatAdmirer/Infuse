package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.inventories.EffectChooser;
import com.catadmirer.infuseSMP.inventories.RecipeListGUI;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.util.CustomArgumentTypes;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfuseCommand {
    private final Infuse plugin;
    
    public static LiteralCommandNode<CommandSourceStack> build(Infuse plugin) {
        InfuseCommand cmd = new InfuseCommand(plugin);

        return Commands.literal("infuse")
            .then(Commands.literal("gui").executes(cmd::gui))
            .then(Commands.literal("reload").executes(cmd::reload))
            .then(Commands.literal("recipes").executes(InfuseCommand::recipes))
            .then(Commands.literal("giveeffect")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .then(Commands.argument("effect", CustomArgumentTypes.INFUSE_EFFECT)
                        .executes(c -> cmd.giveEffect(c, c.getArgument("target", PlayerSelectorArgumentResolver.class), c.getArgument("effect", InfuseEffect.class)))
                    )
                )
            )
            .then(Commands.literal("seteffect")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .then(Commands.argument("effect", CustomArgumentTypes.INFUSE_EFFECT)
                        .then(Commands.argument("slot", CustomArgumentTypes.SLOT)
                            .executes(c -> cmd.setEffect(c, c.getArgument("target", PlayerSelectorArgumentResolver.class), c.getArgument("effect", InfuseEffect.class), c.getArgument("slot", String.class)))
                        )
                    )
                )
            )
            .then(Commands.literal("cleareffects")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .executes(c -> cmd.clearEffects(c, c.getArgument("target", PlayerSelectorArgumentResolver.class)))
                )
            )
            .then(Commands.literal("cooldown")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .executes(c -> cmd.cooldown(c, c.getArgument("target", PlayerSelectorArgumentResolver.class)))
                )
            )
            .then(Commands.literal("controls")
                .then(Commands.argument("choice", CustomArgumentTypes.CONTROL_MODE)
                    .executes(c -> cmd.controls(c, c.getArgument("choice", String.class)))
                )
            )
            .then(Commands.literal("help").executes(cmd::help))
            .build();
    }

    public InfuseCommand(Infuse plugin) {
        this.plugin = plugin;
    }

    public int gui(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }

        player.openInventory(new EffectChooser(plugin).getInventory());
        return 1;
    }

    public int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }

        plugin.getMainConfig().load();
        plugin.getRecipeManager().reload();
        player.sendMessage("Infuse configs reloaded");
        return 1;
    }

    public int giveEffect(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, InfuseEffect effect) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }

        Player target;
        try {
            target = resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException err) {
            sender.sendMessage(err.componentMessage());
            return 1;
        }

        if (target == null || !target.isOnline()) {
            player.sendMessage(new Message(MessageType.ERROR_TARGET_NOT_FOUND).toComponent());
            return 1;
        }

        if (effect == null) {
            player.sendMessage(new Message(MessageType.INFUSE_INVALID_PARAM).toComponent());
            return 1;
        }

        target.getInventory().addItem(effect.createItem());

        Message msg = new Message(MessageType.INFUSE_GIVEEFFECT_SUCCESS);
        msg.applyPlaceholder("effect_color", "<#" + Integer.toHexString(effect.getPotionColor().getRGB() & 0xffffff) + ">");
        msg.applyPlaceholder("effect_name", effect.getName());
        target.sendMessage(msg.toComponent());
        
        return 1;
    }
    
    public int setEffect(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver, InfuseEffect effect, String slot) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }

        Player target;
        
        try {
            target = resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException err) {
            sender.sendMessage(err.componentMessage());
            return 1;
        }
        
        if (effect == null) {
            player.sendMessage(new Message(MessageType.INFUSE_INVALID_PARAM).toComponent());
            return 1;
        }
        
        // Setting the effect
        plugin.getEffectManager().setEffect(target, effect, slot);
        Message msg = new Message(MessageType.INFUSE_SETEFFECT_SUCCESS);
        msg.applyPlaceholder("slot", slot);
        msg.applyPlaceholder("player_name", target.getName());
        msg.applyPlaceholder("effect_name", effect.getName());
        player.sendMessage(msg.toComponent());

        return 1;
    }
    
    public int clearEffects(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }

        // Getting the player and making sure they are online
        Player target;
        try {
            target = resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException err) {
            sender.sendMessage(err.componentMessage());
            return 1;
        }

        // Removing the effects from the player
        plugin.getEffectManager().unequipEffect(target, "1");
        plugin.getEffectManager().unequipEffect(target, "2");
        Message msg = new Message(MessageType.INFUSE_CLEAREFFECTS_SUCCESS);
        msg.applyPlaceholder("player_name", target.getName());
        player.sendMessage(msg.toComponent());

        return 1;
    }
    
    public int cooldown(CommandContext<CommandSourceStack> ctx, PlayerSelectorArgumentResolver resolver) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }

        // Getting the player and making sure they are online
        Player target;
        try {
            target = resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException err) {
            sender.sendMessage(err.componentMessage());
            return 1;
        }

        if (target == null || !target.isOnline()) {
            player.sendMessage(new Message(MessageType.ERROR_TARGET_NOT_FOUND).toComponent());
            return 1;
        }

        // Removing cooldowns from the player
        CooldownManager.removeAllCooldowns(target.getUniqueId());
        Message msg = new Message(MessageType.INFUSE_COOLDOWN_SUCCESS);
        msg.applyPlaceholder("player_name", target.getName());
        player.sendMessage(msg.toComponent());

        return 1;
    }

    public int controls(CommandContext<CommandSourceStack> ctx, String choice) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        // Setting the control mode for the user.
        plugin.getDataManager().setControlMode(player.getUniqueId(), choice);

        // Assigning the permission for offhand use if the user chose offhand mode
        boolean offhandEnabled = choice.equalsIgnoreCase("offhand");
        player.addAttachment(plugin, "ability.use", !offhandEnabled);

        Message msg = new Message(MessageType.INFUSE_CONTROLS_SUCCESS);
        msg.applyPlaceholder("control_mode", choice);
        player.sendMessage(msg.toComponent());

        return 1;
    }

    public static int recipes(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getSender() instanceof Player player) {
            player.openInventory(new RecipeListGUI().getInventory());
        }
        return 1;
    }
    
    public int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        new Message(MessageType.INFUSE_HELP).toComponentList().forEach(sender::sendMessage);

        return 1;
    }
}
