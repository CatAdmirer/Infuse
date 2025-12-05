package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.util.MessageUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUpdater extends BukkitRunnable {
    private final Set<UUID> playersWithActiveEffects = new HashSet<>();

        public String removeAug(String key) {
            if (key.startsWith("aug_")) {
                return key.substring(4);
            }
            return key;
        }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (this.playersWithActiveEffects.contains(uuid)) continue;
            String firstTime = "";
            boolean emptyEffectIcon = Infuse.getInstance().getConfig("empty_effect_icon");
            Character firstEmoji = null;
            if (emptyEffectIcon) {
                firstEmoji = '\uE058';
            }
            String primaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (primaryEffect != null) {
                String stripped = Infuse.getInstance().getEffectReversed(MessageUtil.stripAllColors(primaryEffect));
                String key = removeAug(stripped);
                if (key != null) {
                    net.md_5.bungee.api.ChatColor color = EffectMaps.getColorEffect(stripped);
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        firstTime = this.formatTime(timeLeft, color);
                        firstEmoji = EffectMaps.getActiveEffect(stripped);
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        firstTime = this.formatTime(timeLeft, ChatColor.WHITE);
                        firstEmoji = EffectMaps.getCooldownEffect(stripped);
                    } else {
                        firstEmoji = EffectMaps.getCooldownEffect(stripped);
                    }
                }
            }
            String secondTime = "";
            Character secondEmoji = null;
            if (emptyEffectIcon) {
                secondEmoji = '\uE058';
            }
            String secondaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (secondaryEffect != null) {
                String stripped = Infuse.getInstance().getEffectReversed(MessageUtil.stripAllColors(secondaryEffect));
                String key = removeAug(stripped);
                if (key != null) {
                    net.md_5.bungee.api.ChatColor color = EffectMaps.getColorEffect(stripped);
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        secondTime = this.formatTime(timeLeft, color);
                        secondEmoji = EffectMaps.getActiveEffect(stripped);
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        secondTime = this.formatTime(timeLeft, ChatColor.WHITE);
                        secondEmoji = EffectMaps.getCooldownEffect(stripped);
                    } else {
                        secondEmoji = EffectMaps.getCooldownEffect(stripped);
                    }
                }
            }
            StringBuilder actionBar = new StringBuilder();
            if (!firstTime.isEmpty()) actionBar.append(firstTime).append("  ");
            if (firstEmoji != null) actionBar.append(firstEmoji).append(" ");
            if (secondEmoji != null) actionBar.append(secondEmoji).append("  ");
            if (!secondTime.isEmpty()) actionBar.append(secondTime).append(" ");

            String finalMessage = actionBar.toString().trim();
            if (!finalMessage.isEmpty()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(finalMessage));
            }
        }
    }


    private String formatTime(long totalSeconds, net.md_5.bungee.api.ChatColor color) {
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        String var10000 = String.valueOf(color);
        return var10000 + ChatColor.BOLD + timeString + ChatColor.RESET;
    }
}
