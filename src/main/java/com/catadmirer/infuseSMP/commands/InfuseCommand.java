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
            .then(Commands.literal("recipes").executes(cmd::recipes))
            .then(Commands.literal("giveeffect")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .then(Commands.argument("effect", CustomArgumentTypes.INFUSE_EFFECT)
                        .executes(cmd::giveEffect)
                    )
                )
            )
            .then(Commands.literal("seteffect")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .then(Commands.argument("effect", CustomArgumentTypes.INFUSE_EFFECT)
                        .then(Commands.argument("slot", CustomArgumentTypes.SLOT)
                            .executes(cmd::setEffect)
                        )
                    )
                )
            )
            .then(Commands.literal("cleareffects")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .executes(cmd::clearEffects)
                )
            )
            .then(Commands.literal("cooldown")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .executes(cmd::cooldown)
                )
            )
            .then(Commands.literal("controls")
                .then(Commands.argument("choice", CustomArgumentTypes.CONTROL_MODE)
                    .executes(cmd::controls)
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

    public int recipes(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        player.openInventory(new RecipeListGUI().getInventory());

        return 1;
    }
    
    public int giveEffect(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }

        PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);

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

        InfuseEffect effect = ctx.getArgument("effect", InfuseEffect.class);
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
    
    public int setEffect(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        if (!player.isOp()) {
            player.sendMessage(new Message(MessageType.ERROR_NOT_OP).toComponent());
            return 1;
        }
        
        // Getting the player and making sure they are online.
        PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
        Player target;
        
        try {
            target = resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException err) {
            sender.sendMessage(err.componentMessage());
            return 1;
        }
        
        // Getting the effect key and verifying its integrity.
        InfuseEffect effect = ctx.getArgument("effect", InfuseEffect.class);
        if (effect == null) {
            player.sendMessage(new Message(MessageType.INFUSE_INVALID_PARAM).toComponent());
            return 1;
        }
        
        // Getting the slot to put the effect into and validating it.
        String slot = ctx.getArgument("slot", String.class);

        // Setting the effect
        plugin.getEffectManager().setEffect(target, effect, slot);
        Message msg = new Message(MessageType.INFUSE_SETEFFECT_SUCCESS);
        msg.applyPlaceholder("slot", slot);
        msg.applyPlaceholder("player_name", target.getName());
        msg.applyPlaceholder("effect_name", effect.getName());
        player.sendMessage(msg.toComponent());

        return 1;
    }
    
    public int clearEffects(CommandContext<CommandSourceStack> ctx) {
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
        PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
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
    
    public int cooldown(CommandContext<CommandSourceStack> ctx) {
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
        PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
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
    
    public int controls(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        // Getting the control mode.
        String choice = ctx.getArgument("choice", String.class).toLowerCase();

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
    
    public int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            // TODO: Better logging
            return 1;
        }

        // TODO: Implement
        sender.sendMessage("/infuse help is not currently implemented.  Please wait for a future update");

        return 1;
    }
}