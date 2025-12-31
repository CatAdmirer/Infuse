package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import java.util.regex.Pattern;
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
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        // Finding which slot to activate the spark for.
        String slot;
        if (label.contains("lspark")) slot = "1";
        else if (label.contains("rspark")) slot = "2";
        else {
            sender.sendMessage("§cInvalid command.");
            return true;
        }

        // Getting the name of the equipped effect.
        EffectMapping equippedEffect = plugin.getEffectManager().getEffect(playerUUID, slot);

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

    public String stripAllColors(String input) {
        if (input == null) return null;
        Pattern pattern = Pattern.compile("(§#[0-9a-fA-F]{6})|(§x(§[0-9a-fA-F]){6})|(§[0-9a-fk-orA-FK-OR])");
        return pattern.matcher(input).replaceAll("");
    }
}