package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.WeightedRandom;
import com.catadmirer.infuseSMP.managers.CooldownManager;
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
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.PotionMeta;
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
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!hasEffect(onlinePlayer)) continue;

                    ItemStack mainHand = onlinePlayer.getInventory().getItemInMainHand();
                    Emerald.this.applyPassiveEffects(onlinePlayer);
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

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffect("emerald");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("emerald");
            meta.setColor(Color.LIME);
            meta.setLore(lore);
            meta.setCustomModelData(1);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean hasEffect(Player player) {
        return hasEffect(player, "1") || hasEffect(player, "2");
    }

    public static boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffect("emerald");
        String effectName2 = Infuse.getInstance().getEffect("aug_emerald");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
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
        if (hasEffect(player)) {
            double multiplier = 1.5;
            PotionEffect heroEffect = player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
            if (heroEffect != null && heroEffect.getAmplifier() >= 200) {
                multiplier = 3;
            }

            event.setAmount((int) (event.getAmount() * multiplier));
        }

    }

    @EventHandler
    public void emeraldEnchantBonus(PrepareItemEnchantEvent event) {
        // Setting the enchantment bonus to 15 if the function exists
        if (!Emerald.hasEffect(event.getEnchanter())) return;

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
            if (hasEffect(player)) {
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

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (!player.hasPermission("ability.use")) {
            boolean isPrimary = hasEffect(player, "1");
            boolean isSecondary = hasEffect(player, "2");
            if (!CooldownManager.isOnCooldown(playerUUID, "emerald")) {
                if (player.isSneaking() && isPrimary || !player.isSneaking() && isSecondary) {
                    if (CooldownManager.isOnCooldown(playerUUID, "emerald")) {
                        return;
                    }

                    event.setCancelled(true);
                    activateSpark(player);
                }
            }
        }
    }

    public static void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "emerald")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

            String augmentedName = ChatColor.stripColor(Infuse.getInstance().getEffect("aug_emerald").toLowerCase());
            boolean isAugmented = augmentedName.equalsIgnoreCase(ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1"))) ||
                                  augmentedName.equalsIgnoreCase(ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")));

            long cooldown = Infuse.getInstance().getConfig(isAugmented ? "emerald.cooldown.augmented" : "emerald.cooldown.default");
            long duration = Infuse.getInstance().getConfig(isAugmented ? "emerald.duration.augmented" : "emerald.duration.default");

            CooldownManager.setDuration(playerUUID, "emerald", duration);
            CooldownManager.setCooldown(playerUUID, "emerald", cooldown);
        }
    }

    public static boolean isEffect(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 1;
    }
}
