package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SwapEffects implements CommandExecutor {
    private final Infuse plugin;

    public SwapEffects(Infuse plugin) {
        this.plugin = plugin;
    }
    
    // Defining the command for swapping effects...
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.ERROR_NOT_PLAYER.toComponent());
            return true;
        }

        // Getting the equipped effects
        EffectMapping effect1 = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        EffectMapping effect2 = plugin.getDataManager().getEffect(player.getUniqueId(), "2");

        // Erroring out if the player doesn't have any effects equipped
        if (effect1 == null && effect2 == null) {
            player.sendMessage(Messages.SWAP_NO_EFFECTS.toComponent());
            return true;
        }

        // Swapping the effects
        plugin.getDataManager().setEffect(player.getUniqueId(), "1", effect2);
        plugin.getDataManager().setEffect(player.getUniqueId(), "2", effect1);
        player.sendMessage(Messages.SWAP_SUCCESS.toComponent());
        return true;
    }
}