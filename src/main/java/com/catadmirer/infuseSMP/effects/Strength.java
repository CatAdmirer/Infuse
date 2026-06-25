package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.UUID;

public class Strength extends InfuseEffect {
    private final Infuse plugin;

    public Strength() {
        this(false);
    }

    public Strength(boolean augmented) {
        super("strength", EffectIds.STRENGTH, augmented, EffectConstants.potionColor(EffectIds.STRENGTH), EffectConstants.ritualColor(EffectIds.STRENGTH));

        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(Player owner) {}

    @Override
    public void unequip(Player owner) {}

    @Override
    public void activateSpark(Player owner) {
        UUID uuid = owner.getUniqueId();

        // Skipping players on cooldown
        if (CooldownManager.isOnCooldown(uuid, "strength")) return;

        // Playing sounds
        owner.getWorld().playSound(owner.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(uuid, "strength", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Strength();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Strength(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? Message.MessageType.AUG_STRENGTH_NAME : Message.MessageType.STRENGTH_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? Message.MessageType.AUG_STRENGTH_LORE : Message.MessageType.STRENGTH_LORE);
    }

    //// Listeners ////
    //// These are only registered once, so they need to be able to handle being used for every player, no matter what effects they actually have

    @EventHandler
    public void extraDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Skipping players without the strength effect
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        // Boosting damage based on the attacker's health.
        double damage = event.getDamage();
        double health = attacker.getHealth();
        if (health < 2) {
            event.setDamage(damage + 3);
        } else if (health < 4) {
            event.setDamage(damage + 2);
        } else if (health < 6) {
            event.setDamage(damage + 1);
        }
    }

    /** Automatically crits while the strength spark is active */
    @EventHandler
    public void strengthSparkAutoCrit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        // Fabricating crits if the spark is active, and if it wasn't already a critical hit.
        if (CooldownManager.isEffectActive(player.getUniqueId(), "strength") && !event.isCritical()) {
            // Changing the damage to that of a crit.
            double originalDamage = event.getDamage();
            double critDamage = originalDamage * 1.35;
            event.setDamage(critDamage);
            
            // Playing the crit noise and spawning crit particles.
            Entity hitEntity = event.getEntity();
            hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            hitEntity.getWorld().spawnParticle(Particle.CRIT, hitEntity.getLocation().add(0, hitEntity.getHeight() / 2, 0), 10);
        }
    }

    /** Boosts the piercing level of any arrow to 100 for players with the strength effect. */
    @EventHandler
    public void strengthHighPiercing(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Making sure the shooter has the strength effect
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        // Increasing the piercing level of the shot arrow.
        if (event.getProjectile() instanceof Arrow arrow) {
            arrow.setPierceLevel(100);
        }
    }

    /** Doubles damage against mobs for players with the strength effect. */
    @EventHandler
    public void strengthDoubleDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // Not dealing double damage to other players
        if (entity instanceof Player) return;

        // Making sure the attacker has the strength effect
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        // Doubling the damage of the attack
        event.setDamage(event.getDamage() * 2);
    }

    /** Lengthens the shield cooldown of opponents when their shield is disabled by someone with the strength effect. */
    @EventHandler
    public void strengthLengthenShieldCooldown(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Making sure the player was blocking
        if (!player.isBlocking()) return;
        
        // Making sure the attacker is using an axe
        if (!ItemUtil.isAxe(attacker.getInventory().getItemInMainHand())) return;

        // Making sure the attacker has the strength effect
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        // Playing noise and stunning the opponent
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1, 1);
        
        // TODO: Test if this can be removed
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setCooldown(Material.SHIELD, 200);
        }, 20L);
    }
}