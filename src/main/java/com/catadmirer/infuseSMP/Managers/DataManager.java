package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Infuse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DataManager {

    private final File dataFile;
    private FileConfiguration config;
    private final Map<UUID, Set<UUID>> trustMap = new HashMap<>();

    public DataManager(File dataFolder) {
        File oldFile = new File(dataFolder, "player_hacks.yml");
        this.dataFile = new File(dataFolder, "playerdata.yml");
        if (oldFile.exists() && !dataFile.exists()) {
            boolean success = oldFile.renameTo(dataFile);
            if (!success) {
                Infuse.getInstance().getLogger().warning("Failed to rename the file");
            }
        }
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); }
            catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(dataFile);
        loadTrustData();
    }

    public void addTrust(Player caster, Player trusted) {
        if (caster == null || trusted == null || caster.getUniqueId().equals(trusted.getUniqueId())) return;

        trustMap.computeIfAbsent(caster.getUniqueId(), k -> new HashSet<>()).add(trusted.getUniqueId());
        saveTrustData(caster.getUniqueId());
    }

    public void removeTrust(Player caster, Player trusted) {
        if (caster == null || trusted == null) return;

        Set<UUID> trustedSet = trustMap.get(caster.getUniqueId());
        if (trustedSet != null) {
            trustedSet.remove(trusted.getUniqueId());
            saveTrustData(caster.getUniqueId());
        }
    }

    public boolean isTrusted(Player personwhostrusted, Player person) {
        if (personwhostrusted == null || person == null) return false;
        if (personwhostrusted.getUniqueId().equals(person.getUniqueId())) return true;

        Set<UUID> trustedSet = trustMap.get(person.getUniqueId());
        return trustedSet != null && trustedSet.contains(personwhostrusted.getUniqueId());
    }

    private void loadTrustData() {
        trustMap.clear();
        for (String uuidStr : config.getKeys(false)) {
            List<String> trustedUUIDStrings = config.getStringList(uuidStr + ".trust");
            Set<UUID> trustedSet = new HashSet<>();
            for (String trustedUUIDStr : trustedUUIDStrings) {
                try { trustedSet.add(UUID.fromString(trustedUUIDStr)); }
                catch (IllegalArgumentException ignored) {}
            }
            if (!trustedSet.isEmpty()) {
                try { trustMap.put(UUID.fromString(uuidStr), trustedSet); }
                catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private void saveTrustData(UUID playerUUID) {
        Set<UUID> trustedSet = trustMap.getOrDefault(playerUUID, new HashSet<>());
        List<String> trustedList = trustedSet.stream().map(UUID::toString).collect(Collectors.toList());
        config.set(playerUUID.toString() + ".trust", trustedList);
        saveConfig();
    }

    public void setEffect(UUID playerUUID, String slot, @Nullable EffectMapping effect) {
        if (effect == null) {
            config.set(playerUUID.toString() + "." + slot, null);
        } else {
            config.set(playerUUID.toString() + "." + slot, effect.name());
        }
        saveConfig();
    }


    @Nullable
    public EffectMapping getEffect(UUID playerUUID, String slot) {
        String effectKey = config.getString(playerUUID.toString() + "." + slot);
        if (effectKey == null) return null;

        try {
            return EffectMapping.valueOf(effectKey);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    public void removeEffect(UUID playerUUID, String slot) {
        config.set(playerUUID.toString() + "." + slot, null);
        saveConfig();
    }

    public void setControlDefault(UUID playerUUID, String defaultMode) {
        config.set(playerUUID.toString() + ".controls", defaultMode);
        saveConfig();
    }

    public String getControlDefault(UUID playerUUID) {
        return config.getString(playerUUID.toString() + ".controls", "offhand");
    }

    private void saveConfig() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(dataFile);
        loadTrustData();
    }
}