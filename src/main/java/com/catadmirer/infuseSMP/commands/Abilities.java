package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.*;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Abilities implements CommandExecutor {
    private final Infuse plugin;

    public Abilities(Infuse plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        // Finding which slot to activate the spark for.
        String slot;
        if (label.contains("rspark")) {
            slot = "2";
        } else if (label.contains("lspark")) {
            slot = "1";
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid command.");
            return true;
        }

        // Getting the name of the equipped effect.
        EffectMapping equippedEffect = Infuse.getInstance().getEffectManager().getEffect(playerUUID, slot);

        // Handling if the slot is empty.
        if (equippedEffect == null) {
            String msg = plugin.getMessages().getString("slot_empty", "&cYou don't have any effect equipped in slot %slot%.").replace("%slot%", slot);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }
        
        switch (equippedEffect) {
            case EMERALD, AUG_EMERALD -> Emerald.activateSpark(player);
            case FEATHER, AUG_FEATHER -> Feather.activateSpark(player);
            case FIRE, AUG_FIRE -> Fire.activateSpark(player);
            case FROST, AUG_FROST -> Frost.activateSpark(player);
            case HASTE, AUG_HASTE -> Haste.activateSpark(player);
            case HEART, AUG_HEART -> Heart.activateSpark(player);
            case INVIS, AUG_INVIS -> Invisibility.activateSpark(player);
            case OCEAN, AUG_OCEAN -> Ocean.activateSpark(player);
            case REGEN, AUG_REGEN -> Regen.activateSpark(player);
            case SPEED, AUG_SPEED -> Speed.activateSpark(player);
            case STRENGTH, AUG_STRENGTH -> Strength.activateSpark(player);
            case THUNDER, AUG_THUNDER -> Thunder.activateSpark(player);
            case ENDER, AUG_ENDER -> Ender.activateSpark(player);
            case APOPHIS, AUG_APOPHIS -> Apophis.activateSpark(player);
            case THIEF, AUG_THIEF -> Thief.activateSpark(player);
        }

        return true;
    }

    public String stripAllColors(String input) {
        if (input == null) return null;
        Pattern pattern = Pattern.compile("(§#[0-9a-fA-F]{6})|(§x(§[0-9a-fA-F]){6})|(§[0-9a-fk-orA-FK-OR])");
        return pattern.matcher(input).replaceAll("");
    }
}