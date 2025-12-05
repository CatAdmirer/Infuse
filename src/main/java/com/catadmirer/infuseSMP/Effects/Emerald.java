package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.util.EffectUtil;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
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
                    if (!Emerald.this.hasEffect(onlinePlayer, "1") && !Emerald.this.hasEffect(onlinePlayer, "2")) continue;

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

    public static ItemStack createRegular() {
        return createEffect(false);
    }

    public static ItemStack createAugmented() {
        return createEffect(true);
    }

    public static ItemStack createEffect(boolean augmented) {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName(augmented ? "aug_emerald" : "emerald"));
            meta.setLore(Infuse.getInstance().getEffectLore(augmented ? "aug_emerald" : "emerald"));
            meta.setColor(Color.LIME);

            if (augmented) meta.setCustomModelData(999);
            meta.getPersistentDataContainer().set(Infuse.EFFECT_ID, PersistentDataType.INTEGER, augmented ? 1 : 0);

            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isRegular(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 0;
    }

    public static boolean isAugmented(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 1;
    }

    public static boolean isEffect(ItemStack item) {
        return isRegular(item) || isAugmented(item);
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffectName("emerald");
        String effectName2 = Infuse.getInstance().getEffectName("aug_emerald");
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
        if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
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

        if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
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
            if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
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
            boolean isPrimary = this.hasEffect(player, "1");
            boolean isSecondary = this.hasEffect(player, "2");
            if (!CooldownManager.isOnCooldown(playerUUID, "emerald")) {
                if (player.isSneaking() && isPrimary || !player.isSneaking() && isSecondary) {
                    if (CooldownManager.isOnCooldown(playerUUID, "emerald")) {
                        return;
                    }

                    event.setCancelled(true);
                    this.activateSpark(player);
                }

            }
        }
    }

    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "emerald")) {
            String effectName2 = Infuse.getInstance().getEffectName("aug_emerald");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
            boolean isAugmentedEme = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)))
                    || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));

            long emeDefaultCooldown = Infuse.getInstance().getConfig("emerald.cooldown.default");
            long emeAugmentedCooldown = Infuse.getInstance().getConfig("emerald.cooldown.augmented");
            long emeCooldown = isAugmentedEme ? emeAugmentedCooldown : emeDefaultCooldown;

            long emeDefaultDuration = Infuse.getInstance().getConfig("emerald.duration.default");
            long emeAugmentedDuration = Infuse.getInstance().getConfig("emerald.duration.augmented");
            long emeDuration = isAugmentedEme ? emeAugmentedDuration : emeDefaultDuration;

            CooldownManager.setDuration(playerUUID, "emerald", emeDuration);
            CooldownManager.setCooldown(playerUUID, "emerald", emeCooldown);
            (new BukkitRunnable() {
                public void run() {
                }
            }).runTaskLater(Infuse.getInstance(), 600L);
        }
    }
}
