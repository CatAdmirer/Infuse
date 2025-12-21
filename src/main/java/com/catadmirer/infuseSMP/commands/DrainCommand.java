package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
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

public class DrainCommand implements CommandExecutor, Listener {
    private ApophisManager apophisManager;

    public DrainCommand(ApophisManager apophisManager) {
        this.apophisManager = apophisManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Getting the slot to drain based on the command used.  Accepts /ldrain or /rdrain
        String slot;
        if (label.contains("ldrain")) slot = "1";
        else if (label.contains("rdrain")) slot = "2";
        else {
            String msg = Messages.getMessage(Messages.WITHDRAW_INVALID);
            player.sendMessage(Messages.toComponent(msg));
            return true;
        }

        // Getting the mapping from the slot
        EffectMapping effect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), slot);

        // Handling an invalid or empty mapping
        if (effect == null) {
            String msg = Messages.EFFECT_NONE_EQUIPPED.getMessage();
            msg = msg.replace("%slot%", slot);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        // Making sure the player has inventory space for the drained item.
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cYour inventory is full! Make space before unequipping.");
            return true;
        }
    
        // Resetting the player's health
        // TODO: Make this work better.  It will conflict with other health-managing plugins.
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);

        // Handling special apophis effects
        if (effect == EffectMapping.APOPHIS || effect == EffectMapping.AUG_APOPHIS) {
            Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), slot);
            ItemStack glitchItem = effect.createItem();
            player.getInventory().addItem(glitchItem);
            apophisManager.unsetApophis(Bukkit.getConsoleSender(), player.getName());
            return true;
        }

        // Removing the effect from the player
        Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), slot);
        String currentEffectColored = applyHexColors(effect.getName());
        player.sendMessage("§aYou have drained your: " + currentEffectColored);

        // Giving the player the effect item.
        ItemStack item = effect.createItem();
        player.getInventory().addItem(item);

        return true;
    }

    // TODO: Remove this in favor of components
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