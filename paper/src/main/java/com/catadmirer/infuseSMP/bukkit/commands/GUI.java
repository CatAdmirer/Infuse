package com.catadmirer.infuseSMP.bukkit.commands;

import com.catadmirer.infuseSMP.bukkit.inventories.EffectChooser;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class GUI implements CommandExecutor {
    private final InfusePlugin plugin;

    public GUI(InfusePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NonNull CommandSender sender, Command command, @NonNull String label, String @NonNull [] args) {
        if (command.getName().equalsIgnoreCase("infuses")) {
            // Opening the gui for players only.
            if (sender instanceof Player player) {
                player.openInventory(new EffectChooser(plugin).getInventory());
            } else {
                sender.sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
            }

            return true;
        }

        return false;
    }
}
