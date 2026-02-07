package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.managers.ApophisManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.io.File;
import java.util.List;
import java.util.Random;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EquipEffect implements Listener {
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
        if (!player.hasPlayedBefore() && plugin.getConfigFile().joinEffectsEnabled()) {
            List<EffectMapping> effects = plugin.getConfigFile().joinEffects();
            if (effects.isEmpty()) return;
            EffectMapping effect = effects.get(new Random().nextInt(effects.size()));
            equipEffect(player, effect, "2");
        }
    }

    /**
     * Equips an effect in the primary or secondary slot.
     * If both slots are full, it drains the secondary slot and equips the new effect there.
     * 
     * @param player The player who will get the effect
     * @param effect The effect to give the player
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
     * @param effect The effect to give the player.
     * @param slot The slot to equip the effect into.
     * 
     * @return Returns false if the slot is already taken.
     */
    private boolean equipEffect(Player player, EffectMapping effect, String slot) {
        // Checking for an effect in the slot.
        EffectMapping currentEffect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (currentEffect != null) return false;
        
        // Equipping the effect to the slot.
        plugin.getDataManager().setEffect(player.getUniqueId(), slot, effect);
        String msg = Messages.EFFECT_EQUIPPED.getMessage();
        msg = msg.replace("%effect_name%", effect.getName());
        player.sendMessage(Messages.toComponent(msg));

        if (effect == EffectMapping.THIEF || effect == EffectMapping.AUG_THIEF) {
            Thief.equipThief(player);
        }

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

        // Getting the effect from the item
        EffectMapping effect = EffectMapping.fromItem(mainHandItem);
        // Skipping if the effect is not found.
        if (effect == null) return;

        // Skipping if the plauer's inventory is full.
        if (player.getInventory().firstEmpty() == -1) {
            event.setCancelled(true);
            player.sendMessage(Messages.ERROR_INV_FULL.toComponent());
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
        EffectMapping effect1 = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        EffectMapping effect2 = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        String dropMode = plugin.getConfigFile().effectDrops();
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

        apophisManager.unsetApophis(player);
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
        EffectMapping effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (effect == null) return;

        // Removing the effect from the player.
        plugin.getDataManager().removeEffect(player.getUniqueId(), slot);

        // Dropping the effect item at the player's location
        player.getWorld().dropItemNaturally(player.getLocation(), effect.createItem());
    }
}
