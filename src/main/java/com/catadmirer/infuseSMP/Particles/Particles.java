package com.catadmirer.infuseSMP.Particles;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

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
                AlsoParticles.spawnEffect2(player, Color.GREEN);
                break;
            case 1:
                AlsoParticles.spawnAugmented(player, Color.GREEN);
                break;
            case 2:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0xBEA3CA));
                break;
            case 3:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0xBEA3CA));
                break;
            case 4:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0xFC7803));
                break;
            case 5:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0xFC7803));
                break;
            case 6:
                AlsoParticles.spawnEffect2(player, Color.AQUA);
                break;
            case 7:
                AlsoParticles.spawnAugmented(player, Color.AQUA);
                break;
            case 8:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0xB96C00));
                break;
            case 9:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0xB96C00));
                break;
            case 10:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0xFC0046));
                break;
            case 11:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0xFC0046));
                break;
            case 14:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0x005AFC));
                break;
            case 15:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0x005AFC));
                break;
            case 16:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0xFF03EF));
                break;
            case 17:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0xFF03EF));
                break;
            case 18:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0xD1A44B));
                break;
            case 19:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0xD1A44B));
                break;
            case 20:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0x8B0000));
                break;
            case 21:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0x8B0000));
                break;
            case 22:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0xFCED00));
                break;
            case 23:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0xFCED00));
                break;
            case 24, 26:
                spawnDragon(player);
                break;
            case 25:
                AlsoParticles.spawnEffect2(player, Color.fromRGB(0x45033E));
                break;
            case 27:
                AlsoParticles.spawnAugmented(player, Color.fromRGB(0x45033E));
                break;
            case 28:
                AlsoParticles.spawnEffect2(player, Color.RED);
                break;
            case 29:
                AlsoParticles.spawnAugmented(player, Color.RED);
                break;
            default:
                break;
        }
    }


    public static void spawnDragon(Player player) {
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0.0, 1.0, 0.0), 32, 0.3, 0.5, 0.3, 0.0);
    }
}

