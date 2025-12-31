package com.catadmirer.infuseSMP.placeholders;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.MessageUtil;
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
        return key.replaceFirst("aug_", "");
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
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        boolean useEmptyIcon = plugin.getConfig("empty_effect_icon");
        UUID uuid = player.getUniqueId();

        switch (params.toLowerCase()) {
            case "first_effect":
                return getEffectIcon(useEmptyIcon, uuid, "1");
            case "second_effect":
                return getEffectIcon(useEmptyIcon, uuid, "2");
            case "first_time":
                return getTime(uuid, "1");
            case "second_time":
                return getTime(uuid, "2");
            case "first_effect_raw":
                return getEffectRaw(uuid, "1");
            case "second_effect_raw":
                return getEffectRaw(uuid, "2");
            case "first_effect_name":
                return getEffectName(uuid, "1");
            case "second_effect_name":
                return getEffectName(uuid, "2");
        }

        return null;
    }

    private String formatTime(long totalSeconds, ChatColor color) {
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        return color + "&l" + timeString + "&r";
    }

    public String getEffectIcon(boolean useEmptyIcon, UUID uuid, String slot) {
        EffectMapping effect = plugin.getEffectManager().getEffect(uuid, slot);
        if (effect != null) {
            String key = effect.regular().getKey();
            if (key != null) {
                if (CooldownManager.isEffectActive(uuid, key)) {
                    return "" + effect.getActiveIcon();
                } else {
                    return "" + effect.getIcon();
                }
            }
        }

        if (useEmptyIcon) {
            return "\uE901";
        }

        return "";
    }

    public String getTime(UUID uuid, String slot) {
        EffectMapping effect = plugin.getEffectManager().getEffect(uuid, slot);
        if (effect == null) return "";

        String key = effect.getKey();

        if (CooldownManager.isEffectActive(uuid, key)) {
            return formatTime(CooldownManager.getEffectTimeLeft(uuid, key) / 1000, ChatColor.of(effect.getColor()));
        }

        if (CooldownManager.isOnCooldown(uuid, key)) {
            return formatTime(CooldownManager.getCooldownTimeLeft(uuid, key) / 1000, ChatColor.WHITE);
        }

        return "";
    }

    public String getEffectRaw(UUID uuid, String slot) {
        EffectMapping effect = plugin.getEffectManager().getEffect(uuid, slot);
        if (effect != null) return MessageUtil.stripAllColors(effect.getName());

        return "";
    }

    public String getEffectName(UUID uuid, String slot) {
        EffectMapping effect = plugin.getEffectManager().getEffect(uuid, slot);
        if (effect != null) return effect.getName();

        return "";
    }
}
