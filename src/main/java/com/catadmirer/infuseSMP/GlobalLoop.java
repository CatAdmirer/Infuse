package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.effects.Heart;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
import com.catadmirer.infuseSMP.managers.ParticleManager;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
            InfuseEffect lEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
            InfuseEffect rEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");

            // Drawing particles
            ParticleManager.spawnEffectParticles(player, lEffect);
            ParticleManager.spawnEffectParticles(player, rEffect);
            ParticleManager.spawnCursedParticles(player);

            // Applying passive effects to the player
            if (lEffect != null) lEffect.applyPassives(player);

            // Applying passive effects to the player
            if (rEffect != null) rEffect.applyPassives(player);

            // Making sure the apophis boost has been removed
            if (!plugin.getDataManager().hasEffect(player, new Apophis())) {
                AttributeInstance playerHealth = player.getAttribute(Attribute.MAX_HEALTH);
                playerHealth.removeModifier(Apophis.APOPHIS_BOOST);
            }

            // Making sure the heart boost has been removed
            if (!plugin.getDataManager().hasEffect(player, new Heart())) {
                AttributeInstance playerHealth = player.getAttribute(Attribute.MAX_HEALTH);
                playerHealth.removeModifier(Heart.heartBoost);
            }
        }
    }
}
