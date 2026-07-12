package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainConfig {
    public final File file;
    public final FileConfiguration config;
    public final Infuse plugin;

    public MainConfig(Infuse plugin) {
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
            Infuse.LOGGER.error("{} not loaded, cannot load {}.", plugin.getName(), file.getName());
            return false;
        }

        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), true);
        }

        // Loading the config
        try {
            config.load(file);
            Infuse.LOGGER.info("Successfully loaded config.yml");
            return true;
        } catch (InvalidConfigurationException e) {
            Infuse.LOGGER.warn("{} contains an invalid YAML configuration.  Verify the contents of the file.", file.getName());
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not find {}.  Check that it exists.", file.getName());
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
            Infuse.LOGGER.error("{} not loaded, cannot save {}.", plugin.getName(), file.getName());
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
            Infuse.LOGGER.info("Saved {}", file.getName());
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.warn("Could not save {}.  Make sure the user has write permissions.", file.getName());
        }

        return false;
    }

    public List<String> getBlacklistedWorlds(String effect) {
        return config.getStringList(effect + ".blacklisted-worlds").stream().filter(Objects::nonNull).toList();
    }

    public String lang() {
        return config.getString("lang", "en_US");
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

    public List<InfuseEffect> joinEffects() {
        return config.getStringList("join_effects").stream().map(InfuseEffect::fromString).filter(Objects::nonNull).toList();
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
     * @return The number of effects that can be crafted of the specified {@link InfuseEffect}.
     */
    public int getCraftLimit(InfuseEffect effect) {
        List<Integer> craftLimits = config.getIntegerList("craft_limits." + effect.getKey());

        if (craftLimits.size() != 2) {
            Infuse.LOGGER.error("Craft limits are required to be a list of 2 integers.  Found {} entries for effect {}", craftLimits.size(), effect.getKey());
            Infuse.LOGGER.error("Returning default limits");

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

    public long cooldown(InfuseEffect effect) {
        return config.getLong(effect.getKey() + ".cooldown." + (effect.isAugmented() ? "augmented" : "default"));
    }

    public long duration(InfuseEffect effect) {
        return config.getLong(effect.getKey() + ".duration." + (effect.isAugmented() ? "augmented" : "default"));
    }

    public int speedDashMultiplier() {
        return config.getInt("speed.dashMultiplier");
    }

    public int speedPlayerVelocityMultiplier() {
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

    public int emeraldExpPerHit() {
        return config.getInt("emerald.xp_stolen_per_hit");
    }

    public float emeraldExpPercent() {
        return Math.clamp((float) config.getDouble("emerald.xp_stolen_percent"), 0, 1);
    }

    public float emeraldPercentExpToShare() {
        return Math.clamp((float) config.getDouble("emerald.percent_xp_to_share"), 0, 1);
    }

    public int apophisExpPerHit() {
        return config.getInt("apophis.xp_stolen_per_hit");
    }

    public float apophisExpPercent() {
        return Math.clamp((float) config.getDouble("apophis.xp_stolen_percent"), 0, 1);
    }

    public float apophisPercentExpToShare() {
        return Math.clamp((float) config.getDouble("apophis.percent_xp_to_share"), 0, 1);
    }

    public double apophisLockDurationSeconds() {
        return config.getDouble("apophis.lock_duration_seconds", 10);
    }

    public int apophisLootingLevel() {
        return config.getInt("apophis.enchantment.looting_level");
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

    public double emeraldMultiplierStandard() {
        return config.getDouble("emerald.multiplier-xp.standard");
    }

    public double emeraldMultiplierUseEffect() {
        return config.getDouble("emerald.multiplier-xp.use-effect");
    }

    public double enderPassiveRadius() {
        return config.getDouble("ender.passive.radius");
    }

    public int enderSparkMaxDistance() {
        return config.getInt("ender.spark.max-distance");
    }

    public double featherLandRadius() {
        return config.getDouble("feather.land.radius");
    }

    public double featherLandDamage() {
        return config.getDouble("feather.land.damage");
    }

    public double firePassiveWalkSpeed() {
        return config.getDouble("fire.passive.walk-speed");
    }

    public double fireSparkRadius() {
        return config.getDouble("fire.spark.radius");
    }

    public double fireSparkExplosionRadius() {
        return config.getDouble("fire.spark.explosion-radius");
    }

    public int frostPassiveSnowChangingRadius() {
        return config.getInt("frost.passive.snow-changing-radius");
    }

    public double frostPassiveWalkSpeed() {
        return config.getDouble("frost.passive.walk-speed");
    }

    public double frostSparkRadius() {
        return config.getDouble("frost.spark.radius");
    }

    public int oceanPassiveDrownStrength() {
        return config.getInt("ocean.passive.drown-strength");
    }

    public int oceanPassiveDrownDamage() {
        return config.getInt("ocean.passive.drown-damage");
    }

    public int oceanSparkDrownStrength() {
        return config.getInt("ocean.spark.drown-strength");
    }

    public int oceanSparkDrownDamage() {
        return config.getInt("ocean.spark.drown-damage");
    }

    public double regenSparkHealTrustedRadius() {
        return config.getDouble("regen.spark.heal-trusted-radius");
    }

    public double thunderSparkBaseRadius() {
        return config.getDouble("thunder.spark.base-radius");
    }

    public double thunderSparkPerPlayerBoostRadius() {
        return config.getDouble("thunder.spark.per-player-boost-radius");
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
        if (!config.contains("emerald.percent_xp_to_share")) config.set("emerald.percent_xp_to_share", 0.5);

        if (!config.contains("apophis.percent_xp_to_share")) config.set("apophis.percent_xp_to_share", 0.5);
        if (!config.contains("apophis.xp_stolen_per_hit")) config.set("apophis.xp_stolen_per_hit", 15);
        if (!config.contains("apophis.xp_stolen_percent")) config.set("apophis.xp_stolen_percent", 1);
        if (!config.contains("apophis.enchantment.looting_level")) config.set("apophis.enchantment.looting_level", 5);
        if (!config.contains("apophis.lock_duration_seconds")) config.set("apophis.lock_duration_seconds", 10);

        if (!config.contains("emerald.multiplier-xp.standard")) config.set("emerald.multiplier.standard", 2);
        if (!config.contains("emerald.multiplier-xp.use-effect")) config.set("emerald.multiplier.use-effect", 4);

        if (!config.contains("ender.passive.radius")) config.set("ender.passive.radius", 10);
        if (!config.contains("ender.spark.max-distance")) config.set("ender.spark.max-distance", 15);

        if (!config.contains("feather.land.radius")) config.set("feather.land.radius", 4);
        if (!config.contains("feather.land.damage")) config.set("feather.land.damage", 8);

        if (!config.contains("fire.passive.walk-speed")) config.set("fire.passive.walk-speed", 0.6);
        if (!config.contains("fire.spark.radius")) config.set("fire.spark.radius", 5);
        if (!config.contains("fire.spark.explosion-radius")) config.set("fire.spark.explosion-radius", 5);

        if (!config.contains("frost.passive.snow-changing-radius")) config.set("frost.passive.snow-changing-radius", 3);
        if (!config.contains("frost.passive.walk-speed")) config.set("frost.passive.walk-speed", 0.6);
        if (!config.contains("frost.spark.radius")) config.set("frost.spark.radius", 5);

        if (!config.contains("ocean.passive.drown-strength")) config.set("ocean.passive.drown-strength", 5);
        if (!config.contains("ocean.passive.drown-damage")) config.set("ocean.passive.drown-damage", 1);
        if (!config.contains("ocean.spark.drown-strength")) config.set("ocean.spark.drown-strength", 20);
        if (!config.contains("ocean.spark.drown-damage")) config.set("ocean.spark.drown-damage", 2);

        if (!config.contains("regen.spark.heal-trusted-radius")) config.set("regen.spark.heal-trusted-radius", 5);

        if (!config.contains("thunder.spark.base-radius")) config.set("thunder.spark.base-radius", 10);
        if (!config.contains("thunder.spark.per-player-boost-radius")) config.set("thunder.spark.per-player-boost-radius", 0.3);

        final List<String> blacklisted_worlds = new ArrayList<>();
        blacklisted_worlds.add("Example World");
        InfuseEffect.getRegisteredEffects().values().forEach(effect -> {
            if (!config.contains(effect.getKey() + ".blacklisted-worlds")) config.set(effect.getKey() + ".blacklisted-worlds", blacklisted_worlds);
        });

        save();
    }
}
