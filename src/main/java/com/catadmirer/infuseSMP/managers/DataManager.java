package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            plugin.getLogger().log(Level.INFO, "Saved {0}", dataFile.getName());
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save {0}.  Make sure the user has write permissions.", dataFile.getName());
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
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create {0}.  Make sure the user has the right permissions.", dataFile.getName());
                return false;
            }
        }

        return true;
    }

    public int getCrafted(EffectMapping effect) {
        return config.getInt("effects-crafted." + effect.getKey(), 0);
    }

    public void setCrafted(EffectMapping effect, int crafted) {
        config.set("effects-crafted." + effect.getKey(), crafted);
    }

    public List<Player> getTrusted(Player truster) {
        return new ArrayList<>(config.getStringList(truster.getUniqueId() + ".trust").stream().map(UUID::fromString).map(Bukkit::getPlayer).toList());
    }

    public void setTrusted(Player truster, List<Player> trusted) {
        config.set(truster.getUniqueId() + ".trust", trusted.stream().map(Player::getUniqueId).map(UUID::toString).toList());

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
            config.set(playerUUID.toString() + "." + slot, effect.getKey());
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

    public boolean hasEffect(OfflinePlayer player, EffectMapping effect) {
        return hasEffect(player, effect, false);
    }

    public boolean hasEffect(OfflinePlayer player, EffectMapping effect, boolean differentiateAugmented) {
        return hasEffect(player, effect, differentiateAugmented, "1") || hasEffect(player, effect, differentiateAugmented, "2");        
    }

    public boolean hasEffect(OfflinePlayer player, EffectMapping effect, String slot) {
        return hasEffect(player, effect, false, slot);
    }

    public boolean hasEffect(OfflinePlayer player, EffectMapping effect, boolean differentiateAugmented, String slot) {
        EffectMapping equippedEffect = getEffect(player.getUniqueId(), slot);

        if (equippedEffect == null) return false;

        if (differentiateAugmented) {
            return effect.equals(equippedEffect);
        }

        return effect.getId() == equippedEffect.getId();
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