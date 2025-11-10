package com.catadmirer.infuseSMP.Trust;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class TrustCommand implements CommandExecutor {

    private final Infuse plugin;
    private final EffectManager trustManager;

    public TrustCommand(Infuse plugin, EffectManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("trust_consoleusage", "&cOnly players can use this command."));
            return true;
        }

        Player caster = (Player) sender;

        if (args.length != 1) {
            caster.sendMessage(getMessage("trust_incorrectusage", "&cUsage: /%label% <player>").replace("%label%", label));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            caster.sendMessage(getMessage("trust_noplayer", "&cPlayer not found."));
            return true;
        }

        if (caster.getUniqueId().equals(target.getUniqueId())) {
            caster.sendMessage(getMessage("trust_self", "&cYou always trust yourself. Surely..."));
            return true;
        }

        if (label.equalsIgnoreCase("trust")) {
            trustManager.addTrust(caster, target);
            caster.sendMessage(getMessage("trust_added", "&aYou now trust %target%.").replace("%target%", target.getName()));
            return true;
        }

        if (label.equalsIgnoreCase("untrust")) {
            trustManager.removeTrust(caster, target);
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



