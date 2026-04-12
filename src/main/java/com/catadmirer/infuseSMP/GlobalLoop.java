package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.effects.Ocean;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GlobalLoop extends BukkitRunnable {
    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);

    public void start() {
        this.runTaskTimer(plugin, 0, 20);
    }

    public void stop() {
        this.cancel();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Spawning particles on cursed players
            if (Ender.cursedPlayers.contains(player.getUniqueId())) {
                player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);
            }

            // Applying glowing to players near someone with the ender effect
            Ender enderEffect = new Ender();
            if (plugin.getDataManager().hasEffect(player, enderEffect)) {
                enderEffect.applyGlowingToUntrusted(player);
            }

            // Drowning players near ocean users
            if (plugin.getDataManager().hasEffect(player, new Ocean())) {
                // Boosting the strength and damage of the passive drowning if the spark is active
                int drownStrength = 5;
                int drownDamage = 1;
                if (CooldownManager.isEffectActive(player.getUniqueId(), "ocean"))  {
                    drownStrength = 20;
                    drownDamage = 2;
                }
                
                for (Player otherPlayer : player.getWorld().getPlayers()) {
                    if (otherPlayer.equals(player)) continue;
                    if (otherPlayer.getLocation().distance(player.getLocation()) <= 5) {
                        int newAir = Math.max(otherPlayer.getRemainingAir() - drownStrength, -20);
                        otherPlayer.setRemainingAir(newAir);
                        if (newAir <= 0) {
                            otherPlayer.damage(drownDamage);
                        }
                    }
                }
            }
        }
    }
}
