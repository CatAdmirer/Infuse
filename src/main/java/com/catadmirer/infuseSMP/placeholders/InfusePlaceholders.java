package com.catadmirer.infuseSMP.placeholders;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.util.MessageUtil;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class InfusePlaceholders extends PlaceholderExpansion {
    private Infuse plugin = JavaPlugin.getPlugin(Infuse.class);

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
        UUID uuid = player.getUniqueId();

        switch (params.toLowerCase()) {
            case "first_effect":
                return getEffectIcon(uuid, "1");
            case "second_effect":
                return getEffectIcon(uuid, "2");
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

    public String getEffectIcon(UUID uuid, String slot) {
        InfuseEffect effect = plugin.getDataManager().getEffect(uuid, slot);

        if (effect == null) {
            return plugin.getMainConfig().emptyEffectIcon() ? "\uE901" : "";
        }

        return "" + (CooldownManager.isEffectActive(uuid, effect.getName()) ? effect.getActiveIcon() : effect.getIcon());
    }

    public String getTime(UUID uuid, String slot) {
        InfuseEffect effect = plugin.getDataManager().getEffect(uuid, slot);
        if (effect == null) return "";
        String key = effect.getKey();
        if (CooldownManager.isEffectActive(uuid, key)) {
            long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000;
            return "<#" + Integer.toHexString(EffectConstants.potionColor(effect.getId()).getRGB() & 0xFFFFFF) + ">" + MessageUtil.formatTime(timeLeft);
        } else if (CooldownManager.isOnCooldown(uuid, key)) {
            long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000;
            return "<white>" + MessageUtil.formatTime(timeLeft);
        } else {
            return "";
        }
    }

    public String getEffectRaw(UUID uuid, String slot) {
        InfuseEffect effect = plugin.getDataManager().getEffect(uuid, slot);
        if (effect== null) return "";
        
        return PlainTextComponentSerializer.plainText().serialize(effect.getItemName().toComponent());
    }

    public String getEffectName(UUID uuid, String slot) {
        InfuseEffect effect = plugin.getDataManager().getEffect(uuid, slot);
        if (effect == null) return "";
        
        return effect.getItemName().toString();
    }
}