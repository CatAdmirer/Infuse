package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Abilities implements CommandExecutor {
    private final Infuse plugin;

    public Abilities(Infuse plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.ERROR_NOT_PLAYER.toComponent());
            return true;
        }

        final UUID playerUUID = player.getUniqueId();

        // Finding which slot to activate the spark for.
        String slot;
        if (label.contains("lspark")) {
            slot = "1";
        } else if (label.contains("rspark")) {
            slot = "2";
        } else {
            sender.sendMessage(Messages.ERROR_INVALID_COMMAND.toComponent());
            return true;
        }

        // Getting the name of the equipped effect.
        EffectMapping equippedEffect = plugin.getDataManager().getEffect(playerUUID, slot);

        // Handling if the slot is empty.
        if (equippedEffect == null) {
            String msg = Messages.getMessage(Messages.SLOT_EMPTY);
            msg = msg.replace("%slot%", slot);
            player.sendMessage(Messages.toComponent(msg));
            return true;
        }

        equippedEffect.activateSpark(player);

        return true;
    }
}