package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Heart extends InfuseEffect {
    private static Infuse plugin;

    private final Map<UUID, Map<UUID, Integer>> hitCounts = new HashMap<>();

    public Heart(Infuse plugin) {
        Heart.plugin = plugin;
    }

    public static NamespacedKey heartBoost = new NamespacedKey("infuse", "apophis_boost");
    public static NamespacedKey heartSparkBoost = new NamespacedKey("infuse", "apophis_spark_boost");

    public static void applyPassiveEffects(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute.getModifier(heartBoost) == null) {
            AttributeModifier modifier = new AttributeModifier(heartBoost, 10, Operation.ADD_NUMBER);
            attribute.addModifier(modifier);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (event.getEntity() instanceof LivingEntity target) {
                if (plugin.getDataManager().hasEffect(player, new Heart())) {
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
                    entity.customName(null);
                }
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 10L);
        (new BukkitRunnable() {
            public void run() {
                updateTask.cancel();
                entity.setCustomNameVisible(false);
                entity.customName(null);
            }
        }).runTaskLater(plugin, 200L);
    }

    private void updateHealthDisplay(LivingEntity entity) {
        entity.customName(Messages.toComponent(String.format("<red><b>%.1f", entity.getHealth())));
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataManager().hasEffect(player, new Heart())) {
            ItemStack item = event.getItem();
            if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 4));
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 0));
            }
        }

    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "heart")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

            AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (attribute.getModifier(heartSparkBoost) == null) {
                AttributeModifier modifier = new AttributeModifier(heartSparkBoost, 10, Operation.ADD_NUMBER);
                attribute.addModifier(modifier);
            }
            
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getConfigFile().cooldown(this);
            long duration = plugin.getConfigFile().duration(this);

            CooldownManager.setDuration(playerUUID, "heart", duration);
            CooldownManager.setCooldown(playerUUID, "heart", cooldown);
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> attribute.removeModifier(heartSparkBoost), duration * 20);
        }
    }
}