package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.managers.EffectMaps;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class InfuseCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cInvalid Argument! Please use the tab completions as a reference");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
                if (!player.isOp()) {
                    player.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }

                GUI.openSwordSelectionGUI(player);
                break;

            case "reload":
                if (!player.isOp()) {
                    player.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }

                Infuse.getInstance().reloadDaConfig(player);
                break;
            case "recipes":
                Recipes.openGUI(player);
                break;
            case "giveeffect":
                if (!player.isOp()) {
                    player.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }

                if (args.length != 3) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse giveEffect <Player> <aug_fire|ocean>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§cPlayer not found or not online.");
                    return true;
                }

                String effectKey = args[2].toLowerCase();
                EffectMapping mapping = EffectMapping.fromEffectKey(effectKey);
                if (mapping == null) {
                    player.sendMessage("§cInvalid Argument! Please use the tab completions as a reference");
                    return true;
                }

                target.getInventory().addItem(mapping.createItem());
                String name = mapping.getEffectName();
                String effectName = Infuse.getInstance().stripAllColors(name);
                ChatColor color = EffectMaps.getColorEffect(effectKey);
                target.sendMessage(color + "You received the " + effectName);
                break;
            case "seteffect":
                if (!player.isOp()) {
                    player.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }
                
                if (args.length != 4) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse setEffect <player> <aug_fire|ocean> <1|2>");
                    return true;
                }
                
                // Getting the player and making sure they are online.
                target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§cInvalid Argument!  Could not find a player named \"" + args[1] + "\".  Make sure they are online.");
                    return true;
                }
                
                // Getting the effect key and verifying its integrity.
                effectKey = args[2].toLowerCase();
                mapping = EffectMapping.fromEffectKey(effectKey);
                if (mapping == null) {
                    player.sendMessage("§cInvalid Argument! Please use the tab completions as a reference.");
                    return true;
                }
                
                // Getting the string to put in storage.
                String effect = mapping.getEffectName();

                // Getting the slot to put the effect into and validating it.
                String slot = args[3];
                if (!slot.equals("1") && !slot.equals("2")) {
                    player.sendMessage("§cInvalid Argument! Could not identify slot " + slot + ".  Please use \"1\" or \"2\".");
                    return true;
                }

                // Setting the effect
                Infuse.getInstance().getEffectManager().setEffect(target.getUniqueId(), args[3], effect);
                player.sendMessage("§aSuccessfully set the effect in slot " + slot + " of player " + target.getName() + " to " + effectKey + ".");
                break;
            case "cleareffect":
                if (!player.isOp()) {
                    player.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse clearEffect <player>");
                    return true;
                }

                // Getting the player and making sure they are online
                target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§cPlayer not found or not online.");
                    return true;
                }

                // Removing the effects from the player
                Infuse.getInstance().getEffectManager().removeEffect(target.getUniqueId(), "1");
                Infuse.getInstance().getEffectManager().removeEffect(target.getUniqueId(), "2");
                player.sendMessage(ChatColor.GREEN + "Cleared " + target.getName() + "'s effects");
                break;
            case "cooldown":
                if (!player.isOp()) {
                    player.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse cooldown <Player>");
                    return true;
                }
                
                // Getting the player and making sure they are online
                target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§cPlayer not found or not online.");
                    return true;
                }

                // Removing cooldowns from the player
                CooldownManager.removeAllCooldowns(target.getUniqueId());
                player.sendMessage("§aRemoved " + target.getName() + "'s cooldown");
                break;
            case "controls":
                if (args.length != 2) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse controls <offhand|command>");
                    return true;
                }

                // Getting the control mode and validating the input.
                String choice = args[1].toLowerCase();
                if (!choice.equals("offhand") && !choice.equals("command")) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse controls <offhand|command>");
                    return true;
                }

                // Setting the control mode for the user.
                Infuse.getInstance().getEffectManager().setControlDefault(player.getUniqueId(), choice);

                // Assigning the permission for offhand use if the user chose offhand mode
                boolean offhandEnabled = choice.equalsIgnoreCase("offhand");
                player.addAttachment(Infuse.getInstance(), "ability.use", !offhandEnabled);

                player.sendMessage("§4Your controls are now " + choice);
                break;
            default:
                sender.sendMessage("§cPlease use the tab completions as a reference.");
                break;
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Only tabcompleting for players
        if (!(sender instanceof Player player)) {
            return Arrays.asList();
        }

        if (args.length == 1) {
            List<String> completions = Arrays.asList("recipes", "controls");
            
            if (player.isOp()) {
                completions.addAll(Arrays.asList("gui", "reload", "giveEffect", "setEffect", "clearEffect", "cooldown"));
            }

            return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).sorted().toList();
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "controls":
                    return Stream.of("offhand", "command").filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).toList();
                case "giveeffect":
                case "seteffect":
                case "cleareffect":
                case "cooldown":
                    if (!player.isOp()) return Arrays.asList();
                    return Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).toList();
            }
        }
        
        if (args.length == 3) {
            switch(args[0].toLowerCase()) {
                case "giveEffect":
                case "setEffect":
                    if (!player.isOp()) return Arrays.asList();
                    return EffectMaps.color.keySet().stream().filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase())).toList();
            }
        }
        
        if (args.length == 4 && args[0].equalsIgnoreCase("setEffect") && player.isOp()) {
            return Stream.of("1", "2").filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        }

        return Arrays.asList();
    }
}