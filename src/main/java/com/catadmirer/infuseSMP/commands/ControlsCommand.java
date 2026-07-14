package com.catadmirer.infuseSMP.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.util.CustomArgumentTypes;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ControlsCommand {
    private final Infuse plugin;
    
    public static LiteralCommandNode<CommandSourceStack> build(Infuse plugin) {
        ControlsCommand cmd = new ControlsCommand(plugin);

        return Commands.literal("controls")
            .then(Commands.argument("choice", CustomArgumentTypes.CONTROL_MODE)
                .executes(cmd::controls)
            )
            .build();
    }

    private ControlsCommand(Infuse plugin) {
        this.plugin = plugin;
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
}