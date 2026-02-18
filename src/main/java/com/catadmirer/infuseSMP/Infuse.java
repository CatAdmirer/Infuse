package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.commands.*;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.Apophis;
import com.catadmirer.infuseSMP.extraeffects.Thief;
import com.catadmirer.infuseSMP.managers.*;
import com.catadmirer.infuseSMP.particles.Particles;
import com.catadmirer.infuseSMP.placeholders.InfusePlaceholders;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
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

    private final ApophisManager apophisManager;
    private final DataManager dataManager;
    private final MainConfig mainConfig;
    private final GlobalLoop loop;

    public static final NamespacedKey EFFECT_KEY = new NamespacedKey("infuse", "effect_key");

    public Infuse() {
        this.apophisManager = new ApophisManager(this);
        this.mainConfig = new MainConfig(this);
        this.dataManager = new DataManager(this);
        this.loop = new GlobalLoop(this);
    }

    public void onEnable() {
        // Making sure the plugin hasn't been initialized twice
        if (instance != null) {
            throw new IllegalStateException("Plugin already initialized!");
        }

        // Loading the Infuse plugin instance
        instance = this;

        // Loading the messages
        Messages.load(this);
        
        // Loading the config
        mainConfig.load();

        // Initializing the recipe manager
        new InfuseRecipeManager(this);

        // Loading the data manager
        dataManager.load();

        // Registering infuse commands
        this.registerCommands();

        // Starting the passive effect loop
        loop.start();

        // Registering event listeners for the plugin
        this.registerEvents();

        // Initializing the action bar updater
        new ActionBarUpdater(this).runTaskTimer(this, 0, 20);

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

    public MainConfig getConfigFile() {
        return mainConfig;
    }

    /** Registers the commands for the plugin. */
    private void registerCommands() {
        getCommand("trust").setExecutor(new TrustCommand(dataManager));
        getCommand("untrust").setExecutor(new TrustCommand(dataManager));
        getCommand("recipes").setExecutor(new Recipes(this));
        getCommand("swap").setExecutor(new SwapEffects(this));
        
        getCommand("infuse").setExecutor(new InfuseCommand(this));
        getCommand("infuse").setTabCompleter(new InfuseCommand(this));

        getCommand("ldrain").setExecutor(new DrainCommand(this, apophisManager));
        getCommand("rdrain").setExecutor(new DrainCommand(this, apophisManager));

        getCommand("rspark").setExecutor(new Abilities(this));
        getCommand("lspark").setExecutor(new Abilities(this));

        getCommand("controls").setExecutor((sender, command, label, args) -> {
            // Making sure only players can run the command
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Messages.ERROR_NOT_PLAYER.toComponent());
                return true;
            }

            // Making sure the command has an argument
            if (args.length != 1) {
                player.sendMessage(Messages.CONTROLS_USAGE.toComponent());
                return true;
            }

            // Getting the selected control mode
            String choice = args[0].toLowerCase();

            // Validating the control mode string
            if (!choice.equals("offhand") && !choice.equals("command")) {
                player.sendMessage(Messages.CONTROLS_INVALID_PARAM.toComponent());
                return true;
            }

            // Setting the control mode for the player
            dataManager.setControlMode(player.getUniqueId(), choice);
            player.addAttachment(this, "ability.use", choice.equals("command"));
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

        // Stopping the passive effect loop
        loop.stop();

        // Sending the log message
        this.getLogger().info("Infuse Plugin is disabling...");

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
        // Initializing the particle manager
        new Particles(this).startTask();

        // Initializing the hit tracker
        Bukkit.getPluginManager().registerEvents(new HitTracker(this), this);

        // Registering events for all the listeners
        Bukkit.getPluginManager().registerEvents(new GUI(this), this);
        Bukkit.getPluginManager().registerEvents(new Drop(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSwapHandItemsListener(dataManager), this);
        Bukkit.getPluginManager().registerEvents(new Recipes(this), this);
        Bukkit.getPluginManager().registerEvents(new EquipEffect(this, apophisManager), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new ClearEffect(dataManager), this);

        // Registering events for all the effects
        Bukkit.getPluginManager().registerEvents(new Emerald(this), this);
        Bukkit.getPluginManager().registerEvents(new Ender(this), this);
        Bukkit.getPluginManager().registerEvents(new Feather(this), this);
        Bukkit.getPluginManager().registerEvents(new Fire(this), this);
        Bukkit.getPluginManager().registerEvents(new Frost(dataManager, this), this);
        Bukkit.getPluginManager().registerEvents(new Haste(this), this);
        Bukkit.getPluginManager().registerEvents(new Heart(this), this);
        Bukkit.getPluginManager().registerEvents(new Invisibility(this), this);
        Bukkit.getPluginManager().registerEvents(new Ocean(this), this);
        Bukkit.getPluginManager().registerEvents(new Regen(this), this);
        Bukkit.getPluginManager().registerEvents(new Speed(this), this);
        Bukkit.getPluginManager().registerEvents(new Strength(this), this);
        Bukkit.getPluginManager().registerEvents(new Thunder(this), this);

        // Enabling apophis listeners if the config allows
        if (mainConfig.enableApophis()) {
            getServer().getPluginManager().registerEvents(new Apophis(this), this);
        }

        // Enabling thief listeners if the config allows
        if (mainConfig.enableThief()) {
            getServer().getPluginManager().registerEvents(new Thief(this), this);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        boolean dropHead = mainConfig.playerHeadDrops();

        if (dropHead) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            player.getWorld().dropItem(player.getLocation(), playerHead);
        }
    }

    public String getVersion() {
        return getPluginMeta().getVersion();
    }

    /** Checks the modrinth api for any updates to the plugin. */
    private String getLatestVersion() {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .header("User-Agent", "Infuse/" + getVersion())
            .uri(URI.create("https://api.modrinth.com/v2/project/infusesmp/version"))
            .build();

        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            // Handling http error codes
            if (response.statusCode() != 200) {
                getLogger().log(Level.WARNING, "Recieved error code {0} from api.modrinth.com", response.statusCode());
                return null;
            }

            // Parsing json
            Gson gson = new Gson();
            JsonArray versions = gson.fromJson(response.body(), JsonArray.class);

            // If no versions are returned, defaulting to the current version
            if (versions.size() == 0) {
                getLogger().log(Level.WARNING, "No versions published to modrinth, defaulting to current version");
                return getVersion();
            }

            JsonObject latestVersion = versions.get(0).getAsJsonObject();
            return latestVersion.get("verson_number").getAsString();
        } catch (JsonSyntaxException err) {
            getLogger().log(Level.SEVERE, "Could not parse the json given by modrinth.", err);
        } catch (InterruptedException err) {
            getLogger().log(Level.SEVERE, "Version request was interrupted", err);
        } catch (IOException err) {
            getLogger().log(Level.SEVERE, "Could not get versions from modrinth", err);
        }

        return null;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Telling the player their current control mode
        String controlMode = dataManager.getControlMode(player.getUniqueId());
        if (controlMode == null) controlMode = "Offhand";
        boolean offhandEnabled = controlMode.equalsIgnoreCase("Offhand");
        player.addAttachment(this, "ability.use", !offhandEnabled);

        String msg = Messages.JOIN_ABILITY_NOTIFY.getMessage();
        msg = msg.replace("%control_mode%", controlMode);
        player.sendMessage(Messages.toComponent(msg));

        // Checking for updates but only notifying the player if they are op.
        // TODO: Only run this on startup and save the result for when players join.
        // try {
        //     String currentVersion = getPluginMeta().getVersion();
        //     URL url = new URI("https://api.modrinth.com/v2/project/infusesmp/version").toURL();
        //     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //     connection.setRequestProperty("User-Agent", "Infuse/" + currentVersion);
        //     connection.connect();

        //     if (connection.getResponseCode() != 200) {
        //         player.sendMessage("Could not check for updates: HTTP " + connection.getResponseCode());
        //         return;
        //     }
            
        //     Gson gson = new Gson();
        //     JsonArray versions = gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonArray.class);
        //     if (versions.size() == 0) return;

        //     JsonObject latest = versions.get(0).getAsJsonObject();
        //     String latestVersion = latest.get("version_number").getAsString();

        //     if (!latestVersion.equalsIgnoreCase(currentVersion)) {
        //         String updateMessage = "§d[Infuse] §aA new version (" + latestVersion + ") is available! §7You are on " + currentVersion + " §bhttps://modrinth.com/plugin/infusesmp";
        //         if (player.isOp()) {
        //             player.sendMessage(updateMessage);
        //         }
        //     }

        // } catch (Exception e) {
        //     player.sendMessage("Failed to check for Infuse updates" + e);
        // }
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}