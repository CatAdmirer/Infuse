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
    private final Emerald emeraldAbility;
    private final Feather featherAbility;
    private final Fire fireAbility;
    private final Frost frostAbility;
    private final Haste hasteAbility;
    private final Heart heartAbility;
    private final Invisibility invisibilityAbility;
    private final Ocean oceanAbility;
    private final Regen regenAbility;
    private final Speed speedAbility;
    private final Strength strengthAbility;
    private final Thunder thunderAbility;
    private final Ender enderAbility;
    private final Apophis apophis;
    private final Thief thiefAbility;

    public Abilities(DataManager dataManager, Infuse plugin) {
        this.plugin = plugin;
        this.emeraldAbility = new Emerald(plugin);
        this.featherAbility = new Feather(plugin, dataManager);
        this.fireAbility = new Fire(plugin);
        this.frostAbility = new Frost(dataManager, plugin);
        this.hasteAbility = new Haste(plugin);
        this.heartAbility = new Heart(plugin);
        this.invisibilityAbility = new Invisibility(plugin, dataManager);
        this.oceanAbility = new Ocean(plugin, dataManager);
        this.regenAbility = new Regen(plugin);
        this.speedAbility = new Speed(plugin);
        this.strengthAbility = new Strength(plugin);
        this.thunderAbility = new Thunder(plugin, dataManager);
        this.enderAbility = new Ender(dataManager, plugin);
        this.apophis = new Apophis(plugin);
        this.thiefAbility = new Thief(dataManager, plugin);
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
                    case 0:
                    case 1:
                        this.emeraldAbility.activateSpark(player);
                        break;
                    case 2:
                    case 3:
                        this.featherAbility.activateSpark(player);
                        break;
                    case 4:
                    case 5:
                        this.fireAbility.activateSpark(player);
                        break;
                    case 6:
                    case 7:
                        this.frostAbility.activateSpark(player);
                        break;
                    case 8:
                    case 9:
                        this.hasteAbility.activateSpark(player);
                        break;
                    case 10:
                    case 11:
                        this.heartAbility.activateSpark(player);
                        break;
                    case 12:
                    case 13:
                        this.invisibilityAbility.activateSpark(player);
                        break;
                    case 14:
                    case 15:
                        this.oceanAbility.activateSpark(player);
                        break;
                    case 16:
                    case 17:
                        this.regenAbility.activateSpark(player);
                        break;
                    case 18:
                    case 19:
                        this.speedAbility.activateSpark(player);
                        break;
                    case 20:
                    case 21:
                        this.strengthAbility.activateSpark(player);
                        break;
                    case 22:
                    case 23:
                        this.thunderAbility.activateSpark(player);
                        break;
                    case 24:
                        this.enderAbility.activateSpark(player);
                        break;
                    case 25:
                        this.apophis.activateSpark(player);
                        break;
                    case 26:
                        this.enderAbility.activateSpark(player);
                        break;
                    case 27:
                        this.apophis.activateSpark(player);
                        break;
                    case 28:
                        this.thiefAbility.activateSpark(player);
                        break;
                    case 29:
                        this.thiefAbility.activateSpark(player);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "No valid ability found for the equipped effect.");
                }

                return true;
            }
        }
    }
}