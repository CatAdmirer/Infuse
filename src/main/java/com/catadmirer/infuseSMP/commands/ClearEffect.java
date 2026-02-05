package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ClearEffect implements Listener, CommandExecutor {
    private final Infuse plugin;

    public ClearEffect(Infuse plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("cleareffects")) return false;
        
        if (args.length != 1) {
            sender.sendMessage(Messages.CLEAREFFECTS_USAGE.toComponent());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            plugin.getDataManager().removeEffect(target.getUniqueId(), "1");
            plugin.getDataManager().removeEffect(target.getUniqueId(), "2");
        }
        
        return true;
    }
}