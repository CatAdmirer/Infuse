package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.ApophisManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.io.File;
import java.util.List;
import java.util.Random;
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
    private final Infuse plugin;
    private final ApophisManager apophisManager;

    public EquipEffect(Infuse plugin, ApophisManager apophisManager) {
        this.plugin = plugin;
        this.apophisManager = apophisManager;
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't been given already
        if (!player.hasPlayedBefore() && plugin.<Boolean>getConfig("join_effects_enabled")) {
            List<String> effects = plugin.getConfig("join_effects");
            if (effects.isEmpty()) return;
            String chosenKey = effects.get(new Random().nextInt(effects.size()));
            EffectMapping effect = EffectMapping.fromEffectKey(chosenKey);
            if (effect == null) return;
            equipEffect(player, effect, "2");
        }
    }

    /**
     * Equips an effect in the primary or secondary slot.
     * If both slots are full, it drains the secondary slot and equips the new effect there.
     * 
     * @param player The player who will get the effect
     * @param effectName The effect to give the player
     */
    public void safeEquip(Player player, EffectMapping effect) {
        if (!equipEffect(player, effect, "1") && !equipEffect(player, effect, "2")) {
            player.performCommand("rdrain");
            equipEffect(player, effect, "2");
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
    private boolean equipEffect(Player player, EffectMapping effect, String slot) {
        // Checking for an effect in the slot.
        EffectMapping currentEffect = plugin.getEffectManager().getEffect(player.getUniqueId(), slot);
        if (currentEffect != null) return false;
        
        // Equipping the effect to the slot.
        plugin.getEffectManager().setEffect(player.getUniqueId(), slot, effect);
        player.sendMessage("§aYou have equipped " + effect.getName());
        return true;
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
            player.sendMessage("§cYour inventory is full! Make space before unequipping.");
            return;
        }
         
        // Equipping the effect
        this.safeEquip(player, effect);

        // Removing the effect from the player
        event.setItem(event.getItem().subtract(1));

        // Performing special logic for the apophis effect.
        if (effect == EffectMapping.APOPHIS || effect == EffectMapping.AUG_APOPHIS) {
            apophisManager.disguiseAsApophis(player);
        }
    }

    /**
     * Event handler to remove an effect from the players inventory if they die.
     * 
     * @param event The server PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EffectMapping effect1 = plugin.getEffectManager().getEffect(player.getUniqueId(), "1");
        EffectMapping effect2 = plugin.getEffectManager().getEffect(player.getUniqueId(), "2");
        String dropMode = plugin.getConfig().getString("effect_drops", "random");
        Random rand = new Random();
        switch (dropMode.toLowerCase()) {
            case "1":
                if (effect1 != null) {
                    this.dropEffect(player, "1");
                }
                break;

            case "2":
                if (effect2 != null) {
                    this.dropEffect(player, "2");
                }
                break;

            case "none":
                break;

            case "random":
            default:
                if (effect1 != null && effect2 != null) {
                    String selectedEffect = rand.nextBoolean() ? "1" : "2";
                    this.dropEffect(player, selectedEffect);
                } else if (effect1 != null) {
                    this.dropEffect(player, "1");
                } else if (effect2 != null) {
                    this.dropEffect(player, "2");
                }
                break;
        }

        File disguiseFile = new File(plugin.getDataFolder(), "data/AphopisPlayers/" + player.getUniqueId() + ".yml");

        // Removing the player's apophis disguise file if it exists.
        if (disguiseFile.exists()) {
            disguiseFile.delete();
            plugin.resetSkinWithoutKick(player);
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
        File disguiseFile = new File(plugin.getDataFolder(), "data/AphopisPlayers/" + player.getUniqueId() + ".yml");
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
    private void dropEffect(Player player, String slot) {
        // Getting the equipped effect from the data file.
        EffectMapping effect = plugin.getEffectManager().getEffect(player.getUniqueId(), slot);
        if (effect == null) return;

        // Removing the effect from the player.
        plugin.getEffectManager().removeEffect(player.getUniqueId(), slot);

        // Dropping the effect item at the player's location
        player.getWorld().dropItemNaturally(player.getLocation(), effect.createItem());
    }

    // Defining the command for swapping effects...
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        // Getting the equipped effects
        EffectMapping effect1 = plugin.getEffectManager().getEffect(player.getUniqueId(), "1");
        EffectMapping effect2 = plugin.getEffectManager().getEffect(player.getUniqueId(), "2");

        // Erroring out if the player doesn't have any effects equipped
        if (effect1 == null && effect2 == null) {
            player.sendMessage("§cYou do not have any effects equipped to swap.");
            return true;
        }

        // Swapping the effects
        plugin.getEffectManager().setEffect(player.getUniqueId(), "1", effect2);
        plugin.getEffectManager().setEffect(player.getUniqueId(), "2", effect1);
        player.sendMessage("§aYour Effects have been swapped.");
        return true;
    }
}
