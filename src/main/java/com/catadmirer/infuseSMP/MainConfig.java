package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

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

    public boolean ritualBeacon() {
        return config.getBoolean("ritual_beacon");
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
        return config.getStringList("join_effects").stream().map(EffectMapping::fromEffectKey).filter(Objects::nonNull).toList();
    }

    public boolean enableApophis() {
        return config.getBoolean("extra_effects.Apophis");
    }

    public boolean regularBroadcast() {
        return config.getBoolean("regular_effect_broadcast");
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
     */
    public int getCraftLimit(EffectMapping effect) {
        List<Integer> craftLimits = config.getIntegerList("craft_limits." + effect.regular().getKey());

        if (craftLimits.size() != 2) {
            plugin.getLogger().log(Level.SEVERE, "Craft limits are required to be a list of 2 integers.  Found {0} entries for effect {1}", new Object[] {craftLimits.size(), effect.getKey()});
            plugin.getLogger().log(Level.SEVERE, "Returning default limits");

            return effect.isAugmented() ? 1 : 3;
        }

        return craftLimits.get(effect.isAugmented() ? 0 : 1);
    }

    public double emeraldLockDurationSeconds() {
        return config.getDouble("emerald.lock_duration_seconds", 10);
    }

    public boolean invisHideKills() {
        return config.getBoolean("invis.hide_kills");
    }

    public boolean invisHideDeaths() {
        return config.getBoolean("invis.hide_deaths");
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

    public int hitCounterDecaySeconds() {
        return config.getInt("hit_counter_decay_seconds");
    }

    public float emeraldXPPerHit() {
        return (float) config.getDouble("emerald.xp_stolen_per_hit");
    }

    public float emeraldXPPercent() {
        return Math.clamp((float) config.getDouble("emerald.xp_stolen_percent"), 0, 1);
    }

    public void applyUpdates() {
        if (!config.contains("invis_deaths")) config.set("invis_deaths", null);
        if (!config.contains("invis.hide_kills")) config.set("invis.hide_kills", false);
        if (!config.contains("invis.hide_deaths")) config.set("invis.hide_deaths", false);
        if (!config.contains("haste.enchantment.looting_level")) config.set("haste.enchantment.looting_level", 5);
        if (!config.contains("haste.enchantment.fortune_level")) config.set("haste.enchantment.fortune_level", 5);
        if (!config.contains("haste.enchantment.efficiency_level")) config.set("haste.enchantment.efficiency_level", 10);
        if (!config.contains("haste.enchantment.unbreaking_level")) config.set("haste.enchantment.unbreaking_level", 5);
        if (!config.contains("hit_counter_decay_seconds")) config.set("hit_counter_decay_seconds", 15);
        if (!config.contains("emerald.xp_stolen_per_hit")) config.set("emerald.xp_stolen_per_hit", 15);
        if (!config.contains("emerald.xp_stolen_percent")) config.set("emerald.xp_stolen_percent", 1);

        save();
    }

    public int emeraldLootingLevel() {
        return config.getInt("emerald.enchantment.looting_level");
    }

    public int hasteFortuneLevel() {
        return config.getInt("haste.enchantment.fortune_level");
    }

    public int hasteEfficiencyLevel() {
        return config.getInt("haste.enchantment.efficiency_level");
    }

    public int hasteUnbreakingLevel() {
        return config.getInt("haste.enchantment.unbreaking_level");
    }
}