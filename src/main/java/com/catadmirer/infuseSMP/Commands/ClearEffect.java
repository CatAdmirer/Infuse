package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ClearEffect implements Listener, CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cleareffects")) {
            if (args.length != 1) {
                sender.sendMessage("§cUsage: /cleareffects <player>");
                return true;
            } else {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    Infuse.getInstance().getEffectManager().removeEffect(target.getUniqueId(), "2");
                    Infuse.getInstance().getEffectManager().removeEffect(target.getUniqueId(), "1");
                    return true;
                } else {
                    return true;
                }
            }
        } else {
            return false;
        }
    }
}