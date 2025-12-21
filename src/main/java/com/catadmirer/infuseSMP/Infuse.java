package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.commands.*;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.managers.*;
import com.catadmirer.infuseSMP.particles.Particles;
import com.catadmirer.infuseSMP.placeholders.InfusePlaceholders;
import com.catadmirer.infuseSMP.util.MessageUtil;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Infuse extends JavaPlugin implements Listener {
    private static Infuse instance;
    private DataManager dataManager;
    private Abilities abilitiesHandler;

    private final Map<UUID, PlayerProfile> originalProfiles = new HashMap<>();
    private final Map<UUID, Integer> activeSkinModifiers = new HashMap<>();

    private ApophisManager apophisCommand;

    private final Map<String, String> effectNames = new HashMap<>();
    private final Map<String, String> nameToKey = new HashMap<>();
    private final Map<String, List<String>> effectLore = new HashMap<>();
    private final Map<String, Object> settings = new HashMap<>();

    public static NamespacedKey EFFECT_KEY = NamespacedKey.fromString("infuse:effect_key");

    public static Infuse getInstance() {
        return instance;
    }

    public void onEnable() {
        if (instance != null) {
            throw new IllegalStateException("Plugin already initialized!");
        }

        // Loading the Infuse plugin instance
        instance = this;

        // Saving the default config.yml
        saveDefaultConfig();

        // Loading the config
        loadConfig();

        // Loading the apophis manager
        new ApophisManager(this, "AphopisPlayers/").getApophisFile();
        apophisCommand = new ApophisManager(this, "AphopisPlayers/");

        // Initializing the recipe manager
        new InfuseRecipeManager(this);

        // Getting the data manager
        this.dataManager = new DataManager(getDataFolder());

        // Getting the abilities handler
        this.abilitiesHandler = new Abilities();

        // Initializing PacketEvents and its listeners
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new Invisibility(this), PacketListenerPriority.HIGHEST);

        // Registering infuse commands
        this.registerCommands();

        // Checking for any updates to the plugin
        checkForUpdate();

        // Registering event listeners for the plugin
        this.registerEvents();

        // Initializing the action bar updater
        new ActionBarUpdater().runTaskTimer(this, 0L, 20L);

        // Registering the PlaceholderAPI listener if the plugin is installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new InfusePlaceholders(this).register();
            getLogger().info("Placeholders Enabled!");
        } else {
            getLogger().warning("PlaceholderAPI is not installed, so custom placeholders won't work.");
        }

        // Logging the success message
        getLogger().info("Infuse Plugin has been enabled!");
    }

    public <T> T getConfig(String key) {
        return (T) settings.get(key);
    }

    public String getEffectName(String key) {
        return effectNames.getOrDefault(key, "§cUnknown Effect");
    }

    public List<String> getEffectLore(String key) {
        return effectLore.getOrDefault(key, Collections.singletonList("§cUnknown Effect"));
    }


    /**
     * Reloading the config and returning the amount of time it takes to reload the config.
     * 
     * @return The amount of time it takes to reload the config.
     */
    public long loadConfig() {
        // Reloading the config file itself
        reloadConfig();

        // Getting the start time
        long start = System.nanoTime();

        // Loading the configs
        FileConfiguration config = getConfig();
        Messages.load(this);

        // Clearing the existing maps
        settings.clear();
        effectNames.clear();
        effectLore.clear();

        // Getting various configs
        settings.put("allow_infinite_effects", config.getBoolean("allow_infinite_effects", false));
        settings.put("ritual_duration", config.getInt("ritual_duration", 600));
        settings.put("ritual_duration_ender", config.getInt("ritual_duration_ender", 3600));
        settings.put("brewing_particles", config.getBoolean("brewing_particles", true));
        settings.put("empty_effect_icon", config.getBoolean("empty_effect_icon", true));
        settings.put("player_head_drops", config.getBoolean("player_head_drops", true));
        settings.put("enable_discord_broadcasts", config.getBoolean("enable_discord_broadcasts", false));
        settings.put("discord_webhook_url", config.getString("discord_webhook_url", ""));
        settings.put("invis_deaths", config.getBoolean("invis_deaths", false));
        settings.put("brewing_gui", config.getBoolean("brewing_gui", true));
        settings.put("join_effects_enabled", config.getBoolean("join_effects_enabled", false));
        settings.put("join_effects", config.getStringList("join_effects"));

        // Loading the craft limits for the effects
        for (String effect : config.getConfigurationSection("craft_limits").getKeys(false)) {
            settings.put("craft_limits." + effect + ".augmented_limit", config.getInt("craft_limits." + effect + ".augmented_limit", 0));
            settings.put("craft_limits." + effect + ".regular_limit", config.getInt("craft_limits." + effect + ".regular_limit", 0));
        }

        // Looping over configs to get cooldowns and durations
        for (String effect : config.getKeys(false)) {
            if (config.isConfigurationSection(effect + ".cooldown")) {
                settings.put(effect + ".cooldown.default", config.getInt(effect + ".cooldown.default", 0));
                settings.put(effect + ".cooldown.augmented", config.getInt(effect + ".cooldown.augmented", 0));
            }

            if (config.isConfigurationSection(effect + ".duration")) {
                settings.put(effect + ".duration.default", config.getInt(effect + ".duration.default", 0));
                settings.put(effect + ".duration.augmented", config.getInt(effect + ".duration.augmented", 0));
            }
        }

        // Getting configs from settings
        settings.put("ocean_pulling.pull.interval", config.getInt("ocean_pulling.pull.interval", 20));
        settings.put("ocean_pulling.pull.radius", config.getDouble("ocean_pulling.pull.radius", 5));
        settings.put("ocean_pulling.pull.strength", config.getDouble("ocean_pulling.pull.strength", 0.3));
        settings.put("speed.dashMultiplier", config.getDouble("speed.dashMultiplier", 20));
        settings.put("speed.playerVelocityMultiplier", config.getDouble("speed.playerVelocityMultiplier", 2));
        settings.put("extra_effects.Apophis", config.getBoolean("extra_effects.Apophis", false));
        settings.put("extra_effects.Thief", config.getBoolean("extra_effects.Thief", false));

        // Getting regular effect names
        effectNames.put("emerald",  Messages.EMERALD_NAME.getMessage());
        effectNames.put("ender",    Messages.ENDER_NAME.getMessage());
        effectNames.put("feather",  Messages.FEATHER_NAME.getMessage());
        effectNames.put("fire",     Messages.FIRE_NAME.getMessage());
        effectNames.put("frost",    Messages.FROST_NAME.getMessage());
        effectNames.put("haste",    Messages.HASTE_NAME.getMessage());
        effectNames.put("heart",    Messages.HEART_NAME.getMessage());
        effectNames.put("invis",    Messages.INVIS_NAME.getMessage());
        effectNames.put("ocean",    Messages.OCEAN_NAME.getMessage());
        effectNames.put("regen",    Messages.REGEN_NAME.getMessage());
        effectNames.put("speed",    Messages.SPEED_NAME.getMessage());
        effectNames.put("strength", Messages.STRENGTH_NAME.getMessage());
        effectNames.put("thunder",  Messages.THUNDER_NAME.getMessage());
        effectNames.put("apophis",  Messages.APOPHIS_NAME.getMessage());
        effectNames.put("thief",    Messages.THIEF_NAME.getMessage());
        
        // Getting augmented effect names
        effectNames.put("aug_emerald",  Messages.AUG_EMERALD_NAME.getMessage());
        effectNames.put("aug_ender",    Messages.AUG_ENDER_NAME.getMessage());
        effectNames.put("aug_feather",  Messages.AUG_FEATHER_NAME.getMessage());
        effectNames.put("aug_fire",     Messages.AUG_FIRE_NAME.getMessage());
        effectNames.put("aug_frost",    Messages.AUG_FROST_NAME.getMessage());
        effectNames.put("aug_haste",    Messages.AUG_HASTE_NAME.getMessage());
        effectNames.put("aug_heart",    Messages.AUG_HEART_NAME.getMessage());
        effectNames.put("aug_invis",    Messages.AUG_INVIS_NAME.getMessage());
        effectNames.put("aug_ocean",    Messages.AUG_OCEAN_NAME.getMessage());
        effectNames.put("aug_regen",    Messages.AUG_REGEN_NAME.getMessage());
        effectNames.put("aug_speed",    Messages.AUG_SPEED_NAME.getMessage());
        effectNames.put("aug_strength", Messages.AUG_STRENGTH_NAME.getMessage());
        effectNames.put("aug_thunder",  Messages.AUG_THUNDER_NAME.getMessage());
        effectNames.put("aug_apophis",  Messages.AUG_APOPHIS_NAME.getMessage());
        effectNames.put("aug_thief",    Messages.AUG_THIEF_NAME.getMessage());

        // Creating the name to key map by inverting the key to name map
        effectNames.forEach((key, name) -> {
            nameToKey.put(MessageUtil.stripAllColors(name), key);
        });

        // Getting regular effect lore
        effectLore.put("emerald",  Messages.EMERALD_LORE.getStringList());
        effectLore.put("ender",    Messages.ENDER_LORE.getStringList());
        effectLore.put("feather",  Messages.FEATHER_LORE.getStringList());
        effectLore.put("fire",     Messages.FIRE_LORE.getStringList());
        effectLore.put("frost",    Messages.FROST_LORE.getStringList());
        effectLore.put("haste",    Messages.HASTE_LORE.getStringList());
        effectLore.put("heart",    Messages.HEART_LORE.getStringList());
        effectLore.put("invis",    Messages.INVIS_LORE.getStringList());
        effectLore.put("ocean",    Messages.OCEAN_LORE.getStringList());
        effectLore.put("regen",    Messages.REGEN_LORE.getStringList());
        effectLore.put("speed",    Messages.SPEED_LORE.getStringList());
        effectLore.put("strength", Messages.STRENGTH_LORE.getStringList());
        effectLore.put("thunder",  Messages.THUNDER_LORE.getStringList());
        effectLore.put("apophis",  Messages.APOPHIS_LORE.getStringList());
        effectLore.put("thief",    Messages.THIEF_LORE.getStringList());
        
        // Getting augmented effect lore
        effectLore.put("aug_emerald",  Messages.AUG_EMERALD_LORE.getStringList());
        effectLore.put("aug_ender",    Messages.AUG_ENDER_LORE.getStringList());
        effectLore.put("aug_feather",  Messages.AUG_FEATHER_LORE.getStringList());
        effectLore.put("aug_fire",     Messages.AUG_FIRE_LORE.getStringList());
        effectLore.put("aug_frost",    Messages.AUG_FROST_LORE.getStringList());
        effectLore.put("aug_haste",    Messages.AUG_HASTE_LORE.getStringList());
        effectLore.put("aug_heart",    Messages.AUG_HEART_LORE.getStringList());
        effectLore.put("aug_invis",    Messages.AUG_INVIS_LORE.getStringList());
        effectLore.put("aug_ocean",    Messages.AUG_OCEAN_LORE.getStringList());
        effectLore.put("aug_regen",    Messages.AUG_REGEN_LORE.getStringList());
        effectLore.put("aug_speed",    Messages.AUG_SPEED_LORE.getStringList());
        effectLore.put("aug_strength", Messages.AUG_STRENGTH_LORE.getStringList());
        effectLore.put("aug_thunder",  Messages.AUG_THUNDER_LORE.getStringList());
        effectLore.put("aug_apophis",  Messages.AUG_APOPHIS_LORE.getStringList());
        effectLore.put("aug_thief",    Messages.AUG_THIEF_LORE.getStringList());
        
        return (System.nanoTime() - start) / 1000000;
    }

    /**
     * Getting an effect key from the name of an item.
     * 
     * @param displayName The name of the item.
     * 
     * @return The key of the effect.
     */
    public String getEffectReversed(String displayName) {
        return nameToKey.get(MessageUtil.stripAllColors(displayName));
    }

    /** Registers the commands for the plugin. */
    private void registerCommands() {
        getCommand("trust").setExecutor(new TrustCommand(dataManager));
        getCommand("untrust").setExecutor(new TrustCommand(dataManager));
        getCommand("recipes").setExecutor(new Recipes(this));
        getCommand("swap").setExecutor(new SwapEffects());
        getCommand("infuse").setExecutor(new InfuseCommand());
        getCommand("infuse").setTabCompleter(new InfuseCommand());
        getCommand("ldrain").setExecutor(new DrainCommand(apophisCommand));
        getCommand("rdrain").setExecutor(new DrainCommand(apophisCommand));
        getCommand("rspark").setExecutor(abilitiesHandler);
        getCommand("lspark").setExecutor(abilitiesHandler);
        getCommand("controls").setExecutor((sender, command, label, args) -> {
            // Making sure only players can run the command
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            // Making sure the command has an argument
            if (args.length != 1) {
                player.sendMessage("§cUsage: /controls <offhand|command>");
                return true;
            }

            // Getting the selected control mode
            String choice = args[0].toLowerCase();

            // Validating the control mode string
            if (!choice.equals("offhand") && !choice.equals("command")) {
                player.sendMessage("§cInvalid option. Use 'Offhand' or 'Command'.");
                return true;
            }

            // Setting the control mode for the player
            dataManager.setControlDefault(player.getUniqueId(), choice);
            player.addAttachment(Infuse.getInstance(), "ability.use", choice.equals("command"));
            return true;
        });
        getCommand("controls").setTabCompleter((sender, command, label, args) -> {
            if (args.length == 1) {
                return Stream.of("command", "offhand").filter(opt -> opt.startsWith(args[0])).toList();
            }

            return List.of();
        });
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        (new BukkitRunnable() {
            public void run() {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
                player.setHealth(20);
            }
        }).runTaskLater(this, 10L);
    }

    public void onDisable() {
        // Resetting the instance
        instance = null;

        // Sending the log message
        this.getLogger().info("Infuse Plugin is disabling...");

        // Disabling packetevents
        PacketEvents.getAPI().terminate();

        // Removing ritual beams
        // TODO: Do this in a better way than removing all ender crystals
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(EnderCrystal.class)) {
                entity.remove();
            }
        }

        // Finalizing the message
        getLogger().info("Infuse Plugin has been disabled!");
    }

    private void registerEvents() {
        // Registering strength listener here for some reason?
        Bukkit.getPluginManager().registerEvents(new Strength(), this);

        // Initializing the particle manager
        new Particles().startTask();

        // Registering events for all the listeners
        Bukkit.getPluginManager().registerEvents(new GUI(), this);
        Bukkit.getPluginManager().registerEvents(new Drop(this), this);
        Bukkit.getPluginManager().registerEvents(new Frost(dataManager, this), this);
        Bukkit.getPluginManager().registerEvents(new Invisibility(this), this);
        Bukkit.getPluginManager().registerEvents(new Heart(this), this);
        Bukkit.getPluginManager().registerEvents(new Recipes(this), this);
        Bukkit.getPluginManager().registerEvents(new Emerald(this), this);
        Bukkit.getPluginManager().registerEvents(new EquipEffect(apophisCommand), this);
        Bukkit.getPluginManager().registerEvents(new Ocean(this, dataManager), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new Regen(this), this);
        Bukkit.getPluginManager().registerEvents(new Feather(this, dataManager), this);
        Bukkit.getPluginManager().registerEvents(new Thunder(), this);
        Bukkit.getPluginManager().registerEvents(new Haste(this), this);
        Bukkit.getPluginManager().registerEvents(new Speed(this), this);
        Bukkit.getPluginManager().registerEvents(new Fire(this), this);
        Bukkit.getPluginManager().registerEvents(new Ender(dataManager, this), this);
        Bukkit.getPluginManager().registerEvents(new ClearEffect(), this);

        // Enabling apophis listeners if the config allows
        if (getConfig().getBoolean("extra_effects.Apophis")) {
            getServer().getPluginManager().registerEvents(new Apophis(this), this);
        }

        // Enabling thief listeners if the config allows
        if (getConfig().getBoolean("extra_effects.Thief")) {
            getServer().getPluginManager().registerEvents(new Thief(dataManager, this), this);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        boolean dropHead = getConfig().getBoolean("player_head_drops", true);

        if (dropHead) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            player.getWorld().dropItem(player.getLocation(), playerHead);
        }
    }


    // TODO: Review this implementation...
    /**
     * Saving the skin of a player.
     * 
     * @param player The player to save the skin of.
     */
    public void saveOriginalSkin(Player player) {
        UUID uuid = player.getUniqueId();
        if (!originalProfiles.containsKey(uuid)) {
            originalProfiles.put(uuid, player.getPlayerProfile());
        }
        activeSkinModifiers.put(uuid, activeSkinModifiers.getOrDefault(uuid, 0) + 1);
    }

    /**
     * Overriding a player's skin without kicking them.
     * 
     * @param player The player to set the skin of.
     */
    public void resetSkinWithoutKick(Player player) {
        UUID uuid = player.getUniqueId();
        int count = activeSkinModifiers.getOrDefault(uuid, 0) - 1;

        if (count <= 0) {
            PlayerProfile originalProfile = originalProfiles.get(uuid);
            if (originalProfile != null) {
                player.setPlayerProfile(originalProfile);
            }
            originalProfiles.remove(uuid);
            activeSkinModifiers.remove(uuid);
        } else {
            activeSkinModifiers.put(uuid, count);
        }
    }

    /** Checks the modrinth api for any updates to the plugin. */
    private void checkForUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                String currentVersion = getPluginMeta().getVersion();
                URL url = new URI("https://api.modrinth.com/v2/project/infusesmp/version").toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Infuse/" + currentVersion);
                connection.connect();

                if (connection.getResponseCode() != 200) {
                    getLogger().warning("Could not check for updates: HTTP " + connection.getResponseCode());
                    return;
                }

                Gson gson = new Gson();
                JsonArray versions = gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonArray.class);
                if (versions.size() == 0) return;

                JsonObject latest = versions.get(0).getAsJsonObject();
                String latestVersion = latest.get("version_number").getAsString();

                if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                    getLogger().info("A new version (" + latestVersion + ") is available! You are on " + currentVersion + " " + "https://modrinth.com/plugin/infusesmp");
                }

            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to check for Infuse updates", e);
            }
        });
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Telling the player their current control mode
        String controlMode = dataManager.getControlDefault(player.getUniqueId());
        if (controlMode == null) controlMode = "Offhand";
        boolean offhandEnabled = controlMode.equalsIgnoreCase("Offhand");
        player.addAttachment(Infuse.getInstance(), "ability.use", !offhandEnabled);
        player.sendMessage("§7Your ability mode is set to: " + controlMode);

        // Checking for updates but only notifying the player if they are op.
        // TODO: Only run this on startup and save the result for when players join.
        try {
            String currentVersion = getPluginMeta().getVersion();
            URL url = new URI("https://api.modrinth.com/v2/project/infusesmp/version").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Infuse/" + currentVersion);
            connection.connect();

            if (connection.getResponseCode() != 200) {
                player.sendMessage("Could not check for updates: HTTP " + connection.getResponseCode());
                return;
            }
            
            Gson gson = new Gson();
            JsonArray versions = gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonArray.class);
            if (versions.size() == 0) return;

            JsonObject latest = versions.get(0).getAsJsonObject();
            String latestVersion = latest.get("version_number").getAsString();

            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                String updateMessage = "§d[Infuse] §aA new version (" + latestVersion + ") is available! §7You are on " + currentVersion + " §bhttps://modrinth.com/plugin/infusesmp";
                if (player.isOp()) {
                    player.sendMessage(updateMessage);
                }
            }

        } catch (Exception e) {
            player.sendMessage("Failed to check for Infuse updates" + e);
        }
    }

    public DataManager getEffectManager() {
        return dataManager;
    }
}