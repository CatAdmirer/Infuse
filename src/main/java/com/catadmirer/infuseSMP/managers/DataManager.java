package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
        if (!plugin.isEnabled()) {
            Infuse.LOGGER.error("Infuse not loaded, cannot load {}.", dataFile.getName());
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
            Infuse.LOGGER.info("Successfully loaded {}", dataFile.getName());
            return true;
        } catch (InvalidConfigurationException err) {
            Infuse.LOGGER.warn("{} contains an invalid YAML configuration.  Verify the contents of the file.", dataFile.getName());
        } catch (IOException err) {
            Infuse.LOGGER.error("Could not find {}.  Check that it exists.", dataFile.getName());
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
        if (!plugin.isEnabled()) {
            Infuse.LOGGER.error("Infuse not loaded, cannot save the {}.", dataFile.getName());
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
            Infuse.LOGGER.info("Saved {}", dataFile.getName());
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.warn("Could not save {}.  Make sure the user has write permissions.", dataFile.getName());
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
        if (!plugin.isEnabled()) {
            Infuse.LOGGER.error("Infuse not loaded, cannot create default {}.", dataFile.getName());
            return false;
        }

        // Creating the file if it doesn't exist.
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                Infuse.LOGGER.error("Could not create {}.  Make sure the user has the right permissions.", dataFile.getName());
                return false;
            }
        }

        return true;
    }

    public int getExistingCount(EffectMapping effect) {
        return config.getInt("existing-effects." + effect.getKey(), 0);
    }

    public void setExistingCount(EffectMapping effect, int crafted) {
        config.set("existing-effects." + effect.getKey(), crafted);
        
        save();
    }

    public List<OfflinePlayer> getTrusted(OfflinePlayer truster) {
        return new ArrayList<>(config.getStringList(truster.getUniqueId() + ".trust").stream().map(UUID::fromString).map(Bukkit::getOfflinePlayer).toList());
    }

    public void setTrusted(OfflinePlayer truster, List<OfflinePlayer> trusted) {
        config.set(truster.getUniqueId() + ".trust", trusted.stream().map(OfflinePlayer::getUniqueId).map(UUID::toString).toList());
        save();
    }

    public void addTrust(OfflinePlayer caster, OfflinePlayer toTrust) {
        List<OfflinePlayer> trustedPlayers = getTrusted(caster);
        trustedPlayers.add(toTrust);

        setTrusted(caster, trustedPlayers);
    }

    public void removeTrust(OfflinePlayer caster, OfflinePlayer trusted) {
        List<OfflinePlayer> trustedSet = getTrusted(caster);
        trustedSet.remove(trusted);

        setTrusted(caster, trustedSet);
    }

    public boolean isTrusted(OfflinePlayer caster, OfflinePlayer trusted) {
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
            Infuse.LOGGER.warn("No valid ability found for the equipped effect.");
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

    public void applyUpdates() {
        try {
            Scanner scanner = new Scanner(dataFile);
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                // Replacing old configs
                if (line.startsWith("effects-crafted")) {
                    line = line.replace("effects-crafted", "existing-effects");
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            scanner.close();

            // Emptying the string buffer back into the file
            FileOutputStream fileOut = new FileOutputStream(dataFile);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();
        } catch (IOException err) {
            
        }
    }
}