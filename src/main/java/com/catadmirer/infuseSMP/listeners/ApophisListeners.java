package com.catadmirer.infuseSMP.listeners;

import java.time.Duration;
import java.util.UUID;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Emerald;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.util.ItemUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class ApophisListeners implements Listener {
    private final Infuse plugin;

    public ApophisListeners(Infuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void apophisLooting5(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getDataManager().hasEffect(player, new Emerald())) return;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isSword(item) && item.getEnchantmentLevel(Enchantment.LOOTING) < 5) {
            item.addUnsafeEnchantment(Enchantment.LOOTING, 5);
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        UUID attackerUUID = attacker.getUniqueId();

        if (event.getEntity() instanceof Player target) {
            if (CooldownManager.isEffectActive(attackerUUID, "apophis")) {
                target.showTitle(Title.title(Component.text("\uE090"), Component.empty(), Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)));
            }
        }
    }
}
