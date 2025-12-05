package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.util.EffectUtil;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Regen implements Listener {

    private final Infuse plugin;

    public Regen(Infuse plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (this.hasRegenEquipped(player, "1") || this.hasRegenEquipped(player, "2")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1, false, false));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityHeal(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (this.hasRegenEquipped(player, "1") || this.hasRegenEquipped(player, "2")) {
                if (event.getFinalDamage() <= 0) {
                    player.setSaturation(Math.min(player.getSaturation() + 6, 20));
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
            boolean isPrimary = player.isSneaking() && this.hasRegenEquipped(player, "1");
            boolean isSecondary = !player.isSneaking() && this.hasRegenEquipped(player, "2");
            if (isPrimary || isSecondary) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "regen")) {
                    event.setCancelled(true);
                    activateSpark(player);
                }
            }
        }
    }

    private boolean hasRegenEquipped(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffectName("regen");
        String effectName2 = plugin.getEffectName("aug_regen");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
    }

    @EventHandler
    public void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager) {
            if (CooldownManager.isEffectActive(damager.getUniqueId(), "regen")) {
                double damage = event.getFinalDamage();
                damager.setHealth(Math.min(damager.getHealth() + damage / 2, damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            }
        }
    }

    public static void activateSpark(final Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "regen")) {
            String effectName2 = Infuse.getInstance().getEffectName("aug_regen");
            boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2))) ||
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));
            long defaultCooldown = Infuse.getInstance().getConfig("regen.cooldown.default");;
            long augmentedCooldown = Infuse.getInstance().getConfig("regen.cooldown.augmented");;
            long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;
            long defaultDuration = Infuse.getInstance().getConfig("regen.duration.default");;
            long augmentedDuration = Infuse.getInstance().getConfig("regen.duration.augmented");;
            long duration = isAugmented ? augmentedDuration : defaultDuration;

            CooldownManager.setCooldown(playerUUID, "regen", cooldown);
            CooldownManager.setDuration(playerUUID, "regen", duration);

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
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
            meta.setDisplayName(Infuse.getInstance().getEffectName(augmented ? "aug_regen" : "regen"));
            meta.setLore(Infuse.getInstance().getEffectLore(augmented ? "aug_regen" : "regen"));
            meta.setColor(Color.RED);

            if (augmented) meta.setCustomModelData(999);
            meta.getPersistentDataContainer().set(Infuse.EFFECT_ID, PersistentDataType.INTEGER, augmented ? 1 : 0);

            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isRegular(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 0;
    }

    public static boolean isAugmented(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 1;
    }

    public static boolean isEffect(ItemStack item) {
        return isRegular(item) || isAugmented(item);
    }
}