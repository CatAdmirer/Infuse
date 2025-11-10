package com.catadmirer.infuseSMP.Managers;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap();
    private static final Map<UUID, Map<String, Long>> durations = new ConcurrentHashMap();
    public static final Map<String, String> displayNames = new ConcurrentHashMap();

    public static void setDuration(UUID playerUUID, String key, long seconds) {
        ((Map)durations.computeIfAbsent(playerUUID, (k) -> {
            return new ConcurrentHashMap();
        })).put(key, System.currentTimeMillis() + seconds * 1000L);
    }

    public static boolean isEffectActive(UUID playerUUID, String key) {
        return getEffectTimeLeft(playerUUID, key) > 0L;
    }

    public static long getEffectTimeLeft(UUID playerUUID, String key) {
        Map<String, Long> playerDurations = (Map)durations.get(playerUUID);
        if (playerDurations != null && playerDurations.containsKey(key)) {
            long timeLeft = (Long)playerDurations.get(key) - System.currentTimeMillis();
            return timeLeft > 0L ? timeLeft : 0L;
        } else {
            return 0L;
        }
    }

    public static void clearSpecificDuration(UUID playerUUID, String key) {
        Map<String, Long> playerDurations = (Map)durations.get(playerUUID);
        if (playerDurations != null) {
            playerDurations.remove(key);
        }

    }

    public static void cleanupExpiredDurations() {
        long currentTime = System.currentTimeMillis();
        Iterator var2 = durations.keySet().iterator();

        while(var2.hasNext()) {
            UUID playerUUID = (UUID)var2.next();
            Map<String, Long> playerDurations = (Map)durations.get(playerUUID);
            if (playerDurations != null) {
                playerDurations.entrySet().removeIf((entry) -> {
                    return (Long)entry.getValue() <= currentTime;
                });
            }
        }

    }

    public static boolean isOnCooldown(UUID playerUUID, String key) {
        return getCooldownTimeLeft(playerUUID, key) > 0L;
    }

    public static void setCooldown(UUID playerUUID, String key, long seconds) {
        ((Map)cooldowns.computeIfAbsent(playerUUID, (k) -> {
            return new ConcurrentHashMap();
        })).put(key, System.currentTimeMillis() + seconds * 1000L);
    }

    public static long getCooldownTimeLeft(UUID playerUUID, String key) {
        Map<String, Long> playerCooldowns = (Map)cooldowns.get(playerUUID);
        if (playerCooldowns != null && playerCooldowns.containsKey(key)) {
            long timeLeft = (Long)playerCooldowns.get(key) - System.currentTimeMillis();
            return timeLeft > 0L ? timeLeft : 0L;
        } else {
            return 0L;
        }
    }

    public static void clearSpecificCooldown(UUID playerUUID, String key) {
        Map<String, Long> playerCooldowns = (Map)cooldowns.get(playerUUID);
        if (playerCooldowns != null) {
            playerCooldowns.remove(key);
        }

    }

    public static void cleanupAllExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();
        Iterator var2 = cooldowns.keySet().iterator();

        while(var2.hasNext()) {
            UUID playerUUID = (UUID)var2.next();
            Map<String, Long> playerCooldowns = (Map)cooldowns.get(playerUUID);
            if (playerCooldowns != null) {
                playerCooldowns.entrySet().removeIf((entry) -> {
                    return (Long)entry.getValue() <= currentTime;
                });
            }
        }

    }

    public static void removeAllCooldowns(UUID playerUUID) {
        cooldowns.remove(playerUUID);
    }
}
