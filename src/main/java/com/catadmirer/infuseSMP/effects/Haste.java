package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.util.ItemUtil;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Haste extends InfuseEffect {
    public static final NamespacedKey FORTUNE_KEY = new NamespacedKey("infuse", "haste_fortune");
    public static final NamespacedKey EFFICIENCY_KEY = new NamespacedKey("infuse", "haste_efficiency");
    public static final NamespacedKey UNBREAKING_KEY = new NamespacedKey("infuse", "haste_unbreaking");

    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);
    
    public Haste() {
        super(EffectIds.HASTE, "haste", false);
    }

    public Haste(boolean augmented) {
        super(EffectIds.HASTE, "haste", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_HASTE_NAME : MessageType.HASTE_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_HASTE_LORE : MessageType.HASTE_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Haste(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Haste(false);
    }

    @Override
    public void equip(Player player) {}

    @Override
    public void unequip(Player player) {}

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "haste")) return;

        // Applying effects for the haste spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "haste", duration, cooldown);

        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 15, 3));
    }

    @EventHandler
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        if (!plugin.getDataManager().hasEffect(event.getPlayer(), this)) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtil.isPickaxe(item) || ItemUtil.isAxe(item) || ItemUtil.isShovel(item) || ItemUtil.isHoe(item)) {
            ItemUtil.applySpecialEnchantment(item, FORTUNE_KEY, Enchantment.FORTUNE, plugin.getMainConfig().hasteFortuneLevel());
            ItemUtil.applySpecialEnchantment(item, EFFICIENCY_KEY, Enchantment.EFFICIENCY, plugin.getMainConfig().hasteEfficiencyLevel());
            ItemUtil.applySpecialEnchantment(item, UNBREAKING_KEY, Enchantment.UNBREAKING, plugin.getMainConfig().hasteUnbreakingLevel());
        }
    }

    @EventHandler
    public void extendShieldCooldown(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.SHIELD && player.isBlocking() && plugin.getDataManager().hasEffect(player, this)) {
            if (!(event.getDamager() instanceof Player attacker)) return;
            if (!ItemUtil.isAxe(attacker.getInventory().getItemInMainHand())) return;

            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.setCooldown(Material.SHIELD, 50), 20L);
        }
    }
}