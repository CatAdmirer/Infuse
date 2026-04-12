package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

public class ParticleManager {
    private final Infuse plugin;

    public ParticleManager(Infuse plugin) {
        this.plugin = plugin;
    }

    public void spawnEffectParticles(Player player, String slot) {
        EffectMapping effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (effect == null) return;

        // Handling special particles for ender effect
        // TODO: Decide whether or not to keep this
        if (effect == EffectMapping.ENDER || effect == EffectMapping.AUG_ENDER) {
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0, 1, 0), 32, 0.3, 0.5, 0.3, 0);
            return;
        }

        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0, 1, 0), 2, 0.3, 0.5, 0.3, 0.1, Color.fromARGB(effect.getColor().getRGB()));
    }

    /**
     * Spawns a cloud of effect particles around the player.
     *
     * @param player The player to spawn entity effect particles on.
     * @param color The color the particles should be.
     */
    public static void spawnEffectCloud(Player player, Color color) {
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0, 1, 0), 30, 0.5, 0.6, 0.5, 0, color);
    }

    public static void drawLine(Location start, Location end) {
        drawLine(start, end, 5, new DustOptions(Color.WHITE, 1));
    }

    public static void drawLine(Location start, Location end, int count) {
        drawLine(start, end, count, new DustOptions(Color.WHITE, 1));
    }

    public static void drawLine(Location start, Location end, DustOptions dustOptions) {
        drawLine(start, end, 5, dustOptions);
    }

    public static void drawLine(Location start, Location end, int count, DustOptions dustOptions) {
        if (!start.getWorld().equals(end.getWorld())) {
            Infuse.LOGGER.debug("Cannot draw lines between two worlds!");
            return;
        }

        Location diff = end.subtract(start);
        int points = (int) (diff.length() * 10);
        Location step = diff.multiply(1.0 / points);
        for (int i = 0; i < points; i++) {
            start.getWorld().spawnParticle(Particle.DUST, start, count, 0, 0, 0, 0, dustOptions);
            start.add(step);
        }
        start.getWorld().spawnParticle(Particle.DUST, end, count, 0, 0, 0, 0, dustOptions);
    }
}