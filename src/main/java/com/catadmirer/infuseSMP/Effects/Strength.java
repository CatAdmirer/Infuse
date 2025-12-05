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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

public class Strength implements Listener {
    private final Infuse plugin;

    public Strength(Infuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        this.handleOffhand(event);
    }

    public void handleOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("ability.use")) {
            boolean isPrimary = player.isSneaking() && hasStrengthEquipped(player, "1");
            boolean isSecondary = !player.isSneaking() && hasStrengthEquipped(player, "2");
            if (isPrimary || isSecondary) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "strength")) {
                    event.setCancelled(true);
                    activateSpark(player);
                }
            }
        }
    }

    private boolean hasStrengthEquipped(Player player, String tier) {
        String effectName = plugin.getEffectName("strength");
        String effectName2 = plugin.getEffectName("aug_strength");
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        return currentEffect != null && currentEffect.equals(effectName) || currentEffect != null && currentEffect.equals(effectName2);
    }

    public static void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "strength")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            String effectName2 = Infuse.getInstance().getEffectName("aug_strength");
            boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2)))) ||
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2))));
            long defaultDuration = Infuse.getInstance().getConfig("strength.duration.default");;
            long augmentedDuration = Infuse.getInstance().getConfig("strength.duration.augmented");;
            long duration = isAugmented ? augmentedDuration : defaultDuration;

            long defaultCooldown = Infuse.getInstance().getConfig("strength.cooldown.default");;
            long augmentedCooldown = Infuse.getInstance().getConfig("strength.cooldown.augmented");;
            long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;

            CooldownManager.setDuration(playerUUID, "strength", duration);
            CooldownManager.setCooldown(playerUUID, "strength", cooldown);
        }
    }


    @EventHandler
    public void extraDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (this.hasStrengthEquipped(attacker, "1") || this.hasStrengthEquipped(attacker, "2")) {
                double damage = event.getDamage();
                double health = attacker.getHealth();
                if (health < 2) {
                    event.setDamage(damage + 3);
                } else if (health < 4) {
                    event.setDamage(damage + 2);
                } else if (health < 6) {
                    event.setDamage(damage + 1);
                }
            }
        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (CooldownManager.isEffectActive(player.getUniqueId(), "strength") && !event.isCritical()) {
                double originalDamage = event.getDamage();
                double critDamage = originalDamage * 1.35;
                event.setDamage(critDamage);
                Entity hitEntity = event.getEntity();
                hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                hitEntity.getWorld().spawnParticle(Particle.CRIT, hitEntity.getLocation().add(0, hitEntity.getHeight() / 2, 0), 10);
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getBow() != null && event.getBow().getType() == Material.BOW && this.hasStrengthEquipped(player, "1") || event.getBow() != null && event.getBow().getType() == Material.BOW && this.hasStrengthEquipped(player, "2")) {
                if (event.getProjectile() instanceof Arrow arrow) {
                    arrow.setPierceLevel(100);
                }
            }
        }
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
            meta.setDisplayName(Infuse.getInstance().getEffectName(augmented ? "aug_strength" : "strength"));
            meta.setLore(Infuse.getInstance().getEffectLore(augmented ? "aug_strength" : "strength"));
            meta.setColor(Color.fromRGB(0x8B0000));

            if (augmented) meta.setCustomModelData(999);
            meta.getPersistentDataContainer().set(Infuse.EFFECT_ID, PersistentDataType.INTEGER, augmented ? 21 : 20);

            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isRegular(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 20;
    }

    public static boolean isAugmented(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 21;
    }

    public static boolean isEffect(ItemStack item) {
        return isRegular(item) || isAugmented(item);
    }

    @EventHandler
    public void onEntityDamaeffectob(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (event.getEntity() instanceof LivingEntity entity) {
                if (entity instanceof Player) return;
                if (this.hasStrengthEquipped(attacker, "1") || this.hasStrengthEquipped(attacker, "2")) {
                    double originalDamage = event.getDamage();
                    event.setDamage(originalDamage * 2);
                }
            }
        }

    }

    @EventHandler
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() == Material.SHIELD && player.isBlocking()) {
                if (event.getDamager() instanceof Player attacker) {
                    if (attacker.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE") && this.hasStrengthEquipped(attacker, "1") || attacker.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE") && this.hasStrengthEquipped(attacker, "2")) {
                        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
                        Bukkit.getScheduler().runTaskLater(Infuse.getInstance(), () -> {
                            this.stunShield(player);
                        }, 20L);
                    }
                }
            }
        }

    }

    private void stunShield(Player player) {
        player.setCooldown(Material.SHIELD, 200);
    }
}
