package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.WeightedRandom;
import com.catadmirer.infuseSMP.events.EffectUnequipEvent;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.ItemUtil;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.google.common.collect.Lists;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.tag.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Emerald implements Listener {
    private static Infuse plugin;

    private static final NamespacedKey lootingKey = new NamespacedKey("infuse", "emerald_looting");

    public Emerald(Infuse plugin) {
        Emerald.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 2, false, false));

        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isSword(item)) {
            ItemUtil.applySpecialEnchantment(item, lootingKey, Enchantment.LOOTING, plugin.getMainConfig().emeraldLootingLevel());
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (!(plugin.getDataManager().hasEffect(event.getPlayer().getKiller(), EffectMapping.EMERALD))) return;
        if (event.getView().getType() == InventoryType.PLAYER) return;

        for (ItemStack item : event.getView().getTopInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            ItemUtil.removeSpecialEnchant(item, lootingKey, Enchantment.LOOTING);
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (!(plugin.getDataManager().hasEffect(event.getPlayer(), EffectMapping.EMERALD))) return;
        ItemUtil.removeSpecialEnchant(event.getItemDrop().getItemStack(), lootingKey, Enchantment.LOOTING);
    }

    @EventHandler
    public void onEffectUnequipEvent(EffectUnequipEvent event) {
        if (!(event.getEffect().equals(EffectMapping.EMERALD))) return;

        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            ItemUtil.removeSpecialEnchant(item, lootingKey, Enchantment.LOOTING);
        }
    }

    @EventHandler
    public void tenHitEvent(TenHitEvent event) {
        Infuse.LOGGER.debug("[Emerald] Recieved TenHitEvent");
        Infuse.LOGGER.debug("[Emerald] Attacker: {}", event.getAttacker().getName());
        Infuse.LOGGER.debug("[Emerald] Target: {}", event.getTarget().getName());

        if (!plugin.getDataManager().hasEffect(event.getTarget(), EffectMapping.EMERALD)) return;

        Infuse.LOGGER.debug("[Emerald] Target has emerald effect");
        Infuse.LOGGER.debug("[Emerald] Locking attacker's food and Exp");

        new FoodAndExpLock(event.getAttacker(), plugin.getMainConfig().emeraldLockDurationSeconds());
    }

    public static class FoodAndExpLock implements Listener {
        private final Player player;

        public FoodAndExpLock(Player player, double durationSeconds) {
            this.player = player;

            Bukkit.getPluginManager().registerEvents(this, plugin);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                HandlerList.unregisterAll(this);
                Infuse.LOGGER.debug("[Emerald] Exp lock for {} has been lifted", player.getName());
            }, (long) (durationSeconds * 20));
        }

        /** Preventing the player's food level from changing. */
        @EventHandler
        public void onFoodChange(FoodLevelChangeEvent event) {
            if (event.getEntity().getUniqueId().equals(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        /** Preventing the player's exp level from changing. */
        @EventHandler
        public void onExpChange(PlayerExpChangeEvent event) {
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

        double multiplier = 2;
        if (CooldownManager.isEffectActive(player.getUniqueId(), "emerald")) {
            multiplier = 4;
        }

        int newAmount = (int) Math.round(amount * multiplier);
        orb.setExperience(newAmount);
    }

    public static record EnchantInstance(Enchantment enchantment, int level) {}

    public static int getEnchantmentCost(Random random, int slot, ItemStack item) {
        Enchantable enchantable = item.getData(DataComponentTypes.ENCHANTABLE);
        if (enchantable == null) return 0;

        int i = random.nextInt(8) + 8 + random.nextInt(16);
        
        if (slot == 0) return Math.max(i / 3, 1);

        return slot == 1 ? i * 2 / 3 + 1 : Math.max(i, 30);
    }

    private List<EnchantInstance> getEnchantmentList(Player player, ItemStack item, int index, int baseCost, Random random) {
        random.setSeed(player.getEnchantmentSeed() + index);
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Tag<Enchantment> allEnchantments = registry.getTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE);
        if (allEnchantments.isEmpty()) return List.of();

        List<EnchantInstance> enchants = selectEnchantment(random, item, baseCost, allEnchantments.resolve(registry).stream());
        if (item.getType() == Material.BOOK && enchants.size() > 1) {
            enchants.remove(random.nextInt(enchants.size()));
        }

        return enchants;
    }

    public static List<EnchantInstance> selectEnchantment(Random random, ItemStack item, int cost, Stream<Enchantment> allEnchantments) {

        Enchantable enchantable = item.getData(DataComponentTypes.ENCHANTABLE);
        if (enchantable == null) return List.of();

        cost += 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
        float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        cost = Math.clamp(Math.round(cost + cost * f), 1, Integer.MAX_VALUE);
        List<EnchantInstance> availableEnchants = getAvailableEnchantmentResults(cost, item, allEnchantments);
        if (availableEnchants.isEmpty()) return List.of();

        List<EnchantInstance> selectedEnchantments = new ArrayList<>();
        selectedEnchantments.add(WeightedRandom.getRandomItem(random, availableEnchants, e -> e.enchantment.getWeight()));

        while (random.nextInt(50) <= cost) {
            // Filtering incompatible enchantments
            if (!selectedEnchantments.isEmpty()) {
                filterCompatibleEnchantments(availableEnchants, selectedEnchantments.getLast());
            }

            if (availableEnchants.isEmpty()) break;

            selectedEnchantments.add(WeightedRandom.getRandomItem(random, availableEnchants, e -> e.enchantment.getWeight()));
            cost /= 2;
        }

        return selectedEnchantments;
    }

    public static void filterCompatibleEnchantments(List<EnchantInstance> enchantments, EnchantInstance enchant) {
        enchantments.removeIf(e -> enchant.enchantment().conflictsWith(e.enchantment()));
    }

    public static List<EnchantInstance> getAvailableEnchantmentResults(int cost, ItemStack item, Stream<Enchantment> allEnchantments) {
        List<EnchantInstance> list = Lists.newArrayList();
        boolean flag = item.getType() == Material.BOOK;
        allEnchantments.filter(ench -> {
            if (flag) return true;

            RegistryKeySet<ItemType> primaryItems = ench.getPrimaryItems();
            if (primaryItems == null) {
                primaryItems = ench.getSupportedItems();
            }

            return primaryItems.contains(TypedKey.create(RegistryKey.ITEM, item.getType().key()));
        }).forEach(e -> {
            for (int i = e.getMaxLevel(); i >= 1; i--) {
                if (cost >= e.getMinModifiedCost(i) && cost <= e.getMaxModifiedCost(i)) {
                    list.add(new EnchantInstance(e, i));
                    break;
                }
            }
        });
        return list;
    }

    @EventHandler
    public void emeraldEnchantBonus(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();

        // Skipping non-enchantable items
        if (!item.hasData(DataComponentTypes.ENCHANTABLE)) return;

        // Skipping already enchanted items
        if (!item.getEnchantments().isEmpty()) return;

        // Making sure the enchanter has the emerald effect
        Player player = event.getEnchanter();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.EMERALD)) return;

        // mojang impl:
        EnchantmentOffer[] offers = event.getOffers();

        Random random = new Random(player.getEnchantmentSeed());

        // Calculating the costs
        for (int k = 0; k < 3; k++) {
            int cost = -1;

            Enchantable enchantable = item.getData(DataComponentTypes.ENCHANTABLE);
            if (enchantable == null) {
                offers[k] = null;
                continue;
            }

            int i = random.nextInt(1, 9) + 7 + random.nextInt(0, 16);
            
            if (k == 0) {
                cost = Math.max(i / 3, 1);
            } else if (k == 1) {
                cost = i * 2 / 3 + 1;
            } else {
                cost = Math.max(i, 30);
            }

            if (cost < k + 1) {
                offers[k] = null;
                continue;
            }

            List<EnchantInstance> list = getEnchantmentList(player, item, k, cost, random);
            if (!list.isEmpty()) {
                EnchantInstance enchantmentinstance = list.get(random.nextInt(list.size()));
                offers[k] = new EnchantmentOffer(enchantmentinstance.enchantment(), enchantmentinstance.level(), cost);
            }
        }

        // On enchant:
        // getEnchantmentList(player, item, i, costs[i], random);
    }

    @EventHandler
    public void stealExp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player attacker)) return;
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.EMERALD)) return;

        // Getting configs
        int exp = damaged.getTotalExperience();
        int expPerHit = plugin.getMainConfig().emeraldExpPerHit();

        // Updating the xp of the players
        damaged.setTotalExperience(exp - expPerHit);

        int toGain = (int) (expPerHit * plugin.getMainConfig().emeraldExpPercent());
        attacker.setTotalExperience(attacker.getTotalExperience() + toGain);

        // Calling the exp change event to allow for sharing if the spark is active
        new PlayerExpChangeEvent(attacker, toGain).callEvent();
    }

    @EventHandler
    public void emeraldPreserveConsumables(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Making sure the player has the emerald effect
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.EMERALD)) return;

        ItemStack consumedItem = event.getItem();

        // Not allowing potions to be be preserved
        if (consumedItem.getType() == Material.POTION) return;

        // Getting the chance for the item to not be consumed
        double chance = 0.5;
        if (CooldownManager.isEffectActive(player.getUniqueId(), "emerald")) chance = 0.75;

        // Rolling the dice
        if (Math.random() > chance) return;

        // Refunding the item
        consumedItem.add(1);
        event.setItem(consumedItem);

        // Playing a noise
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 3, 1.5, 0.5, 0.5, 0.01);
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (!CooldownManager.isEffectActive(player.getUniqueId(), "emerald")) return;

        for (OfflinePlayer trusted : plugin.getDataManager().getTrusted(player)) {
            if (!trusted.isOnline()) continue;

            Player trustedPlayer = trusted.getPlayer();
            int toGain = (int) (event.getAmount() * plugin.getMainConfig().emeraldPercentExpToShare());
            trustedPlayer.setTotalExperience(trustedPlayer.getTotalExperience() + toGain);

            // Not calling PlayerExpChangeEvent to prevent infinite looping
        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "emerald")) return;

        // Applying effects for the emerald spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_EMERALD : EffectMapping.EMERALD);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_EMERALD : EffectMapping.EMERALD);

        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, (int) duration * 20, 4));

        CooldownManager.setTimes(playerUUID, "emerald", duration, cooldown);
    }
}
