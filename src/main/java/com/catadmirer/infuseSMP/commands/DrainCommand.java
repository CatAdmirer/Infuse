package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.ApophisManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class DrainCommand implements CommandExecutor, Listener {
    private final Infuse plugin;
    private final ApophisManager apophisManager;

    public DrainCommand(Infuse plugin, ApophisManager apophisManager) {
        this.plugin = plugin;
        this.apophisManager = apophisManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.ERROR_NOT_PLAYER.toComponent());
            return true;
        }

        // Getting the slot to drain based on the command used.  Accepts /ldrain or /rdrain
        String slot;
        if (label.contains("ldrain")) slot = "1";
        else if (label.contains("rdrain")) slot = "2";
        else {
            player.sendMessage(Messages.WITHDRAW_INVALID.toComponent());
            return true;
        }

        // Getting the mapping from the slot
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);

        // Handling an invalid or empty mapping
        if (effect == null) {
            String msg = Messages.EFFECT_NONE_EQUIPPED.getMessage();
            msg = msg.replace("%slot%", slot);
            player.sendMessage(Messages.toComponent(msg));
            return true;
        }

        // Making sure the player has inventory space for the drained item.
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Messages.ERROR_INV_FULL.toComponent());
            return true;
        }
    
        // Resetting the player's health
        // TODO: Make this work better.  It will conflict with other health-managing plugins.
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);

        // Handling special apophis effects
        if (effect.getId() == EffectIds.APOPHIS) {
            plugin.getDataManager().removeEffect(player.getUniqueId(), slot);
            ItemStack glitchItem = effect.createItem();
            player.getInventory().addItem(glitchItem);
            apophisManager.unsetApophis(player);
            return true;
        }

        // Removing the effect from the player
        plugin.getDataManager().removeEffect(player.getUniqueId(), slot);
        String msg = Messages.DRAIN_SUCCESS.getMessage();
        msg = msg.replace("%effect_name%", effect.getName());
        player.sendMessage(Messages.toComponent(msg));

        // Giving the player the effect item.
        ItemStack item = effect.createItem();
        player.getInventory().addItem(item);

        return true;
    }
}