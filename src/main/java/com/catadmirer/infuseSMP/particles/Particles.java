package com.catadmirer.infuseSMP.particles;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Particles {
    private final Infuse plugin;

    public Particles(Infuse plugin) {
        this.plugin = plugin;
    }

    // TODO: There HAS to be a better way to do this.  Maybe a listener like this could be started when a player equips an effect?
    public void startTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyParticlesForEffect(player, "1");
                applyParticlesForEffect(player, "2");
            }
        }, 1, 20);
    }

    private void applyParticlesForEffect(Player player, String slot) {
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);
        if (effect == null) return;

        final double regularRadius = 0;
        final double augmentedRadius = 0.3;
        double radius = effect.isAugmented() ? augmentedRadius : regularRadius;

        // Handling special particles for ender effect
        // TODO: Decide whether or not to keep this
        if (effect.getId() == EffectIds.ENDER) {
            player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0, 1, 0), 32, radius, 0.5, radius, 0);
            return;
        }

        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0, 1, 0), 2, radius, 0.5, radius, 0.1, Color.fromARGB(effect.getPotionColor().getRGB()));
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
}