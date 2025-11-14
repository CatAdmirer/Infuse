package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import java.util.List;
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
            boolean isLegendary = player.isSneaking() && hasStrengthEquipped(player, "1");
            boolean isCommon = !player.isSneaking() && hasStrengthEquipped(player, "2");
            if (isLegendary || isCommon) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "strength")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasStrengthEquipped(Player player, String tier) {
        String gemName = plugin.getEffect("strength");
        String gemName2 = plugin.getEffect("aug_strength");
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        return currentHack != null && currentHack.equals(gemName) || currentHack != null && currentHack.equals(gemName2);
    }

    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "strength")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            String gemName2 = plugin.getEffect("aug_strength");
            boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(gemName2)))) ||
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(gemName2))));
            long defaultDuration = Infuse.getInstance().getCanfig("strength.duration.default");;
            long augmentedDuration = Infuse.getInstance().getCanfig("strength.duration.augmented");;
            long duration = isAugmented ? augmentedDuration : defaultDuration;

            long defaultCooldown = Infuse.getInstance().getCanfig("strength.cooldown.default");;
            long augmentedCooldown = Infuse.getInstance().getCanfig("strength.cooldown.augmented");;
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
                if (health < 2.0) {
                    event.setDamage(damage + 3.0);
                } else if (health < 4.0) {
                    event.setDamage(damage + 2.0);
                } else if (health < 6.0) {
                    event.setDamage(damage + 1.0);
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
                hitEntity.getWorld().spawnParticle(Particle.CRIT, hitEntity.getLocation().add(0.0, hitEntity.getHeight() / 2.0, 0.0), 10);
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

    public static ItemStack createStealthGem() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("strength");
            meta.setDisplayName(gemName);
            meta.setColor(Color.fromRGB(139, 0, 0));
            List<String> lore = Infuse.getInstance().getEffectLore("strength");
            meta.setLore(lore);
            meta.setCustomModelData(11);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    @EventHandler
    public void onEntityDamageMob(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (event.getEntity() instanceof LivingEntity entity) {
                if (entity instanceof Player) return;
                if (this.hasStrengthEquipped(attacker, "1") || this.hasStrengthEquipped(attacker, "2")) {
                    double originalDamage = event.getDamage();
                    event.setDamage(originalDamage * 2.0);
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

    public static boolean isStealthGem(ItemStack item) {
        if (item != null && item.getType() == Material.POTION && item.getItemMeta() != null) {
            return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 11;
        } else {
            return false;
        }
    }

    private void stunShield(Player player) {
        player.setCooldown(Material.SHIELD, 200);
    }
}
