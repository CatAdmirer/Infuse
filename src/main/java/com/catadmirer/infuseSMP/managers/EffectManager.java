package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.events.EffectEquipEvent;
import com.catadmirer.infuseSMP.events.EffectUnequipEvent;

import java.util.List;
import java.util.Random;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EffectManager implements Listener {
    private final Infuse plugin;

    public EffectManager(Infuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't been given already
        if (!player.hasPlayedBefore() && plugin.getMainConfig().joinEffectsEnabled()) {
            List<InfuseEffect> effects = plugin.getMainConfig().joinEffects();
            if (effects.isEmpty()) return;
            InfuseEffect effect = effects.get(new Random().nextInt(effects.size()));
            equipEffect(player, effect, "1");
        }
    }

    /**
     * Equips an effect in the primary or secondary slot.
     * If both slots are full, it drains the secondary slot and equips the new effect there.
     * 
     * @param player The player who will get the effect
     * @param effect The effect to give the player
     */
    public void safeEquip(Player player, InfuseEffect effect) {
        if (plugin.getDataManager().getEffect(player.getUniqueId(), "1") == null) {
            equipEffect(player, effect, "1");
            return;
        }

        if (plugin.getDataManager().getEffect(player.getUniqueId(), "2") == null) {
            equipEffect(player, effect, "2");
            return;
        }

        player.performCommand("rdrain");
        equipEffect(player, effect, "2");
    }

    /**
     * Equips an effect in the specified slot.
     * 
     * @param player The player who will get the effect
     * @param effect The effect to give the player.
     * @param slot The slot to equip the effect into.
     */
    private void equipEffect(Player player, InfuseEffect effect, String slot) {
        // Checking for an effect in the slot.
        InfuseEffect currentEffect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (currentEffect != null) return;

        // Calling an EffectEquipEvent and stopping if it is canceled.
        EffectEquipEvent event = new EffectEquipEvent(player, effect, slot);
        if (!event.callEvent()) return;

        // Equipping the effect and updating the player data
        effect.equip(player);
        plugin.getDataManager().setEffect(player.getUniqueId(), slot, effect);

        // Notifying the player
        Message msg = new Message(MessageType.EFFECT_EQUIPPED);
        msg.applyPlaceholder("effect_name", effect.getName());
        player.sendMessage(msg.toComponent());
    }

    public void unequipEffect(Player player, String slot) {
        // Getting the equipped effect
        InfuseEffect currentEffect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (currentEffect == null) return;

        // Calling an EffectUnequipEvent and stopping if it is canceled.
        EffectUnequipEvent event = new EffectUnequipEvent(player, currentEffect, slot);
        if (!event.callEvent()) return;

        // Unequipping the effect and updating the player data
        currentEffect.unequip(player);
        plugin.getDataManager().removeEffect(player.getUniqueId(), slot);
    }

    /**
     * Handling when players drink an infuse potion.
     * 
     * @param event The consume event.
     */
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = event.getItem();

        // Getting the effect from the item
        InfuseEffect effect = InfuseEffect.fromItem(mainHandItem);
        // Skipping if the effect is not found.
        if (effect == null) return;

        // Skipping if the player's inventory is full.
        if (player.getInventory().firstEmpty() == -1) {
            event.setCancelled(true);
            player.sendMessage(new Message(MessageType.ERROR_INV_FULL).toComponent());
            return;
        }
         
        // Equipping the effect
        this.safeEquip(player, effect);

        // Removing the effect from the player
        event.setItem(event.getItem().subtract(1));
    }

    /**
     * Event handler to remove an effect from the players inventory if they die.
     * 
     * @param event The server PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        InfuseEffect effect1 = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        InfuseEffect effect2 = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        String dropMode = plugin.getMainConfig().effectDrops();
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
    }

    /**
     * Calling an EffectEquipEvent for each player that joins.
     * 
     * @param event The server PlayerJoinEvent to catch.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        if (effect != null) {
            EffectEquipEvent e = new EffectEquipEvent(player, effect, "1");
            if (e.callEvent()) effect.equip(player);
        }
        
        effect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        if (effect != null) {
            EffectEquipEvent e = new EffectEquipEvent(player, effect, "2");
            if (e.callEvent()) effect.equip(player);
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
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (effect == null) return;

        // Removing the effect from the player.
        plugin.getDataManager().removeEffect(player.getUniqueId(), slot);
        new EffectUnequipEvent(player, effect, slot).callEvent();

        // Dropping the effect item at the player's location
        player.getWorld().dropItemNaturally(player.getLocation(), effect.createItem());
    }
}