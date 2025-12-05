package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
import com.catadmirer.infuseSMP.util.MessageUtil;
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
                String msg = plugin.getMessages().getString(
                        "slot_empty",
                        "&cYou don't have any effect equipped in slot %slot%."
                ).replace("%slot%", slot);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            } else {
                String strippedEffect = MessageUtil.stripAllColors(equippedEffect);
                strippedEffect = plugin.getEffectReversed(strippedEffect);
                Integer abilityId = EffectMaps.getEffectNumber(strippedEffect);

                if (abilityId == null) {
                    player.sendMessage(ChatColor.RED + "No valid ability found for the equipped effect.");
                    return true;
                }
                switch (abilityId) {
                    case 0, 1 -> Emerald.activateSpark(player);
                    case 2, 3 -> Feather.activateSpark(player);
                    case 4, 5 -> Fire.activateSpark(player);
                    case 6, 7 -> Frost.activateSpark(player);
                    case 8, 9 -> Haste.activateSpark(player);
                    case 10, 11 -> Heart.activateSpark(player);
                    case 12, 13 -> Invisibility.activateSpark(player);
                    case 14, 15 -> Ocean.activateSpark(player);
                    case 16, 17 -> Regen.activateSpark(player);
                    case 18, 19 -> Speed.activateSpark(player);
                    case 20, 21 -> Strength.activateSpark(player);
                    case 22, 23 -> Thunder.activateSpark(player);
                    case 24 -> Ender.activateSpark(player);
                    case 25 -> Apophis.activateSpark(player);
                    case 26 -> Ender.activateSpark(player);
                    case 27 -> Apophis.activateSpark(player);
                    case 28 -> Thief.activateSpark(player);
                    case 29 -> Thief.activateSpark(player);
                    default -> player.sendMessage(ChatColor.RED + "No valid ability found for the equipped effect.");
                }

                return true;
            }
        }
    }
}