package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.util.MessageUtil;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarUpdater extends BukkitRunnable {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Infuse plugin;

    public ActionBarUpdater(Infuse plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();

            // Composing the action bar
            String key;
            InfuseEffect effect;

            String placeholder = plugin.getMainConfig().emptyEffectIcon() ? "\ue901\ue904" : "";

            String leftPad = "";
            String leftTime = "";
            String leftEmoji = placeholder;
            String rightEmoji = placeholder;
            String rightTime = "";
            String rightPad = "";

            // Loading info for the first effect
            effect = plugin.getDataManager().getEffect(uuid, "1");
            if (effect != null) {
                leftEmoji = effect.getIcon() + "\ue904";

                key = effect.getPlainKey();
                if (CooldownManager.isEffectActive(uuid, key)) {
                    leftEmoji = String.valueOf(effect.getActiveIcon());

                    long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                    leftTime = "<#" + Integer.toHexString(effect.getPotionColor().getRGB() & 0xFFFFFF) + ">" + MessageUtil.formatTime(timeLeft);
                    rightPad = getSpaceTimeStr(mm.stripTags(leftTime));
                } else if (CooldownManager.isOnCooldown(uuid, key)) {
                    long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                    leftTime = MessageUtil.formatTime(timeLeft);
                    rightPad = getSpaceTimeStr(mm.stripTags(leftTime));
                }
            }

            // Loading info for the second effect
            effect = plugin.getDataManager().getEffect(uuid, "2");
            if (effect != null) {
                rightEmoji = effect.getIcon() + "\ue904";

                key = effect.getPlainKey();
                if (CooldownManager.isEffectActive(uuid, key)) {
                    rightEmoji = String.valueOf(effect.getActiveIcon());

                    long timeLeft = CooldownManager.getEffectTimeLeft(uuid, key) / 1000L;
                    rightTime = "<#" + Integer.toHexString(effect.getPotionColor().getRGB() & 0xFFFFFF) + ">" + MessageUtil.formatTime(timeLeft);
                    leftPad = getSpaceTimeStr(mm.stripTags(rightTime));
                } else if (CooldownManager.isOnCooldown(uuid, key)) {
                    long timeLeft = CooldownManager.getCooldownTimeLeft(uuid, key) / 1000L;
                    rightTime = MessageUtil.formatTime(timeLeft);
                    leftPad = getSpaceTimeStr(mm.stripTags(rightTime));
                }
            }

            // Sending the action bar
            player.sendActionBar(mm.deserialize(String.format("<b>%s%s</b> <white>%s %s <b>%s%s</b>", leftPad, leftTime, leftEmoji, rightEmoji, rightTime, rightPad)));
        });
    }

    public String getSpaceTimeStr(String timeStr) {
        return "\ue905".repeat(timeStr.length() - 1) + (timeStr.contains(":") ? "\ue904" : "\ue905");
    }
}
