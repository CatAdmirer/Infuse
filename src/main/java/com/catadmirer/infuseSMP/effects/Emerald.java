package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InfuseDebug;
import com.catadmirer.infuseSMP.WeightedRandom;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.ItemUtil;
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Emerald implements Listener {
    private static Infuse plugin;

    public Emerald(Infuse plugin) {
        Emerald.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 2, false, false));

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (ItemUtil.isSword(mainHand) && mainHand.getEnchantmentLevel(Enchantment.LOOTING) < 5) {
            mainHand.addUnsafeEnchantment(Enchantment.LOOTING, 5);
        }
    }

    @EventHandler
    public void tenHitEvent(TenHitEvent event) {
        InfuseDebug.log("[Emerald] Recieved TenHitEvent");
        InfuseDebug.log("[Emerald] Attacker: {}", event.getAttacker().getName());
        InfuseDebug.log("[Emerald] Target: {}", event.getTarget().getName());

        if (!plugin.getDataManager().hasEffect(event.getTarget(), EffectMapping.EMERALD)) return;

        InfuseDebug.log("[Emerald] Target has emerald effect");
        InfuseDebug.log("[Emerald] Locking attacker's food and XP");

        new FoodAndXPLock(event.getAttacker(), plugin.getConfigFile().emeraldLockDurationSeconds());
    }

    public static class FoodAndXPLock implements Listener {
        private final Player player;

        public FoodAndXPLock(Player player, double durationSeconds) {
            this.player = player;
            
            Bukkit.getPluginManager().registerEvents(this, plugin);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                HandlerList.unregisterAll(this);
            }, (long) (durationSeconds * 20));
        }

        /** Preventing the player's food level from changing. */
        @EventHandler
        public void onFoodChange(FoodLevelChangeEvent event) {
            if (event.getEntity().getUniqueId().equals(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        /** Preventing the player's xp level from changing. */
        @EventHandler
        public void onXPChange(PlayerExpChangeEvent event) {
            if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                event.setAmount(0);
            }
        }
    }

    @EventHandler
    public void emeraldExpMultiplier(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getDataManager().hasEffect(player, EffectMapping.EMERALD)) return;

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
        if (!plugin.getDataManager().hasEffect(event.getEnchanter(), EffectMapping.EMERALD)) return;

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
    public void emeraldPreserveConsumables(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Making sure the player has the emerald effect
        if (!(plugin.getDataManager().hasEffect(player, EffectMapping.EMERALD))) return;

        ItemStack consumedItem = event.getItem();

        // Not allowing potions to be be preserved
        if (consumedItem.getType() == Material.POTION) return;

        // Getting the chance for the item to not be consumed
        double chance = 0.15;
        if (CooldownManager.isEffectActive(player.getUniqueId(), "emerald")) chance = 0.25;

        // Rolling the dice
        if (Math.random() > chance) return;

        // Refunding the item
        consumedItem.add(1);
        event.setItem(consumedItem);

        // Playing a noise
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 3, 1.5, 0.5, 0.5, 0.01);
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "emerald")) return;

        // Applying effects for the emerald spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_EMERALD : EffectMapping.EMERALD);
        long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_EMERALD : EffectMapping.EMERALD);

        CooldownManager.setDuration(playerUUID, "emerald", duration);
        CooldownManager.setCooldown(playerUUID, "emerald", cooldown);
    }
}