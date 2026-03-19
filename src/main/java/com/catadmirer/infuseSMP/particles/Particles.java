package com.catadmirer.infuseSMP.particles;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
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
}