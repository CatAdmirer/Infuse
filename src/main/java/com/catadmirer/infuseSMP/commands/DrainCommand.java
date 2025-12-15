package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
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
    private final Infuse plugin;

    private ApophisManager apophisManager;

    public DrainCommand(Infuse plugin, ApophisManager apophisManager) {
        this.plugin = plugin;
        this.apophisManager = apophisManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Determining the slot the player is draining the effect from.
        String slot;
        if (label.contains("rdrain")) {
            slot = "2";
        } else if (label.contains("ldrain")) {
            slot = "1";
        } else {
            String msg = plugin.getMessages().getString("withdraw_invalid", "&cInvalid usage. Use /rdrain or /ldrain");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        // Getting the effect from the player
        final String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), slot);
        if (currentEffect == null) {
            String msg = plugin.getMessages().getString("effect_none_equipped", "&cYou don't have an Effect equipped in slot %slot%.");
            msg = msg.replace("%slot%", slot);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        // Cancelling the drain if the player's inventory is full
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Your inventory is full! Make space before unequipping.");
            return true;
        }

        // Doing special stuff for the apophis effect.
        if (ChatColor.stripColor(currentEffect).equalsIgnoreCase("Apohpis Effect") || ChatColor.stripColor(currentEffect).equalsIgnoreCase("Augmented Apohpis Effect")) {
        }

        // Removing the effect
        Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), slot);
        String currentEffectColored = applyHexColors(currentEffect);
        player.sendMessage(ChatColor.GREEN + "You have drained your: " + currentEffectColored);

        // TODO: Why even do this?  Is there an issue without the scheduler?
        player.getScheduler().runDelayed(plugin, task -> {
            // Skipping if the task was cancelled.
            if (task.isCancelled()) return;

            // Resetting the player's max health
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);

            // Getting the drained effect.
            EffectMapping mapping = EffectMapping.fromEffectName(currentEffect);
            if (mapping != null) {
                ItemStack effectItem = mapping.createItem();

                // Doing special stuff for the apophis effect
                if (Apophis.isEffect(effectItem)) {
                    apophisManager.unsetApophis(Bukkit.getConsoleSender(), player.getName());
                }

                // Adding the item to the player's inventory
                player.getInventory().addItem(effectItem);
            }
        }, null, 10);

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