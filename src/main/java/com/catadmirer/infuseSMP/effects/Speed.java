package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.ParticleManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Speed extends InfuseEffect {
    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);
    
    public Speed() {
        super(EffectIds.SPEED, "speed", false);
    }

    public Speed(boolean augmented) {
        super(EffectIds.SPEED, "speed", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_SPEED_NAME : MessageType.SPEED_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_SPEED_LORE : MessageType.SPEED_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Speed(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Speed(false);
    }

    @Override
    public void equip(Player player) {}

    @Override
    public void unequip(Player player) {}

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "speed")) return;

        // Applying effects for the speed spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        ParticleManager.spawnEffectCloud(player, Color.fromRGB(0xD1A44B));
        final Vector direction = player.getEyeLocation().getDirection().normalize();
        double playerVelocityMultiplier = plugin.getMainConfig().speedPlayerVelocityMultiplier();
        player.setVelocity(direction.clone().multiply(playerVelocityMultiplier));
        final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0xE6DCAA), 1.5F);
        final Location[] previousLocation = new Location[]{player.getLocation().clone()};
        final int[] ticksPassed = new int[]{0};
        final Location anchor = player.getLocation();
        Bukkit.getRegionScheduler().runAtFixedRate(plugin, anchor, (task) -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }

            Location currentLocation = player.getLocation();
            double distance = previousLocation[0].distance(currentLocation);

            if (distance > 0.1) {
                Vector step = currentLocation.toVector().subtract(previousLocation[0].toVector()).normalize().multiply(0.3);
                Location particleLocation = previousLocation[0].clone();

                for (double d = 0; d <= distance; d += step.length()) {
                    particleLocation.add(step);
                    player.getWorld().spawnParticle(Particle.DUST, particleLocation, 5, 0.1, 0.05, 0.1, 0.05, dustOptions);
                }

                previousLocation[0] = currentLocation.clone();
            }

            if (ticksPassed[0] >= 3 && player.isOnGround()) {
                task.cancel();
            }

            ticksPassed[0]++;
        }, 1L, 1L);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "speed", duration, cooldown);
    }
}