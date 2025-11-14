package com.catadmirer.infuseSMP.Effects;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Haste implements Listener {
    
    public Haste(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (Haste.this.hasImmortalHackEquipped2(player, "1") || (Haste.this.hasImmortalHackEquipped2(player, "2"))) {
                        Haste.this.enchantItemIfApplicable(player);
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    public static ItemStack createEffect() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("haste");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("haste");
            meta.setColor(Color.fromRGB(255, 204, 51));
            meta.setLore(lore);
            meta.setCustomModelData(5);
            gem.setItemMeta(meta);
        }

        return gem;
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
                if (!CooldownManager.isOnCooldown(playerUUID, "haste")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasImmortalHackEquipped2(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = Infuse.getInstance().getEffect("aug_haste");
        String gemName2 = Infuse.getInstance().getEffect("haste");
        return currentHack != null && (currentHack.equals(gemName2) || currentHack.equals(gemName));
    }

    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "haste")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            String gemName = Infuse.getInstance().getEffect("aug_haste");
            boolean isAugmentedHaste =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName)));
            long hasteDefaultCooldown = Infuse.getInstance().getCanfig("haste.cooldown.default");
            long hasteAugmentedCooldown = Infuse.getInstance().getCanfig("haste.cooldown.augmented");
            long hasteCooldown = isAugmentedHaste ? hasteAugmentedCooldown : hasteDefaultCooldown;

            long hasteDefaultDuration = Infuse.getInstance().getCanfig("haste.duration.default");
            long hasteAugmentedDuration = Infuse.getInstance().getCanfig("haste.duration.augmented");
            long hasteDuration = isAugmentedHaste ? hasteAugmentedDuration : hasteDefaultDuration;
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 15, 3));
            CooldownManager.setDuration(playerUUID, "haste", hasteDuration);
            CooldownManager.setCooldown(playerUUID, "haste", hasteCooldown);
        }
    }


    @EventHandler
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() == Material.SHIELD && player.isBlocking() && this.hasImmortalHackEquipped2(player, "1") || (offHand.getType() == Material.SHIELD && player.isBlocking() && this.hasImmortalHackEquipped2(player, "2"))) {
                if (event.getDamager() instanceof Player attacker) {
                    if (attacker.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
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
        player.setCooldown(Material.SHIELD, 50);
    }

    public static boolean isEffect(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 5;
    }
}
