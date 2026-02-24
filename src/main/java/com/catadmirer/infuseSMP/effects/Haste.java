package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.ItemUtil;
import java.util.UUID;
import org.bukkit.Bukkit;
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

public class Haste implements Listener {
    private static Infuse plugin;

    public Haste(Infuse plugin) {
        Haste.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isPickaxe(item) || ItemUtil.isAxe(item) || ItemUtil.isShovel(item) || ItemUtil.isHoe(item)) {
            item.addUnsafeEnchantment(Enchantment.FORTUNE, 5);
            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 10);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "haste")) return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);
        long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);

        CooldownManager.setDuration(playerUUID, "haste", duration);
        CooldownManager.setCooldown(playerUUID, "haste", cooldown);

        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 15, 3));
    }

    @EventHandler
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.SHIELD && player.isBlocking() && plugin.getDataManager().hasEffect(player, EffectMapping.HASTE)) {
            if (!(event.getDamager() instanceof Player attacker)) return;
            if (!ItemUtil.isAxe(attacker.getInventory().getItemInMainHand())) return;

            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                this.stunShield(player);
            }, 20L);
        }
    }

    private void stunShield(Player player) {
        player.setCooldown(Material.SHIELD, 50);
    }
}