package com.catadmirer.infuseSMP.Commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ClearEffect implements Listener, CommandExecutor {
    private final Set<Player> cooldownPlayers = new HashSet();
    private final JavaPlugin plugin;
    private final EquipEffect hackEquipListener;
    private final Map<UUID, Long> netherStarCooldowns = new HashMap();

    public ClearEffect(JavaPlugin plugin, EquipEffect hackEquipListener) {
        this.plugin = plugin;
        this.hackEquipListener = hackEquipListener;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("clearhack")) {
            if (args.length != 1) {
                sender.sendMessage(String.valueOf(ChatColor.RED) + "Usage: /clearhack <player>");
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
