package com.catadmirer.infuseSMP.Effects;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Heart implements Listener {
    
    private final Map<UUID, Map<UUID, Integer>> hitCounts = new HashMap();

    private final Infuse plugin;

    public Heart(Infuse plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, Infuse.getInstance());
        this.startHealthCheckTask();
        this.plugin = plugin;
    }

    private void startHealthCheckTask() {
        (new BukkitRunnable() {
            public void run() {
                Iterator var1 = Bukkit.getOnlinePlayers().iterator();

                while(true) {
                    Player player;
                    AttributeInstance maxHealthAttribute;
                    double currentMaxHealth;
                    do {
                        do {
                            if (!var1.hasNext()) {
                                return;
                            }

                            player = (Player)var1.next();
                            maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        } while(maxHealthAttribute == null);

                        currentMaxHealth = maxHealthAttribute.getBaseValue();
                    } while(!Heart.this.hasImmortalHackEquipped2(player, "1") && !Heart.this.hasImmortalHackEquipped2(player, "2"));

                    if (currentMaxHealth == 20.0D) {
                        maxHealthAttribute.setBaseValue(30.0D);
                    }
                }
            }
        }).runTaskTimer(Infuse.getInstance(), 0L, 20L);
    }

    public static ItemStack createHeart() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("heart");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("heart");
            meta.setColor(Color.RED);
            meta.setLore(lore);
            meta.setCustomModelData(6);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static boolean isHeartEffect(ItemStack item) {
        String gemName = Infuse.getInstance().getEffect("heart");
        if (item != null && item.getType() == Material.POTION) {
            ItemMeta meta = item.getItemMeta();
            return meta != null && meta.getDisplayName().equals(gemName) && meta.getCustomModelData() == 6;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity var3 = event.getDamager();
        if (var3 instanceof Player) {
            Player player = (Player)var3;
            Entity var4 = event.getEntity();
            if (var4 instanceof LivingEntity) {
                LivingEntity target = (LivingEntity)var4;
                if (this.hasImmortalHackEquipped2(player, "1") || this.hasImmortalHackEquipped2(player, "2")) {
                    UUID playerUUID = player.getUniqueId();
                    UUID targetUUID = target.getUniqueId();
                    this.hitCounts.putIfAbsent(playerUUID, new HashMap());
                    Map<UUID, Integer> playerHits = (Map)this.hitCounts.get(playerUUID);
                    int hitCount = (Integer)playerHits.getOrDefault(targetUUID, 0) + 1;
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
                    entity.setCustomName((String)null);
                }
            }
        };
        updateTask.runTaskTimer(Infuse.getInstance(), 0L, 10L);
        (new BukkitRunnable() {
            public void run() {
                updateTask.cancel();
                entity.setCustomNameVisible(false);
                entity.setCustomName((String)null);
            }
        }).runTaskLater(Infuse.getInstance(), 200L);
    }

    private void updateHealthDisplay(LivingEntity entity) {
        double health = entity.getHealth();
        String var10000 = String.valueOf(ChatColor.RED);
        String healthText = var10000 + String.valueOf(ChatColor.BOLD) + "❤ " + String.format("%.1f", health);
        entity.setCustomName(healthText);
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (this.hasImmortalHackEquipped2(player, "1") || this.hasImmortalHackEquipped2(player, "2")) {
            ItemStack item = event.getItem();
            if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 4));
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 0));
            }
        }

    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        this.handleOffhand(event);
    }

    public void handleOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("ability.use")) {
            boolean isLegendary = player.isSneaking() && this.hasImmortalHackEquipped2(player, "1");
            boolean isCommon = !player.isSneaking() && this.hasImmortalHackEquipped2(player, "2");
            if (isLegendary || isCommon) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "heart")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasImmortalHackEquipped2(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = Infuse.getInstance().getEffect("heart");
        String gemName2 = Infuse.getInstance().getEffect("aug_heart");
        return currentHack != null && (currentHack.equals(gemName) || currentHack.equals(gemName2));
    }

    public void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "heart")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);

            final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttribute != null) {
                maxHealthAttribute.setBaseValue(40.0D);
            }
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            String gemName2 = Infuse.getInstance().getEffect("aug_heart");
            boolean isAugmentedHeart =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2)));
            long defaultCooldown = ((Integer) Infuse.getInstance().getCanfig("heart.cooldown.default")).longValue();
            long augmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("heart.cooldown.augmented")).longValue();
            long cooldown = isAugmentedHeart ? augmentedCooldown : defaultCooldown;

            long defaultDuration = ((Integer) Infuse.getInstance().getCanfig("heart.duration.default")).longValue();
            long augmentedDuration = ((Integer) Infuse.getInstance().getCanfig("heart.duration.augmented")).longValue();
            long duration = isAugmentedHeart ? augmentedDuration : defaultDuration;

            CooldownManager.setDuration(playerUUID, "heart", duration);
            CooldownManager.setCooldown(playerUUID, "heart", cooldown);

            new BukkitRunnable() {
                public void run() {
                    if (maxHealthAttribute != null) {
                        maxHealthAttribute.setBaseValue(20.0D);
                    }
                    player.sendMessage(ChatColor.RED + "Your Health Boost has ended.");
                }
            }.runTaskLater(plugin, duration * 20L);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        (new BukkitRunnable() {
            public void run() {
                AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20.0D);
                }

            }
        }).runTaskLater(Infuse.getInstance(), 15L);
    }
}