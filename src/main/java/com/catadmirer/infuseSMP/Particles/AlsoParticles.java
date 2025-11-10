package com.catadmirer.infuseSMP.Particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AlsoParticles {
    public static void spawnEffect(Player player, Color color) {
        Location base = player.getLocation().add(0.0D, 0.0D, 0.0D);
        int count = 30;
        double spread = 0.5D;

        for(int i = 0; i < count; ++i) {
            double x = (Math.random() - 0.5D) * spread * 2.0D;
            double y = Math.random() * 1.2D;
            double z = (Math.random() - 0.5D) * spread * 2.0D;
            Location spawn = base.clone().add(x, y, z);
            player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, spawn, 1, 0.0D, 0.0D, 0.0D, 0.0D, color);
        }

    }

    public static void spawnEffect2(Player player, Color color) {
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0.0D, 1.0D, 0.0D), 2, 0.0D, 0.5D, 0.0D, 0.1D, color);
    }

    public static void spawnAugmented(Player player, Color color) {
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0.0D, 1.0D, 0.0D), 2, 0.3D, 0.5D, 0.3D, 0.1D, color);
    }

    public static void spawnHearts(Player player) {
        Location location = player.getLocation().add(0.0D, 1.0D, 0.0D);
        Vector direction = player.getLocation().getDirection();
        double primarySpread = 0.5D;
        double secondarySpread = 0.1D;
        double offsetX;
        double offsetZ;
        if (Math.abs(direction.getZ()) > Math.abs(direction.getX())) {
            offsetX = primarySpread;
            offsetZ = secondarySpread;
        } else {
            offsetX = secondarySpread;
            offsetZ = primarySpread;
        }

        int count = 5;

        for(int i = 0; i < count; ++i) {
            double randomX = (Math.random() * 2.0D - 1.0D) * offsetX;
            double randomY = Math.pow(Math.random(), 2.0D) * 1.0D;
            double randomZ = (Math.random() * 2.0D - 1.0D) * offsetZ;
            Location spawnLoc = location.clone().add(randomX, randomY, randomZ);
            player.getWorld().spawnParticle(Particle.HEART, spawnLoc, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

    }
}
