package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.ApophisManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrainCommand implements CommandExecutor, Listener {
    private final Infuse plugin;

    private ApophisManager aphopisCommand;

    public DrainCommand(Infuse plugin, ApophisManager aphopisCommand) {
        this.plugin = plugin;
        this.aphopisCommand = aphopisCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        String hackType;
        if (label.contains("rdrain")) {
            hackType = "2";
        } else if (label.contains("ldrain")) {
            hackType = "1";
        } else {
            String msg = plugin.getMessages().getString("withdraw_invalid",
                    "&cInvalid usage. Use /rdrain or /ldrain");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        final String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), hackType);
        if (currentHack == null) {
            String msg = plugin.getMessages().getString("effect_none_equipped",
                    "&cYou don't have an Effect equipped in slot %slot%.");
            msg = msg.replace("%slot%", hackType);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        if (ChatColor.stripColor(currentHack).equalsIgnoreCase("Apohpis Effect")
                || ChatColor.stripColor(currentHack).equalsIgnoreCase("Augmented Apohpis Effect")) {
            Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), hackType);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0D);
            EffectMapping mapping = EffectMapping.fromEffectName(currentHack);
            ItemStack glitchItem = mapping.createItem();
            player.getInventory().addItem(glitchItem);
            aphopisCommand.unsetAphopis(Bukkit.getConsoleSender(), player.getName());
            return true;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Your inventory is full! Make space before unequipping.");
            return true;
        }

        Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), hackType);
        String currentHackColored = applyHexColors(currentHack);
        player.sendMessage(ChatColor.GREEN + "You have drained your: " + currentHackColored);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0D);
                EffectMapping mapping = EffectMapping.fromEffectName(currentHack);
                if (mapping != null) {
                    ItemStack glitchItem = mapping.createItem();
                    if (glitchItem != null) {
                        player.getInventory().addItem(glitchItem);
                    }
                }
            }
        }.runTaskLater(this.plugin, 10L);

        return true;
    }


    public static String applyHexColors(String input) {
        String regex = "(#(?:[0-9a-fA-F]{6}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String colorCode = net.md_5.bungee.api.ChatColor.of(hexCode).toString();
            matcher.appendReplacement(result, colorCode);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}

