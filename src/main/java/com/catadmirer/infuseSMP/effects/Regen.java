package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InfuseDebug;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.destroystokyo.paper.MaterialSetTag;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.Bukkit;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Regen implements Listener {
    private static Infuse plugin;

    public Regen(Infuse plugin) {
        Regen.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        if (plugin.getDataManager().hasEffect(player, EffectMapping.REGEN) ) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, false, false));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false));
            }
        }
    }

    @EventHandler
    public void consume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) {
            float sat = player.getSaturation();
            player.setSaturation(sat + 6);
        }
    }

    @EventHandler
    public void rclickfood(PlayerInteractEvent event) {
        if (!(event.getAction().isRightClick())) return;
        Player player = event.getPlayer();
        if (event.getItem().getType().isEdible()) {
            if (plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) {
                event.getItem().editMeta(meta -> {
                    meta.getFood().setCanAlwaysEat(true);
                });
            } else {
                event.getItem().editMeta(meta -> {
                    meta.getFood().setCanAlwaysEat(false);
                });
            }
        }
    }

    @EventHandler
    public void onTenthAttack(TenHitEvent event) {
        if (!plugin.getDataManager().hasEffect(event.getAttacker(), EffectMapping.REGEN)) return;

        int currentFood = event.getTarget().getFoodLevel();
        event.getTarget().setFoodLevel(currentFood - 2);
    }

    public Map<Player,Integer> foodLevelMap = new HashMap<>();

    @EventHandler
    public void foodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) return;

        if (foodLevelMap.containsKey(player)) {
            if (event.getFoodLevel() < foodLevelMap.get(player)) {
                event.setFoodLevel(foodLevelMap.get(player));
            }
        } else {
            foodLevelMap.put(player, event.getFoodLevel());
        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "regen")) {
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);
            long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);

            CooldownManager.setDuration(playerUUID, "regen", duration);
            CooldownManager.setCooldown(playerUUID, "regen", cooldown);

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        }
    }
}