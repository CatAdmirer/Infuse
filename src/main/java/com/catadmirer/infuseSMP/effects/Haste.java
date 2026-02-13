package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Haste implements Listener {
    private static Infuse plugin;

    public Haste(Infuse plugin) {
        Haste.plugin = plugin;
        (new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.getDataManager().hasEffect(player, EffectMapping.HASTE)) {
                        Haste.this.enchantItemIfApplicable(player);
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    private void enchantItemIfApplicable(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() != Material.AIR) {
            Set<Material> validTools = EnumSet.of(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE, Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE, Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL);
            if (validTools.contains(item.getType())) {
                item.addUnsafeEnchantment(Enchantment.FORTUNE, 5);
                item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 10);
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
            }

        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "haste")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);
            long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);

            CooldownManager.setDuration(playerUUID, "haste", duration);
            CooldownManager.setCooldown(playerUUID, "haste", cooldown);

            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 15, 3));
        }
    }


    @EventHandler
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() == Material.SHIELD && player.isBlocking() && plugin.getDataManager().hasEffect(player, EffectMapping.HASTE)) {
                if (event.getDamager() instanceof Player attacker) {
                    if (attacker.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
                        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            this.stunShield(player);
                        }, 20L);
                    }
                }
            }
        }

    }

    private void stunShield(Player player) {
        player.setCooldown(Material.SHIELD, 50);
    }
}
