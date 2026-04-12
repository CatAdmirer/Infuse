package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Fire extends InfuseEffect {
    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);

    public Fire() {
        super(EffectIds.FIRE, "fire", false);
    }

    public Fire(boolean augmented) {
        super(EffectIds.FIRE, "fire", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_FIRE_NAME : MessageType.FIRE_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_FIRE_LORE : MessageType.FIRE_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Fire(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Fire(false);
    }

    @Override
    public void equip(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
    }

    @Override
    public void unequip(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }

    /*
    TODO: implement
    if (player.isInLava()) {
        player.setGliding(true);
    } else if (player.isInPowderedSnow()) {
        player.setGliding(true);
    }
     */
    
    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "fire")) return;

        // Applying effects for the fire spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setFireTicks(100);
            }
        }

        fireSparkEffect(plugin, player);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "fire", duration, cooldown);
    }

    private final void fireSparkEffect(Infuse plugin, Player caster) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> stage0(caster), 20);
        for (int i = 0; i < 5; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> stage1(caster), 20 * (i + 1));
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> stage2(caster), 100);
        for (int i = 1; i < 11; i++) {
            final int j = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> stage3(caster, j), 100 + j);
        }
    }

    private final void stage0(Player player) {
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
    }

    private final void stage1(Player caster) {
        Location center = caster.getLocation();
        World world = center.getWorld();

        world.playSound(center, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);

        for(int angle = 0; angle < 360; angle += 20) {
            double rad = Math.toRadians(angle);
            double offsetX = 5 * Math.cos(rad);
            double offsetZ = 5 * Math.sin(rad);
            Location particleLoc = center.clone().add(offsetX, 0.1, offsetZ);
            world.spawnParticle(Particle.LAVA, particleLoc, 10, 0.05, 0.05, 0.05, 0.01);
        }

        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && target.getLocation().distance(center) <= 5) {
                target.damage(8, caster);
            }
        }
    }

    private final void stage2(Player player) {
        final World world = player.getWorld();
        double explosionRadius = 5;
        for (Player target : world.getPlayers()) {
            if (!target.equals(player) && target.getLocation().distance(player.getLocation()) <= explosionRadius) {
                target.setVelocity(new Vector(0, 2, 0));
            }
        }

        world.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
    }

    private static void stage3(Player player, int iteration) {
        World world = player.getWorld();
        
        double baseRadius = 5;
        double spreadFactor = iteration * 0.1;
        double circleRadius = baseRadius + spreadFactor;
        double particleHeightOffset = iteration * 3;
        for(int angle = 0; angle < 360; ++angle) {
            double rad = Math.toRadians(angle);
            double offsetX = circleRadius * Math.cos(rad);
            double offsetZ = circleRadius * Math.sin(rad);
            Location particleLoc = player.getLocation().clone().add(offsetX, particleHeightOffset, offsetZ);
            world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());
        }
    }

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (event.isGliding()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        if (player.isInLava() || player.isInPowderedSnow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inLava = player.isInLava();
        Vector direction = player.getLocation().getDirection().normalize();
        if (inLava && plugin.getDataManager().hasEffect(player, this)) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            double boostStrength = 0.6;
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        if (event.getForce() >= 1 && event.getProjectile() instanceof Projectile projectile) {
            projectile.setFireTicks(100);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != DamageCause.FALL) return;
        if (!plugin.getDataManager().hasEffect(player, this)) return;
        Material blockType = player.getLocation().getBlock().getType();
        if (blockType == Material.LAVA || blockType == Material.LAVA_CAULDRON) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void fireCombustTarget(TenHitEvent event) {
        Player attacker = event.getAttacker();
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        event.getTarget().setFireTicks(100);
    }
}