package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.ItemUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.UUID;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Apophis implements Listener {
    private static Infuse plugin;

    public Apophis(Infuse plugin) {
        Apophis.plugin = plugin;
    }

    public static NamespacedKey apophisBoost = new NamespacedKey("infuse", "apophis_boost");
    public static NamespacedKey apophisSparkBoost = new NamespacedKey("infuse", "apophis_spark_boost");

    public static void applyPassiveEffects(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute.getModifier(apophisBoost) == null) {
            AttributeModifier modifier = new AttributeModifier(apophisBoost, 10, Operation.ADD_NUMBER);
            attribute.addModifier(modifier);
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (ItemUtil.isSword(mainHand) && mainHand.getEnchantmentLevel(Enchantment.LOOTING) < 5) {
            mainHand.addUnsafeEnchantment(Enchantment.LOOTING, 5);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 2, false, false));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, task -> {
            AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttribute != null) {
                maxHealthAttribute.setBaseValue(20);
            }
        }, 15);
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

    public static void activateSpark(Boolean isAugmented, Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "apophis")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setFireTicks(100);
                }
            }

            spawnSparkEffect(player);
            (new BukkitRunnable() {
                public void run() {
                    player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
                }
            }).runTaskLater(plugin, 20L);

            AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (attribute.getModifier(apophisSparkBoost) == null) {
                attribute.addModifier(new AttributeModifier(apophisSparkBoost, 10, Operation.ADD_NUMBER));
            }
            
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_APOPHIS : EffectMapping.APOPHIS);
            long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_APOPHIS : EffectMapping.APOPHIS);

            CooldownManager.setDuration(playerUUID, "apophis", duration);
            CooldownManager.setCooldown(playerUUID, "apophis", cooldown);

            Bukkit.getScheduler().runTaskLater(plugin, () -> attribute.removeModifier(apophisSparkBoost), duration * 20);
        }
    }

    private static void spawnSparkEffect(final Player caster) {
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 100) {
                    startDarkRedDustEffect(caster.getLocation(), caster);
                    this.cancel();
                } else {
                    Location center = caster.getLocation();
                    World world = center.getWorld();
                    if (this.tick > 0 && this.tick % 20 == 0) {
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

                    ++this.tick;
                }
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }

    private static void startDarkRedDustEffect(final Location startLoc, Player caster) {
        final World world = startLoc.getWorld();
        double explosionRadius = 5;
        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && target.getLocation().distance(startLoc) <= explosionRadius) {
                target.setVelocity(new Vector(0, 2, 0));
            }
        }

        world.playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 60) {
                    this.cancel();
                } else {
                    double baseRadius = 5;
                    double spreadFactor = this.tick * 0.1;
                    double circleRadius = baseRadius + spreadFactor;
                    double particleHeightOffset = this.tick * 3;
                    if (particleHeightOffset > 30) {
                        this.cancel();
                    } else {
                        for(int angle = 0; angle < 360; ++angle) {
                            double rad = Math.toRadians(angle);
                            double offsetX = circleRadius * Math.cos(rad);
                            double offsetZ = circleRadius * Math.sin(rad);
                            Location particleLoc = startLoc.clone().add(offsetX, particleHeightOffset, offsetZ);
                            world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());
                        }

                        ++this.tick;
                    }
                }
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }
}

