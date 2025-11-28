package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ClearEffect implements Listener, CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cleareffects")) {
            // Handling invalid arguments.
            if (args.length != 1) {
                sender.sendMessage(String.valueOf(ChatColor.RED) + "Usage: /cleareffects <player>");
                return true;
            }
            
            // Finding the player by their username
            Player target = Bukkit.getPlayer(args[0]);

            // Removing the effects from that player if they're online.
            if (target != null && target.isOnline()) {
                Infuse.getInstance().getEffectManager().removeEffect(target.getUniqueId(), "1");
                Infuse.getInstance().getEffectManager().removeEffect(target.getUniqueId(), "2");
            }

            return true;
        }

        return false;
    }
}