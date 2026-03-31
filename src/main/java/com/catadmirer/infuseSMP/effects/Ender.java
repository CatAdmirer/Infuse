package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.kyori.adventure.text.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Ender implements Listener {
    public static final Component fireballName = Component.text("Cursing Projectile");
    public static final Set<UUID> cursedPlayers = new HashSet<>();

    private static Infuse plugin;

    public Ender(Infuse plugin) {
        Ender.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.ENDER)) return;

        double radius = 10;

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player nearby)) continue;
            if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
            if (plugin.getDataManager().isTrusted(nearby, player)) continue;
            nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) return;

        // Making sure the damaged player is cursed
        UUID damagedUUID = damagedPlayer.getUniqueId();
        if (!cursedPlayers.contains(damagedUUID)) return;

        // Making sure the damage source isn't the one made by this plugin (prevents looping curse damage)
        if (event.getDamageSource().getDamageType() == DamageType.CAMPFIRE && event.getDamageSource().getDirectEntity() != null) return;
        
        // Making the fake damageSource
        DamageSource fakeSource = DamageSource.builder(DamageType.CAMPFIRE).withDirectEntity(damagedPlayer).build();

        // Sharing curse damage with all other cursed players
        for (UUID cursedUUID : cursedPlayers) {
            // Skipping the player who was hit
            if (cursedUUID == damagedUUID) continue;

            Player player = Bukkit.getPlayer(cursedUUID);
            player.damage(event.getDamage(), fakeSource);
        }
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_ENDER : EffectMapping.ENDER);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_ENDER : EffectMapping.ENDER);

        CooldownManager.setTimes(playerUUID, "ender", duration, cooldown);

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Teleporting the player in the direction they're looking
        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();
        int maxDistance = 15;

        Location targetLoc = null;

        for (int i = 1; i <= maxDistance; i++) {
            Location checkLoc = startLoc.clone().add(direction.clone().multiply(i));
            if (isSafeTeleportLocation(checkLoc)) {
                targetLoc = checkLoc;
            } else {
                break;
            }
        }

        if (targetLoc != null) {
            Location finalLoc = targetLoc.clone();
            finalLoc.setYaw(player.getLocation().getYaw());
            finalLoc.setPitch(player.getLocation().getPitch());
            player.teleport(finalLoc);
        }
    }

    private static boolean isSafeTeleportLocation(Location loc) {
        return loc.getBlock().getType().isAir() && loc.clone().add(0, 1, 0).getBlock().getType().isAir();
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
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.ENDER)) return;

        // Making sure the player used a bottle of dragons breath
        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getType() != Material.DRAGON_BREATH) return;

        // Making sure the cursing fireball isn't on cooldown
        if (CooldownManager.isOnCooldown(player.getUniqueId(), "ender_fireball")) return;

        shootCursingFireball(player);
        event.setCancelled(true);
    }

    public void shootCursingFireball(Player player) {
        UUID uuid = player.getUniqueId();

        if (CooldownManager.isOnCooldown(player.getUniqueId(), "ender_fireball")) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            // Is this necessary?
            player.getInventory().setItemInMainHand(null);
        }

        Fireball fireball = player.launchProjectile(DragonFireball.class);
        fireball.setIsIncendiary(false);
        fireball.customName(fireballName);

        CooldownManager.setCooldown(uuid, "ender_fireball", 30);

        Vector velocity = fireball.getVelocity();
        velocity.multiply(2.0);
        fireball.setVelocity(velocity);
    }

    @EventHandler
    public void enderCurseHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.ENDER)) return;

        cursePlayer(target.getUniqueId(), 1200);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof DragonFireball fireball)) return;
        if (!fireballName.equals(fireball.customName())) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (plugin.getDataManager().isTrusted(target, shooter)) return;

        cursePlayer(target.getUniqueId(), 1200);

        event.setDamage(0);
    }

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof DragonFireball fireball)) return;
        if (!fireballName.equals(fireball.customName())) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (plugin.getDataManager().isTrusted(target, shooter)) return;

        cursePlayer(target.getUniqueId(), 1200);
    }

    public void cursePlayer(UUID playerUUID, long delayTicks) {
        cursedPlayers.add(playerUUID);

        Bukkit.getScheduler().runTaskLater(plugin, task -> cursedPlayers.remove(playerUUID), delayTicks);
    }
}