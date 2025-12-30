package com.catadmirer.infuseSMP;

import java.util.UUID;

import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapHandItemsListener {
    /**
     * Listens for when the player swaps the items in their main and offhand.
     * When they do so, it will be used to activate their left or right spark based on whether or not they are crouching.
     * 
     * @param event The {@link PlayerSwapHandItemsEvent} to process
     */
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        // TODO: Compare this with the stored data, not a permission.
        if (!player.hasPermission("ability.use")) {
            // Getting the effect equipped in each slot
            EffectMapping lEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
            EffectMapping rEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");

            // Cancelling the event
            // The event will be un-cancelled if a spark is not activated.
            event.setCancelled(true);

            // Activating the left effect's spark if the player was sneaking and the effect wasn't on cooldown.
            if (!player.isSneaking() && !CooldownManager.isOnCooldown(playerUUID, lEffect.regular().getKey())) {
                switch (lEffect) {
                    case AUG_APOPHIS, APOPHIS -> Apophis.activateSpark(player);
                    case AUG_EMERALD, EMERALD -> Emerald.activateSpark(player);
                    case AUG_ENDER, ENDER -> Ender.activateSpark(player);
                    case AUG_FEATHER, FEATHER -> Feather.activateSpark(player);
                    case AUG_FIRE, FIRE -> Fire.activateSpark(player);
                    case AUG_FROST, FROST -> Frost.activateSpark(player);
                    case AUG_HASTE, HASTE -> Haste.activateSpark(player);
                    case AUG_HEART, HEART -> Heart.activateSpark(player);
                    case AUG_INVIS, INVIS -> Invisibility.activateSpark(player);
                    case AUG_OCEAN, OCEAN -> Ocean.activateSpark(player);
                    case AUG_REGEN, REGEN -> Regen.activateSpark(player);
                    case AUG_SPEED, SPEED -> Speed.activateSpark(player);
                    case AUG_STRENGTH, STRENGTH -> Strength.activateSpark(player);
                    case AUG_THIEF, THIEF -> Thief.activateSpark(player);
                    case AUG_THUNDER, THUNDER -> Thunder.activateSpark(player);
                    default -> event.setCancelled(false);
                }
            }

            // Activating the right effect's spark if the player was not sneaking and the effect wasn't on cooldown.
            if (player.isSneaking() && !CooldownManager.isOnCooldown(playerUUID, rEffect.regular().getKey())) {
                switch (rEffect) {
                    case AUG_APOPHIS, APOPHIS -> Apophis.activateSpark(player);
                    case AUG_EMERALD, EMERALD -> Emerald.activateSpark(player);
                    case AUG_ENDER, ENDER -> Ender.activateSpark(player);
                    case AUG_FEATHER, FEATHER -> Feather.activateSpark(player);
                    case AUG_FIRE, FIRE -> Fire.activateSpark(player);
                    case AUG_FROST, FROST -> Frost.activateSpark(player);
                    case AUG_HASTE, HASTE -> Haste.activateSpark(player);
                    case AUG_HEART, HEART -> Heart.activateSpark(player);
                    case AUG_INVIS, INVIS -> Invisibility.activateSpark(player);
                    case AUG_OCEAN, OCEAN -> Ocean.activateSpark(player);
                    case AUG_REGEN, REGEN -> Regen.activateSpark(player);
                    case AUG_SPEED, SPEED -> Speed.activateSpark(player);
                    case AUG_STRENGTH, STRENGTH -> Strength.activateSpark(player);
                    case AUG_THIEF, THIEF -> Thief.activateSpark(player);
                    case AUG_THUNDER, THUNDER -> Thunder.activateSpark(player);
                    default -> event.setCancelled(false);
                }
            }
        }
    }
}