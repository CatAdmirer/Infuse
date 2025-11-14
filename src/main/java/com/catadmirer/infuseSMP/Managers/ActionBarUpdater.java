package com.catadmirer.infuseSMP.Managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.catadmirer.infuseSMP.Infuse;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUpdater extends BukkitRunnable {
    private final Set<UUID> playersWithActiveEffects = new HashSet();

        public String removeAug(String key) {
            if (key.startsWith("aug_")) {
                return key.substring(4);
            }
            return key;
        }

    public String stripAllColors(String input) {
        if (input == null) return null;
        Pattern pattern = Pattern.compile(
                "(§#[0-9a-fA-F]{6})" +
                        "|(§x(§[0-9a-fA-F]){6})" +
                        "|(§[0-9a-fk-orA-FK-OR])"
        );
        return pattern.matcher(input).replaceAll("");
    }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (this.playersWithActiveEffects.contains(uuid)) continue;
            String firstTime = "";
            boolean emptyEffectIcon = Infuse.getInstance().getCanfig("empty_effect_icon");
            String firstEmoji = "";
            if (emptyEffectIcon) {
                firstEmoji = "\uE058";
            }
            String commonHack = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (commonHack != null) {
                String stripped = Infuse.getInstance().getEffectReversed(stripAllColors(commonHack));
                String key = removeAug(stripped);
                if (key != null) {
                    net.md_5.bungee.api.ChatColor color = EffectMaps.getColorEffect(stripped);
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        firstTime = this.formatTime(timeLeft, color);
                        firstEmoji = EffectMaps.getActiveEffect(stripped);
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        firstTime = this.formatTime(timeLeft, net.md_5.bungee.api.ChatColor.WHITE);
                        firstEmoji = EffectMaps.getCooldownEffect(stripped);
                    } else {
                        firstEmoji = EffectMaps.getCooldownEffect(stripped);
                    }
                }
            }
            String secondTime = "";
            String secondEmoji = "";
            if (emptyEffectIcon) {
                secondEmoji = "\uE058";
            }
            String legendaryHack = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (legendaryHack != null) {
                String stripped = Infuse.getInstance().getEffectReversed(stripAllColors(legendaryHack));
                String key = removeAug(stripped);
                if (key != null) {
                    net.md_5.bungee.api.ChatColor color = EffectMaps.getColorEffect(stripped);
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        secondTime = this.formatTime(timeLeft, color);
                        secondEmoji = EffectMaps.getActiveEffect(stripped);
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        secondTime = this.formatTime(timeLeft, net.md_5.bungee.api.ChatColor.WHITE);
                        secondEmoji = EffectMaps.getCooldownEffect(stripped);
                    } else {
                        secondEmoji = EffectMaps.getCooldownEffect(stripped);
                    }
                }
            }
            StringBuilder actionBar = new StringBuilder();
            if (!firstTime.isEmpty()) actionBar.append(firstTime).append("  ");
            if (!firstEmoji.isEmpty()) actionBar.append(firstEmoji).append(" ");
            if (!secondEmoji.isEmpty()) actionBar.append(secondEmoji).append("  ");
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
        return var10000 + net.md_5.bungee.api.ChatColor.BOLD + timeString + ChatColor.RESET;
    }
}
