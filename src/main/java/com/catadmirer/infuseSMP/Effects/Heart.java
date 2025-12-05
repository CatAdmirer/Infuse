package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.util.EffectUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Heart implements Listener {

    private final Map<UUID, Map<UUID, Integer>> hitCounts = new HashMap<>();

    public Heart(Infuse plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, Infuse.getInstance());
        this.startHealthCheckTask();
    }

    private void startHealthCheckTask() {
        (new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealthAttribute == null) continue;
                    double currentMaxHealth = maxHealthAttribute.getBaseValue();
                    if (!Heart.this.hasEffect(player, "1") && !Heart.this.hasEffect(player, "2")) continue;

                    if (currentMaxHealth == 20) maxHealthAttribute.setBaseValue(30);
                }
            }
        }).runTaskTimer(Infuse.getInstance(), 0L, 20L);
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
            meta.setDisplayName(Infuse.getInstance().getEffectName(augmented ? "aug_heart" : "heart"));
            meta.setLore(Infuse.getInstance().getEffectLore(augmented ? "aug_heart" : "heart"));
            meta.setColor(Color.RED);

            if (augmented) meta.setCustomModelData(999);
            meta.getPersistentDataContainer().set(Infuse.EFFECT_ID, PersistentDataType.INTEGER, augmented ? 11 : 10);

            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isRegular(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 10;
    }

    public static boolean isAugmented(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 11;
    }

    public static boolean isEffect(ItemStack item) {
        return isRegular(item) || isAugmented(item);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (event.getEntity() instanceof LivingEntity target) {
                if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
                    UUID playerUUID = player.getUniqueId();
                    UUID targetUUID = target.getUniqueId();
                    this.hitCounts.putIfAbsent(playerUUID, new HashMap<>());
                    Map<UUID, Integer> playerHits = this.hitCounts.get(playerUUID);
                    int hitCount = playerHits.getOrDefault(targetUUID, 0) + 1;
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
                    entity.setCustomName(null);
                }
            }
        };
        updateTask.runTaskTimer(Infuse.getInstance(), 0L, 10L);
        (new BukkitRunnable() {
            public void run() {
                updateTask.cancel();
                entity.setCustomNameVisible(false);
                entity.setCustomName(null);
            }
        }).runTaskLater(Infuse.getInstance(), 200L);
    }

    private void updateHealthDisplay(LivingEntity entity) {
        double health = entity.getHealth();
        String healthText = "§c§l❤ " + String.format("%.1f", health);
        entity.setCustomName(healthText);
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
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
            boolean isPrimary = player.isSneaking() && this.hasEffect(player, "1");
            boolean isSecondary = !player.isSneaking() && this.hasEffect(player, "2");
            if (isPrimary || isSecondary) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "heart")) {
                    event.setCancelled(true);
                    activateSpark(player);
                }
            }
        }
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffectName("heart");
        String effectName2 = Infuse.getInstance().getEffectName("aug_heart");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
    }

    public static void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "heart")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

            final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttribute != null) {
                maxHealthAttribute.setBaseValue(40);
            }
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            String effectName2 = Infuse.getInstance().getEffectName("aug_heart");
            boolean isAugmentedHeart =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));
            long defaultCooldown = Infuse.getInstance().getConfig("heart.cooldown.default");
            long augmentedCooldown = Infuse.getInstance().getConfig("heart.cooldown.augmented");
            long cooldown = isAugmentedHeart ? augmentedCooldown : defaultCooldown;

            long defaultDuration = Infuse.getInstance().getConfig("heart.duration.default");
            long augmentedDuration = Infuse.getInstance().getConfig("heart.duration.augmented");
            long duration = isAugmentedHeart ? augmentedDuration : defaultDuration;

            CooldownManager.setDuration(playerUUID, "heart", duration);
            CooldownManager.setCooldown(playerUUID, "heart", cooldown);

            new BukkitRunnable() {
                public void run() {
                    if (maxHealthAttribute != null) {
                        maxHealthAttribute.setBaseValue(20);
                    }
                    player.sendMessage("§cYour Health Boost has ended.");
                }
            }.runTaskLater(Infuse.getInstance(), duration * 20L);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        (new BukkitRunnable() {
            public void run() {
                AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20);
                }

            }
        }).runTaskLater(Infuse.getInstance(), 15L);
    }
}