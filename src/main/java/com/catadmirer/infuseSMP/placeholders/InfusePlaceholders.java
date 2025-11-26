package com.catadmirer.infuseSMP.placeholders;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMaps;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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
            String primaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (primaryEffect != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(primaryEffect));
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
            String primaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "1");
            if (primaryEffect != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(primaryEffect));
                String key = removeAug(stripped);
                if (key != null) {
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, EffectMaps.getColorEffect(stripped));
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, ChatColor.WHITE);
                    }
                }
            }
            return "";
        }

        if (params.equalsIgnoreCase("second_effect")) {
            String secondaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (secondaryEffect != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(secondaryEffect));
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
            String secondaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, "2");
            if (secondaryEffect != null) {
                String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(secondaryEffect));
                String key = removeAug(stripped);
                if (key != null) {
                    if (CooldownManager.isEffectActive(uuid, key)) {
                        long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, EffectMaps.getColorEffect(stripped));
                    } else if (CooldownManager.isOnCooldown(uuid, key)) {
                        long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                        return formatTime(timeLeft, ChatColor.WHITE);
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
        return var10000 + String.valueOf(ChatColor.BOLD) + timeString + String.valueOf(ChatColor.RESET);
    }
}

