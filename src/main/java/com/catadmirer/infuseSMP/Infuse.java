package com.catadmirer.infuseSMP;

import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.catadmirer.infuseSMP.Commands.*;
import com.catadmirer.infuseSMP.Effects.*;
import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Managers.*;
import com.catadmirer.infuseSMP.Particles.Particles;
import com.catadmirer.infuseSMP.Placeholders.InfusePlaceholders;
import com.catadmirer.infuseSMP.util.MessageUtil;
import com.catadmirer.infuseSMP.Commands.TrustCommand;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Infuse extends JavaPlugin implements Listener {
    private static Infuse instance;
    private final ConcurrentMap<UUID, String> effectManager = new ConcurrentHashMap<>();
    private DataManager dataManager;
    private Abilities abilitiesHandler;

    private Particles particles;

    private final Map<UUID, PlayerProfile> originalProfiles = new HashMap<>();
    private final Map<UUID, Integer> activeSkinModifiers = new HashMap<>();

    private ApophisManager apophisCommand;

    private FileConfiguration messages;

    private final Map<String, String> effectNames = new HashMap<>();
    private final Map<String, List<String>> effectLore = new HashMap<>();
    private final Map<String, Object> settings = new HashMap<>();

    private File messagesFile;

    public static Infuse getInstance() {
        return instance;
    }

    public void onEnable() {
        if (instance != null) {
            throw new IllegalStateException("Plugin already initialized!");
        } else {
            instance = this;
            this.saveDefaultConfig();
            loadMessages();

            settings.clear();
            settings.put("allow_infinite_effects", getConfig().getBoolean("allow_infinite_effects", false));
            settings.put("ritual_duration", getConfig().getInt("ritual_duration", 600));
            settings.put("ritual_duration_ender", getConfig().getInt("ritual_duration_ender", 3600));
            settings.put("brewing_particles", getConfig().getBoolean("brewing_particles", true));
            settings.put("empty_effect_icon", getConfig().getBoolean("empty_effect_icon", true));
            settings.put("player_head_drops", getConfig().getBoolean("player_head_drops", true));
            settings.put("enable_discord_broadcasts", getConfig().getBoolean("enable_discord_broadcasts", false));
            settings.put("discord_webhook_url", getConfig().getString("discord_webhook_url", ""));
            settings.put("invis_deaths", getConfig().getBoolean("invis_deaths", false));
            settings.put("brewing_gui", getConfig().getBoolean("brewing_gui", true));
            settings.put("join_effects_enabled", getConfig().getBoolean("join_effects_enabled", false));
            settings.put("join_effects", getConfig().getStringList("join_effects"));
            for (String key : getConfig().getConfigurationSection("craft_limits").getKeys(false)) {
                settings.put("craft_limits." + key + ".augmented_limit", getConfig().getInt("craft_limits." + key + ".augmented_limit", 0));
                settings.put("craft_limits." + key + ".regular_limit", getConfig().getInt("craft_limits." + key + ".regular_limit", 0));
            }
            for (String effect : new String[]{
                    "invisibility", "apophis", "emerald", "ender", "feather", "fire",
                    "frost", "haste", "heart", "ocean", "regen", "speed", "strength",
                    "thunder", "thief"
            }) {
                if (getConfig().isConfigurationSection(effect)) {
                    if (getConfig().isConfigurationSection(effect + ".cooldown")) {
                        settings.put(effect + ".cooldown.default", getConfig().getInt(effect + ".cooldown.default", 0));
                        settings.put(effect + ".cooldown.augmented", getConfig().getInt(effect + ".cooldown.augmented", 0));
                    }
                    if (getConfig().isConfigurationSection(effect + ".duration")) {
                        settings.put(effect + ".duration.default", getConfig().getInt(effect + ".duration.default", 0));
                        settings.put(effect + ".duration.augmented", getConfig().getInt(effect + ".duration.augmented", 0));
                    }
                    if (effect.equals("speed")) {
                        settings.put("speed.dashMultiplier", getConfig().getDouble("speed.dashMultiplier", 20.0));
                        settings.put("speed.playerVelocityMultiplier", getConfig().getDouble("speed.playerVelocityMultiplier", 2.0));
                    }
                }
            }
            settings.put("ocean_pulling.pull.interval", getConfig().getInt("ocean_pulling.pull.interval", 20));
            settings.put("ocean_pulling.pull.radius", getConfig().getDouble("ocean_pulling.pull.radius", 5.0));
            settings.put("ocean_pulling.pull.strength", getConfig().getDouble("ocean_pulling.pull.strength", 0.3));
            settings.put("extra_effects.Apophis", getConfig().getBoolean("extra_effects.Apophis", false));
            settings.put("extra_effects.Thief", getConfig().getBoolean("extra_effects.Thief", false));

            settings.put("invis.kill_invis", getMessages().getString("invis.kill_invis"));
            settings.put("invis.death_invis", getMessages().getString("invis.death_invis"));

            effectNames.clear();
            effectNames.put("strength", getMessages().getString("strength.effect_name", "§4Strength Effect"));
            effectNames.put("aug_strength", getMessages().getString("aug_strength.effect_name", "§4Augmented Strength Effect"));
            effectNames.put("thunder", getMessages().getString("thunder.effect_name", "§eThunder Effect"));
            effectNames.put("aug_thunder", getMessages().getString("aug_thunder.effect_name", "§eAugmented Thunder Effect"));
            effectNames.put("speed", getMessages().getString("speed.effect_name", "§#E8BD74Speed Effect"));
            effectNames.put("aug_speed", getMessages().getString("aug_speed.effect_name", "§#E8BD74Augmented Speed Effect"));
            effectNames.put("regen", getMessages().getString("regen.effect_name", "§cRegeneration Effect"));
            effectNames.put("aug_regen", getMessages().getString("aug_regen.effect_name", "§cAugmented Regeneration Effect"));
            effectNames.put("ocean", getMessages().getString("ocean.effect_name", "§9Ocean Effect"));
            effectNames.put("aug_ocean", getMessages().getString("aug_ocean.effect_name", "§9Augmented Ocean Effect"));
            effectNames.put("invis", getMessages().getString("invisibility.effect_name", "§5Invisibility Effect"));
            effectNames.put("aug_invis", getMessages().getString("aug_invisibility.effect_name", "§5Augmented Invisibility Effect"));
            effectNames.put("heart", getMessages().getString("heart.effect_name", "§cHeart Effect"));
            effectNames.put("aug_heart", getMessages().getString("aug_heart.effect_name", "§cAugmented Heart Effect"));
            effectNames.put("haste", getMessages().getString("haste.effect_name", "§6Haste Effect"));
            effectNames.put("aug_haste", getMessages().getString("aug_haste.effect_name", "§6Augmented Haste Effect"));
            effectNames.put("frost", getMessages().getString("frost.effect_name", "§bFrost Effect"));
            effectNames.put("aug_frost", getMessages().getString("aug_frost.effect_name", "§bAugmented Frost Effect"));
            effectNames.put("fire", getMessages().getString("fire.effect_name", "§#E85720Fire Effect"));
            effectNames.put("aug_fire", getMessages().getString("aug_fire.effect_name", "§#E85720Augmented Fire Effect"));
            effectNames.put("feather", getMessages().getString("feather.effect_name", "§#BEA3CAFeather Effect"));
            effectNames.put("aug_feather", getMessages().getString("aug_feather.effect_name", "§#BEA3CAAugmented Feather Effect"));
            effectNames.put("ender", getMessages().getString("ender.effect_name", "§5Ender Effect"));
            effectNames.put("aug_ender", getMessages().getString("aug_ender.effect_name", "§5Augmented Ender Effect"));
            effectNames.put("emerald", getMessages().getString("emerald.effect_name", "§aEmerald Effect"));
            effectNames.put("aug_emerald", getMessages().getString("aug_emerald.effect_name", "§aAugmented Emerald Effect"));
            effectNames.put("apophis", getMessages().getString("apophis.effect_name", "§5Apophis Effect"));
            effectNames.put("aug_apophis", getMessages().getString("aug_apophis.effect_name", "§5Augmented Apophis Effect"));
            effectNames.put("thief", getMessages().getString("thief.effect_name", "§4Thief Effect"));
            effectNames.put("aug_thief", getMessages().getString("aug_thief.effect_name", "§4Augmented Thief Effect"));

            effectLore.clear();
            effectLore.put("strength", getMessages().getStringList("strength.effect_lore"));
            effectLore.put("aug_strength", getMessages().getStringList("aug_strength.effect_lore"));
            effectLore.put("thunder", getMessages().getStringList("thunder.effect_lore"));
            effectLore.put("aug_thunder", getMessages().getStringList("aug_thunder.effect_lore"));
            effectLore.put("speed", getMessages().getStringList("speed.effect_lore"));
            effectLore.put("aug_speed", getMessages().getStringList("aug_speed.effect_lore"));
            effectLore.put("regen", getMessages().getStringList("regen.effect_lore"));
            effectLore.put("aug_regen", getMessages().getStringList("aug_regen.effect_lore"));
            effectLore.put("ocean", getMessages().getStringList("ocean.effect_lore"));
            effectLore.put("aug_ocean", getMessages().getStringList("aug_ocean.effect_lore"));
            effectLore.put("invis", getMessages().getStringList("invisibility.effect_lore"));
            effectLore.put("aug_invis", getMessages().getStringList("aug_invisibility.effect_lore"));
            effectLore.put("heart", getMessages().getStringList("heart.effect_lore"));
            effectLore.put("aug_heart", getMessages().getStringList("aug_heart.effect_lore"));
            effectLore.put("haste", getMessages().getStringList("haste.effect_lore"));
            effectLore.put("aug_haste", getMessages().getStringList("aug_haste.effect_lore"));
            effectLore.put("frost", getMessages().getStringList("frost.effect_lore"));
            effectLore.put("aug_frost", getMessages().getStringList("aug_frost.effect_lore"));
            effectLore.put("fire", getMessages().getStringList("fire.effect_lore"));
            effectLore.put("aug_fire", getMessages().getStringList("aug_fire.effect_lore"));
            effectLore.put("feather", getMessages().getStringList("feather.effect_lore"));
            effectLore.put("aug_feather", getMessages().getStringList("aug_feather.effect_lore"));
            effectLore.put("ender", getMessages().getStringList("ender.effect_lore"));
            effectLore.put("aug_ender", getMessages().getStringList("aug_ender.effect_lore"));
            effectLore.put("emerald", getMessages().getStringList("emerald.effect_lore"));
            effectLore.put("aug_emerald", getMessages().getStringList("aug_emerald.effect_lore"));
            effectLore.put("apophis", getMessages().getStringList("apophis.effect_lore"));
            effectLore.put("aug_apophis", getMessages().getStringList("aug_apophis.effect_lore"));
            effectLore.put("thief", getMessages().getStringList("thief.effect_lore"));
            effectLore.put("aug_thief", getMessages().getStringList("aug_thief.effect_lore"));
            PacketEvents.getAPI().init();
            new ApophisManager(this, "AphopisPlayers/").getApophisFile();
            apophisCommand = new ApophisManager(this, "AphopisPlayers/");
            new InfuseRecipeManager(this);
            this.dataManager = new DataManager(getDataFolder());
            this.abilitiesHandler = new Abilities(dataManager, this);
            PacketEvents.getAPI().getEventManager().registerListener(
                    new Invisibility(this, dataManager), PacketListenerPriority.HIGHEST);
            PacketEvents.getAPI().getEventManager().registerListener(
                    new Thief(dataManager, this), PacketListenerPriority.HIGHEST);
            PacketEvents.getAPI().getEventManager().registerListener(
                    new Fire(this), PacketListenerPriority.HIGHEST);
            this.registerCommands();
            checkForUpdate();
            this.registerEvents();
            new ActionBarUpdater().runTaskTimer(this, 0L, 20L);
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new InfusePlaceholders(this).register();
                getLogger().info("Placeholders Enabled!");
            } else {
                getLogger().warning("PlaceholderAPI is not installed, so custom placeholders won't work.");
            }

            getLogger().info("Infuse Plugin has been enabled!");
        }
    }

    public <T> T getConfig(String key) {
        return (T) settings.get(key);
    }

    public String getEffect(String key) {
        return effectNames.getOrDefault(key, "§cUnknown Effect");
    }

    public List<String> getEffectLore(String key) {
        return effectLore.getOrDefault(key, Collections.singletonList("§cUnknown Effect"));
    }

    public void loadMessages() {
        messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadDaConfig(Player player) {
        reloadConfig();
        long start = System.nanoTime();
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        settings.clear();
        settings.put("allow_infinite_effects", getConfig().getBoolean("allow_infinite_effects", false));
        settings.put("ritual_duration", getConfig().getInt("ritual_duration", 600));
        settings.put("ritual_duration_ender", getConfig().getInt("ritual_duration_ender", 3600));
        settings.put("brewing_particles", getConfig().getBoolean("brewing_particles", true));
        settings.put("empty_effect_icon", getConfig().getBoolean("empty_effect_icon", true));
        settings.put("player_head_drops", getConfig().getBoolean("player_head_drops", true));
        settings.put("enable_discord_broadcasts", getConfig().getBoolean("enable_discord_broadcasts", false));
        settings.put("discord_webhook_url", getConfig().getString("discord_webhook_url", ""));
        settings.put("invis_deaths", getConfig().getBoolean("invis_deaths", false));
        settings.put("brewing_gui", getConfig().getBoolean("brewing_gui", true));
        settings.put("join_effects_enabled", getConfig().getBoolean("join_effects_enabled", false));
        settings.put("join_effects", getConfig().getStringList("join_effects"));
        for (String key : getConfig().getConfigurationSection("craft_limits").getKeys(false)) {
            settings.put("craft_limits." + key + ".augmented_limit", getConfig().getInt("craft_limits." + key + ".augmented_limit", 0));
            settings.put("craft_limits." + key + ".regular_limit", getConfig().getInt("craft_limits." + key + ".regular_limit", 0));
        }
        for (String effect : new String[]{
                "invisibility", "apophis", "emerald", "ender", "feather", "fire",
                "frost", "haste", "heart", "ocean", "regen", "speed", "strength",
                "thunder", "thief"
        }) {
            if (getConfig().isConfigurationSection(effect)) {
                if (getConfig().isConfigurationSection(effect + ".cooldown")) {
                    settings.put(effect + ".cooldown.default", getConfig().getInt(effect + ".cooldown.default", 0));
                    settings.put(effect + ".cooldown.augmented", getConfig().getInt(effect + ".cooldown.augmented", 0));
                }
                if (getConfig().isConfigurationSection(effect + ".duration")) {
                    settings.put(effect + ".duration.default", getConfig().getInt(effect + ".duration.default", 0));
                    settings.put(effect + ".duration.augmented", getConfig().getInt(effect + ".duration.augmented", 0));
                }
                if (effect.equals("speed")) {
                    settings.put("speed.dashMultiplier", getConfig().getDouble("speed.dashMultiplier", 20.0));
                    settings.put("speed.playerVelocityMultiplier", getConfig().getDouble("speed.playerVelocityMultiplier", 2.0));
                }
            }
        }
        settings.put("ocean_pulling.pull.interval", getConfig().getInt("ocean_pulling.pull.interval", 20));
        settings.put("ocean_pulling.pull.radius", getConfig().getDouble("ocean_pulling.pull.radius", 5.0));
        settings.put("ocean_pulling.pull.strength", getConfig().getDouble("ocean_pulling.pull.strength", 0.3));
        settings.put("extra_effects.Apophis", getConfig().getBoolean("extra_effects.Apophis", false));
        settings.put("extra_effects.Thief", getConfig().getBoolean("extra_effects.Thief", false));

        settings.put("invis.kill_invis", getMessages().getString("invis.kill_invis"));
        settings.put("invis.death_invis", getMessages().getString("invis.death_invis"));

        effectNames.clear();
        effectNames.put("strength", getMessages().getString("strength.effect_name", "§4Strength Effect"));
        effectNames.put("aug_strength", getMessages().getString("aug_strength.effect_name", "§4Augmented Strength Effect"));
        effectNames.put("thunder", getMessages().getString("thunder.effect_name", "§eThunder Effect"));
        effectNames.put("aug_thunder", getMessages().getString("aug_thunder.effect_name", "§eAugmented Thunder Effect"));
        effectNames.put("speed", getMessages().getString("speed.effect_name", "§#E8BD74Speed Effect"));
        effectNames.put("aug_speed", getMessages().getString("aug_speed.effect_name", "§#E8BD74Augmented Speed Effect"));
        effectNames.put("regen", getMessages().getString("regen.effect_name", "§cRegeneration Effect"));
        effectNames.put("aug_regen", getMessages().getString("aug_regen.effect_name", "§cAugmented Regeneration Effect"));
        effectNames.put("ocean", getMessages().getString("ocean.effect_name", "§9Ocean Effect"));
        effectNames.put("aug_ocean", getMessages().getString("aug_ocean.effect_name", "§9Augmented Ocean Effect"));
        effectNames.put("invis", getMessages().getString("invisibility.effect_name", "§5Invisibility Effect"));
        effectNames.put("aug_invis", getMessages().getString("aug_invisibility.effect_name", "§5Augmented Invisibility Effect"));
        effectNames.put("heart", getMessages().getString("heart.effect_name", "§cHeart Effect"));
        effectNames.put("aug_heart", getMessages().getString("aug_heart.effect_name", "§cAugmented Heart Effect"));
        effectNames.put("haste", getMessages().getString("haste.effect_name", "§6Haste Effect"));
        effectNames.put("aug_haste", getMessages().getString("aug_haste.effect_name", "§6Augmented Haste Effect"));
        effectNames.put("frost", getMessages().getString("frost.effect_name", "§bFrost Effect"));
        effectNames.put("aug_frost", getMessages().getString("aug_frost.effect_name", "§bAugmented Frost Effect"));
        effectNames.put("fire", getMessages().getString("fire.effect_name", "§#E85720Fire Effect"));
        effectNames.put("aug_fire", getMessages().getString("aug_fire.effect_name", "§#E85720Augmented Fire Effect"));
        effectNames.put("feather", getMessages().getString("feather.effect_name", "§#BEA3CAFeather Effect"));
        effectNames.put("aug_feather", getMessages().getString("aug_feather.effect_name", "§#BEA3CAAugmented Feather Effect"));
        effectNames.put("ender", getMessages().getString("ender.effect_name", "§5Ender Effect"));
        effectNames.put("aug_ender", getMessages().getString("aug_ender.effect_name", "§5Augmented Ender Effect"));
        effectNames.put("emerald", getMessages().getString("emerald.effect_name", "§aEmerald Effect"));
        effectNames.put("aug_emerald", getMessages().getString("aug_emerald.effect_name", "§aAugmented Emerald Effect"));
        effectNames.put("apophis", getMessages().getString("apophis.effect_name", "§5Apophis Effect"));
        effectNames.put("aug_apophis", getMessages().getString("aug_apophis.effect_name", "§5Augmented Apophis Effect"));
        effectNames.put("thief", getMessages().getString("thief.effect_name", "§4Thief Effect"));
        effectNames.put("aug_thief", getMessages().getString("aug_thief.effect_name", "§4Augmented Thief Effect"));

        effectLore.clear();
        effectLore.put("strength", getMessages().getStringList("strength.effect_lore"));
        effectLore.put("aug_strength", getMessages().getStringList("aug_strength.effect_lore"));
        effectLore.put("thunder", getMessages().getStringList("thunder.effect_lore"));
        effectLore.put("aug_thunder", getMessages().getStringList("aug_thunder.effect_lore"));
        effectLore.put("speed", getMessages().getStringList("speed.effect_lore"));
        effectLore.put("aug_speed", getMessages().getStringList("aug_speed.effect_lore"));
        effectLore.put("regen", getMessages().getStringList("regen.effect_lore"));
        effectLore.put("aug_regen", getMessages().getStringList("aug_regen.effect_lore"));
        effectLore.put("ocean", getMessages().getStringList("ocean.effect_lore"));
        effectLore.put("aug_ocean", getMessages().getStringList("aug_ocean.effect_lore"));
        effectLore.put("invis", getMessages().getStringList("invisibility.effect_lore"));
        effectLore.put("aug_invis", getMessages().getStringList("aug_invisibility.effect_lore"));
        effectLore.put("heart", getMessages().getStringList("heart.effect_lore"));
        effectLore.put("aug_heart", getMessages().getStringList("aug_heart.effect_lore"));
        effectLore.put("haste", getMessages().getStringList("haste.effect_lore"));
        effectLore.put("aug_haste", getMessages().getStringList("aug_haste.effect_lore"));
        effectLore.put("frost", getMessages().getStringList("frost.effect_lore"));
        effectLore.put("aug_frost", getMessages().getStringList("aug_frost.effect_lore"));
        effectLore.put("fire", getMessages().getStringList("fire.effect_lore"));
        effectLore.put("aug_fire", getMessages().getStringList("aug_fire.effect_lore"));
        effectLore.put("feather", getMessages().getStringList("feather.effect_lore"));
        effectLore.put("aug_feather", getMessages().getStringList("aug_feather.effect_lore"));
        effectLore.put("ender", getMessages().getStringList("ender.effect_lore"));
        effectLore.put("aug_ender", getMessages().getStringList("aug_ender.effect_lore"));
        effectLore.put("emerald", getMessages().getStringList("emerald.effect_lore"));
        effectLore.put("aug_emerald", getMessages().getStringList("aug_emerald.effect_lore"));
        effectLore.put("apophis", getMessages().getStringList("apophis.effect_lore"));
        effectLore.put("aug_apophis", getMessages().getStringList("aug_apophis.effect_lore"));
        effectLore.put("thief", getMessages().getStringList("thief.effect_lore"));
        effectLore.put("aug_thief", getMessages().getStringList("aug_thief.effect_lore"));
        long duration = (System.nanoTime() - start) / 1_000_000;
        player.sendMessage("§aInfuse reloaded in " + duration + "ms!");
    }

    public String getEffectReversed(String displayName) {
        String strippedName = MessageUtil.stripAllColors(displayName);

        for (Map.Entry<String, String> entry : effectNames.entrySet()) {
            if (MessageUtil.stripAllColors(entry.getValue()).equalsIgnoreCase(strippedName)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private void registerCommands() {
        this.getCommand("trust").setExecutor(new TrustCommand(this, dataManager));
        this.getCommand("untrust").setExecutor(new TrustCommand(this, dataManager));
        this.getCommand("recipes").setExecutor(new Recipes(this));
        this.getCommand("swap").setExecutor(new EquipEffect(apophisCommand));
        this.getCommand("infuse").setExecutor(new InfuseCommand());
        this.getCommand("infuse").setTabCompleter(new InfuseCommand());
        this.getCommand("ldrain").setExecutor(new DrainCommand(this, apophisCommand));
        this.getCommand("rdrain").setExecutor(new DrainCommand(this, apophisCommand));
        this.getCommand("rspark").setExecutor(this.abilitiesHandler);
        this.getCommand("lspark").setExecutor(this.abilitiesHandler);
        this.getCommand("controls").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }

                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /controls <Offhand|Command>");
                    return true;
                }

                String choice = args[0];
                if (!choice.equals("Offhand") && !choice.equals("Command")) {
                    player.sendMessage(ChatColor.RED + "Invalid option. Use 'Offhand' or 'Command'.");
                    return true;
                }
                DataManager dataManager = Infuse.getInstance().getEffectManager();
                dataManager.setControlDefault(player.getUniqueId(), choice);
                boolean offhandEnabled = choice.equals("Offhand");
                player.addAttachment(Infuse.getInstance(), "ability.use", !offhandEnabled);
                return true;
            }
        });
        this.getCommand("controls").setTabCompleter((sender, command, alias, args) -> {
            if (args.length == 1) {
                return Arrays.asList("Offhand", "Command").stream()
                        .filter(opt -> opt.startsWith(args[0]))
                        .collect(Collectors.toList());
            }
            return List.of();
        });
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        (new BukkitRunnable() {
            public void run() {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                player.setHealth(20);
            }
        }).runTaskLater(this, 10L);
    }

    public void onDisable() {
        instance = null;
        this.getLogger().info("Infuse Plugin is disabling...");
        PacketEvents.getAPI().terminate();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(EnderCrystal.class)) {
                entity.remove();
            }
        }

        this.effectManager.clear();
        this.getLogger().info("Infuse Plugin has been disabled!");
    }

    public DataManager getEffectManager() {
        return dataManager;
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new Strength(this), this);

        this.particles = new Particles();

        this.particles.startTask();

        Bukkit.getPluginManager().registerEvents(new GUI(), this);
        Bukkit.getPluginManager().registerEvents(new Drop(this), this);
        Bukkit.getPluginManager().registerEvents(new Frost(dataManager, this), this);
        Bukkit.getPluginManager().registerEvents(new Invisibility(this, dataManager), this);
        Bukkit.getPluginManager().registerEvents(new Heart(this), this);
        Bukkit.getPluginManager().registerEvents(new Recipes(this), this);
        Bukkit.getPluginManager().registerEvents(new Emerald(this), this);
        Bukkit.getPluginManager().registerEvents(new EquipEffect(apophisCommand), this);
        Bukkit.getPluginManager().registerEvents(new Ocean(this, dataManager), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getConfig().getBoolean("extra_effects.Apophis")) {
            getServer().getPluginManager().registerEvents(new Apophis(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new Regen(this), this);
        Bukkit.getPluginManager().registerEvents(new Feather(this, dataManager), this);
        Bukkit.getPluginManager().registerEvents(new Thunder(this, dataManager), this);
        Bukkit.getPluginManager().registerEvents(new Haste(this), this);
        Bukkit.getPluginManager().registerEvents(new Speed(this), this);
        Bukkit.getPluginManager().registerEvents(new Fire(this), this);
        Bukkit.getPluginManager().registerEvents(new Ender(dataManager, this), this);
        Bukkit.getPluginManager().registerEvents(new ClearEffect(), this);
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

    public void saveOriginalSkin(Player player) {
        UUID uuid = player.getUniqueId();
        if (!originalProfiles.containsKey(uuid)) {
            originalProfiles.put(uuid, player.getPlayerProfile());
        }
        activeSkinModifiers.put(uuid, activeSkinModifiers.getOrDefault(uuid, 0) + 1);
    }

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

    public FileConfiguration getMessages() {
        return messages;
    }

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

                JSONParser parser = new JSONParser();
                JSONArray versions = (JSONArray) parser.parse(new InputStreamReader(connection.getInputStream()));
                if (versions.isEmpty()) return;

                JSONObject latest = (JSONObject) versions.get(0);
                String latestVersion = (String) latest.get("version_number");

                if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                    String updateMessage = "A new version (" + latestVersion + ") is available! You are on " + currentVersion + " " + "https://modrinth.com/plugin/infusesmp";
                    getLogger().info(ChatColor.stripColor(updateMessage));
                }

            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to check for Infuse updates", e);
            }
        });
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DataManager dataManager = Infuse.getInstance().getEffectManager();
        String controlMode = dataManager.getControlDefault(player.getUniqueId());
        if (controlMode == null) controlMode = "Offhand";
        boolean offhandEnabled = controlMode.equalsIgnoreCase("Offhand");
        player.addAttachment(Infuse.getInstance(), "ability.use", !offhandEnabled);
        player.sendMessage(ChatColor.GRAY + "Your ability mode is set to: " + controlMode);
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

            JSONParser parser = new JSONParser();
            JSONArray versions = (JSONArray) parser.parse(new InputStreamReader(connection.getInputStream()));
            if (versions.isEmpty()) return;

            JSONObject latest = (JSONObject) versions.get(0);
            String latestVersion = (String) latest.get("version_number");

            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                String updateMessage = ChatColor.LIGHT_PURPLE + "[Infuse] " + ChatColor.GREEN + "A new version (" + latestVersion + ") is available! "
                        + ChatColor.GRAY + "You are on " + currentVersion + " "
                        + ChatColor.AQUA + "https://modrinth.com/plugin/infusesmp";
                if (player.isOp()) {
                    player.sendMessage(updateMessage);
                }
            }

        } catch (Exception e) {
            player.sendMessage("Failed to check for Infuse updates" + e);
        }
    }
}
