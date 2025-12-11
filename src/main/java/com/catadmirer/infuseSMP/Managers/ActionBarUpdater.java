package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUpdater extends BukkitRunnable {
    private final Set<UUID> playersWithActiveEffects = new HashSet<>();

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (this.playersWithActiveEffects.contains(uuid)) continue;
            String firstTime = "";
            boolean emptyEffectIcon = Infuse.getInstance().getConfig("empty_effect_icon");
            char firstEmoji = 0;
            if (emptyEffectIcon) {
                firstEmoji = '\uE901';
            }
            EffectMapping primaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (primaryEffect != null) {
                String key = primaryEffect.regular().getKey();
                if (CooldownManager.isEffectActive(uuid, key)) {
                    long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                    firstTime = this.formatTime(timeLeft, ChatColor.of(primaryEffect.getColor()));
                    firstEmoji = primaryEffect.getActiveIcon();
                } else if (CooldownManager.isOnCooldown(uuid, key)) {
                    long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                    firstTime = this.formatTime(timeLeft, ChatColor.WHITE);
                    firstEmoji = primaryEffect.getIcon();
                } else {
                    firstEmoji = primaryEffect.getIcon();
                }
            }
            String secondTime = "";
            char secondEmoji = 0;
            if (emptyEffectIcon) {
                secondEmoji = '\uE901';
            }
            EffectMapping secondaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (secondaryEffect != null) {
                String key = secondaryEffect.regular().getKey();
                if (CooldownManager.isEffectActive(uuid, key)) {
                    long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                    secondTime = this.formatTime(timeLeft, ChatColor.of(secondaryEffect.getColor()));
                    secondEmoji = secondaryEffect.getActiveIcon();
                } else if (CooldownManager.isOnCooldown(uuid, key)) {
                    long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                    secondTime = this.formatTime(timeLeft, ChatColor.WHITE);
                    secondEmoji = secondaryEffect.getIcon();
                } else {
                    secondEmoji = secondaryEffect.getIcon();
                }
            }
            StringBuilder actionBar = new StringBuilder();
            if (!firstTime.isEmpty()) actionBar.append(firstTime).append("  ");
            if (firstEmoji != 0) actionBar.append(firstEmoji).append(" ");
            if (secondEmoji != 0) actionBar.append(secondEmoji).append("  ");
            if (!secondTime.isEmpty()) actionBar.append(secondTime).append(" ");

            String finalMessage = actionBar.toString().trim();
            if (!finalMessage.isEmpty()) {
                player.sendActionBar(TextComponent.fromLegacy(finalMessage));
            }
        }
    }


    private String formatTime(long totalSeconds, ChatColor color) {
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        return color + "§l" + timeString + "§r";
    }
}
