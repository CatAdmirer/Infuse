package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Heart implements Listener {
    private static Infuse plugin;

    public Heart(Infuse plugin) {
        Heart.plugin = plugin;
    }

    public static NamespacedKey heartBoost = new NamespacedKey("infuse", "heart_boost");
    public static NamespacedKey heartSparkBoost = new NamespacedKey("infuse", "heart_spark_boost");

    public static void applyPassiveEffects(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute.getModifier(heartBoost) == null) {
            AttributeModifier modifier = new AttributeModifier(heartBoost, 10, Operation.ADD_NUMBER);
            attribute.addModifier(modifier);
        }
    }

    @EventHandler
    public void heartShowTargetHealth(TenHitEvent event) {
        Player attacker = event.getAttacker();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.HEART)) return;

        this.showAndUpdateHealthAboveEntity(event.getTarget());
    }

    private void showAndUpdateHealthAboveEntity(Entity player) {
        Location ploc = player.getLocation().add(0, 2.5, 0);

        TextDisplay as = (TextDisplay) ploc.getWorld().spawn(ploc, TextDisplay.class);

        as.setGravity(false);
        as.setCustomNameVisible(true);
        as.customName();
        updateHealthDisplay(as, (LivingEntity) player);
        player.addPassenger(as);
        final BukkitRunnable updateTask = new BukkitRunnable() {
            public void run() {
                if (!player.isDead() && player.isValid()) {
                    Heart.this.updateHealthDisplay(as, (LivingEntity) player);
                } else {
                    this.cancel();
                    as.setCustomNameVisible(false);
                    as.customName(null);
                }
            }
        };

        updateTask.runTaskTimer(plugin, 0L, 10L);
        (new BukkitRunnable() {
            public void run() {
                updateTask.cancel();
                as.setCustomNameVisible(false);
                as.customName(null);
                player.removePassenger(as);
            }
        }).runTaskLater(plugin, 200L);
    }

    private void updateHealthDisplay(TextDisplay entity, LivingEntity player) {
        if (player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
            entity.customName(Message.toComponent(String.format("<yellow><b>%.1f ❤", player.getHealth()) + player.getAbsorptionAmount()));
        } else {
            entity.customName(Message.toComponent(String.format("<red><b>%.1f ❤", player.getHealth())));
        }
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.HEART)) return;

        ItemStack item = event.getItem();
        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 4));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 0));
        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "heart")) return;
        
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute.getModifier(heartSparkBoost) == null) {
            AttributeModifier modifier = new AttributeModifier(heartSparkBoost, 10, Operation.ADD_NUMBER);
            attribute.addModifier(modifier);
        }
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_HEART : EffectMapping.HEART);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_HEART : EffectMapping.HEART);

        CooldownManager.setTimes(playerUUID, "heart", duration, cooldown);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> attribute.removeModifier(heartSparkBoost), duration * 20);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AttributeInstance maxHealth = event.getPlayer().getAttribute(Attribute.MAX_HEALTH);
        maxHealth.removeModifier(heartBoost);
        maxHealth.removeModifier(heartSparkBoost);
    }
}