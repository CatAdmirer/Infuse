package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.ApophisManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DrainCommand implements CommandExecutor, Listener {
    private final Infuse plugin;

    private ApophisManager apophisCommand;

    public DrainCommand(Infuse plugin, ApophisManager apophisCommand) {
        this.plugin = plugin;
        this.apophisCommand = apophisCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        String slot;
        if (label.contains("rdrain")) {
            slot = "2";
        } else if (label.contains("ldrain")) {
            slot = "1";
        } else {
            String msg = plugin.getMessages().getString("withdraw_invalid",
                    "&cInvalid usage. Use /rdrain or /ldrain");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        EffectMapping effect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), slot);
        if (effect == null) {
            String msg = plugin.getMessages().getString("effect_none_equipped", "&cYou don't have an Effect equipped in slot %slot%.");
            msg = msg.replace("%slot%", slot);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        String effectName = effect.getName();

        if (ChatColor.stripColor(effectName).equalsIgnoreCase("Apohpis Effect") || ChatColor.stripColor(effectName).equalsIgnoreCase("Augmented Apohpis Effect")) {
            Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), slot);
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
            ItemStack glitchItem = effect.createItem();
            player.getInventory().addItem(glitchItem);
            apophisCommand.unsetApophis(Bukkit.getConsoleSender(), player.getName());
            return true;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cYour inventory is full! Make space before unequipping.");
            return true;
        }

        Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), slot);
        String currentEffectColored = applyHexColors(effectName);
        player.sendMessage("§aYou have drained your: " + currentEffectColored);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
                ItemStack glitchItem = effect.createItem();
                if (glitchItem != null) {
                    player.getInventory().addItem(glitchItem);
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
            String colorCode = ChatColor.of(hexCode).toString();
            matcher.appendReplacement(result, colorCode);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}