package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
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

        // Finding the command the player used.  This executor works for /lspark and /rspark
        String slot;
        if (label.equals("lspark")) slot = "1";
        else if (label.equals("rspark")) slot = "2";
        else {
            sender.sendMessage("§cInvalid command.");
            return true;
        }

        // Getting the equipped effect
        EffectMapping effect = Infuse.getInstance().getEffectManager().getEffect(playerUUID, slot);

        // Handling when there isn't an effect equipped in the targeted slot
        if (effect == null) {
            String msg = plugin.getMessages().getString("slot_empty", "&cYou don't have an effect equipped in slot %slot%.").replace("%slot%", slot);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }
        
        // Activating the effect's spark
        effect.activateSpark(player);
        return true;
    }
}