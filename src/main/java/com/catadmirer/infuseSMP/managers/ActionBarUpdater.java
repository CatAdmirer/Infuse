package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.util.MessageUtil;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
            Component actionBar = getPart1(uuid, "1")
                    .append(Component.space())
                    .append(getPart2(uuid, "2"));

            // Sending the action bar
            if (!actionBar.equals(Component.empty())) {
                player.sendActionBar(actionBar);
            }
        });
    }

    private Component getPart1(UUID uuid, String slot) {
        Component time = Component.empty();

        // Getting the effect equipped in the slot
        InfuseEffect effect = plugin.getDataManager().getEffect(uuid, slot);

        // Handling empty slots
        boolean emptyEffectEmoji = plugin.getConfigFile().emptyEffectIcon();
        if (effect == null) {
            return emptyEffectEmoji ? Component.text("\uE901 ") : Component.empty();
        }

        // Getting the right emoji to use and time to display
        String key = effect.getName();
        char emoji = effect.getIcon();
        if (CooldownManager.isEffectActive(uuid, key)) {
            long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
            time = MessageUtil.formatTime(timeLeft, TextColor.color(EffectConstants.potionColor(effect.getId()).getRGB()));
            emoji = effect.getActiveIcon();
        } else if (CooldownManager.isOnCooldown(uuid, key)) {
            long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
            time = MessageUtil.formatTime(timeLeft, NamedTextColor.WHITE);
        }

        // Building the result
        if (time.equals(Component.empty())) return Component.text(emoji);

        return time.append(Messages.toComponent("  <white>" + emoji).color(NamedTextColor.WHITE));
    }

    private Component getPart2(UUID uuid, String slot) {
        Component time = Component.empty();

        // Getting the effect equipped in the slot
        InfuseEffect effect = plugin.getDataManager().getEffect(uuid, slot);

        // Handling empty slots
        boolean emptyEffectEmoji = plugin.getConfigFile().emptyEffectIcon();
        if (effect == null) {
            return emptyEffectEmoji ? Component.text("\uE901 ") : Component.empty();
        }

        // Getting the right emoji to use and time to display
        String key = effect.getName();

        char emoji = effect.getIcon();
        // Handling thief's stolen effects
        if (effect instanceof Thief thiefEffect) {
            if (CooldownManager.isEffectActive(uuid, "thief_stolen")) {
                long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                time = MessageUtil.formatTime(timeLeft, TextColor.color(EffectConstants.potionColor(effect.getId()).getRGB()));

                emoji = thiefEffect.getStolenEffect().getActiveIcon();
            }
        } else if (CooldownManager.isEffectActive(uuid, key)) {
            long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
            time = MessageUtil.formatTime(timeLeft, TextColor.color(EffectConstants.potionColor(effect.getId()).getRGB()));
            emoji = effect.getActiveIcon();
        } else if (CooldownManager.isOnCooldown(uuid, key)) {
            long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
            time = MessageUtil.formatTime(timeLeft, NamedTextColor.WHITE);
        }

        // Building the result
        if (time.equals(Component.empty())) return Component.text(emoji);
        return Messages.toComponent("<white>" + emoji + "  ").color(NamedTextColor.WHITE).append(time).color(NamedTextColor.WHITE);
    }
}