package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.catadmirer.infuseSMP.Infuse;

public class SwapEffects implements CommandExecutor {
    private final Infuse plugin;

    public SwapEffects(Infuse plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        
        EffectMapping effect1 = plugin.getEffectManager().getEffect(player.getUniqueId(), "1");
        EffectMapping effect2 = plugin.getEffectManager().getEffect(player.getUniqueId(), "2");
        if (effect1 == null && effect2 == null) {
            player.sendMessage("§cYou do not have any effects equipped to swap.");
            return true;
        }
        
        plugin.getEffectManager().setEffect(player.getUniqueId(), "1", effect2);
        plugin.getEffectManager().setEffect(player.getUniqueId(), "2", effect1);
        player.sendMessage("§aYour Effects have been swapped.");
        return true;
    }
}