package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.ApophisManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
            sender.sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
            return true;
        }

        // Getting the slot to drain based on the command used.  Accepts /ldrain or /rdrain
        String slot;
        if (label.contains("ldrain")) slot = "1";
        else if (label.contains("rdrain")) slot = "2";
        else {
            player.sendMessage(new Message(MessageType.WITHDRAW_INVALID).toComponent());
            return true;
        }

        // Getting the mapping from the slot
        EffectMapping effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);

        // Handling an invalid or empty mapping
        if (effect == null) {
            Message msg = new Message(MessageType.EFFECT_NONE_EQUIPPED);
            msg.applyPlaceholder("slot", slot);
            player.sendMessage(msg.toComponent());
            return true;
        }

        // Making sure the player has inventory space for the drained item.
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(new Message(MessageType.ERROR_INV_FULL).toComponent());
            return true;
        }

        // Removing the effect from the player
        plugin.getDataManager().removeEffect(player.getUniqueId(), slot);
        Message msg = new Message(MessageType.DRAIN_SUCCESS);
        msg.applyPlaceholder("effect_name", effect.getName());
        player.sendMessage(msg.toComponent());

        // Giving the player the effect item.
        player.getInventory().addItem(effect.createItem());

        // Handling special apophis effects
        if (effect == EffectMapping.APOPHIS || effect == EffectMapping.AUG_APOPHIS) {
            apophisManager.unsetApophis(player);
        }
        return true;
    }
}