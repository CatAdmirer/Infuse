package com.catadmirer.infuseSMP.playerdata.databases;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.playerdata.DataManager;

public class YamlDataManager implements DataManager {
    private final Infuse plugin;
    private final File dataFile;
    private final YamlConfiguration config;

    public YamlDataManager(Infuse plugin) {   
        this.plugin = plugin;     
        this.dataFile = new File(plugin.getDataFolder(), "data/playerdata.yml");
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Reloads the player data.
     *
     * @return True if the data was loaded successfully, false otherwise.
     */
    @Override
    public boolean load() {
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "{0} not loaded, cannot load {1}.", new String[]{plugin.getName(), dataFile.getName()});
            return false;
        }

        // Creating the file if it doesn't exist.
        createFile();

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
     * Writes the player data to disk.
     * 
     * @return Whether or not the data was successfully written.
     */
    public boolean save() {
        // Getting a plugin instance to use
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "{0} not loaded, cannot save the {1}.", new String[]{plugin.getName(), dataFile.getName()});
            return false;
        }

        // Creating the file if it doesn't exist.
        createFile();

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
     * Creating the data file. If it doesn't exist, it just makes an empty file.
     * 
     * @return True if the file was created successfully, false otherwise.
     */
    public boolean createFile() {
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

    /**
     * Gets a list of the players that the truster trusts.
     * 
     * @param truster
     * 
     * @return The list of players trusted by the truster.
     */
    @Override
    @NotNull
    public List<OfflinePlayer> getTrusted(@NotNull OfflinePlayer truster) {
        return config.getStringList(truster.getUniqueId() + ".trust").stream().map(UUID::fromString).map(Bukkit::getOfflinePlayer).toList();
    }

    /**
     * Sets the players that the truster trusts.
     * 
     * @param truster The player to modify
     * @param trusted The list of players the truster now trusts
     */
    @Override
    public void setTrusted(@NotNull OfflinePlayer truster, @NotNull List<OfflinePlayer> trusted) {
        config.set(truster.getUniqueId() + ".trust", trusted.stream().map(OfflinePlayer::getUniqueId).toList());

        save();
    }

    /**
     * Adds a player to the list of trusted people.
     * 
     * @param truster The person whose trusted list to modify.
     * @param toTrust The person the truster now trusts.
     */
    @Override
    public void addTrust(@NotNull OfflinePlayer caster, @NotNull OfflinePlayer toTrust) {
        List<OfflinePlayer> trustedPlayers = getTrusted(caster);
        trustedPlayers.add(toTrust);

        setTrusted(caster, trustedPlayers);
    }

    /**
     * Removes a player from another player's list of trusted people.
     * 
     * @param truster The player whose trusted list to modify.
     * @param toRemove The person to remove from the truster's trust.
     */
    @Override
    public void removeTrust(@NotNull OfflinePlayer caster, @NotNull OfflinePlayer trusted) {
        List<OfflinePlayer> trustedSet = getTrusted(caster);
        trustedSet.remove(trusted);

        setTrusted(caster, trustedSet);
    }

    /**
     * Checks if a player is trusted by another player.
     * 
     * @param truster The player whose trusted list to check.
     * @param toCheck The player to check if truster trusts.
     * 
     * @return True if the truster trusts the toCheck player, false otherwise
     */
    @Override
    public boolean isTrusted(@NotNull OfflinePlayer caster, @NotNull OfflinePlayer trusted) {
        if (caster == null || trusted == null) return false;
        if (caster.getUniqueId().equals(trusted.getUniqueId())) return true;

        return getTrusted(caster).contains(trusted);
    }

    /**
     * Sets the infuse effect in a specific slot for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param slot The slot to equip the effect in.
     * @param effect The {@link EffectMapping} for the infuse effect.
     */
    @Override
    public void setEffect(@NotNull UUID playerUUID, @NotNull String slot, @NotNull EffectMapping effect) {
        if (effect == null) {
            config.set(playerUUID.toString() + "." + slot, null);
        } else {
            config.set(playerUUID.toString() + "." + slot, effect.getKey());
        }
        save();
    }

    /**
     * Gets the infuse effect a player has in a specific slot.
     * 
     * @param playerUUID The UUID of the player.
     * @param slot The slot to get the effect from.
     * 
     * @return null if there is not an effect equipped there or if the EffectMapping could not be deserialized.  Otherwise, it returns the deserialized EffectMapping.
     */
    @Override
    @Nullable
    public EffectMapping getEffect(@NotNull UUID playerUUID, @NotNull String slot) {
        String effectKey = config.getString(playerUUID.toString() + "." + slot, null);
        EffectMapping effect = EffectMapping.fromEffectKey(effectKey);
        if (effectKey != null && effect == null) {
            Bukkit.getLogger().warning("No valid ability found for the equipped effect.");
        }

        return effect;
    }

    public boolean hasEffect(OfflinePlayer player, EffectMapping effect, boolean differentiateAugmented, String slot) {
        EffectMapping equippedEffect = getEffect(player.getUniqueId(), slot);

        if (equippedEffect == null) return false;

        if (differentiateAugmented) {
            return effect.equals(equippedEffect);
        }

        return effect.getId() == equippedEffect.getId();
    }

    /**
     * Removes an infuse effect from a specific slot for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param slot The slot to remove an effect from.
     */
    @Override
    public void removeEffect(UUID playerUUID, String slot) {
        config.set(playerUUID.toString() + "." + slot, null);
        save();
    }

    /**
     * Sets the control mode for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param defaultMode The new control mode to use.
     */
    @Override
    public void setControlMode(@NotNull UUID playerUUID, @NotNull String defaultMode) {
        config.set(playerUUID.toString() + ".controls", defaultMode);
        save();
    }

    /**
     * Gets the control mode of a player.
     * 
     * @param playerUUID The UUID of the player.
     * 
     * @return Either "command" or "offhand".  Defaults to "offhand"
     */
    @Override
    @NotNull
    public String getControlMode(@NotNull UUID playerUUID) {
        return config.getString(playerUUID.toString() + ".controls", "offhand");
    }
}