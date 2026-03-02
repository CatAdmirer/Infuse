package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.inventories.EffectChooser;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class InfuseCommand implements CommandExecutor, TabCompleter {
    private final Infuse plugin;
    
    public InfuseCommand(Infuse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Messages.INFUSE_INVALID_PARAM.toComponent());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
                if (!player.isOp()) {
                    player.sendMessage(Messages.ERROR_NOT_OP.toComponent());
                    return true;
                }

                player.openInventory(new EffectChooser(plugin).getInventory());
                break;

            case "reload":
                if (!player.isOp()) {
                    player.sendMessage(Messages.ERROR_NOT_OP.toComponent());
                    return true;
                }

                plugin.getMainConfig().load();
                Messages.load(plugin);
                player.sendMessage("Infuse configs reloaded");
                break;
            case "recipes":
                Recipes.openGUI(player);
                break;
            case "giveeffect":
                if (!player.isOp()) {
                    player.sendMessage(Messages.ERROR_NOT_OP.toComponent());
                    return true;
                }

                if (args.length != 3) {
                    player.sendMessage(Messages.INFUSE_GIVEEFFECT_USAGE.toComponent());
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(Messages.ERROR_TARGET_NOT_FOUND.toComponent());
                    return true;
                }

                String effectKey = args[2].toLowerCase();
                EffectMapping mapping = EffectMapping.fromEffectKey(effectKey);
                if (mapping == null) {
                    player.sendMessage(Messages.INFUSE_INVALID_PARAM.toComponent());
                    return true;
                }

                target.getInventory().addItem(mapping.createItem());

                String msg = Messages.INFUSE_GIVEEFFECT_SUCCESS.getMessage();
                msg = msg.replace("%effect_color%", "<#" + Integer.toHexString(mapping.getColor().getRGB() & 0xffffff) + ">");
                msg = msg.replace("%effect_name%", mapping.getName());
                target.sendMessage(Messages.toComponent(msg));
                break;
            case "seteffect":
                if (!player.isOp()) {
                    player.sendMessage(Messages.ERROR_NOT_OP.toComponent());
                    return true;
                }
                
                if (args.length != 4) {
                    player.sendMessage(Messages.INFUSE_SETEFFECT_USAGE.toComponent());
                    return true;
                }
                
                // Getting the player and making sure they are online.
                target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(Messages.ERROR_TARGET_NOT_FOUND.toComponent());
                    return true;
                }
                
                // Getting the effect key and verifying its integrity.
                effectKey = args[2].toLowerCase();
                mapping = EffectMapping.fromEffectKey(effectKey);
                if (mapping == null) {
                    player.sendMessage(Messages.INFUSE_INVALID_PARAM.toComponent());
                    return true;
                }
                
                // Getting the slot to put the effect into and validating it.
                String slot = args[3];
                if (!slot.equals("1") && !slot.equals("2")) {
                    msg = Messages.INFUSE_INVALID_SLOT.getMessage();
                    msg = msg.replace("%slot%", slot);
                    player.sendMessage(Messages.toComponent(msg));
                    return true;
                }

                // Setting the effect
                plugin.getDataManager().setEffect(target.getUniqueId(), args[3], mapping);
                msg = Messages.INFUSE_SETEFFECT_SUCCESS.getMessage();
                msg = msg.replace("%slot%", slot);
                msg = msg.replace("%player_name%", target.getName());
                msg = msg.replace("%effect_name%", mapping.getName());
                player.sendMessage(Messages.toComponent(msg));
                break;
            case "cleareffect":
                if (!player.isOp()) {
                    player.sendMessage(Messages.ERROR_NOT_OP.toComponent());
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage(Messages.INFUSE_CLEAREFFECT_USAGE.toComponent());
                    return true;
                }

                // Getting the player and making sure they are online
                target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(Messages.ERROR_TARGET_NOT_FOUND.toComponent());
                    return true;
                }

                // Removing the effects from the player
                plugin.getDataManager().removeEffect(target.getUniqueId(), "1");
                plugin.getDataManager().removeEffect(target.getUniqueId(), "2");
                msg = Messages.INFUSE_CLEAREFFECT_SUCCESS.getMessage();
                msg = msg.replace("%player_name%", target.getName());
                player.sendMessage(Messages.toComponent(msg));
                break;
            case "cooldown":
                if (!player.isOp()) {
                    player.sendMessage(Messages.ERROR_NOT_OP.toComponent());
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage(Messages.INFUSE_COOLDOWN_USAGE.toComponent());
                    return true;
                }
                
                // Getting the player and making sure they are online
                target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(Messages.ERROR_TARGET_NOT_FOUND.toComponent());
                    return true;
                }

                // Removing cooldowns from the player
                CooldownManager.removeAllCooldowns(target.getUniqueId());
                msg = Messages.INFUSE_COOLDOWN_SUCCESS.getMessage();
                msg = msg.replace("%player_name%", target.getName());
                player.sendMessage(Messages.toComponent(msg));
                break;
            case "controls":
                if (args.length != 2) {
                    player.sendMessage(Messages.INFUSE_CONTROLS_USAGE.toComponent());
                    return true;
                }

                // Getting the control mode and validating the input.
                String choice = args[1].toLowerCase();
                if (!choice.equals("offhand") && !choice.equals("command")) {
                    player.sendMessage(Messages.INFUSE_CONTROLS_USAGE.toComponent());
                    return true;
                }

                // Setting the control mode for the user.
                plugin.getDataManager().setControlMode(player.getUniqueId(), choice);

                // Assigning the permission for offhand use if the user chose offhand mode
                boolean offhandEnabled = choice.equalsIgnoreCase("offhand");
                player.addAttachment(plugin, "ability.use", !offhandEnabled);

                msg = Messages.INFUSE_CONTROLS_SUCCESS.getMessage();
                msg = msg.replace("%controlMode%", choice);
                player.sendMessage(Messages.toComponent(msg));
                break;
            default:
                sender.sendMessage(Messages.INFUSE_INVALID_PARAM.toComponent());
                break;
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("recipes", "controls"));
            
            if (sender.isOp()) {
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
                    if (!sender.isOp()) return Arrays.asList();
                    return Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).toList();
            }
        }
        
        if (args.length == 3) {
            switch(args[0].toLowerCase()) {
                case "giveEffect":
                case "setEffect":
                    if (!sender.isOp()) return Arrays.asList();
                    return Stream.of(EffectMapping.values()).map(EffectMapping::getKey).filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase())).toList();
            }
        }
        
        if (args.length == 4 && args[0].equalsIgnoreCase("setEffect") && sender.isOp()) {
            return Stream.of("1", "2").filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        }

        return List.of();
    }
}