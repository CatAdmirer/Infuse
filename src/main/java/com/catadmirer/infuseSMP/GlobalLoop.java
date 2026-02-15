package com.catadmirer.infuseSMP;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.catadmirer.infuseSMP.effects.Ender;
import com.catadmirer.infuseSMP.effects.Heart;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
import com.catadmirer.infuseSMP.managers.EffectMapping;

public class GlobalLoop extends BukkitRunnable {
    private final Infuse plugin;

    public GlobalLoop(Infuse plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.runTaskTimer(plugin, 0, 20);
    }

    public void stop() {
        this.cancel();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Getting the player's equipped effects
            EffectMapping lEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
            EffectMapping rEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");

            // Applying passive effects to the player
            if (lEffect != null) {
                lEffect.applyPassiveEffects(player);
            }

            // Applying passive effects to the player
            if (rEffect != null) {
                rEffect.applyPassiveEffects(player);
            }

            // Making sure the apophis boost has been removed
            if (!plugin.getDataManager().hasEffect(player, EffectMapping.APOPHIS)) {
                AttributeInstance playerHealth = player.getAttribute(Attribute.MAX_HEALTH);
                playerHealth.removeModifier(Apophis.apophisBoost);
            }

            // Making sure the heart boost has been removed
            if (!plugin.getDataManager().hasEffect(player, EffectMapping.HEART)) {
                AttributeInstance playerHealth = player.getAttribute(Attribute.MAX_HEALTH);
                playerHealth.removeModifier(Heart.heartBoost);
            }

            // Spawning particles on cursed players
            if (Ender.cursedPlayers.contains(player.getUniqueId())) {
                player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);
            }
        }
    }
}
