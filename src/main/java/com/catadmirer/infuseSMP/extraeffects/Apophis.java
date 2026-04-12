package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.util.ItemUtil;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Apophis extends InfuseEffect {
    public static final NamespacedKey apophisBoost = new NamespacedKey("infuse", "apophis_boost");
    public static final NamespacedKey apophisSparkBoost = new NamespacedKey("infuse", "apophis_spark_boost");

    public Apophis() {
        super(EffectIds.APOPHIS, "apophis", false);
    }

    public Apophis(boolean augmented) {
        super(EffectIds.APOPHIS, "apophis", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_APOPHIS_NAME : MessageType.APOPHIS_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_APOPHIS_LORE : MessageType.APOPHIS_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Apophis(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Apophis(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        
        // Doubling the strength if the user already has the modifier active
        int healthBoost = (maxHealth.getModifier(apophisBoost) != null) ? 20 : 10;
        AttributeModifier healthModifier = new AttributeModifier(apophisBoost, healthBoost, Operation.ADD_NUMBER);
        
        maxHealth.addModifier(healthModifier);
        player.heal(healthBoost);

        // TODO: Move to an event listener
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (ItemUtil.isSword(mainHand) && mainHand.getEnchantmentLevel(Enchantment.LOOTING) < 5) {
            mainHand.addUnsafeEnchantment(Enchantment.LOOTING, 5);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffect.INFINITE_DURATION, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 2, false, false));
    }
    
    @Override
    public void unequip(Infuse plugin, Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeModifier healthModifier = maxHealth.getModifier(apophisBoost);

        // Ignoring players who dont have the modifier
        if (healthModifier == null) return;

        // Removing the modifier
        maxHealth.removeModifier(apophisBoost);

        // Checking if the modifier was applied twice and adding it back if it was.
        if (healthModifier.getAmount() > 10) {
            healthModifier = new AttributeModifier(apophisBoost, 10, Operation.ADD_NUMBER);
            maxHealth.addModifier(healthModifier);
        }

        player.removePotionEffect(PotionEffectType.LUCK);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "apophis")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setFireTicks(100);
                }
            }

            fireSparkEffect(plugin, player);

            AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (attribute.getModifier(apophisSparkBoost) == null) {
                attribute.addModifier(new AttributeModifier(apophisSparkBoost, 10, Operation.ADD_NUMBER));
                player.heal(10);
            }
            
            // Applying cooldowns and durations for the effect
            long cooldown = plugin.getMainConfig().cooldown(this);
            long duration = plugin.getMainConfig().duration(this);

            CooldownManager.setTimes(playerUUID, "apophis", duration, cooldown);

            Bukkit.getScheduler().runTaskLater(plugin, () -> attribute.removeModifier(apophisSparkBoost), duration * 20);
        }
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

    public static class Listeners implements Listener {
        private final Infuse plugin;
        private final Apophis effect = new Apophis();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AttributeInstance maxHealth = event.getPlayer().getAttribute(Attribute.MAX_HEALTH);
        maxHealth.removeModifier(apophisBoost);
        maxHealth.removeModifier(apophisSparkBoost);
    }
}
