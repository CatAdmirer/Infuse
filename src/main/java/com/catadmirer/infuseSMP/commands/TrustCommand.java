package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.DataManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class TrustCommand implements CommandExecutor {
    private final Infuse plugin;
    private final DataManager dataManager;

    public TrustCommand(Infuse plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Limiting this command to only players.
        if (!(sender instanceof Player caster)) {
            sender.sendMessage(getMessage("trust_consoleusage", "&cOnly players can use this command."));
            return true;
        }

        // Validating the number of args
        if (args.length != 1) {
            caster.sendMessage(getMessage("trust_incorrectusage", "&cUsage: /%label% <player>").replace("%label%", label));
            return true;
        }

        // Getting the target to trust/untrust
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            caster.sendMessage(getMessage("trust_noplayer", "&cPlayer not found."));
            return true;
        }

        // Preventing the caster from trusting/untrusting themself.
        if (caster.getUniqueId().equals(target.getUniqueId())) {
            caster.sendMessage(getMessage("trust_self", "&cYou always trust yourself. Surely..."));
            return true;
        }

        // Making the caster trust the target.
        if (label.equalsIgnoreCase("trust")) {
            dataManager.addTrust(caster, target);
            caster.sendMessage(getMessage("trust_added", "&aYou now trust %target%.").replace("%target%", target.getName()));
            return true;
        }

        // Making the caster untrust the target.
        if (label.equalsIgnoreCase("untrust")) {
            dataManager.removeTrust(caster, target);
            caster.sendMessage(getMessage("trust_removed", "&eYou no longer trust %target%.").replace("%target%", target.getName()));
            return true;
        }

        return false;
    }

    private String getMessage(String path, String defaultMessage) {
        FileConfiguration messages = plugin.getMessages();
        String message = messages.getString(path, defaultMessage);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}