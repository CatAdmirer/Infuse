package com.catadmirer.infuseSMP.effects;

import java.util.UUID;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.events.EffectUnequipEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.catadmirer.infuseSMP.EffectIds;

public class Haste extends InfuseEffect {
    private static final NamespacedKey fortuneKey = new NamespacedKey("infuse", "haste_fortune");
    private static final NamespacedKey efficiencyKey = new NamespacedKey("infuse", "haste_efficiency");
    private static final NamespacedKey unbreakingKey = new NamespacedKey("infuse", "haste_unbreaking");

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
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

    @Override
    public void activateSpark(Infuse plugin, Player player) {
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

    public static class Listeners implements Listener {
        private final Infuse plugin;
        private final Haste effect = new Haste();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onPlayerHoldItem(PlayerItemHeldEvent event) {
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (ItemUtil.isPickaxe(item) || ItemUtil.isAxe(item) || ItemUtil.isShovel(item) || ItemUtil.isHoe(item)) {
                ItemUtil.applySpecialEnchantment(item, fortuneKey, Enchantment.FORTUNE, plugin.getMainConfig().hasteFortuneLevel());
                ItemUtil.applySpecialEnchantment(item, efficiencyKey, Enchantment.EFFICIENCY, plugin.getMainConfig().hasteEfficiencyLevel());
                ItemUtil.applySpecialEnchantment(item, unbreakingKey, Enchantment.UNBREAKING, plugin.getMainConfig().hasteUnbreakingLevel());
            }
        }

        @EventHandler
        public void onInventoryCloseEvent(InventoryCloseEvent event) {
            if (event.getView().getTopInventory().equals(event.getPlayer().getInventory())) return;

            for (ItemStack item : event.getView().getTopInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) continue;

                ItemUtil.removeSpecialEnchant(item, efficiencyKey, Enchantment.EFFICIENCY);
                ItemUtil.removeSpecialEnchant(item, fortuneKey, Enchantment.FORTUNE);
                ItemUtil.removeSpecialEnchant(item, unbreakingKey, Enchantment.UNBREAKING);
            }
        }

        @EventHandler
        public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
            final ItemStack item = event.getItemDrop().getItemStack();

            ItemUtil.removeSpecialEnchant(item, efficiencyKey, Enchantment.EFFICIENCY);
            ItemUtil.removeSpecialEnchant(item, fortuneKey, Enchantment.FORTUNE);
            ItemUtil.removeSpecialEnchant(item, unbreakingKey, Enchantment.UNBREAKING);
        }

        @EventHandler
        public void onEffectUnequipEvent(EffectUnequipEvent event) {
            if (!(event.getEffect().equals(effect))) return;

            for (ItemStack item : event.getPlayer().getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) continue;

                ItemUtil.removeSpecialEnchant(item, efficiencyKey, Enchantment.EFFICIENCY);
                ItemUtil.removeSpecialEnchant(item, fortuneKey, Enchantment.FORTUNE);
                ItemUtil.removeSpecialEnchant(item, unbreakingKey, Enchantment.UNBREAKING);
            }
        }
    }
}