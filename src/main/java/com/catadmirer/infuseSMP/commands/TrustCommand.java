package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TrustCommand {
    private final DataManager dataManager;
    private final boolean trust;

    public static LiteralCommandNode<CommandSourceStack> build(DataManager manager, boolean trust) {
        TrustCommand cmd = new TrustCommand(manager, trust);

        return Commands.literal(trust ? "trust" : "untrust")
            .then(Commands.argument("target", ArgumentTypes.players()).executes(cmd::trust))
            .executes(cmd::trust)
            .build();
    }

    private TrustCommand(DataManager dataManager, boolean trust) {
        this.dataManager = dataManager;
        this.trust = trust;
    }

    public int trust(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        // Limiting this command to only players.
        if (!(sender instanceof Player caster)) {
            sender.sendMessage(new Message(MessageType.TRUST_CONSOLE_USAGE).toComponent());
            return 1;
        }

        // Getting the targets
        List<Player> targets;
        try {
            PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
            targets = resolver.resolve(ctx.getSource());
        } catch (IllegalArgumentException err) {
            Message msg = new Message(MessageType.TRUST_INCORRECT_USAGE);
            msg.applyPlaceholder("label", trust ? "trust" : "untrust");
            caster.sendMessage(msg.toComponent());
            return 1;
        } catch (CommandSyntaxException err) {
            sender.sendMessage(err.componentMessage());
            return 1;
        }

        targets.remove(caster);

        if (trust) {
            targets.forEach(t -> addTrust(caster, t));
        } else {
            targets.forEach(t -> removeTrust(caster, t));
        }

        return 1;
    }

    public void addTrust(Player caster, Player target) {
        // Preventing duplicate trust entries
        Message msg = new Message(MessageType.TRUST_ALREADY_TRUSTED);

        if (!dataManager.getTrusted(caster).contains(target)) {
            dataManager.addTrust(caster, target);
            msg = new Message(MessageType.TRUST_ADDED);
        }

        msg.applyPlaceholder("target", target.getName());
        caster.sendMessage(msg.toComponent());
    }

    public void removeTrust(Player caster, Player target) {
        // Removing trust
        Message msg = new Message(MessageType.TRUST_NOT_TRUSTED);

        if (dataManager.getTrusted(caster).contains(target)) {
            dataManager.removeTrust(caster, target);
            msg = new Message(MessageType.TRUST_REMOVED);
        }

        msg.applyPlaceholder("target", target.getName());
        caster.sendMessage(msg.toComponent());
    }
}