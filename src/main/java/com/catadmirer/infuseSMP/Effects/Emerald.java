package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;

import java.util.UUID;

import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Emerald implements Listener {
    private final Plugin plugin;

    public Emerald(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!EffectMapping.EMERALD.hasEffect(player)) continue;

                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    Emerald.this.applyPassiveEffects(player);
                    if (Emerald.this.isSword(mainHand) && mainHand.getEnchantmentLevel(Enchantment.LOOTING) < 5) {
                        mainHand.addUnsafeEnchantment(Enchantment.LOOTING, 5);
                    }
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    private void applyPassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 2, false, false));
    }

    private boolean isSword(ItemStack item) {
        if (item == null) {
            return false;
        } else {
            Material type = item.getType();
            return type == Material.WOODEN_SWORD || type == Material.STONE_SWORD || type == Material.IRON_SWORD || type == Material.GOLDEN_SWORD || type == Material.DIAMOND_SWORD || type == Material.NETHERITE_SWORD;
        }
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (EffectMapping.EMERALD.hasEffect(player)) {
            double multiplier = 1.5;
            PotionEffect heroEffect = player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
            if (heroEffect != null && heroEffect.getAmplifier() >= 200) {
                multiplier = 3;
            }

            event.setAmount((int) (event.getAmount() * multiplier));
        }

    }

    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getEnchanter();

        if (EffectMapping.EMERALD.hasEffect(player)) {
            try {
                event.getClass()
                        .getMethod("setEnchantmentBonus", int.class)
                        .invoke(event, 15);
            } catch (Exception ignored) {}
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            final Player player = event.getPlayer();
            ItemStack consumedItem = event.getItem();
            int originalCount = consumedItem.getAmount();
            if (EffectMapping.EMERALD.hasEffect(player)) {
                if (consumedItem.getType() == Material.POTION) {
                    return;
                }

                double chance = 0.15;
                if (CooldownManager.isEffectActive(player.getUniqueId(), "emerald")) chance = 0.25;

                if (Math.random() < chance) {
                    ItemStack refund = consumedItem.clone();
                    refund.setAmount(originalCount + 1);
                    event.setItem(refund);
                    (new BukkitRunnable() {
                        public void run() {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 3, 1.5, 0.5, 0.5, 0.01);
                        }
                    }).runTaskLater(this.plugin, 1L);
                }
            }

        }
    }

    public static void activateSpark(Player player) {
        if (!EffectMapping.EMERALD.hasEffect(player)) return;

        UUID playerUUID = player.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "emerald")) return;

        // Applying effects for the emerald spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("emerald.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("emerald.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "emerald", duration);
        CooldownManager.setCooldown(playerUUID, "emerald", cooldown);
    }
}
