package com.catadmirer.infuseSMP.Placeholders;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectManager;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InfusePlaceholders extends PlaceholderExpansion {

    private Infuse plugin;

    public InfusePlaceholders(Infuse plugin) {
        this.plugin = plugin;
    }

    public String removeAug(String key) {
        if (key.startsWith("aug_")) {
            return key.substring(4);
        }
        return key;
    }

    @Override
    public String getAuthor() {
        return "catadmirer";
    }

    @Override
    public String getIdentifier() {
        return "infuse";
    }

    @Override
    public String getVersion() {
        return "1.5.2";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        boolean emptyEffectIcon = plugin.getCanfig("empty_effect_icon");
        UUID uuid = player.getUniqueId();

        if (params.equalsIgnoreCase("first_effect")) {
            String commonHack = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (commonHack != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(commonHack));
                String key = removeAug(stripped);
                if (key != null) {
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        return EffectMaps.getActiveEffect(stripped);
                    } else {
                        return EffectMaps.getCooldownEffect(stripped);
                    }
                }
            }
            if (emptyEffectIcon) {
                return "\uE058";
            } else {
                return "";
            }
        }

        if (params.equalsIgnoreCase("first_time")) {
            String commonHack = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (commonHack != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(commonHack));
                String key = removeAug(stripped);
                if (key != null) {
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, EffectMaps.getColorEffect(stripped));
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, net.md_5.bungee.api.ChatColor.WHITE);
                    }
                }
            }
            return "";
        }

        if (params.equalsIgnoreCase("second_effect")) {
            String legendaryHack = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (legendaryHack != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(legendaryHack));
                String key = removeAug(stripped);
                if (key != null) {
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        return EffectMaps.getActiveEffect(stripped);
                    } else {
                        return EffectMaps.getCooldownEffect(stripped);
                    }
                }
            }
            if (emptyEffectIcon) {
                return "\uE058";
            } else {
                return "";
            }
        }

        if (params.equalsIgnoreCase("second_time")) {
            String legendaryHack = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (legendaryHack != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(legendaryHack));
                String key = removeAug(stripped);
                if (key != null) {
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, EffectMaps.getColorEffect(stripped));
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, net.md_5.bungee.api.ChatColor.WHITE);
                    }
                }
            }
            return "";
        }

        if (params.equalsIgnoreCase("first_effect_raw")) {
            String strip = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (strip != null) {
                String stripped = stripAllColors(strip);
                return stripped;
            }
            return "";
        }

        if (params.equalsIgnoreCase("second_effect_raw")) {
            String strip = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (strip != null) {
                String stripped = stripAllColors(strip);
                return stripped;
            }
            return "";
        }

        if (params.equalsIgnoreCase("first_effect_name")) {
            String strip = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (strip != null) {
                return strip;
            }
            return "";
        }

        if (params.equalsIgnoreCase("second_effect_name")) {
            String strip = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (strip != null) {
                return strip;
            }
            return "";
        }

        return null;
    }

    public static String stripAllColors(String input) {
        if (input == null) return null;
        return input.replaceAll("§#[A-Fa-f0-9]{6}", "")
                .replaceAll("§[0-9a-fk-orK-OR]", "");
    }

    private String formatTime(long totalSeconds, net.md_5.bungee.api.ChatColor color) {
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        String var10000 = String.valueOf(color);
        return var10000 + String.valueOf(net.md_5.bungee.api.ChatColor.BOLD) + timeString + String.valueOf(net.md_5.bungee.api.ChatColor.RESET);
    }
}

