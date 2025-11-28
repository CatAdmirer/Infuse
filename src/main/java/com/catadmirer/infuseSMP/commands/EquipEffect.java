package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.ApophisManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EquipEffect implements Listener, CommandExecutor {
    private ApophisManager apophisManager;

    public EquipEffect(ApophisManager apophisManager) {
        this.apophisManager = apophisManager;
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't been given already
        if (!player.hasPlayedBefore() && Infuse.getInstance().<Boolean>getConfig("join_effects_enabled")) {
            List<String> effects = Infuse.getInstance().getConfig("join_effects");
            if (effects.isEmpty()) return;
            String chosenKey = effects.get(new Random().nextInt(effects.size()));
            String effectName = Infuse.getInstance().getEffect(chosenKey);
            if (effectName == null) return;
            equipEffect(player, effectName, "2");
        }
    }

    /**
     * Equips an effect in the primary or secondary slot.
     * If both slots are full, it drains the secondary slot and equips the new effect there.
     * 
     * @param player The player who will get the effect
     * @param effectName The effect to give the player
     */
    public void safeEquip(Player player, String effectName) {
        if (!this.equipEffect(player, effectName, "1") && !this.equipEffect(player, effectName, "2")) {
            player.performCommand("rdrain");
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugins()[0], () -> {
                this.equipEffect(player, effectName, "2");
            }, 1L);
        }
    }

    /**
     * Equips an effect in the specified slot.
     * 
     * @param player The player who will get the effect
     * @param effectName The effect to give the player.
     * @param slot The slot to equip the effect into.
     * 
     * @return Returns false if the slot is already taken.
     */
    private boolean equipEffect(Player player, String effectName, String slot) {
        // Checking for an effect in the slot.
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), slot);
        if (currentEffect != null) {
            return false;
        }
        
        // Equipping the effect to the slot.
        Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), slot, effectName);
        player.sendMessage(ChatColor.GREEN + "You have equipped " + effectName);
        this.consumeMainHandItem(player);
        return true;
    }

    /**
     * Converts strings with hex colors to mojang-compliant messages.
     * 
     * @param input The string to modify.
     * 
     * @return A minecraft message that uses the hex code(s) specified
     */
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

    /**
     * Handling when players drink an infuse potion.
     * 
     * @param event The consume event.
     */
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        // Ignoring if the player is not holding a potion
        if (mainHandItem.getType() == Material.POTION) return;

        // Getting the effect from the item
        EffectMapping effect = EffectMapping.fromItem(mainHandItem);

        // Skipping if the effect is not found.
        if (effect == null) return;

        // Skipping if the plauer's inventory is full.
        if (player.getInventory().firstEmpty() == -1) {
            event.setCancelled(true);
            player.sendMessage(String.valueOf(ChatColor.RED) + "Your inventory is full! Make space before unequipping.");
            return;
        }
         
        // Equipping the effect
        this.safeEquip(player, effect.getEffectName());
        this.consumeMainHandItem(player);
        String effectName = effect.getEffectName();

        // Performing special logic for the apophis effect.
        if (effectName.equalsIgnoreCase(ChatColor.DARK_PURPLE + "Apohpis Effect") ||
                effectName.equalsIgnoreCase(ChatColor.DARK_PURPLE + "Augmented Apohpis Effect")) {
            apophisManager.disguiseAsApophis(player);
        }
    }

    /**
     * Removes once item from the player's main hand.
     * 
     * @param player The player to take an item from.
     */
    private void consumeMainHandItem(Player player) {
        Bukkit.getScheduler().runTaskLater(Infuse.getInstance(), () -> {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (mainHandItem.getType() != Material.AIR) {
                if (mainHandItem.getAmount() > 1) {
                    mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand((ItemStack)null);
                }
            }
        }, 1L);
    }

    /**
     * Event handler to remove an effect from the players inventory if they die.
     * 
     * @param event The server PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String effect1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
        String effect2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
        String dropMode = Infuse.getInstance().getConfig().getString("effect_drops", "random");
        Random rand = new Random();
        switch (dropMode.toLowerCase()) {
            case "1":
                if (effect1 != null) {
                    this.dropEffectOnDeath(player, "1");
                }
                break;

            case "2":
                if (effect2 != null) {
                    this.dropEffectOnDeath(player, "2");
                }
                break;

            case "none":
                break;

            case "random":
            default:
                if (effect1 != null && effect2 != null) {
                    String selectedEffect = rand.nextBoolean() ? "1" : "2";
                    this.dropEffectOnDeath(player, selectedEffect);
                } else if (effect1 != null) {
                    this.dropEffectOnDeath(player, "1");
                } else if (effect2 != null) {
                    this.dropEffectOnDeath(player, "2");
                }
                break;
        }

        File disguiseFile = new File(
                Infuse.getInstance().getDataFolder(),
                "AphopisPlayers/" + player.getUniqueId() + ".yml"
        );

        // Removing the player's apophis disguise file if it exists.
        if (disguiseFile.exists()) {
            disguiseFile.delete();
            Infuse.getInstance().resetSkinWithoutKick(player);
        }
    }

    /**
     * Disguising players who join that have the apophis effect.
     * 
     * @param event The server PlayerJoinEvent to catch.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File disguiseFile = new File(Infuse.getInstance().getDataFolder(), "AphopisPlayers/" + player.getUniqueId() + ".yml");
        if (disguiseFile.exists()) {
            apophisManager.disguiseAsApophis(player);
        }
    }

    /**
     * Removes a player's effect from the specified slot and drops it on the ground.
     * 
     * @param player The player to remove an effect from.
     * @param slot The slot to remove the effect from.
     */
    private void dropEffectOnDeath(Player player, String slot) {
        // Getting the equipped effect from the data file.
        String effectName = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), slot);
        if (effectName == null) return;

        // Removing the effect from the player.
        Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), slot);

        // Getting the EffectMapping reference
        EffectMapping effect = EffectMapping.fromEffectName(effectName);
        if (effect == null) return;

        // Dropping the effect item at the player's location
        player.getWorld().dropItemNaturally(player.getLocation(), effect.createItem());
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "Only players can use this command.");
            return true;
        }

        // Getting the equipped effects
        String effect1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
        String effect2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");

        // Erroring out if the player doesn't have any effects equipped
        if (effect1 == null && effect2 == null) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "You do not have any effects equipped to swap.");
            return true;
        }

        // Swapping the effects
        Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), "1", effect2);
        Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), "2", effect1);
        player.sendMessage(String.valueOf(ChatColor.GREEN) + "Your Effects have been swapped.");
        return true;
    }
}
