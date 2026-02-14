package com.catadmirer.infuseSMP;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import javax.naming.ConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.catadmirer.infuseSMP.managers.EffectMapping;

public class MainConfig {
    public final File file;
    public final FileConfiguration config;
    public final Plugin plugin;

    public MainConfig(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully.
     */
    public boolean load() {
        // Not doing anything if the plugin isn't enabled
        if (!plugin.isEnabled()) {
            Bukkit.getLogger().log(Level.SEVERE, "{0} not loaded, cannot load {1}.", new String[]{plugin.getName(), file.getName()});
            return false;
        }

        // load config

        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            if (!file.exists()) plugin.saveResource("config.yml", false);
            config.load(file);
            plugin.getLogger().info("Successfully loaded config.yml");
            return true;

        } catch (InvalidConfigurationException e) {
            plugin.getLogger().warning(file.getName() + "is broken :wilted_rose:");
        } catch (IOException e) {
            plugin.getLogger().severe("uh broken as well :wilted_rose: " + file.getName());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Writes the config to the file.
     * 
     * @return Whether or not the config was successfully written.
     */
    public boolean save() {
        // Not doing anything if the plugin isn't enabled
        if (!plugin.isEnabled()) {
            Bukkit.getLogger().log(Level.SEVERE, "{0} not loaded, cannot save {1}.", new String[]{plugin.getName(), file.getName()});
            return false;
        }

        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        // Saving the config
        try {
            config.save(file);
            plugin.getLogger().log(Level.INFO, "Saved {0}", file.getName());
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save {0}.  Make sure the user has write permissions.", file.getName());
        }

        return false;
    }

    public boolean allowInfiniteEffects() {
        return config.getBoolean("allow_infinite_effects");
    }

    public int ritualDuration() {
        return config.getInt("ritual_duration");
    }

    public int ritualDurationEnder() {
        return config.getInt("ritual_duration_ender");
    }

    public boolean brewingParticles() {
        return config.getBoolean("brewing_particles");
    }

    public boolean emptyEffectIcon() {
        return config.getBoolean("empty_effect_icon");
    }

    public boolean playerHeadDrops() {
        return config.getBoolean("player_head_drops");
    }

    public boolean enableDiscordBroadcasts() {
        return config.getBoolean("enable_discord_broadcasts");
    }

    public String discordWebhookUrl() {
        return config.getString("discord_webhook_url");
    }

    public boolean invisDeaths() {
        return config.getBoolean("invis_deaths");
    }

    public boolean brewingGui() {
        return config.getBoolean("brewing_gui");
    }

    public String effectDrops() {
        return config.getString("effect_drops");
    }

    public boolean joinEffectsEnabled() {
        return config.getBoolean("join_effects_enabled");
    }

    public List<EffectMapping> joinEffects() {
        return config.getStringList("join_effects").stream().map(EffectMapping::fromEffectKey).filter(Objects::isNull).toList();
    }

    public boolean enableApophis() {
        return config.getBoolean("extra_effects.Apophis");
    }

    public boolean enableThief() {
        return config.getBoolean("extra_effects.Thief");
    }

    /**
     * Gets the amount of each effect that can be crafted
     * 
     * @param effect The effect to check
     * 
     * @return The number of effects that can be crafted of the specified {@link EffectMapping}.
     * 
     * @throws ConfigurationException Thrown when the craft limits cannot be found for the provided effect.
     */
    public int getCraftLimit(EffectMapping effect) throws ConfigurationException {
        List<Integer> craftLimits = config.getIntegerList("craft-limits." + effect.getKey());

        if (craftLimits.size() != 2) {
            throw new ConfigurationException("Craft limits are required to be a list of 2 integers.  Found " + craftLimits.size() + " for effect " + effect.getKey());
        }

        return craftLimits.get(effect.isAugmented() ? 0 : 1);
    }

    public long cooldown(EffectMapping effect) {
        return config.getLong(effect.regular().getKey() + ".cooldown." + (effect.isAugmented() ? "augmented" : "default"));
    }

    public long duration(EffectMapping effect) {
        return config.getLong(effect.regular().getKey() + ".duration." + (effect.isAugmented() ? "augmented" : "default"));
    }

    public int speedDashMultiplier() {
        return config.getInt("speed.dashMultiplier");
    }

    public double speedPlayerVelocityMultiplier() {
        return config.getInt("speed.playerVelocityMultiplier");
    }

    public int oceanPullInterval() {
        return config.getInt("ocean_pulling.pull.interval");
    }

    public int oceanPullRadius() {
        return config.getInt("ocean_pulling.pull.radius");
    }

    public double oceanPullStrength() {
        return config.getDouble("ocean_pulling.pull.strength");
    }
}