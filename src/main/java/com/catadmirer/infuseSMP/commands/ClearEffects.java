package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.playerdata.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NonNull;

public class ClearEffects implements Listener, CommandExecutor {
    private final DataManager dataManager;

    public ClearEffects(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        if (!command.getName().equalsIgnoreCase("cleareffects")) return false;
        
        if (args.length != 1) {
            sender.sendMessage(new Message(MessageType.CLEAREFFECTS_USAGE).toComponent());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            dataManager.removeEffect(target, "1");
            dataManager.removeEffect(target, "2");
        }
        
        return true;
    }
}