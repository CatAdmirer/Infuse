package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.LimitedRunnable;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import java.util.List;
import java.util.UUID;
import java.awt.Color;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Apophis extends InfuseEffect {
    public Apophis() {
        super(EffectIds.APOPHIS, "apophis", false, new Color(0x440044), BossBar.Color.PURPLE);
    }

    public Apophis(boolean augmented) {
        super(EffectIds.APOPHIS, "apophis", augmented, new Color(0x440044), BossBar.Color.PURPLE);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_APOPHIS_NAME.toComponent() : Messages.APOPHIS_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_APOPHIS_LORE.getComponentList() : Messages.APOPHIS_LORE.getComponentList();
    }

    @Override
    public void equip(Player player) {
        // Setting the player's max health
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        maxHealth.setBaseValue(30);
        player.setHealth(30);
        player.sendHealthUpdate();

        // Adding potion effects to the player
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffect.INFINITE_DURATION, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 2, false, false));
    }

    @Override
    public void unequip(Player player) {
        // Resetting the player's max health
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        maxHealth.setBaseValue(20);
        player.sendHealthUpdate();
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "apophis")) return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setFireTicks(100);
            }
        }

        // Playing the graphics
        new ApophisParticles(plugin, player).start();

        // Increasing the player's max health
        AttributeModifier sparkModifier = new AttributeModifier(new NamespacedKey(plugin, "apophis_spark"), 10, Operation.ADD_NUMBER);
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        maxHealthAttribute.addModifier(sparkModifier);
        player.setHealth(maxHealthAttribute.getBaseValue());

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "apophis", duration);
        CooldownManager.setCooldown(playerUUID, "apophis", cooldown);

        // Setting up a task to reset the player's health when the spark deactivates.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            maxHealthAttribute.removeModifier(sparkModifier);
        }, duration * 20);
    }

    public static class ApophisParticles {
        private final Infuse plugin;
        private final Player caster;

        public ApophisParticles(Infuse plugin, Player caster) {
            this.plugin = plugin;
            this.caster = caster;
        }

        public void start() {
            // Stage 1 (ticks 0-100)
            new LimitedRunnable(5, this::stage1).runTaskTimer(plugin, 0, 20);

            // Stage 2 (ticks 100-110)
            new LimitedRunnable(10, this::stage2).runTaskTimer(plugin, 100, 1);
        }

        /**
         * Stage 1 runnable.
         * Should run from ticks 0-100, with 5 iterations.
         * 
         * @param iteration The iteration to play
         */
        public void stage1(int iteration) {
            if (iteration == 0) {
                caster.getWorld().spawnParticle(Particle.EXPLOSION, caster.getLocation(), 1);
            }

            Location center = caster.getLocation();
            World world = center.getWorld();
            world.playSound(center, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);

            for (int angle = 0; angle < 360; angle += 20) {
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

        /**
         * Stage 2 runnable.
         * Should run from ticks 100-110, with 10 iterations.
         * 
         * @param iteration The iteration to play
         */
        public void stage2(int iteration) {
            if (iteration == 0) {
                double explosionRadius = 5;
                for (Player target : caster.getWorld().getPlayers()) {
                    if (!target.equals(caster) && target.getLocation().distance(caster.getLocation()) <= explosionRadius) {
                        target.setVelocity(new Vector(0, 2, 0));
                    }
                }

                caster.getWorld().playSound(caster, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            }

            double baseRadius = 5;
            double spreadFactor = iteration * 0.1;
            double circleRadius = baseRadius + spreadFactor;
            double particleHeightOffset = iteration * 3;

            for (int angle = 0; angle < 360; angle++) {
                double rad = Math.toRadians(angle);
                double offsetX = circleRadius * Math.cos(rad);
                double offsetZ = circleRadius * Math.sin(rad);
                Location particleLoc = caster.getLocation().clone().add(offsetX, particleHeightOffset, offsetZ);
                caster.getWorld().spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());
            }
        }
    }
}
