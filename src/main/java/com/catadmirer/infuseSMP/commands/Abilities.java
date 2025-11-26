package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.catadmirer.infuseSMP.managers.EffectMaps;
import java.util.UUID;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Abilities implements CommandExecutor {
    private final Infuse plugin;

    public Abilities(DataManager trustManager, Infuse plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        } else {
            UUID playerUUID = player.getUniqueId();
            String slot;
            if (label.contains("rspark")) {
                slot = "2";
            } else if (label.contains("lspark")) {
                slot = "1";
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid command.");
                return true;
            }
            String equippedEffect = Infuse.getInstance().getEffectManager().getEffect(playerUUID, slot);

            if (equippedEffect == null) {
                String msg = plugin.getMessages().getString("slot_empty", "&cYou don't have any effect equipped in slot %slot%.").replace("%slot%", slot);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            } else {
                String strippedEffect = stripAllColors(equippedEffect);
                strippedEffect = plugin.getEffectReversed(strippedEffect);
                Integer abilityId = EffectMaps.getEffectNumber(strippedEffect);

                if (abilityId == null) {
                    player.sendMessage(ChatColor.RED + "No valid ability found for the equipped effect.");
                    return true;
                }
                switch (abilityId) {
                    case 0:
                    case 1:
                        Emerald.activateSpark(player);
                        break;
                    case 2:
                    case 3:
                        Feather.activateSpark(player);
                        break;
                    case 4:
                    case 5:
                        Fire.activateSpark(player);
                        break;
                    case 6:
                    case 7:
                        Frost.activateSpark(player);
                        break;
                    case 8:
                    case 9:
                        Haste.activateSpark(player);
                        break;
                    case 10:
                    case 11:
                        Heart.activateSpark(player);
                        break;
                    case 12:
                    case 13:
                        Invisibility.activateSpark(player);
                        break;
                    case 14:
                    case 15:
                        Ocean.activateSpark(player);
                        break;
                    case 16:
                    case 17:
                        Regen.activateSpark(player);
                        break;
                    case 18:
                    case 19:
                        Speed.activateSpark(player);
                        break;
                    case 20:
                    case 21:
                        Strength.activateSpark(player);
                        break;
                    case 22:
                    case 23:
                        Thunder.activateSpark(player);
                        break;
                    case 24:
                        Ender.activateSpark(player);
                        break;
                    case 25:
                        Apophis.activateSpark(player);
                        break;
                    case 26:
                        Ender.activateSpark(player);
                        break;
                    case 27:
                        Apophis.activateSpark(player);
                        break;
                    case 28:
                        Thief.activateSpark(player);
                        break;
                    case 29:
                        Thief.activateSpark(player);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "No valid ability found for the equipped effect.");
                }

                return true;
            }
        }
    }

    public String stripAllColors(String input) {
        if (input == null) return null;
        Pattern pattern = Pattern.compile(
                "(§#[0-9a-fA-F]{6})" +
                        "|(§x(§[0-9a-fA-F]){6})" +
                        "|(§[0-9a-fk-orA-FK-OR])"
        );
        return pattern.matcher(input).replaceAll("");
    }
}