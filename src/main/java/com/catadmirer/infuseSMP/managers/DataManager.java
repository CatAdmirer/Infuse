package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DataManager {
    private final Infuse plugin;
    private final File dataFile;
    private final YamlConfiguration config;
    private final Map<UUID, Set<UUID>> trustMap = new HashMap<>();

    public DataManager(Infuse plugin) {   
        this.plugin = plugin;     
        this.dataFile = new File(plugin.getDataFolder(), "data/playerdata.yml");
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully.
     */
    public boolean load() {
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "{0} not loaded, cannot load {1}.", new String[]{plugin.getName(), dataFile.getName()});
            return false;
        }

        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!createFile(false)) {
            return false;
        }

        // Loading the config
        try {
            config.load(dataFile);
            plugin.getLogger().log(Level.INFO, "Successfully loaded {0}", dataFile.getName());
            return true;
        } catch (InvalidConfigurationException err) {
            plugin.getLogger().log(Level.WARNING, "{0]} contains an invalid YAML configuration.  Verify the contents of the file.", dataFile.getName());
        } catch (IOException err) {
            plugin.getLogger().log(Level.SEVERE, "Could not find {0}.  Check that it exists.", dataFile.getName());
        }

        return false;
    }

    /**
     * Writes the config to the file.
     * 
     * @return Whether or not the config was successfully written.
     */
    public boolean save() {
        // Getting a plugin instance to use
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "{0} not loaded, cannot save the {1}.", new String[]{plugin.getName(), dataFile.getName()});
            return false;
        }

        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!createFile(false)) {
            return false;
        }

        // Saving the config
        try {
            config.save(dataFile);
            plugin.getLogger().log(Level.INFO, "Successfully saved the config to {0}", dataFile.getName());
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save to {0}.  Make sure the user has write permissions.", dataFile.getName());
        }

        return false;
    }

    /**
     * Creating the config file. If it doesn't exist, it loads the default config. If the file does
     * exist, it will only replace it if the parameter is true.
     * 
     * @param replace Whether or not to replace the config file with the default configs.
     * @return Whether or not the file was created successfully.
     */
    public boolean createFile(boolean replace) {
        // Getting a plugin instance to use
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "{0} not loaded, cannot create default {1}.", new String[]{plugin.getName(), dataFile.getName()});
            return false;
        }

        // Creating the file if it doesn't exist.
        if (!dataFile.exists()) {
            plugin.saveResource(dataFile.getName(), replace);
        }

        // Checking if the file still doesn't exist.
        if (!dataFile.exists()) {
            plugin.getLogger().log(Level.SEVERE, "Could not create {1}.  Check if it already exists.", dataFile.getName());

            return false;
        }

        return true;
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
        String effectKey = config.getString(playerUUID.toString() + "." + slot, null);
        EffectMapping effect = EffectMapping.fromEffectKey(effectKey);
        if (effectKey != null && effect == null) {
            Bukkit.getLogger().warning("No valid ability found for the equipped effect.");
        }
        return effect;
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
        try {
            config.load(dataFile);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + dataFile.getName(), ex);
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + dataFile.getName(), ex);
        }

        loadTrustData();
    }
}