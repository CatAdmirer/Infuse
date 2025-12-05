package com.catadmirer.infuseSMP.Particles;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
import net.md_5.bungee.api.ChatColor;
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

        }, 1, 19);
    }

    private void applyParticlesForEffect(Player player, String type) {
        String effectName = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (effectName == null) return;

        String stripped = ChatColor.stripColor(effectName);
        Integer abilityId = EffectMaps.getEffectNumber(stripped);
        if (abilityId == null) return;
        
        final double regularRadius = 0;
        final double augmentedRadius = 0.3;

        switch (abilityId) {
            case 0 -> spawnEffect(player, Color.GREEN, regularRadius);
            case 1 -> spawnEffect(player, Color.GREEN, augmentedRadius);
            case 2 -> spawnEffect(player, Color.fromRGB(0xBEA3CA), regularRadius);
            case 3 -> spawnEffect(player, Color.fromRGB(0xBEA3CA), augmentedRadius);
            case 4 -> spawnEffect(player, Color.fromRGB(0xFC7803), regularRadius);
            case 5 -> spawnEffect(player, Color.fromRGB(0xFC7803), augmentedRadius);
            case 6 -> spawnEffect(player, Color.AQUA, regularRadius);
            case 7 -> spawnEffect(player, Color.AQUA, augmentedRadius);
            case 8 -> spawnEffect(player, Color.fromRGB(0xB96C00), regularRadius);
            case 9 -> spawnEffect(player, Color.fromRGB(0xB96C00), augmentedRadius);
            case 10 -> spawnEffect(player, Color.fromRGB(0xFC0046), regularRadius);
            case 11 -> spawnEffect(player, Color.fromRGB(0xFC0046), augmentedRadius);
            case 14 -> spawnEffect(player, Color.fromRGB(0x005AFC), regularRadius);
            case 15 -> spawnEffect(player, Color.fromRGB(0x005AFC), augmentedRadius);
            case 16 -> spawnEffect(player, Color.fromRGB(0xFF03EF), regularRadius);
            case 17 -> spawnEffect(player, Color.fromRGB(0xFF03EF), augmentedRadius);
            case 18 -> spawnEffect(player, Color.fromRGB(0xD1A44B), augmentedRadius);
            case 19 -> spawnEffect(player, Color.fromRGB(0xD1A44B), regularRadius);
            case 20 -> spawnEffect(player, Color.fromRGB(0x8B0000), regularRadius);
            case 21 -> spawnEffect(player, Color.fromRGB(0x8B0000), augmentedRadius);
            case 22 -> spawnEffect(player, Color.fromRGB(0xFCED00), regularRadius);
            case 23 -> spawnEffect(player, Color.fromRGB(0xFCED00), augmentedRadius);
            case 24, 26 -> spawnDragon(player);
            case 25 -> spawnEffect(player, Color.fromRGB(0x45033E), regularRadius);
            case 27 -> spawnEffect(player, Color.fromRGB(0x45033E), augmentedRadius);
            case 28 -> spawnEffect(player, Color.RED, regularRadius);
            case 29 -> spawnEffect(player, Color.RED, augmentedRadius);
        }
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