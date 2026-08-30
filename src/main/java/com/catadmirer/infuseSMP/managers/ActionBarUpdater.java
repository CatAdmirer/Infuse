package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUpdater extends BukkitRunnable {
    private final Key EFFECTS_FONT = Key.key("infuse", "effects");
    private final Infuse plugin;

    public ActionBarUpdater(Infuse plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();

            // Composing the action bar
            InfuseEffect effect;

            String placeholder = plugin.getMainConfig().emptyEffectIcon() ? "\uffff\ufff2" : "";

            String leftEmoji = placeholder;
            String rightEmoji = placeholder;

            // Loading info for the first effect
            effect = plugin.getDataManager().getEffect(uuid, "1");
            if (effect != null) {
                char icon = effect.getIcon(player, "1");
                leftEmoji = icon + "\ufff2";
            }

            // Loading info for the second effect
            effect = plugin.getDataManager().getEffect(uuid, "2");
            if (effect != null) {
                char icon = effect.getIcon(player, "2");
                rightEmoji = icon + "\ufff2";
            }

            // Sending the action bar
            player.sendActionBar(Component.text(leftEmoji + " " + rightEmoji).font(EFFECTS_FONT));
        });
    }
}
