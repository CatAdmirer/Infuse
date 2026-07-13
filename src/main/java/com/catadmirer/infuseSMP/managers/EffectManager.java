package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.events.EffectEquipEvent;
import com.catadmirer.infuseSMP.events.EffectUnequipEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public class EffectManager implements Listener {
    private final Infuse plugin;

    public EffectManager(Infuse plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets the effect a player has in a slot.
     * Overrides any effect already in that slot.
     *
     * @param player The {@link Player} to give an effect to.
     * @param effect The {@link InfuseEffect} to equip.
     * @param slot The slot to put the effect in.
     */
    public EquipResult setEffect(Player player, InfuseEffect effect, String slot) {
        return equipEffect(player, effect, slot, true);
    }

    /**
     * Equips an effect to a player.
     * Fails if the {@link EffectEquipEvent} was canceled or there was an effect equipped and override was set to false.
     *
     * @param player The {@link Player} to give an effect to.
     * @param effect The {@link InfuseEffect} to equip.
     * @param slot The slot to put the effect in.
     * @param override Whether to replace an existing effect.
     *
     * @return A {@link EquipResult}
     */
    public EquipResult equipEffect(Player player, InfuseEffect effect, String slot, boolean override) {
        // Calling an EffectEquipEvent and stopping if it is canceled.
        EffectEquipEvent event = new EffectEquipEvent(player, effect, slot);
        if (!event.callEvent()) return new EquipResult(EquipResultType.CANCELLED, effect);

        InfuseEffect equipped = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (equipped != null && !override) return new EquipResult(EquipResultType.FAIL);

        // Unequipping the old effect
        if (equipped != null) {
            EquipResult res = unequipEffect(player, slot);

            if (res.type != EquipResultType.SUCCESS) return new EquipResult(res.type, effect);
        }

        // Equipping the effect and updating the player data
        // If the player is in a blocked location, the effect is equipped but not activated.
        if (!effect.isLocationBlocked(player.getLocation())) effect.equip(player);
        plugin.getDataManager().setEffect(player.getUniqueId(), slot, effect);

        return new EquipResult(EquipResultType.SUCCESS, effect);
    }

    /**
     * Drains an effect from a player.
     * Sends feedback messages to the player and gives them the effect item.
     * Fails if the player's inventory is full or if they don't have an effect equipped.
     *
     * @param player The player who is draining an effect.
     * @param slot The slot to drain an effect from.
     */
    public EquipResult drainEffect(Player player, String slot) {
        // Unequipping the effect
        EquipResult result = unequipEffect(player, slot);

        // Checking if an effect was removed
        if (result.type == EquipResultType.FAIL) {
            Message msg = new Message(MessageType.EFFECT_NONE_EQUIPPED);
            msg.applyPlaceholder("slot", slot);
            player.sendMessage(msg.toComponent());
            return result;
        }

        // Skipping if the unequip event was canceled
        if (result.type == EquipResultType.CANCELLED) {
            if (result.effect == null) {
                throw new IllegalStateException("Cancelled unequip events should still return their related effect");
            }

            Message msg = new Message(MessageType.DRAIN_CANCELLED);
            msg.applyPlaceholder("effect_name", result.effect.getName());
            player.sendMessage(msg.toComponent());
            return result;
        }

        // Making sure the effect is not null
        if (result.effect == null) {
            throw new IllegalStateException("Successful unequip events need to return their related effect.");
        }

        // Making sure the player has inventory space for the drained item if is meant to be given to them.
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(new Message(MessageType.ERROR_INV_FULL).toComponent());
            return result;
        }

        // Giving the player the item
        player.getInventory().addItem(result.effect.createItem());

        // Sending the success message
        Message msg = new Message(MessageType.DRAIN_SUCCESS);
        msg.applyPlaceholder("effect_name", result.effect.getName());
        player.sendMessage(msg.toComponent());

        return result;
    }

    /**
     * Removes a player's effect from the specified slot and drops it on the ground.
     * Fails if the player doesn't have an effect equipped or the event was canceled.
     *
     * @param player The player to remove an effect from.
     * @param slot The slot to remove the effect from.
     */
    public EquipResult dropEffect(Player player, String slot) {
        EquipResult result = unequipEffect(player, slot);

        // Checking if an effect was removed
        if (result.type == EquipResultType.FAIL) return result;

        // Skipping if the unequip event was canceled
        if (result.type == EquipResultType.CANCELLED) {
            if (result.effect == null) {
                throw new IllegalStateException("Cancelled unequip events should still return their related effect");
            }

            return result;
        }

        // Making sure the effect is not null
        if (result.effect == null) {
            throw new IllegalStateException("Successful unequip events need to return their related effect.");
        }

        // Dropping the item
        player.getWorld().dropItem(player.getLocation(), result.effect.createItem());

        return result;
    }

    /**
     * Unequips an effect from a player.
     * Fails if the {@link EffectUnequipEvent} was canceled or if the player's inventory was full.
     *
     * @param player The {@link Player} to remove an effect from.
     * @param slot The slot to remove the effect from.
     */
    public EquipResult unequipEffect(Player player, String slot) {
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (effect == null) return new EquipResult(EquipResultType.FAIL);

        // Calling an EffectUnequipEvent
        EffectUnequipEvent event = new EffectUnequipEvent(player, effect, slot);
        if (!event.callEvent()) return new EquipResult(EquipResultType.CANCELLED, effect);

        // Unequipping the effect and updating the player data
        effect.unequip(player);
        plugin.getDataManager().removeEffect(player.getUniqueId(), slot);

        return new EquipResult(EquipResultType.SUCCESS, effect);
    }

    /**
     * Handling when players drink an infuse potion.
     * 
     * @param event The consume event.
     */
    @EventHandler
    public void onDrinkEffect(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Getting the effect from the item
        InfuseEffect effect = InfuseEffect.fromItem(item);

        // Skipping if the effect is not found.
        if (effect == null) return;

        // Equipping the effect
        EquipResult result = this.equipEffect(player, effect, "1", false);

        // Equipping the slot in the players other slot
        if (result.type == EquipResultType.FAIL) {
            // Drain slot 2 if an effect is equipped there
            if (plugin.getDataManager().getEffect(player.getUniqueId(), "2") != null) {
                result = this.drainEffect(player, "2");

                // If the drain failed or was cancelled, exit
                if (result.type == EquipResultType.CANCELLED) return;
                if (result.type == EquipResultType.FAIL) return;
            }

            result = this.equipEffect(player, effect, "2", false);
        }

        // Skipping the rest of the logic if the equip event was cancelled
        if (result.type == EquipResultType.CANCELLED) return;

        // Notifying the player
        Message msg = new Message(MessageType.EFFECT_EQUIPPED);
        msg.applyPlaceholder("effect_name", effect.getName());
        player.sendMessage(msg.toComponent());

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
        EquipResult result;
        Player player = event.getEntity();
        String dropMode = plugin.getMainConfig().effectDrops();
        switch (dropMode.toLowerCase()) {
            case "random" -> {
                String slot = (Math.random() > 0.5) ? "1" : "2";

                result = dropEffect(player, slot);
                if (result.type == EquipResultType.FAIL) {
                    dropEffect(player, slot.equals("1") ? "2" : "1");
                }
            }
            case "prefer_1" -> {
                result = dropEffect(player, "1");
                if (result.type == EquipResultType.FAIL) {
                    dropEffect(player, "2");
                }
            }
            case "prefer_2" -> {
                result = dropEffect(player, "2");
                if (result.type == EquipResultType.FAIL) {
                    dropEffect(player, "1");
                }
            }
            case "only_1" -> dropEffect(player, "1");
            case "only_2" -> dropEffect(player, "2");
            case "none" -> {}
        }
    }

    /** Activates the player's effects and assigns them a starting effect if they haven't played before. */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't joined before
        if (plugin.getMainConfig().joinEffectsEnabled() && !player.hasPlayedBefore()) {
            List<InfuseEffect> effects = plugin.getMainConfig().joinEffects();
            if (effects.isEmpty()) return;

            InfuseEffect effect = effects.get((int) (Math.random() * effects.size()));
            equipEffect(player, effect, "1", false);
            return;
        }

        // Enabling each effect
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        if (effect != null && !effect.isLocationBlocked(player.getLocation())) effect.equip(player);

        effect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        if (effect != null && !effect.isLocationBlocked(player.getLocation())) effect.equip(player);
    }

    /** Unequips a player's effects when they leave the game. */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Deactivating the player's effects
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        if (effect != null) effect.unequip(player);

        effect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        if (effect != null) effect.unequip(player);
    }

    /**
     * A record containing the result of an {@link EffectManager#equipEffect} or {@link EffectManager#unequipEffect} call.
     *
     * @param type The {@link EquipResultType} (Pass/Fail/Cancelled)
     * @param effect
     */
    public record EquipResult(EquipResultType type, @Nullable InfuseEffect effect) {
        public EquipResult(EquipResultType type) {
            this(type, null);
        }
    }

    public enum EquipResultType {
        FAIL,
        CANCELLED,
        SUCCESS
    }
}