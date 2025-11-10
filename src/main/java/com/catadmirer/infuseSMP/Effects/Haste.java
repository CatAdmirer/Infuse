package com.catadmirer.infuseSMP.Effects;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
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
    
    private final Plugin plugin;

    public Haste(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                Iterator<? extends Player> var1 = Bukkit.getOnlinePlayers().iterator();

                while(var1.hasNext()) {
                    Player player = (Player)var1.next();
                    if (Haste.this.hasImmortalHackEquipped2(player, "1") || (Haste.this.hasImmortalHackEquipped2(player, "2"))) {
                        Haste.this.enchantItemIfApplicable(player);
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    public static ItemStack createFake() {
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
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
            String gemName = Infuse.getInstance().getEffect("aug_haste");
            boolean isAugmentedHaste =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName)));
            long hasteDefaultCooldown = ((Integer) Infuse.getInstance().getCanfig("haste.cooldown.default")).longValue();
            long hasteAugmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("haste.cooldown.augmented")).longValue();
            long hasteCooldown = isAugmentedHaste ? hasteAugmentedCooldown : hasteDefaultCooldown;

            long hasteDefaultDuration = ((Integer) Infuse.getInstance().getCanfig("haste.duration.default")).longValue();
            long hasteAugmentedDuration = ((Integer) Infuse.getInstance().getCanfig("haste.duration.augmented")).longValue();
            long hasteDuration = isAugmentedHaste ? hasteAugmentedDuration : hasteDefaultDuration;
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 15, 3));
            CooldownManager.setDuration(playerUUID, "haste", hasteDuration);
            CooldownManager.setCooldown(playerUUID, "haste", hasteCooldown);
        }
    }


    @EventHandler
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent event) {
        Entity var3 = event.getEntity();
        if (var3 instanceof Player) {
            Player player = (Player)var3;
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() == Material.SHIELD && player.isBlocking() && this.hasImmortalHackEquipped2(player, "1") || (offHand.getType() == Material.SHIELD && player.isBlocking() && this.hasImmortalHackEquipped2(player, "2"))) {
                Entity var5 = event.getDamager();
                if (var5 instanceof Player) {
                    Player attacker = (Player)var5;
                    if (attacker.getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
                        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0F, 1.0F);
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

    public static boolean isInvincibilityGem(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 5;
    }
}
