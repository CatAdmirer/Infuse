package com.catadmirer.infuseSMP.Effects;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;

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
                Iterator var1 = Bukkit.getOnlinePlayers().iterator();

                while(true) {
                    Player onlinePlayer;
                    do {
                        if (!var1.hasNext()) {
                            return;
                        }

                        onlinePlayer = (Player)var1.next();
                    } while(!Emerald.this.hasImmortalHackEquipped(onlinePlayer, "1") && !Emerald.this.hasImmortalHackEquipped(onlinePlayer, "2"));

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

    public static ItemStack createInvincibilityGem() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("emerald");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("emerald");
            meta.setColor(Color.fromRGB(0, 255, 0));
            meta.setLore(lore);
            meta.setCustomModelData(1);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    private boolean hasImmortalHackEquipped(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = Infuse.getInstance().getEffect("emerald");
        String gemName2 = Infuse.getInstance().getEffect("aug_emerald");
        return currentHack != null && (currentHack.equals(gemName) || currentHack.equals(gemName2));
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
        if (this.hasImmortalHackEquipped(player, "1") || this.hasImmortalHackEquipped(player, "2")) {
            double multiplier = 1.5D;
            PotionEffect heroEffect = player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
            if (heroEffect != null && heroEffect.getAmplifier() >= 200) {
                multiplier = 3.0D;
            }

            event.setAmount((int)((double)event.getAmount() * multiplier));
        }

    }

    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getEnchanter();

        if (this.hasImmortalHackEquipped(player, "1") || this.hasImmortalHackEquipped(player, "2")) {
            try {
                event.getClass()
                        .getMethod("setEnchantmentBonus", int.class)
                        .invoke(event, 15);
            } catch (Exception ignored) {
            }
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            final Player player = event.getPlayer();
            ItemStack consumedItem = event.getItem();
            int originalCount = consumedItem.getAmount();
            if (this.hasImmortalHackEquipped(player, "1") || this.hasImmortalHackEquipped(player, "2")) {
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
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 3, 1.5D, 0.5D, 0.5D, 0.01D);
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
            boolean isLegendary = this.hasImmortalHackEquipped(player, "1");
            boolean isCommon = this.hasImmortalHackEquipped(player, "2");
            if (!CooldownManager.isOnCooldown(playerUUID, "emerald")) {
                if (player.isSneaking() && isLegendary || !player.isSneaking() && isCommon) {
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
            String gemName2 = Infuse.getInstance().getEffect("aug_emerald");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
            boolean isAugmentedEme = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2)))
                    || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2)));

            long emeDefaultCooldown = ((Integer) Infuse.getInstance().getCanfig("emerald.cooldown.default")).longValue();
            long emeAugmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("emerald.cooldown.augmented")).longValue();
            long emeCooldown = isAugmentedEme ? emeAugmentedCooldown : emeDefaultCooldown;

            long emeDefaultDuration = ((Integer) Infuse.getInstance().getCanfig("emerald.duration.default")).longValue();
            long emeAugmentedDuration = ((Integer) Infuse.getInstance().getCanfig("emerald.duration.augmented")).longValue();
            long emeDuration = isAugmentedEme ? emeAugmentedDuration : emeDefaultDuration;

            CooldownManager.setDuration(playerUUID, "emerald", emeDuration);
            CooldownManager.setCooldown(playerUUID, "emerald", emeCooldown);
            (new BukkitRunnable() {
                public void run() {
                }
            }).runTaskLater(Infuse.getInstance(), 600L);
        }
    }

    public static boolean isInvincibilityGem(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 1;
    }
}
