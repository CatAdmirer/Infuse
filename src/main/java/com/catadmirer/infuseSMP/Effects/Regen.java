package com.catadmirer.infuseSMP.Effects;

import java.util.List;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Regen implements Listener {
    
    private final Infuse plugin;

    public Regen(Infuse plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static boolean isInvincibilityGem(ItemStack item) {
        if (item != null && item.getType() == Material.POTION && item.getItemMeta() != null) {
            return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 9;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player)event.getDamager();
            if (this.hasRegenEquipped(player, "1") || this.hasRegenEquipped(player, "2")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1, false, false));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityHeal(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            if (this.hasRegenEquipped(player, "1") || this.hasRegenEquipped(player, "2")) {
                if (event.getFinalDamage() <= 0.0D) {
                    player.setSaturation(Math.min(player.getSaturation() + 6.0F, 20.0F));
                }

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
            boolean isLegendary = player.isSneaking() && this.hasRegenEquipped(player, "1");
            boolean isCommon = !player.isSneaking() && this.hasRegenEquipped(player, "2");
            if (isLegendary || isCommon) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "regen")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasRegenEquipped(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = Infuse.getInstance().getEffect("regen");
        String gemName2 = plugin.getEffect("aug_regen");
        return currentHack != null && (currentHack.equals(gemName) || currentHack.equals(gemName2));
    }

    @EventHandler
    public void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player)event.getDamager();
            if (CooldownManager.isEffectActive(damager.getUniqueId(), "regen")) {
                double damage = event.getFinalDamage();
                damager.setHealth(Math.min(damager.getHealth() + damage / 2.0D, damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            }
        }
    }

    public void activateSpark(final Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "regen")) {
            String gemName2 = plugin.getEffect("aug_regen");
            boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2))) ||
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2)));
            long defaultCooldown = ((Integer) Infuse.getInstance().getCanfig("regen.cooldown.default")).longValue();
            long augmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("regen.cooldown.augmented")).longValue();
            long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;
            long defaultDuration = ((Integer) Infuse.getInstance().getCanfig("regen.duration.default")).longValue();
            long augmentedDuration = ((Integer) Infuse.getInstance().getCanfig("regen.duration.augmented")).longValue();
            long duration = isAugmented ? augmentedDuration : defaultDuration;

            CooldownManager.setCooldown(playerUUID, "regen", cooldown);
            CooldownManager.setDuration(playerUUID, "regen", duration);

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
        }
    }


    public static ItemStack createFake() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("regen");
            meta.setDisplayName(gemName);
            meta.setColor(Color.fromRGB(255, 0, 0));
            List<String> lore = Infuse.getInstance().getEffectLore("regen");
            meta.setLore(lore);
            meta.setCustomModelData(9);
            gem.setItemMeta(meta);
        }

        return gem;
    }
}