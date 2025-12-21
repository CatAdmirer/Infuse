package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrustCommand implements CommandExecutor {
    private final DataManager dataManager;

    public TrustCommand(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Limiting this command to only players.
        if (!(sender instanceof Player caster)) {
            sender.sendMessage(Messages.TRUST_CONSOLEUSAGE.toComponent());
            return true;
        }

        // Validating the number of args
        if (args.length != 1) {
            String msg = Messages.TRUST_INCORRECTUSAGE.getMessage();
            msg = msg.replace("%label%", label);
            caster.sendMessage(Messages.toComponent(msg));
            return true;
        }

        // Getting the target to trust/untrust
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            caster.sendMessage(Messages.TRUST_NOPLAYER.getMessage());
            return true;
        }

        // Preventing the caster from trusting/untrusting themself.
        if (caster.getUniqueId().equals(target.getUniqueId())) {
            caster.sendMessage(Messages.TRUST_SELF.getMessage());
            return true;
        }

        // Making the caster trust the target.
        if (label.equalsIgnoreCase("trust")) {
            dataManager.addTrust(caster, target);
            String msg = Messages.TRUST_ADDED.getMessage();
            msg = msg.replace("%target%", target.getName());
            caster.sendMessage(Messages.toComponent(msg));
            return true;
        }

        // Making the caster untrust the target.
        if (label.equalsIgnoreCase("untrust")) {
            dataManager.removeTrust(caster, target);
            String msg = Messages.TRUST_REMOVED.getMessage();
            msg = msg.replace("%target%", target.getName());
            caster.sendMessage(Messages.toComponent(msg));
            return true;
        }

        return false;
    }
}