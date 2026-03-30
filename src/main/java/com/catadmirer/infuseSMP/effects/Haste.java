package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.events.EffectUnequipEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.ItemUtil;
import java.util.UUID;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Haste implements Listener {

    private static Infuse plugin;

    private static NamespacedKey fortuneKey;
    private static NamespacedKey efficiencyKey;
    private static NamespacedKey unbreakingKey;

    public Haste(Infuse plugin) {
        Haste.plugin = plugin;

        Haste.fortuneKey = new NamespacedKey(plugin, "fortuneLevel");
        Haste.efficiencyKey = new NamespacedKey(plugin, "efficiencyLevel");
        Haste.unbreakingKey = new NamespacedKey(plugin, "unbreakingLevel");
    }

    public static void applyPassiveEffects(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isPickaxe(item) || ItemUtil.isAxe(item) || ItemUtil.isShovel(item) || ItemUtil.isHoe(item)) {

            item.editMeta(meta -> {
                meta.getPersistentDataContainer().set(fortuneKey, PersistentDataType.INTEGER, item.getEnchantmentLevel(Enchantment.FORTUNE));
                meta.getPersistentDataContainer().set(efficiencyKey, PersistentDataType.INTEGER, item.getEnchantmentLevel(Enchantment.EFFICIENCY));
                meta.getPersistentDataContainer().set(unbreakingKey, PersistentDataType.INTEGER, item.getEnchantmentLevel(Enchantment.UNBREAKING));
            });

            item.addUnsafeEnchantment(Enchantment.FORTUNE, 5);
            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 10);
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);

            player.getInventory().setItemInMainHand(item);
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().equals(event.getPlayer().getInventory())) return;

        int slot = -1;
        for (ItemStack item : event.getView().getTopInventory().getContents()) {
            slot++;

            if (item == null || item.getType() == Material.AIR) continue;

            final PersistentDataContainerView container = item.getPersistentDataContainer();
            if (!(container.has(fortuneKey, PersistentDataType.INTEGER)) || !(container.has(efficiencyKey, PersistentDataType.INTEGER)) || !(container.has(unbreakingKey, PersistentDataType.INTEGER))) return;

            final Integer fortune = container.get(fortuneKey, PersistentDataType.INTEGER);
            final Integer efficiency = container.get(efficiencyKey, PersistentDataType.INTEGER);
            final Integer unbreaking = container.get(unbreakingKey, PersistentDataType.INTEGER);

            item.editMeta(meta -> {
                meta.removeEnchant(Enchantment.FORTUNE);
                meta.removeEnchant(Enchantment.EFFICIENCY);
                meta.removeEnchant(Enchantment.UNBREAKING);

                if (fortune != null && fortune > 0) meta.addEnchant(Enchantment.FORTUNE, fortune, false);
                if (efficiency != null && efficiency > 0) meta.addEnchant(Enchantment.FORTUNE, efficiency, false);
                if (unbreaking != null && unbreaking > 0) meta.addEnchant(Enchantment.FORTUNE, unbreaking, false);

                meta.getPersistentDataContainer().remove(fortuneKey);
                meta.getPersistentDataContainer().remove(efficiencyKey);
                meta.getPersistentDataContainer().remove(unbreakingKey);
            });

            event.getView().getTopInventory().setItem(slot, item);
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        final ItemStack item = event.getItemDrop().getItemStack();
        final PersistentDataContainerView container = item.getPersistentDataContainer();

        if (!(container.has(fortuneKey, PersistentDataType.INTEGER) || container.has(efficiencyKey, PersistentDataType.INTEGER) || container.has(unbreakingKey, PersistentDataType.INTEGER))) return;

        final Integer fortune = container.get(fortuneKey, PersistentDataType.INTEGER);
        final Integer efficiency = container.get(efficiencyKey, PersistentDataType.INTEGER);
        final Integer unbreaking = container.get(unbreakingKey, PersistentDataType.INTEGER);

        item.editMeta(meta -> {
            meta.removeEnchant(Enchantment.FORTUNE);
            meta.removeEnchant(Enchantment.EFFICIENCY);
            meta.removeEnchant(Enchantment.UNBREAKING);

            if (fortune != null && fortune > 0) meta.addEnchant(Enchantment.FORTUNE, fortune, false);
            if (efficiency != null && efficiency > 0) meta.addEnchant(Enchantment.FORTUNE, efficiency, false);
            if (unbreaking != null && unbreaking > 0) meta.addEnchant(Enchantment.FORTUNE, unbreaking, false);

            meta.getPersistentDataContainer().remove(fortuneKey);
            meta.getPersistentDataContainer().remove(efficiencyKey);
            meta.getPersistentDataContainer().remove(unbreakingKey);
        });

        event.getItemDrop().setItemStack(item);
    }

    @EventHandler
    public void onEffectUnequipEvent(EffectUnequipEvent event) {
        if (!(event.getEffect().equals(EffectMapping.EMERALD))) return;

        int slot = -1;
        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            slot++;

            if (item == null || item.getType() == Material.AIR) continue;

            final PersistentDataContainerView container = item.getPersistentDataContainer();
            if (!(container.has(fortuneKey, PersistentDataType.INTEGER)) || !(container.has(efficiencyKey, PersistentDataType.INTEGER)) || !(container.has(unbreakingKey, PersistentDataType.INTEGER))) return;

            final Integer fortune = container.get(fortuneKey, PersistentDataType.INTEGER);
            final Integer efficiency = container.get(efficiencyKey, PersistentDataType.INTEGER);
            final Integer unbreaking = container.get(unbreakingKey, PersistentDataType.INTEGER);

            item.editMeta(meta -> {
                meta.removeEnchant(Enchantment.FORTUNE);
                meta.removeEnchant(Enchantment.EFFICIENCY);
                meta.removeEnchant(Enchantment.UNBREAKING);

                if (fortune != null && fortune > 0) meta.addEnchant(Enchantment.FORTUNE, fortune, false);
                if (efficiency != null && efficiency > 0) meta.addEnchant(Enchantment.FORTUNE, efficiency, false);
                if (unbreaking != null && unbreaking > 0) meta.addEnchant(Enchantment.FORTUNE, unbreaking, false);

                meta.getPersistentDataContainer().remove(fortuneKey);
                meta.getPersistentDataContainer().remove(efficiencyKey);
                meta.getPersistentDataContainer().remove(unbreakingKey);
            });

            event.getPlayer().getInventory().setItem(slot, item);
        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "haste")) return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);

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