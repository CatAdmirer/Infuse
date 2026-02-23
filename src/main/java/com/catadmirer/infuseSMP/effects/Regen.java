package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.UUID;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
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
    }

    @EventHandler
    public void consume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) return;

        float sat = player.getSaturation();
        player.setSaturation(sat + 6);
    }

    @EventHandler
    public void regenCanAlwaysEat(PlayerInteractEvent event) {
        if (!(event.getAction().isRightClick())) return;
        Player player = event.getPlayer();

        // Filtering inedible items
        if (!event.getItem().getType().isEdible()) return;
        
        // Filtering always edible items
        if (new ItemStack(event.getItem().getType()).getItemMeta().getFood().canAlwaysEat()) return;

        // Making the food always edible only if the player has the regen effect.  Makes food not always edible otherwise
        event.getItem().editMeta(meta -> {
            meta.getFood().setCanAlwaysEat(plugin.getDataManager().hasEffect(player, EffectMapping.REGEN));
        });
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
        long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);
        long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);

        CooldownManager.setDuration(playerUUID, "regen", duration);
        CooldownManager.setCooldown(playerUUID, "regen", cooldown);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
    }
}