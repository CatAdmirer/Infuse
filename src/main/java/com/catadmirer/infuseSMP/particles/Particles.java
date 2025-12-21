package com.catadmirer.infuseSMP.particles;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Particles {
    public void startTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Infuse.getInstance(), (task) -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyParticlesForEffect(player, "1");
                applyParticlesForEffect(player, "2");
            }

        }, 1, 20);
    }

    private void applyParticlesForEffect(Player player, String type) {
        EffectMapping effect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (effect == null) return;

        // Handling special particles for ender effect
        // TODO: Decide whether or not to keep this
        if (effect == EffectMapping.ENDER || effect == EffectMapping.AUG_ENDER) {
            spawnDragon(player);
            return;
        }

        final double regularRadius = 0;
        final double augmentedRadius = 0.3;

        spawnEffect(player, Color.fromRGB(effect.getColor().getRGB()), effect.isAugmented() ? augmentedRadius : regularRadius);
    }

    public static void spawnDragon(Player player) {
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0, 1, 0), 32, 0.3, 0.5, 0.3, 0);
    }

    /**
     * Spawns a cloud of effect particles around the player.
     *
     * @param player The player to spawn entity effect particles on.
     * @param color The color the particles should be.
     */
    public static void spawnEffectCloud(Player player, Color color) {
        Location base = player.getLocation().clone();
        int count = 30;
        double spread = 0.5;

        for(int i = 0; i < count; ++i) {
            double x = (Math.random() - 0.5) * spread * 2;
            double y = Math.random() * 1.2;
            double z = (Math.random() - 0.5) * spread * 2;
            player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, base.clone().add(x, y, z), 1, 0, 0, 0, 0, color);
        }

    }

    /**
     * Spawns a ring of effect particles around a player.
     *
     * @param player The player to spawn the particles on.
     * @param color The color the particles should be.
     * @param radius The offset radius of the effects.
     */
    public static void spawnEffect(Player player, Color color, double radius) {
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0, 1, 0), 2, radius, 0.5, radius, 0.1, color);
    }
}