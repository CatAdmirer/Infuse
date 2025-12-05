package com.catadmirer.infuseSMP.Particles;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Particles {
    public void startTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Infuse.getInstance(), (task) -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.applyParticlesForEffect(player, "1");
                this.applyParticlesForEffect(player, "2");
            }

        }, 1, 19);
    }

    private void applyParticlesForEffect(Player player, String type) {
        String effectName = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (effectName == null) return;

        String stripped = ChatColor.stripColor(effectName);
        Integer abilityId = EffectMaps.getEffectNumber(stripped);
        if (abilityId == null) return;
        switch (abilityId) {
            case 0:
                spawnEffect2(player, Color.GREEN);
                break;
            case 1:
                spawnAugmented(player, Color.GREEN);
                break;
            case 2:
                spawnEffect2(player, Color.fromRGB(190, 163, 202));
                break;
            case 3:
                spawnAugmented(player, Color.fromRGB(190, 163, 202));
                break;
            case 4:
                spawnEffect2(player, Color.fromRGB(252, 120, 3));
                break;
            case 5:
                spawnAugmented(player, Color.fromRGB(252, 120, 3));
                break;
            case 6:
                spawnEffect2(player, Color.fromRGB(0, 255, 255));
                break;
            case 7:
                spawnAugmented(player, Color.fromRGB(0, 255, 255));
                break;
            case 8:
                spawnEffect2(player, Color.fromRGB(185, 108, 0));
                break;
            case 9:
                spawnAugmented(player, Color.fromRGB(185, 108, 0));
                break;
            case 10:
                spawnEffect2(player, Color.fromRGB(252, 0, 70));
                break;
            case 11:
                spawnAugmented(player, Color.fromRGB(252, 0, 70));
                break;
            case 14:
                spawnEffect2(player, Color.fromRGB(0, 90, 252));
                break;
            case 15:
                spawnAugmented(player, Color.fromRGB(0, 90, 252));
                break;
            case 16:
                spawnEffect2(player, Color.fromRGB(255, 3, 239));
                break;
            case 17:
                spawnAugmented(player, Color.fromRGB(255, 3, 239));
                break;
            case 18:
                spawnAugmented(player, Color.fromRGB(209, 164, 75));
                break;
            case 19:
                spawnEffect2(player, Color.fromRGB(209, 164, 75));
                break;
            case 20:
                spawnEffect2(player, Color.fromRGB(139, 0, 0));
                break;
            case 21:
                spawnAugmented(player, Color.fromRGB(139, 0, 0));
                break;
            case 22:
                spawnEffect2(player, Color.fromRGB(252, 237, 0));
                break;
            case 23:
                spawnAugmented(player, Color.fromRGB(252, 237, 0));
                break;
            case 24, 26:
                spawnDragon(player);
                break;
            case 25:
                spawnEffect2(player, Color.fromRGB(69, 3, 62));
                break;
            case 27:
                spawnAugmented(player, Color.fromRGB(69, 3, 62));
                break;
            case 28:
                spawnEffect2(player, Color.fromRGB(255, 0, 0));
                break;
            case 29:
                spawnAugmented(player, Color.fromRGB(255, 0, 0));
                break;
            default:
                break;
        }
    }

    public static void spawnDragon(Player player) {
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0.0, 1.0, 0.0), 32, 0.3, 0.5, 0.3, 0.0);
    }

    public static void spawnEffect(Player player, Color color) {
        Location base = player.getLocation().add(0.0, 0.0, 0.0);
        int count = 30;
        double spread = 0.5;

        for(int i = 0; i < count; ++i) {
            double x = (Math.random() - 0.5) * spread * 2.0;
            double y = Math.random() * 1.2;
            double z = (Math.random() - 0.5) * spread * 2.0;
            Location spawn = base.clone().add(x, y, z);
            player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, spawn, 1, 0.0, 0.0, 0.0, 0.0, color);
        }

    }

    public static void spawnEffect2(Player player, Color color) {
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0.0, 1.0, 0.0), 2, 0.0, 0.5, 0.0, 0.1, color);
    }

    public static void spawnAugmented(Player player, Color color) {
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation().add(0.0, 1.0, 0.0), 2, 0.3, 0.5, 0.3, 0.1, color);
    }

    public static void spawnHearts(Player player) {
        Location location = player.getLocation().add(0.0, 1.0, 0.0);
        Vector direction = player.getLocation().getDirection();
        double primarySpread = 0.5;
        double secondarySpread = 0.1;
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
            double randomX = (Math.random() * 2.0 - 1.0) * offsetX;
            double randomY = Math.pow(Math.random(), 2.0) * 1.0;
            double randomZ = (Math.random() * 2.0 - 1.0) * offsetZ;
            Location spawnLoc = location.clone().add(randomX, randomY, randomZ);
            player.getWorld().spawnParticle(Particle.HEART, spawnLoc, 1, 0.0, 0.0, 0.0, 0.0);
        }

    }
}

