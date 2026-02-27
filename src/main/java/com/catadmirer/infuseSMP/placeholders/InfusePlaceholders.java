package com.catadmirer.infuseSMP.placeholders;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.util.MessageUtil;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class InfusePlaceholders extends PlaceholderExpansion {
    private Infuse plugin;

    public InfusePlaceholders(Infuse plugin) {
        this.plugin = plugin;
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
        EffectMapping effect = plugin.getDataManager().getEffect(uuid, slot);

        if (effect == null) {
            return plugin.getConfigFile().emptyEffectIcon() ? "\uE901" : "";
        }

        return "" + (CooldownManager.isEffectActive(uuid, effect.regular().getKey()) ? effect.getActiveIcon() : effect.getIcon());
    }

    public String getTime(UUID uuid, String slot) {
        EffectMapping effect = plugin.getDataManager().getEffect(uuid, slot);
        if (effect == null) return "";
        String key = effect.getKey();
        Component comp;
        if (CooldownManager.isEffectActive(uuid, key)) {
            long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000;
            comp = MessageUtil.formatTime(timeLeft, TextColor.color(effect.getColor().getRGB()));
        } else if (CooldownManager.isOnCooldown(uuid, key)) {
            long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000;
            comp = MessageUtil.formatTime(timeLeft, NamedTextColor.WHITE);
        } else {
            return "";
        }
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    public String getEffectRaw(UUID uuid, String slot) {
        EffectMapping effect = plugin.getDataManager().getEffect(uuid, slot);
        if (effect== null) return "";
        
        return PlainTextComponentSerializer.plainText().serialize(effect.getName().toComponent());
    }

    public String getEffectName(UUID uuid, String slot) {
        EffectMapping effect = plugin.getDataManager().getEffect(uuid, slot);
        if (effect == null) return "";
        
        return effect.getName().toString();
    }
}