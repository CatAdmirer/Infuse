package com.catadmirer.infuseSMP.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;

public class OceanListeners {
    private final Infuse plugin;

    public OceanListeners(Infuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (plugin.getConfigFile().invisDeaths()) {
            if (killer != null && killer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                String msg = Messages.INVIS_KILL.getMessage();
                msg = msg.replace("%victim%", victim.getName());
                msg = msg.replace("%killer%", "<gray><obf>Someone");
                event.deathMessage(Messages.toComponent(msg));
            } else if (victim.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                if (killer != null) {
                    String msg = Messages.INVIS_DEATH.getMessage();
                    msg = msg.replace("%victim%", "<gray><obf>Someone");
                    msg = msg.replace("%killer%", killer.getName());
                    event.deathMessage(Messages.toComponent(msg));
                }
            }
        }
    }
}