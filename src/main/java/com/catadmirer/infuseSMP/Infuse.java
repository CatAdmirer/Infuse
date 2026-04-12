package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.commands.*;
import com.catadmirer.infuseSMP.effects.*;
import com.catadmirer.infuseSMP.extraeffects.*;
import com.catadmirer.infuseSMP.managers.*;
import com.catadmirer.infuseSMP.placeholders.InfusePlaceholders;
import com.catadmirer.infuseSMP.util.ItemUtil;
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
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Infuse extends JavaPlugin implements Listener {
    private static Infuse instance;

    public static final Logger LOGGER = LoggerFactory.getLogger("Infuse");
    public static final NamespacedKey EFFECT_KEY = new NamespacedKey("infuse", "effect_key");

    private final DataManager dataManager;
    private final MainConfig mainConfig;
    private final MessageConfig messageConfig;
    private final GlobalLoop loop;
    private final RecipeManager recipeManager;
    private final ParticleManager particleManager;

    public Infuse() {
        this.mainConfig = new MainConfig();
        this.messageConfig = new MessageConfig();
        this.dataManager = new DataManager();
        this.loop = new GlobalLoop();
        this.recipeManager = new RecipeManager();
        this.particleManager = new ParticleManager();
    }

    public void onEnable() {
        // Making sure the plugin hasn't been initialized twice
        if (instance != null && instance.isEnabled()) {
            throw new IllegalStateException("Infuse already enabled!");
        }

        // Loading the Infuse plugin instance
        instance = this;

        // Loading configs
        messageConfig.load();
        mainConfig.load();
        dataManager.load();

        // Applying config updates
        messageConfig.applyUpdates();
        mainConfig.applyUpdates();
        dataManager.applyUpdates();

        // Registering infuse commands
        this.registerCommands();

        // Starting the global loop
        loop.start();

        // Registering event listeners for the plugin
        this.registerEvents();

        // Registering the infuse recipes
        recipeManager.registerRecipes();

        // Initializing the action bar updater
        new ActionBarUpdater().runTaskTimer(this, 0, 20);

        // Registering the PlaceholderAPI listener if the plugin is installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new InfusePlaceholders().register();
            LOGGER.info("Placeholders Enabled!");
        } else {
            LOGGER.warn("PlaceholderAPI is not installed, so custom placeholders won't work.");
        }

        // Logging the success message
        LOGGER.info("Infuse Plugin has been enabled!");
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /** Registers the commands for the plugin. */
    private void registerCommands() {
        getCommand("trust").setExecutor(new TrustCommand());
        getCommand("untrust").setExecutor(new TrustCommand());
        getCommand("recipes").setExecutor(new Recipes());
        getCommand("swap").setExecutor(new SwapEffects());
        
        getCommand("infuse").setExecutor(new InfuseCommand());
        getCommand("infuse").setTabCompleter(new InfuseCommand());

        getCommand("ldrain").setExecutor(new DrainCommand());
        getCommand("rdrain").setExecutor(new DrainCommand());

        getCommand("rspark").setExecutor(new Abilities());
        getCommand("lspark").setExecutor(new Abilities());

        getCommand("draw").setExecutor(new Draw());

        getCommand("controls").setExecutor((sender, command, label, args) -> {
            // Making sure only players can run the command
            if (!(sender instanceof Player player)) {
                sender.sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                return true;
            }

            // Making sure the command has an argument
            if (args.length != 1) {
                player.sendMessage(new Message(MessageType.CONTROLS_USAGE).toComponent());
                return true;
            }

            // Getting the selected control mode
            String choice = args[0].toLowerCase();

            // Validating the control mode string
            if (!choice.equals("offhand") && !choice.equals("command")) {
                player.sendMessage(new Message(MessageType.CONTROLS_INVALID_PARAM).toComponent());
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

    public void onDisable() {
        // Stopping the passive effect loop
        loop.stop();

        // Sending the log message
        LOGGER.info("Infuse Plugin is disabling...");

        // Removing ritual beams
        EffectCraftManager.removeBeam();

        // Finalizing the message
        LOGGER.info("Infuse Plugin has been disabled!");
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    private void registerEvents() {
        // Initializing the hit tracker
        Bukkit.getPluginManager().registerEvents(new HitTracker(), this);

        // Registering events for all the listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new GUI(), this);
        Bukkit.getPluginManager().registerEvents(new Drop(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSwapHandItemsListener(), this);
        Bukkit.getPluginManager().registerEvents(new Recipes(), this);
        Bukkit.getPluginManager().registerEvents(new EquipEffect(), this);
        Bukkit.getPluginManager().registerEvents(new ClearEffect(dataManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemUtil(), this);
        Bukkit.getPluginManager().registerEvents(new EffectCraftManager(), this);

        // Registering events for all the effects
        Bukkit.getPluginManager().registerEvents(new Emerald(), this);
        Bukkit.getPluginManager().registerEvents(new Ender(), this);
        Bukkit.getPluginManager().registerEvents(new Feather(), this);
        Bukkit.getPluginManager().registerEvents(new Fire(), this);
        Bukkit.getPluginManager().registerEvents(new Frost(), this);
        Bukkit.getPluginManager().registerEvents(new Haste(), this);
        Bukkit.getPluginManager().registerEvents(new Heart(), this);
        Bukkit.getPluginManager().registerEvents(new Invis(), this);
        Bukkit.getPluginManager().registerEvents(new Ocean(), this);
        Bukkit.getPluginManager().registerEvents(new Regen(), this);
        Bukkit.getPluginManager().registerEvents(new Speed(), this);
        Bukkit.getPluginManager().registerEvents(new Strength(), this);
        Bukkit.getPluginManager().registerEvents(new Thunder(), this);

        // Enabling apophis listeners if the config allows
        if (mainConfig.enableApophis()) {
            getServer().getPluginManager().registerEvents(new Apophis(), this);
        }

        // Enabling thief listeners if the config allows
        if (mainConfig.enableThief()) {
            getServer().getPluginManager().registerEvents(new Thief(), this);
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
                LOGGER.warn("Recieved error code {} from api.modrinth.com", response.statusCode());
                return null;
            }

            // Parsing json
            Gson gson = new Gson();
            JsonArray versions = gson.fromJson(response.body(), JsonArray.class);

            // If no versions are returned, defaulting to the current version
            if (versions.size() == 0) {
                LOGGER.warn("No versions published to modrinth, defaulting to current version");
                return getVersion();
            }

            JsonObject latestVersion = versions.get(0).getAsJsonObject();
            return latestVersion.get("verson_number").getAsString();
        } catch (JsonSyntaxException err) {
            LOGGER.error("Could not parse the json given by modrinth.", err);
        } catch (InterruptedException err) {
            LOGGER.error("Version request was interrupted", err);
        } catch (IOException err) {
            LOGGER.error("Could not get versions from modrinth", err);
        }

        return null;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player all the infuse recipes
        InfuseEffect.getAllEffects().stream().map(recipeManager::getRecipeKey).forEach(player::discoverRecipe);
        
        // Telling the player their current control mode
        String controlMode = dataManager.getControlMode(player.getUniqueId());
        if (controlMode == null) controlMode = "Offhand";
        boolean offhandEnabled = controlMode.equalsIgnoreCase("Offhand");
        player.addAttachment(this, "ability.use", !offhandEnabled);

        Message msg = new Message(MessageType.JOIN_ABILITY_NOTIFY);
        msg.applyPlaceholder("control_mode", controlMode);
        player.sendMessage(msg.toComponent());

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

    @EventHandler
    public void lowerCraftLimitOnDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        InfuseEffect effect = InfuseEffect.fromItem(item);
        if (effect == null) return;

        // Decrementing the number of crafted effects
        dataManager.setExistingCount(effect, dataManager.getExistingCount(effect) - 1);
    }

    @EventHandler
    public void lowerCraftLimitOnDestroy(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Item itemEntity)) return;

        ItemStack item = itemEntity.getItemStack();
        InfuseEffect effect = InfuseEffect.fromItem(item);
        if (effect == null) return;

        // Decrementing the number of crafted effects
        dataManager.setExistingCount(effect, dataManager.getExistingCount(effect) - 1);
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}