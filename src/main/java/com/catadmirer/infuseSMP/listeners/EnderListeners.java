package com.catadmirer.infuseSMP.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.managers.CooldownManager;

public class EnderListeners implements Listener {
    private final Infuse plugin;
    private final Ender genericEnder = new Ender();

    public EnderListeners(Infuse plugin) {
        this.plugin = plugin;

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) return;

        // Making sure the damaged player is cursed
        UUID damagedUUID = damagedPlayer.getUniqueId();
        if (!Ender.cursedPlayers.contains(damagedUUID)) return;

        // Making sure the damage source isn't the one made by this plugin (prevents looping curse damage)
        if (event.getDamageSource().getDamageType() == DamageType.CAMPFIRE && event.getDamageSource().getDirectEntity() != null) return;
        
        // Making the fake damageSource
        DamageSource fakeSource = DamageSource.builder(DamageType.CAMPFIRE).withDirectEntity(damagedPlayer).build();

        // Sharing curse damage with all other cursed players
        for (UUID cursedUUID : Ender.cursedPlayers) {
            // Skipping the player who was hit
            if (cursedUUID == damagedUUID) continue;

            Player player = Bukkit.getPlayer(cursedUUID);
            player.damage(event.getDamage(), fakeSource);
        }
    }

     @EventHandler
    public void enderOnehitMobs(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (event.getEntity() instanceof Player) return;

        UUID attackerUUID = attacker.getUniqueId();
        if (CooldownManager.isEffectActive(attackerUUID, "ender")) {
            mob.setHealth(0);
        }
    }

    @EventHandler
    public void onUseDragonBreath(PlayerInteractEvent event) {
        // Listening for right clicks
        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;
        
        // Making sure the player has the ender effect
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, genericEnder)) return;

        // Making sure the player used a bottle of dragons breath
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.DRAGON_BREATH) return;

        // Making sure the cursing fireball isn't on cooldown
        if (CooldownManager.isOnCooldown(player.getUniqueId(), "ender_fireball")) return;

        Ender ender = new Ender();
        ender.setOwner(player);
        ender.shootCursingFireball(plugin);

        event.setCancelled(true);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof DragonFireball fireball)) return;
        if (!Ender.fireballName.equals(fireball.customName())) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (plugin.getDataManager().isTrusted(target, shooter)) return;

        genericEnder.cursePlayer(plugin, target, 1200);

        event.setDamage(0);
    }

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof DragonFireball fireball)) return;
        if (!Ender.fireballName.equals(fireball.customName())) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (plugin.getDataManager().isTrusted(target, shooter)) return;

        genericEnder.cursePlayer(plugin, target, 1200);
    }
}