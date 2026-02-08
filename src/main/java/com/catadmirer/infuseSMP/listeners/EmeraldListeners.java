package com.catadmirer.infuseSMP.listeners;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.WeightedRandom;
import com.catadmirer.infuseSMP.effects.Emerald;
import com.catadmirer.infuseSMP.managers.CooldownManager;
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
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class EmeraldListeners implements Listener {
    private final Infuse plugin;

    public EmeraldListeners(Infuse plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void emeraldLooting5(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getDataManager().hasEffect(player, new Emerald())) return;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isSword(item) && item.getEnchantmentLevel(Enchantment.LOOTING) < 5) {
            item.addUnsafeEnchantment(Enchantment.LOOTING, 5);
        }
    }

    @EventHandler
    public void emeraldExpMultiplier(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getDataManager().hasEffect(player, new Emerald())) return;

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
        if (!plugin.getDataManager().hasEffect(event.getEnchanter(), new Emerald())) return;

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
        if (!(plugin.getDataManager().hasEffect(player, new Emerald()))) return;

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
}