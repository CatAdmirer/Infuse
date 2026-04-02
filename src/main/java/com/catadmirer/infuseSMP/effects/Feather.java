package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.particles.Particles;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Feather implements Listener {
    private static Infuse plugin;

    public Feather(Infuse plugin) {
        Feather.plugin = plugin;
    }

    @EventHandler
    public void FeatherLand(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double radius = plugin.getMainConfig().featherLandLaunchRadius();

        UUID playerUUID = player.getUniqueId();
        if (player.isOnGround() && CooldownManager.isEffectActive(playerUUID, "feathermace")) {
            CooldownManager.setDuration(playerUUID, "feathermace", 0L);
            Location loc = player.getLocation();
            World world = player.getWorld();

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {

                if (!(entity instanceof LivingEntity target)) continue;
                if (target instanceof Player targetPlayer && plugin.getDataManager().isTrusted(player, targetPlayer)) continue;

                int damage = plugin.getMainConfig().featherLandLaunchDamage();
                target.damage(damage);
                Vector knockback = new Vector(0, 1, 0);
                target.setVelocity(target.getVelocity().add(knockback));
                Location anchor = target.getLocation();
                LivingEntity finalTarget = target;
                Bukkit.getRegionScheduler().run(plugin, anchor, (task) -> {
                    finalTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 0, false, false, false));
                });
            }

            world.spawnParticle(Particle.CLOUD, loc, 50, 0, 0, 0, 2);
            world.playSound(loc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.5F, 1);
            Location anchor = player.getLocation();
            Bukkit.getRegionScheduler().runDelayed(plugin, anchor, (task) -> {
                if (player.isOnline()) {
                    Vector dashDirection = player.getEyeLocation().getDirection().normalize();
                    Vector launchVector = dashDirection.multiply(5);
                    player.setVelocity(launchVector);
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onTenthHit(TenHitEvent event) {
        Player player = event.getTarget();
        Player target = event.getAttacker();

        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FEATHER)) return;

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 2));
        Location chargeLocation = player.getLocation().add(0, 1, 0);
        WindCharge windCharge = player.getWorld().spawn(chargeLocation, WindCharge.class);
        Location targetLocation = player.getLocation().subtract(0, 1, 0);
        Vector direction = targetLocation.toVector().subtract(chargeLocation.toVector()).normalize();
        windCharge.setVelocity(direction.multiply(1));
        windCharge.setShooter(player);
        player.setVelocity(new Vector(0, 0.5, 0));
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != DamageCause.FALL) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FEATHER)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRightClickWindcharge(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FEATHER)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.WIND_CHARGE) return;
        if (player.hasCooldown(Material.WIND_CHARGE)) return;
        if (!event.getAction().isRightClick()) return;

        Location anchor = player.getLocation();
        Bukkit.getRegionScheduler().runDelayed(plugin, anchor, (task) -> {
            player.setCooldown(Material.WIND_CHARGE, 5);
        }, 1L);
    }

    @EventHandler
    public void onWindChargeLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof WindCharge windCharge)) return;
        if (!(windCharge.getShooter() instanceof Player player)) return;
        
        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(2);
        windCharge.setVelocity(direction);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.FEATHER)) return;

        double fallDistance = attacker.getFallDistance();
        if (fallDistance < 7) return;

        attacker.getWorld().playSound(attacker.getLocation(), Sound.ITEM_MACE_SMASH_AIR, 1, 1);
        Location startLoc = attacker.getLocation();
        World world = startLoc.getWorld();
        Location particleLoc = event.getDamager().getLocation();
        world.spawnParticle(Particle.GUST_EMITTER_SMALL, particleLoc, 1, 0, 0, 0, 0);
        attacker.setVelocity(new Vector(0, 1.8, 0));
        double multiplier = 1.1;
        event.setDamage(event.getDamage() * multiplier);
    }

    public static String applyHexColors(String input) {
        String regex = "(#(?:[0-9a-fA-F]{6}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String colorCode = ChatColor.of(hexCode).toString();
            matcher.appendReplacement(result, colorCode);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "feather")) return;

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        Particles.spawnEffectCloud(player, Color.fromRGB(0xBEA3CA));
        Vector dashDirection = player.getEyeLocation().getDirection().normalize();
        Vector launchVector = dashDirection.multiply(0).setY(1);
        player.setVelocity(launchVector);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 10));
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_FEATHER : EffectMapping.FEATHER);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_FEATHER : EffectMapping.FEATHER);

        CooldownManager.setTimes(playerUUID, "feather", duration, cooldown);

        player.getScheduler().runDelayed(plugin, t -> {
            CooldownManager.setDuration(playerUUID, "feathermace", 5L);
        }, null, 10);
    }
}