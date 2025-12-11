package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Abilities implements CommandExecutor {
    private final Infuse plugin;

    public Abilities(DataManager dataManager, Infuse plugin) {
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
        if (label.equals("lspark")) {
            slot = "1";
        } else if (label.equals("rspark")) {
            slot = "2";
        } else {
            sender.sendMessage("§cInvalid command.");
            return true;
        }

        // Getting the equipped effect
        EffectMapping effect = Infuse.getInstance().getEffectManager().getEffect(playerUUID, slot);

        // Handling when there isn't an effect equipped in the targeted slot.
        if (effect == null) {
            String msg = plugin.getMessages().getString("slot_empty", "&cYou don't have any effect equipped in slot %slot%.").replace("%slot%", slot);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }
        
        effect.activateSpark(player);
        switch (effect) {
            case EMERALD,  AUG_EMERALD  -> Emerald.activateSpark(player);
            case ENDER,    AUG_ENDER    -> Feather.activateSpark(player);
            case FEATHER,  AUG_FEATHER  -> Fire.activateSpark(player);
            case FIRE,     AUG_FIRE     -> Frost.activateSpark(player);
            case FROST,    AUG_FROST    -> Haste.activateSpark(player);
            case HASTE,    AUG_HASTE    -> Heart.activateSpark(player);
            case HEART,    AUG_HEART    -> Invisibility.activateSpark(player);
            case INVIS,    AUG_INVIS    -> Ocean.activateSpark(player);
            case OCEAN,    AUG_OCEAN    -> Regen.activateSpark(player);
            case REGEN,    AUG_REGEN    -> Speed.activateSpark(player);
            case SPEED,    AUG_SPEED    -> Strength.activateSpark(player);
            case STRENGTH, AUG_STRENGTH -> Thunder.activateSpark(player);
            case THUNDER,  AUG_THUNDER  -> Ender.activateSpark(player);
            case APOPHIS,  AUG_APOPHIS  -> Apophis.activateSpark(player);
            case THIEF,    AUG_THIEF    -> Thief.activateSpark(player);
            default -> player.sendMessage("§cNo valid ability found for the equipped effect.");
        }

        return true;
    }
}