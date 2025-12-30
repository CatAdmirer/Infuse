package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.Infuse;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.catadmirer.infuseSMP.Managers.ApophisManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EquipEffect implements Listener {
    private ApophisManager apophisCommand;

    public EquipEffect(ApophisManager apophisCommand) {
        this.apophisCommand = apophisCommand;
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore() && Infuse.getInstance().<Boolean>getConfig("join_effects_enabled")) {
            List<String> effects = Infuse.getInstance().getConfig("join_effects");
            if (effects.isEmpty()) return;
            String effectKey = effects.get(new Random().nextInt(effects.size()));
            equipEffect(player, EffectMapping.fromEffectKey(effectKey), "2");
        }
    }

    /**
     * Forces an effect to be equipped.
     * Attempts to equip the effect normally, but will drain the secondary effect if the player has two effects equipped already.
     * 
     * @param player The player to equip an effect for.
     * @param effect The effect to equip.
     */
    public void forceEquipEffect(Player player, EffectMapping effect) {
        if (!equipEffect(player, effect, "1") && !equipEffect(player, effect, "2")) {
            player.performCommand("rdrain");
            equipEffect(player, effect, "2");
        }
    }

    private boolean equipEffect(Player player, EffectMapping effect, String slot) {
        EffectMapping currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), slot);
        if (currentEffect != null) return false;

        Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), slot, effect);

        String effectName = effect.getName();
        effectName = applyHexColors(effectName);
        player.sendMessage("§aYou have equipped " + effectName);
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

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack effectItem = event.getItem();
        if (effectItem != null && effectItem.getType() != Material.AIR) {
            EffectMapping effect = EffectMapping.fromItem(effectItem);
            if (effect != null) {
                if (player.getInventory().firstEmpty() == -1) {
                    event.setCancelled(true);
                    player.sendMessage("§cYour inventory is full! Make space before unequipping.");
                } else {
                    this.forceEquipEffect(player, effect);
                    effectItem.subtract(1);
                    String effectName = effect.getKey();
                    if (effectName.equalsIgnoreCase("§5Apohpis Effect") ||
                            effectName.equalsIgnoreCase("§5Augmented Apohpis Effect")) {
                        apophisCommand.disguiseAsApophis(player);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EffectMapping effect1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
        EffectMapping effect2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
        String dropMode = Infuse.getInstance().getConfig().getString("effect_drops", "random");
        switch (dropMode.toLowerCase()) {
            case "1":
                if (effect1 != null) event.getDrops().add(effect1.createItem());
                break;
            case "2":
                if (effect2 != null) event.getDrops().add(effect2.createItem());;
                break;
            case "none":
                break;
            case "random":
                if (effect1 != null && effect2 != null) {
                    EffectMapping selected = new Random().nextBoolean() ? effect1 : effect2;
                    event.getDrops().add(selected.createItem());
                } else if (effect1 != null) {
                    event.getDrops().add(effect1.createItem());
                } else if (effect2 != null) {
                    event.getDrops().add(effect2.createItem());
                }
                break;
            default:
                // TODO: Log about invalid config option
                return;
        }

        // Removing the apophis disguise file if it exists
        File disguiseFile = new File(Infuse.getInstance().getDataFolder(), "AphopisPlayers/" + player.getUniqueId() + ".yml");

        if (disguiseFile.exists()) {
            disguiseFile.delete();
            Infuse.getInstance().resetSkinWithoutKick(player);
        }
    }

    @EventHandler
    public void apophisDisguiseOnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File disguiseFile = new File(Infuse.getInstance().getDataFolder(), "AphopisPlayers/" + player.getUniqueId() + ".yml");
        if (disguiseFile.exists()) {
            apophisCommand.disguiseAsApophis(player);
        }
    }

    
}
