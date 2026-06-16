package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.events.EffectEquipEvent;
import com.catadmirer.infuseSMP.events.EffectUnequipEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class EffectManager implements Listener {
    private final Infuse plugin;

    public EffectManager(Infuse plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles logic for when a player joins the server.
     * <p>
     * It gives the player their starting effect if they haven't played before, and also enables the abilities for any effects the player has.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't joined before
        if (!player.hasPlayedBefore() && plugin.getMainConfig().joinEffectsEnabled()) {
            List<InfuseEffect> effects = plugin.getMainConfig().joinEffects();
            if (effects.isEmpty()) return;
            InfuseEffect effect = effects.get(new Random().nextInt(effects.size()));
            equipEffect(player, effect, "1");
        }

        // Enabling each effect
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
     * Fails if the {@link EffectEquipEvent} is canceled.
     * 
     * @param player The player who will get the effect
     * @param effect The effect to give the player.
     * @param slot The slot to equip the effect into.
     *
     * @return true if the effect was equipped successfully, false otherwise.
     */
    public boolean equipEffect(Player player, InfuseEffect effect, String slot) {
        // Calling an EffectEquipEvent and stopping if it is canceled.
        EffectEquipEvent event = new EffectEquipEvent(player, effect, slot);
        if (!event.callEvent()) return false;

        // Equipping the effect and updating the player data
        effect.equip(player);
        plugin.getDataManager().setEffect(player.getUniqueId(), slot, effect);

        // Notifying the player
        Message msg = new Message(MessageType.EFFECT_EQUIPPED);
        msg.applyPlaceholder("effect_name", effect.getName());
        player.sendMessage(msg.toComponent());

        return true;
    }

    /**
     * Removes an effect from a player.
     * The effect item is not given to them, and the number of that effect that exists is lowered.
     * Fails if they don't have an effect equipped.
     *
     * @param player The player to remove an effect from.
     * @param slot The slot to remove an effect from.
     */
    public void unequipEffect(Player player, String slot) {
        unequipEffect(Audience.empty(), player, slot, UnequipItemHandler.DESTROY);
    }

    /**
     * Drains an effect from a player.
     * Sends feedback messages to the player and gives them the effect item.
     * Fails if the player's inventory is full or if they don't have an effect equipped.
     *
     * @param player The player who is draining an effect.
     * @param slot The slot to drain an effect from.
     */
    public void drainEffect(Player player, String slot) {
        unequipEffect(player, player, slot, UnequipItemHandler.GIVE);
    }

    /**
     * Removes a player's effect from the specified slot and drops it on the ground.
     * Fails if the player doesn't have an effect equipped.
     *
     * @param player The player to remove an effect from.
     * @param slot The slot to remove the effect from.
     */
    public void dropEffect(Player player, String slot) {
        unequipEffect(Audience.empty(), player, slot, UnequipItemHandler.DROP);
    }

    /**
     * Unequips an effect from a player.
     * Fails if the {@link EffectUnequipEvent} was canceled or if the player's inventory was full.
     *
     * @param audience The {@link Audience} to send messages to.
     * @param player The {@link Player} to remove an effect from.
     * @param slot The slot to remove the effect from.
     * @param itemHandler Whether to drop the item, give it to the player, or destroy it.
     */
    public void unequipEffect(Audience audience, Player player, String slot, UnequipItemHandler itemHandler) {
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);

        // Checking if the effect is null
        if (effect == null) {
            Message msg = new Message(MessageType.EFFECT_NONE_EQUIPPED);
            msg.applyPlaceholder("slot", slot);
            audience.sendMessage(msg.toComponent());
            return;
        }

        // Calling an EffectUnequipEvent
        EffectUnequipEvent event = new EffectUnequipEvent(player, effect, slot);
        if (!event.callEvent()) {
            audience.sendMessage(new Message(MessageType.DRAIN_CANCELLED).toComponent());
            return;
        }

        // Making sure the player has inventory space for the drained item if it meant to be given to them.
        if (itemHandler == UnequipItemHandler.GIVE && player.getInventory().firstEmpty() == -1) {
            audience.sendMessage(new Message(MessageType.ERROR_INV_FULL).toComponent());
            return;
        }

        // Unequipping the effect and updating the player data
        effect.unequip(player);
        plugin.getDataManager().removeEffect(player.getUniqueId(), slot);

        // Checking the UnequipItemHandler
        switch (itemHandler) {
            case DROP -> player.getWorld().dropItem(player.getLocation(), effect.createItem());
            case GIVE -> player.getInventory().addItem(effect.createItem());
            case DESTROY -> {
                int count = plugin.getDataManager().getExistingCount(effect);
                plugin.getDataManager().setExistingCount(effect, count - 1);
            }
        }

        // Sending the success message
        Message msg = new Message(MessageType.DRAIN_SUCCESS);
        msg.applyPlaceholder("effect_name", effect.getName());
        audience.sendMessage(msg.toComponent());
    }

    /**
     * Handles logic for when a player joins the server.
     * <p>
     * It gives the player their starting effect if they haven't played before, and also enables the abilities for any effects the player has.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't joined before
        if (!player.hasPlayedBefore() && plugin.getMainConfig().joinEffectsEnabled()) {
            List<InfuseEffect> effects = plugin.getMainConfig().joinEffects();
            if (effects.isEmpty()) return;
            InfuseEffect effect = effects.get(new Random().nextInt(effects.size()));
            equipEffect(player, effect, "1");
        }

        // Enabling each effect
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

        effect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        if (effect != null) effect.unequip(player);
    }

    /** Used to tell the unequip function what to do with the effect item when a player removes their effect. */
    public enum UnequipItemHandler {
        /// Drops the item on the ground.
        DROP,
        /// Gives the item to the player (unequip fails if the player's inventory is full).
        GIVE,
        /// Destroys/doesn't do anything with the item.
        DESTROY
    }
}