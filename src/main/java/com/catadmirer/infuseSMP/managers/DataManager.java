package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DataManager {
    private final Infuse plugin;
    private final File dataFile;
    private final YamlConfiguration config;

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

    public List<Player> getTrusted(Player truster) {
        return config.getStringList(truster.getUniqueId() + ".trust").stream().map(UUID::fromString).map(Bukkit::getPlayer).toList();
    }

    public void setTrusted(Player truster, List<Player> trusted) {
        config.set(truster.getUniqueId() + ".trust", trusted.stream().map(Player::getUniqueId).toList());

        save();
    }

    public void addTrust(Player caster, Player toTrust) {
        List<Player> trustedPlayers = getTrusted(caster);
        trustedPlayers.add(toTrust);

        setTrusted(caster, trustedPlayers);
    }

    public void removeTrust(Player caster, Player trusted) {
        List<Player> trustedSet = getTrusted(caster);
        trustedSet.remove(trusted);

        setTrusted(caster, trustedSet);
    }

    public boolean isTrusted(Player caster, Player trusted) {
        if (caster == null || trusted == null) return false;
        if (caster.getUniqueId().equals(trusted.getUniqueId())) return true;

        return getTrusted(caster).contains(trusted);
    }

    public void setEffect(UUID playerUUID, String slot, @Nullable EffectMapping effect) {
        if (effect == null) {
            config.set(playerUUID.toString() + "." + slot, null);
        } else {
            config.set(playerUUID.toString() + "." + slot, effect.name());
        }
        save();
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
        save();
    }

    public void setControlMode(UUID playerUUID, String defaultMode) {
        config.set(playerUUID.toString() + ".controls", defaultMode);
        save();
    }

    public String getControlMode(UUID playerUUID) {
        return config.getString(playerUUID.toString() + ".controls", "offhand");
    }
}