package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
import com.catadmirer.infuseSMP.util.MessageUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
            sender.sendMessage("§cInvalid Argument! Please use the tab completions as a reference");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
                if (!player.isOp()) {
                    sender.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }
                GUI.openSwordSelectionGUI(player);
                break;

            case "reload":
                if (!player.isOp()) {
                    sender.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }
                Infuse.getInstance().reloadDaConfig(player);
                break;
            case "recipes":
                Recipes.openGUI(player);
                break;
            case "giveeffect":
                if (!player.isOp()) {
                    sender.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }
                if (args.length != 3) {
                    sender.sendMessage("§cInvalid Argument! Please use /infuse giveEffect <Player> <aug_fire|ocean>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage("§cPlayer not found or not online.");
                    return true;
                }
                String effectKey = args[2].toLowerCase();
                if (EffectMaps.getEffectItem(effectKey) == null) {
                    player.sendMessage("§cInvalid Argument! Please use the tab completions as a reference");
                    return true;
                }
                target.getInventory().addItem(EffectMaps.getEffectItem(effectKey));
                String name = Infuse.getInstance().getEffectName(effectKey);
                String effectName = MessageUtil.stripAllColors(name);
                ChatColor color = EffectMaps.getColorEffect(effectKey);
                target.sendMessage(color + "You received the " + effectName);
                break;
            case "seteffect":
                if (!player.isOp()) {
                    sender.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }
                if (args.length != 4) {
                    sender.sendMessage("§cInvalid Argument! Please use /infuse setEffect <Player> <aug_fire|ocean> <1|2>");
                    return true;
                }
                Player namearg = Bukkit.getPlayer(args[1]);
                if (namearg == null || !namearg.isOnline()) {
                    sender.sendMessage("§cPlayer not found or not online.");
                    return true;
                }
                String effectky = args[2].toLowerCase();
                if (EffectMaps.getEffectItem(effectky) == null) {
                    player.sendMessage("§cInvalid Argument! Please use the tab completions as a reference");
                    return true;
                }
                String effect = Infuse.getInstance().getEffectName(effectky);
                Infuse.getInstance().getEffectManager().setEffect(namearg.getUniqueId(), args[3], effect);
                break;
            case "cleareffect":
                if (!player.isOp()) {
                    sender.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage("§cInvalid Argument! Please use /infuse clearEffect <Player>");
                    return true;
                }
                Player arg1 = Bukkit.getPlayer(args[1]);
                String arg1Name = arg1.getName();
                if (arg1 == null || !arg1.isOnline()) {
                    sender.sendMessage("§cPlayer not found or not online.");
                    return true;
                }
                Infuse.getInstance().getEffectManager().removeEffect(arg1.getUniqueId(), "2");
                Infuse.getInstance().getEffectManager().removeEffect(arg1.getUniqueId(), "1");
                player.sendMessage(ChatColor.GREEN + "Cleared " + arg1Name + "'s effects");
                break;
            case "cooldown":
                if (!player.isOp()) {
                    sender.sendMessage("§cYou must be OP to run this command.");
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage("§cInvalid Argument! Please use /infuse Cooldown <Player>");
                    return true;
                }
                Player arg3 = Bukkit.getPlayer(args[1]);
                String arg1meow = arg3.getName();
                if (arg3 == null || !arg3.isOnline()) {
                    sender.sendMessage("§cPlayer not found or not online.");
                    return true;
                }
                CooldownManager.removeAllCooldowns(arg3.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Cleared " + arg1meow + "'s cooldown");
                break;
            case "controls":
                if (args.length != 2) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse controls <Offhand|Command>");
                    return true;
                }
                String choice = args[1];
                if (!choice.equalsIgnoreCase("Offhand") && !choice.equalsIgnoreCase("Command")) {
                    player.sendMessage("§cInvalid Argument! Please use /infuse controls <Offhand|Command>");
                    return true;
                }

                Infuse.getInstance().getEffectManager().setControlDefault(player.getUniqueId(), choice);

                boolean offhandEnabled = choice.equalsIgnoreCase("Offhand");
                player.addAttachment(Infuse.getInstance(), "ability.use", !offhandEnabled);

                player.sendMessage("§4Your controls are now " + choice);
                break;
            default:
                sender.sendMessage("§cPlease take reference to the tab completions they took me time :(");
                break;
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (!player.isOp()) {
                completions.addAll(Arrays.asList("Recipes", "Controls"));
            } else {
                completions.addAll(Arrays.asList("GUI", "Reload", "Recipes", "Controls", "giveEffect", "setEffect", "clearEffect", "Cooldown"));
            }
            completions = completions.stream()
                    .filter(opt -> opt.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("controls")) {
            completions.addAll(Arrays.asList("Offhand", "Command"));
            completions = completions.stream()
                    .filter(opt -> opt.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("giveEffect")) {
            if (player.isOp()) {
                completions.addAll(
                        Bukkit.getOnlinePlayers().stream()
                                .map(player2 -> player2.getName())
                                .toList()
                );

                completions = completions.stream()
                        .filter(opt -> opt.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setEffect")) {
            if (player.isOp()) {
                completions.addAll(
                        Bukkit.getOnlinePlayers().stream()
                                .map(player2 -> player2.getName())
                                .toList()
                );

                completions = completions.stream()
                        .filter(opt -> opt.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("clearEffect")) {
            if (player.isOp()) {
                completions.addAll(
                        Bukkit.getOnlinePlayers().stream()
                                .map(player2 -> player2.getName())
                                .toList()
                );

                completions = completions.stream()
                        .filter(opt -> opt.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("Cooldown")) {
            if (player.isOp()) {
                completions.addAll(
                        Bukkit.getOnlinePlayers().stream()
                                .map(player2 -> player2.getName())
                                .toList()
                );

                completions = completions.stream()
                        .filter(opt -> opt.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("giveEffect")) {
            if (player.isOp()) {
                completions.addAll(EffectMaps.color.keySet().stream()
                        .filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase()))
                        .toList());
            }
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("setEffect")) {
            if (player.isOp()) {
                completions.addAll(EffectMaps.color.keySet().stream()
                        .filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase()))
                        .toList());
            }
        }
        else if (args.length == 4 && args[0].equalsIgnoreCase("setEffect")) {
            if (player.isOp()) {
                completions.addAll(Arrays.asList("1", "2"));
            }
        }

        return completions;
    }
}