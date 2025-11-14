package com.catadmirer.infuseSMP.Particles;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Particles {

    
    private final CooldownManager cooldownManager = new CooldownManager();

    public void startTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(Infuse.getInstance(), (task) -> {
            Iterator var2 = Bukkit.getOnlinePlayers().iterator();

            while(var2.hasNext()) {
                Player player = (Player)var2.next();
                this.applyParticlesForHack(player, "1");
                this.applyParticlesForHack(player, "2");
            }

        }, 1L, 19L);
    }

    private void applyParticlesForHack(Player player, String type) {
        String hackName = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (hackName == null) return;

        String stripped = ChatColor.stripColor(hackName);
        Integer abilityId = EffectMaps.getEffectNumber(stripped);
        if (abilityId == null) return;
        switch (abilityId) {
            case 0:
                AlsoParticles.spawnEffect2(player, Color.GREEN);
                break;
            case 1:
                AlsoParticles.spawnAugmented(player, Color.GREEN);
                break;
            case 2:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(190, 163, 202));
                break;
            case 3:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(190, 163, 202));
                break;
            case 4:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(252, 120, 3));
                break;
            case 5:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(252, 120, 3));
                break;
            case 6:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0, 255, 255));
                break;
            case 7:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0, 255, 255));
                break;
            case 8:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(185, 108, 0));
                break;
            case 9:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(185, 108, 0));
                break;
            case 10:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(252, 0, 70));
                break;
            case 11:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(252, 0, 70));
                break;
            case 14:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0, 90, 252));
                break;
            case 15:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0, 90, 252));
                break;
            case 16:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(255, 3, 239));
                break;
            case 17:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(255, 3, 239));
                break;
            case 18:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(209, 164, 75));
                break;
            case 19:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(209, 164, 75));
                break;
            case 20:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(139, 0, 0));
                break;
            case 21:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(139, 0, 0));
                break;
            case 22:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(252, 237, 0));
                break;
            case 23:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(252, 237, 0));
                break;
            case 24, 26:
                spawnDragon(player);
                break;
            case 25:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(69, 3, 62));
                break;
            case 27:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(69, 3, 62));
                break;
            case 28:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(255, 0, 0));
                break;
            case 29:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(255, 0, 0));
                break;
            default:
                break;
        }
    }


    public static void spawnDragon(Player player) {
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0.0D, 1.0D, 0.0D), 32, 0.3D, 0.5D, 0.3D, 0.0D);
    }
}

