package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.WeightedRandom;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.Tag;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Emerald implements Listener {
    private static Infuse plugin;

    public Emerald(Infuse plugin) {
        Emerald.plugin = plugin;

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
    public void pickupexp(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();

        if (!EffectMapping.EMERALD.hasEffect(player)) return;

        ExperienceOrb orb = event.getExperienceOrb();
        int amount = orb.getExperience();

        double multiplier = 1.5;
        if (CooldownManager.isEffectActive(player.getUniqueId(), "emerald")) {
            multiplier = 3.0;
        }

        int newAmount = (int) Math.round(amount * multiplier);
        orb.setExperience(newAmount);
    }

    @EventHandler
    public void emeraldEnchantBonus(PrepareItemEnchantEvent event) {
        // Setting the enchantment bonus to 15 if the function exists
        if (!EffectMapping.EMERALD.hasEffect(event.getEnchanter())) return;

        // Getting the world seed of the player
        long worldSeed = event.getEnchanter().getWorld().getSeed();

        Random rand = new Random();
        EnchantmentOffer[] currentOffers = event.getOffers();
        Registry<Enchantment> enchantRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Tag<Enchantment> inEnchantingTable = enchantRegistry.getTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE);
        ItemStack item = event.getItem();

        // Getting the enchantability of the item
        Enchantable enchantable = item.getType().getDefaultData(DataComponentTypes.ENCHANTABLE);
        if (enchantable == null) return;

        for (int i = 0; i < 3; i++) {
            // Ofsetting the rng seed
            rand.setSeed(worldSeed + i);

            // Calculating the initial cost of the enchantment with 15 bookshelves
            // The algorithm changes based on i
            int cost = 0;
            if (i == 0) cost = Math.max((rand.nextInt(8) + 8 + rand.nextInt(16)) / 3, 1);
            if (i == 1) cost = (rand.nextInt(8) + 8 + rand.nextInt(16)) * 2 / 3 + 1;
            if (i == 2) cost = Math.max(rand.nextInt(8) + 8 + rand.nextInt(16), 30);

            // Calculating the final cost of the enchantment
            cost += 1 + rand.nextInt(enchantable.value() / 4 + 1) + rand.nextInt(enchantable.value() / 4 + 1);
            float f = (rand.nextFloat() + rand.nextFloat() - 1) * 0.15F;
            cost = Math.clamp(Math.round(cost + cost * f), 1, Integer.MAX_VALUE);
            final int finalCost = cost;
            
            // Overriding the existing enchantment offers
            if (!inEnchantingTable.isEmpty()) {
                List<EnchantmentOffer> applicableEnchants = inEnchantingTable.resolve(enchantRegistry).stream()
                    .filter(e -> e.getPrimaryItems().contains(TypedKey.create(RegistryKey.ITEM, item.getType().key())) || item.getType() == Material.BOOK)
                    .map(e -> {
                    for (int level = e.getMaxLevel(); level >= e.getStartLevel(); level--) {
                        if (finalCost >= e.getMinModifiedCost(level) && finalCost <= e.getMaxModifiedCost(level)) {
                            return new EnchantmentOffer(e, level, finalCost);
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).toList();
        
                // Overriding the current offer with a random one.
                currentOffers[i] = WeightedRandom.getRandomItem(rand, applicableEnchants, e -> e.getEnchantment().getWeight());
            }
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
                    refund.setAmount(originalCount);
                    event.setItem(refund);
                    (new BukkitRunnable() {
                        public void run() {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 3, 1.5, 0.5, 0.5, 0.01);
                        }
                    }).runTaskLater(plugin, 1L);
                }
            }
        }
    }

    public static void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "emerald")) return;

        // Applying effects for the emerald spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        boolean isAugmented = plugin.getEffectManager().getEffect(playerUUID, "1") == EffectMapping.AUG_EMERALD || plugin.getEffectManager().getEffect(playerUUID, "2") == EffectMapping.AUG_EMERALD;
        long cooldown = plugin.getConfig("emerald.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = plugin.getConfig("emerald.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "emerald", duration);
        CooldownManager.setCooldown(playerUUID, "emerald", cooldown);
    }
}