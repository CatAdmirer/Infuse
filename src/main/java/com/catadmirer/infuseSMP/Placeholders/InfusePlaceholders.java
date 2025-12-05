package com.catadmirer.infuseSMP.Placeholders;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectMaps;
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
                return getEffectKey(useEmptyIcon, uuid, "1");
            case "second_effect":
                return getEffectKey(useEmptyIcon, uuid, "2");
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

    private String formatTime(long totalSeconds, net.md_5.bungee.api.ChatColor color) {
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        String var10000 = String.valueOf(color);
        return var10000 + String.valueOf(ChatColor.BOLD) + timeString + String.valueOf(ChatColor.RESET);
    }

    public String getEffectKey(boolean useEmptyIcon, UUID uuid, String slot) {
        String effect = Infuse.getInstance().getEffectManager().getEffect(uuid, slot);
        if (effect != null) {
            String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(effect));
            String key = removeAug(stripped);
            if (key != null) {
                if (CooldownManager.isEffectActive(uuid, key)) {
                    return "" + EffectMaps.getActiveEffect(stripped);
                } else {
                    return "" + EffectMaps.getCooldownEffect(stripped);
                }
            }
        }

        if (useEmptyIcon) {
            return "\uE058";
        }

        return "";
    }

    public String getTime(UUID uuid, String slot) {
        String primaryEffect = Infuse.getInstance().getEffectManager().getEffect(uuid, slot);
        if (primaryEffect == null) return "";

        String stripped = Infuse.getInstance().getEffectReversed(ChatColor.stripColor(primaryEffect));
        String key = removeAug(stripped);
        if (key == null) return "";

        if (CooldownManager.isEffectActive(uuid, key)) {
            return formatTime(CooldownManager.getEffectTimeLeft(uuid, key) / 1000, EffectMaps.getColorEffect(stripped));
        }
        
        if (CooldownManager.isOnCooldown(uuid, key)) {
            return formatTime(CooldownManager.getCooldownTimeLeft(uuid, key) / 1000, ChatColor.WHITE);
        }

        return "";
    }

    public String getEffectRaw(UUID uuid, String slot) {
        String effect = Infuse.getInstance().getEffectManager().getEffect(uuid, slot);
        if (effect != null) return MessageUtil.stripAllColors(effect);

        return "";
    }

    public String getEffectName(UUID uuid, String slot) {
        String effect = Infuse.getInstance().getEffectManager().getEffect(uuid, slot);
        if (effect != null) return effect;

        return "";
    }
}
