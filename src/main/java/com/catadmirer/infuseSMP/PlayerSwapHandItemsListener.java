package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapHandItemsListener implements Listener {
    private final DataManager dataManager;

    public PlayerSwapHandItemsListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

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
        String data = dataManager.getControlDefault(playerUUID);
        if (data.equals("offhand")) {
            // Getting the effect equipped in each slot
            EffectMapping lEffect = dataManager.getEffect(player.getUniqueId(), "1");
            EffectMapping rEffect = dataManager.getEffect(player.getUniqueId(), "2");

            // Activating the left effect's spark if the player was sneaking and the effect wasn't on cooldown.
            if (lEffect != null && !player.isSneaking() && !CooldownManager.isOnCooldown(playerUUID, lEffect.regular().getKey())) {
                event.setCancelled(true);
                lEffect.activateSpark(player);
            }

            // Activating the right effect's spark if the player was not sneaking and the effect wasn't on cooldown.
            if (rEffect != null && player.isSneaking() && !CooldownManager.isOnCooldown(playerUUID, rEffect.regular().getKey())) {
                event.setCancelled(true);
                rEffect.activateSpark(player);
            }
        }
    }
}