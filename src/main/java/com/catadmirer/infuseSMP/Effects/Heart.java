package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Heart implements Listener {
    private final Map<UUID, Map<UUID, Integer>> hitCounts = new HashMap<>();

    public Heart(Infuse plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, Infuse.getInstance());
        this.startHealthCheckTask();
    }

    private void startHealthCheckTask() {
        (new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealthAttribute == null) continue;
                    double currentMaxHealth = maxHealthAttribute.getBaseValue();
                    if (!EffectMapping.HEART.hasEffect(player)) continue;

                    if (currentMaxHealth == 20) maxHealthAttribute.setBaseValue(30);
                }
            }
        }).runTaskTimer(Infuse.getInstance(), 0L, 20L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (event.getEntity() instanceof LivingEntity target) {
                if (EffectMapping.HEART.hasEffect(player)) {
                    UUID playerUUID = player.getUniqueId();
                    UUID targetUUID = target.getUniqueId();
                    this.hitCounts.putIfAbsent(playerUUID, new HashMap<>());
                    Map<UUID, Integer> playerHits = this.hitCounts.get(playerUUID);
                    int hitCount = playerHits.getOrDefault(targetUUID, 0) + 1;
                    playerHits.put(targetUUID, hitCount);
                    if (hitCount == 20) {
                        this.showAndUpdateHealthAboveEntity(target, player);
                        playerHits.put(targetUUID, 0);
                    }

                }
            }
        }
    }

    private void showAndUpdateHealthAboveEntity(final LivingEntity entity, Player player) {
        this.updateHealthDisplay(entity);
        entity.setCustomNameVisible(true);
        final BukkitRunnable updateTask = new BukkitRunnable() {
            public void run() {
                if (!entity.isDead() && entity.isValid()) {
                    Heart.this.updateHealthDisplay(entity);
                } else {
                    this.cancel();
                    entity.setCustomNameVisible(false);
                    entity.setCustomName(null);
                }
            }
        };
        updateTask.runTaskTimer(Infuse.getInstance(), 0L, 10L);
        (new BukkitRunnable() {
            public void run() {
                updateTask.cancel();
                entity.setCustomNameVisible(false);
                entity.setCustomName(null);
            }
        }).runTaskLater(Infuse.getInstance(), 200L);
    }

    private void updateHealthDisplay(LivingEntity entity) {
        double health = entity.getHealth();
        String healthText = "§c§l❤ " + String.format("%.1f", health);
        entity.setCustomName(healthText);
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (EffectMapping.HEART.hasEffect(player)) {
            ItemStack item = event.getItem();
            if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 4));
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 0));
            }
        }

    }

    public static void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "heart")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

            final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttribute != null) {
                maxHealthAttribute.setBaseValue(40);
            }
            player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
            
            // Applying cooldowns and durations for the effect
            boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
            long cooldown = Infuse.getInstance().getConfig("heart.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = Infuse.getInstance().getConfig("heart.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "heart", duration);
            CooldownManager.setCooldown(playerUUID, "heart", cooldown);

            new BukkitRunnable() {
                public void run() {
                    if (maxHealthAttribute != null) {
                        maxHealthAttribute.setBaseValue(20);
                    }
                    player.sendMessage("§cYour Health Boost has ended.");
                }
            }.runTaskLater(Infuse.getInstance(), duration * 20L);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        (new BukkitRunnable() {
            public void run() {
                AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20);
                }

            }
        }).runTaskLater(Infuse.getInstance(), 15L);
    }
}