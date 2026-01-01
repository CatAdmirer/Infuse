package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUpdater extends BukkitRunnable {
    private final Infuse plugin;

    public ActionBarUpdater(Infuse plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();

            // Composing the action bar
            StringBuilder actionBar = new StringBuilder();
            actionBar.append(getPart(uuid, "1"));
            if (!actionBar.isEmpty()) actionBar.append(" ");
            actionBar.append(getPart(uuid, "2"));

            // Sending the action bar
            if (!actionBar.isEmpty()) {
                player.sendActionBar(Messages.toComponent(actionBar.toString()));
            }
        });
    }

    private String getPart(UUID uuid, String slot) {
        String time = "";

        // Getting the effect equipped in the slot
        EffectMapping effect = plugin.getEffectManager().getEffect(uuid, slot);

        // Handling empty slots
        boolean emptyEffectEmoji = plugin.getConfig("empty_effect_icon");
        if (effect == null) {
            return emptyEffectEmoji ? "\uE901 " : "";
        }

        // Getting the right emoji to use and time to display
        String key = effect.regular().getKey();
        char emoji = effect.getIcon();
        if (CooldownManager.isEffectActive(uuid, key)) {
            long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
            time = formatTime(timeLeft, ChatColor.of(effect.getColor()));
            emoji = effect.getActiveIcon();
        } else if (CooldownManager.isOnCooldown(uuid, key)) {
            long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
            time = formatTime(timeLeft, ChatColor.WHITE);
        }

        // Building the result
        StringBuilder result = new StringBuilder();
        if (!time.isEmpty()) result.append(time).append("  ");
        if (emoji != 0) result.append(emoji);

        return result.toString();
    }

    private String formatTime(long totalSeconds, ChatColor color) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        return color + "<b>" + timeString + "<reset>";
    }
}