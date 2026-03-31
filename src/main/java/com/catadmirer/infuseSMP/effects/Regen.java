package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Regen implements Listener {
    private static Infuse plugin;

    public Regen(Infuse plugin) {
        Regen.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, false, false));
    }

    @EventHandler
    public void regenRegenerateOnHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false));
        if (CooldownManager.isEffectActive(player.getUniqueId(), "regen")) {
            for (Entity loopentity : player.getNearbyEntities(5, 5, 5)) {
                if (loopentity instanceof Player otherplayer) {
                    if (plugin.getDataManager().isTrusted(player, otherplayer)) {
                        otherplayer.heal(event.getDamage()/2);
                    }
                }
            }
        }
    }

    @EventHandler
    public void consume(PlayerItemConsumeEvent event) {
        if (!plugin.getDataManager().hasEffect(event.getPlayer(), EffectMapping.REGEN)) return;

        final float sat = event.getPlayer().getSaturation();
        event.getPlayer().setSaturation(sat + 6);
    }

    @EventHandler
    public void regenCanAlwaysEat(PlayerInteractEvent event) {
        if (!(plugin.getDataManager().hasEffect(event.getPlayer(), EffectMapping.REGEN))) return;
        if (!(event.getAction().isRightClick())) return;

        Player player = event.getPlayer();

        // Filtering an empty hand
        if (event.getItem() == null) return;
        
        // Filtering inedible items
        if (!event.getItem().getType().isEdible()) return;
        // Check if it already can always eat before this.
        if (event.getItem().getItemMeta().getFood().canAlwaysEat()) return;

        final ItemStack item = event.getItem().clone();

        // Making the food always edible only if the player has the regen effect.
        item.editMeta(meta -> {
            final FoodComponent food = meta.getFood();
            food.setCanAlwaysEat(plugin.getDataManager().hasEffect(player, EffectMapping.REGEN));
            meta.setFood(food);
        });

        item.setAmount(event.getItem().getAmount());
        player.getInventory().setItemInMainHand(item);
    }

    @EventHandler
    public void onTenthAttack(TenHitEvent event) {
        if (!plugin.getDataManager().hasEffect(event.getAttacker(), EffectMapping.REGEN)) return;

        int currentFood = event.getTarget().getFoodLevel();
        event.getTarget().setFoodLevel(currentFood - 2);
    }

    @EventHandler
    public void regenPreserveHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) return;

        event.setFoodLevel(20);
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "regen")) return;
            
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);

        CooldownManager.setDuration(playerUUID, "regen", duration);
        CooldownManager.setCooldown(playerUUID, "regen", cooldown);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
    }
}