package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Thunder extends InfuseEffect {
    private final Map<UUID, Long> entityLightningCooldowns = new HashMap<>();

    public Thunder() {
        super(EffectIds.THUNDER, "thunder", false);
    }

    public Thunder(boolean augmented) {
        super(EffectIds.THUNDER, "thunder", augmented);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_THUNDER_NAME.toComponent() : Messages.THUNDER_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_THUNDER_LORE.getComponentList() : Messages.THUNDER_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Thunder(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Thunder(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "thunder")) return;

        // Applying effects for the thunder spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "thunder", duration);
        CooldownManager.setCooldown(playerUUID, "thunder", cooldown);

        long durationTicks = duration * 20;
        World world = player.getWorld();

        // Future configs
        double radius = 10;

        // Starting the lightning storm
        new BukkitRunnable() {
            int ticksElapsed = 0;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    return;
                }

                Location center = player.getLocation();
                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;

                    if (target instanceof Player p) {
                        if (plugin.getDataManager().isTrusted(p, player)) continue;
                    }

                    target.getWorld().strikeLightningEffect(target.getLocation());
                    target.damage(4, player);
                    world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                }

                this.ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void chainLightning(Plugin plugin, LivingEntity startEntity, Player attacker) {
        List<LivingEntity> lightningTargets = new ArrayList<>();
        lightningTargets.add(startEntity);

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            // Stopping once enough people have been hit
            if (lightningTargets.size() > 5) {
                task.cancel();
                return;
            }

            // Getting the current target
            LivingEntity livingEntity = lightningTargets.get(0);

            // Damaging the current target
            livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());
            livingEntity.damage(4, attacker);
            livingEntity.getWorld().spawnParticle(Particle.DUST, livingEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));

            // Finding the next target
            for (Entity entity : livingEntity.getNearbyEntities(3, 3, 3)) {
                if (!(entity instanceof LivingEntity living)) continue;
                if (entity.equals(attacker)) continue;
                if (lightningTargets.contains(entity)) continue;

                lightningTargets.add(0, living);
            }
        }, 0, 20);
    }

    public static class Listeners implements Listener {
        private final Infuse plugin;
        private final Thunder effect = new Thunder();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void thunderAutoChanneling(EntityDamageByEntityEvent event) {
            // Ignoring non-trident damage
            if (!(event.getDamager() instanceof Trident trident)) return;

            // Making sure the shooter has the thunder effect
            if (!(trident.getShooter() instanceof Player attacker)) return;
            if (!plugin.getDataManager().hasEffect(attacker, effect)) return;

            // Only summoning lightning if the target is a living entity
            if (event.getEntity() instanceof LivingEntity target) {
                // TODO: Talk with cat about just striking lightning normally
                //target.getWorld().strikeLightning(target.getLocation());
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.damage(4, attacker);
                target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
            }
        }

        @EventHandler
        public void thunderChainLightning(EntityDamageByEntityEvent event) {
            // Making sure the attacker has the thunder effect
            if (!(event.getDamager() instanceof Player attacker)) return;
            if (!plugin.getDataManager().hasEffect(attacker, effect)) return;

            // Making sure the target is a living entity
            if (!(event.getEntity() instanceof LivingEntity target)) return;

            // Adding the target to the chain lightning cooldown
            UUID targetUUID = target.getUniqueId();
            long currentTime = System.currentTimeMillis();
            if (effect.entityLightningCooldowns.containsKey(targetUUID)) {
                long lastStrikeTime = effect.entityLightningCooldowns.get(targetUUID);
                if (currentTime - lastStrikeTime < 2000L) {
                    return;
                }
            }

            effect.entityLightningCooldowns.put(targetUUID, currentTime);

            // Finding the next target of the lightning chain
            List<Entity> nearbyEntities = target.getNearbyEntities(3, 3, 3);
            Optional<Entity> nextChainTarget = nearbyEntities.stream().filter((e) -> {
                return e instanceof LivingEntity && !e.equals(attacker);
            }).findFirst();

            // TODO: Make a lightning bolt particle effect that shows the chain
            // Only striking if there is another target?
            if (nextChainTarget.isPresent()) {
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.damage(4, attacker);
                target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                effect.chainLightning(plugin, target, attacker);
            }
        }
    }
}