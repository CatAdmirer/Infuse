package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.ExtraEffects.Apophis;
import com.catadmirer.infuseSMP.ExtraEffects.Thief;
import com.catadmirer.infuseSMP.Inventories.BrewingStandGUI;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Effects.*;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

public class InfuseRecipeManager implements Listener {
    private final Infuse plugin;
    private final Map<String, ItemStack> firstTimeRewards;
    private final Map<String, ItemStack> standardResults;

    private BossBar activeBossBar;

    FileConfiguration recipesConfig;

    LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();
    PlainTextComponentSerializer plaintext = PlainTextComponentSerializer.plainText();

    public InfuseRecipeManager(Infuse plugin) {
        this.plugin = plugin;
        this.firstTimeRewards = new HashMap<>();
        this.standardResults = new HashMap<>();
        try {
            File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
            if (!recipesFile.exists()) {
                plugin.saveResource("recipes.yml", false);
            }
            recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load recipes.yml!");
            e.printStackTrace();
            recipesConfig = null;
        }

        if (recipesConfig != null) {
            loadRecipes();
        } else {
            plugin.getLogger().warning("Recipes config is null.");
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadRecipes() {
        ConfigurationSection recipesSection = recipesConfig.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return;
        }

        for (String recipeKey : recipesSection.getKeys(false)) {
            boolean enabled = recipesSection.getBoolean(recipeKey + ".enabled", false);
            if (!enabled) {
                plugin.getLogger().info("Recipe " + recipeKey + " is disabled in config, skipping.");
                continue;
            }

            switch (recipeKey.toLowerCase()) {
                case "emerald":
                    this.Emerald();
                    break;
                case "feather":
                    this.Feather();
                    break;
                case "fire":
                    this.Fire();
                    break;
                case "frost":
                    this.Frost();
                    break;
                case "haste":
                    this.Haste();
                    break;
                case "heart":
                    this.Heart();
                    break;
                case "invis":
                    this.Invis();
                    break;
                case "ocean":
                    this.Ocean();
                    break;
                case "regen":
                    this.Regen();
                    break;
                case "speed":
                    this.Speed();
                    break;
                case "strength":
                    this.Strength();
                    break;
                case "thunder":
                    this.Thunder();
                    break;
                case "end_first":
                    this.Ender();
                    break;
                case "end_second":
                    break;
                case "apophis":
                    this.Apophis();
                    break;
                case "thief":
                    this.Thief();
                    break;
                default:
                    plugin.getLogger().warning("Unknown recipe key: " + recipeKey);
                    break;
            }
        }
    }

        private void spawnCustomBeam(Location brewingStandLocation, String recipeKey) {
            if (Infuse.getInstance().<Boolean>getConfig("brewing_particles")) {
                World world = brewingStandLocation.getWorld();
                Location crystalLoc = new Location(world, brewingStandLocation.getX(), -5000, brewingStandLocation.getZ());
                final EnderCrystal crystal = (EnderCrystal) world.spawnEntity(crystalLoc, EntityType.END_CRYSTAL);
                crystal.setShowingBottom(false);
                crystal.setInvulnerable(true);
                crystal.setInvisible(true);
                Location targetLoc = brewingStandLocation.clone().add(0, 600 - brewingStandLocation.getY(), 0);
                final ArmorStand marker = (ArmorStand) world.spawnEntity(targetLoc, EntityType.ARMOR_STAND);
                marker.setMarker(true);
                marker.setInvisible(true);
                marker.setInvulnerable(true);
                marker.setSilent(true);
                marker.setCustomNameVisible(false);
                crystal.setBeamTarget(marker.getLocation().toBlockLocation());
                int ritualDuration;
                if (recipeKey.equalsIgnoreCase("end_first")) {
                    ritualDuration = Infuse.getInstance().getConfig("ritual_duration_ender");
                } else {
                    ritualDuration = Infuse.getInstance().getConfig("ritual_duration");
                }
                (new BukkitRunnable() {
                    public void run() {
                        if (!crystal.isDead()) {
                            crystal.remove();
                        }

                        if (!marker.isDead()) {
                            marker.remove();
                        }

                    }
                }).runTaskLater(this.plugin, ritualDuration * 20L);
            }
        }

    private boolean isRitualActive = false;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.activeBossBar != null) {
            this.activeBossBar.addPlayer(event.getPlayer());
        }
    }

    private void startRitual(Player player, String recipeKey, Location playerLocation, final ItemStack craftedItem) {
        if (isRitualActive) {
            player.sendMessage(ChatColor.RED + "A ritual is already in progress!");
            return;
        }
        final Location brewingStandLocation = this.findNearestBrewingStand(playerLocation);
        if (brewingStandLocation == null) {
            String invalidMessage = ChatColor.translateAlternateColorCodes('&',
                    plugin.getMessages().getString("effect_nobrewing", "&cYou need to craft this in a brewing stand!")
            );
            player.sendMessage(invalidMessage);
            return;
        }

        isRitualActive = true;

        Component itemName = craftedItem.getItemMeta().displayName();
        TextColor itemColor = itemName.color();
        String formattedItemName = legacySection.serialize(Component.text("\uD83E\uDDEA ", itemColor, TextDecoration.BOLD).append(itemName).append(Component.text(" \uD83E\uDDEA")));

        BarColor barColor = this.getColorFromItemName(recipeKey);
        this.activeBossBar = Bukkit.createBossBar(formattedItemName, barColor, BarStyle.SOLID);

        for (Player p : Bukkit.getOnlinePlayers()) {
            activeBossBar.addPlayer(p);
        }

        String worldName = brewingStandLocation.getWorld().getName();
        String dimensionMessage;
        if (worldName.equalsIgnoreCase("world")) {
            dimensionMessage = ChatColor.GREEN + "Overworld";
        } else if (worldName.equalsIgnoreCase("world_end") || worldName.equalsIgnoreCase("world_the_end")) {
            dimensionMessage = ChatColor.DARK_PURPLE + "End";
        } else if (worldName.equalsIgnoreCase("world_nether") || worldName.equalsIgnoreCase("world_the_nether")) {
            dimensionMessage = ChatColor.DARK_RED + "Nether";
        } else {
            dimensionMessage = ChatColor.GRAY + worldName;
        }

        String messageTemplate = plugin.getMessages().getString("effect_broadcast");
        String discordTemplate = plugin.getMessages().getString("discord_broadcast");

        String x = String.valueOf(brewingStandLocation.getBlockX());
        String y = String.valueOf(brewingStandLocation.getBlockY());
        String z = String.valueOf(brewingStandLocation.getBlockZ());

        String formattedMessage = messageTemplate
                .replace("%player%", player.getName())
                .replace("%item%", legacySection.serialize(itemName))
                .replace("%x%", x)
                .replace("%y%", y)
                .replace("%z%", z)
                .replace("%dimension%", dimensionMessage);

        String formattedDiscordMessage = discordTemplate
                .replace("%player%", player.getName())
                .replace("%item%", plaintext.serialize(itemName))
                .replace("%x%", x)
                .replace("%y%", y)
                .replace("%z%", z)
                .replace("%dimension%", ChatColor.stripColor(dimensionMessage));

        Bukkit.broadcastMessage(formattedMessage);
        if (Infuse.getInstance().<Boolean>getConfig("enable_discord_broadcasts")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast " + formattedDiscordMessage);
        }
        String webhookUrl = Infuse.getInstance().getConfig("discord_webhook_url");
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            sendToDiscord(webhookUrl, formattedDiscordMessage);
        }

        this.spawnCustomBeam(brewingStandLocation, recipeKey);
        int ritualDuration;

        if (recipeKey.equalsIgnoreCase("end_first")) {
            ritualDuration = Infuse.getInstance().getConfig("ritual_duration_ender");
        } else {
            ritualDuration = Infuse.getInstance().getConfig("ritual_duration");
        }


        new BukkitRunnable() {
            double progress = 1;
            final double progressDecrement = 1 / (ritualDuration * 20);

            public void run() {
                if (progress <= 0) {
                    activeBossBar.removeAll();
                    String finishedTemplate = plugin.getMessages().getString("effect_finished", "%item% has been brewed!");
                    String finishedMessage = finishedTemplate.replace("%item%", legacySection.serialize(itemName));
                    Bukkit.broadcastMessage(ChatColor.WHITE + finishedMessage);
                    brewingStandLocation.getWorld().dropItemNaturally(brewingStandLocation, craftedItem);
                    isRitualActive = false;
                    this.cancel();
                } else {
                    progress -= progressDecrement;
                    activeBossBar.setProgress(progress);
                }
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }

    private void sendToDiscord(String webhookUrl, String message) {
        try {
            String payload = "{\"content\": \"" + message + "\"}";
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                plugin.getLogger().info("Message sent to Discord!");
            } else {
                plugin.getLogger().info("Error sending message to Discord: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Location findNearestBrewingStand(Location playerLocation) {
        Location nearestLocation = null;
        double nearestDist = Double.MAX_VALUE;

        for (int x = -5; x <= 5; ++x) {
            for (int y = -5; y <= 5; ++y) {
                for (int z = -5; z <= 5; ++z) {
                    Location checkLocation = playerLocation.clone().add(x, y, z);

                    if (checkLocation.getBlock().getType() == Material.BREWING_STAND) {
                        double checkDist = checkLocation.distance(playerLocation);
                        if (checkDist < nearestDist) {
                            nearestDist = checkDist;
                            nearestLocation = checkLocation;
                        }
                    }
                }
            }
        }

        return nearestLocation;
    }

    private BarColor getColorFromItemName(String itemName) {
        switch (itemName.toLowerCase()) {
            case "emerald":
            case "aug_emerald":
                return BarColor.GREEN;
            case "haste":
            case "aug_haste":
                return BarColor.YELLOW;
            case "heart":
            case "aug_heart":
                return BarColor.RED;
            case "invis":
            case "aug_invis":
            case "end_first":
            case "end_second":
            case "apophis":
            case "aug_apophis":
                return BarColor.PURPLE;
            case "frost":
            case "aug_frost":
                return BarColor.BLUE;
            case "feather":
            case "aug_feather":
                return BarColor.WHITE;
            case "thunder":
            case "aug_thunder":
                return BarColor.YELLOW;
            case "speed":
            case "aug_speed":
                return BarColor.BLUE;
            case "regen":
            case "aug_regen":
                return BarColor.RED;
            case "ocean":
            case "aug_ocean":
                return BarColor.BLUE;
            case "fire":
            case "aug_fire":
                return BarColor.RED;
            case "strength":
            case "aug_strength":
            case "thief":
            case "aug_thief":
                return BarColor.RED;
            default:
                return BarColor.WHITE;
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getRecipe() instanceof ShapedRecipe shaped)) return;
        String recipeKey = shaped.getKey().getKey();
        if (!firstTimeRewards.containsKey(recipeKey) && !standardResults.containsKey(recipeKey)) return;
        Player player = (Player) event.getWhoClicked();
        if (recipeKey.equals("end_second")) {
            int endFirstAugLimit = Infuse.getInstance().getConfig("craft_limits.end_first.augmented_limit");
            if (endFirstAugLimit > 0) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        Inventory topInv = event.getView().getTopInventory();
        if (topInv.getType() != InventoryType.WORKBENCH) {
            event.setCancelled(true);
            return;
        }

        if (!isNearBrewingStand(player.getLocation())) {
            event.setCancelled(true);
            return;
        }
        Number augmentedLimitNumber = plugin.getConfig("craft_limits." + recipeKey + ".augmented_limit");
        Number regularLimitNumber = plugin.getConfig("craft_limits." + recipeKey + ".regular_limit");

        if (augmentedLimitNumber == null || regularLimitNumber == null) {
            event.setCancelled(true);
            return;
        }

        int augmentedLimit = augmentedLimitNumber.intValue();
        int regularLimit = regularLimitNumber.intValue();
        boolean allowInfinite = Infuse.getInstance().getConfig("allow_infinite_effects");

        ItemStack result = event.getCurrentItem();
        if (result == null) {
            event.setCancelled(true);
            return;
        }

        ItemStack augmentedItem = firstTimeRewards.get(recipeKey);

        boolean isAugmented = result.isSimilar(augmentedItem);

        if (isAugmented) {
            if (isRitualActive) {
                player.sendMessage(ChatColor.RED + "A ritual is already in progress!");
                event.setCancelled(true);
                return;
            }
            if (augmentedLimit <= 0 && !allowInfinite) {
                event.setCancelled(true);
                return;
            }

            CraftingInventory inv = event.getInventory();
            ItemStack[] matrix = inv.getMatrix();
            for (int i = 0; i < matrix.length; ++i) {
                if (matrix[i] != null && matrix[i].getType() != Material.AIR) {
                    int newAmt = matrix[i].getAmount() - 1;
                    matrix[i] = newAmt > 0 ? new ItemStack(matrix[i].getType(), newAmt) : null;
                }
            }

            inv.setMatrix(matrix);
            player.updateInventory();
            player.closeInventory();
            if (!allowInfinite) {
                plugin.getConfig().set("craft_limits." + recipeKey + ".augmented_limit", augmentedLimit - 1);
                plugin.saveConfig();
            }

            this.startRitual(player, recipeKey, player.getLocation(), augmentedItem);
            event.setCancelled(true);

        } else {
            if (regularLimit <= 0 && !allowInfinite) {
                event.setCancelled(true);
                return;
            }
            if (!allowInfinite) {
                plugin.getConfig().set("craft_limits." + recipeKey + ".regular_limit", regularLimit - 1);
                plugin.saveConfig();
            }
        }
    }


    private void registerRecipeFromConfig(String recipeKey, ItemStack result) {
        FileConfiguration config = recipesConfig;

        String basePath = "recipes." + recipeKey;

        if (!config.getBoolean(basePath + ".enabled", false)) {
            plugin.getLogger().info("Recipe " + recipeKey + " is disabled in config, skipping.");
            return;
        }

        List<String> shape = config.getStringList(basePath + ".shape");
        if (shape.isEmpty()) {
            plugin.getLogger().warning("Recipe shape is missing for " + recipeKey);
            return;
        }

        ConfigurationSection ingredientsSection = config.getConfigurationSection(basePath + ".ingredients");
        if (ingredientsSection == null) {
            plugin.getLogger().warning("Ingredients section missing for recipe " + recipeKey);
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape.toArray(new String[0]));

        for (String charKey : ingredientsSection.getKeys(false)) {
            String matName = ingredientsSection.getString(charKey);
            try {
                Material mat = Material.valueOf(matName);
                recipe.setIngredient(charKey.charAt(0), mat);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material '" + matName + "' for recipe '" + recipeKey + "'");
                return;
            }
        }

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Registered recipe: " + recipeKey);
    }



    @EventHandler
    public void onCrafter(CrafterCraftEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.POTION) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof ShapedRecipe shaped)) return;
        String recipeKey = shaped.getKey().getKey();

        if (!firstTimeRewards.containsKey(recipeKey) || !standardResults.containsKey(recipeKey)) return;

        Inventory topInv = event.getView().getTopInventory();
        Player player = (Player) event.getView().getPlayer();

        if (topInv.getType() != InventoryType.WORKBENCH) {
            event.getInventory().setResult(null);
            return;
        }

        if (!isNearBrewingStand(player.getLocation())) {
            event.getInventory().setResult(null);
            return;
        }

        boolean allowInfinite = Infuse.getInstance().getConfig("allow_infinite_effects");
        Number augmentedLimitNumber = plugin.getConfig("craft_limits." + recipeKey + ".augmented_limit");
        Number regularLimitNumber = plugin.getConfig("craft_limits." + recipeKey + ".regular_limit");

        if (augmentedLimitNumber == null || regularLimitNumber == null) {
            event.getInventory().setResult(null);
            return;
        }

        int augmentedLimit = augmentedLimitNumber.intValue();
        int regularLimit = regularLimitNumber.intValue();

        if (allowInfinite || augmentedLimit > 0) {
            event.getInventory().setResult(firstTimeRewards.get(recipeKey));
        } else if (regularLimit > 0) {
            event.getInventory().setResult(standardResults.get(recipeKey));
        } else {
            event.getInventory().setResult(null);
        }
    }


    private boolean isNearBrewingStand(Location loc) {
        World world = loc.getWorld();
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    if (world.getBlockAt(loc.clone().add(x, y, z)).getType() == Material.BREWING_STAND) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private final Map<UUID, BrewingStand> brewingStandCache = new HashMap<>();

    @EventHandler
    public void onBrewingStandInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.BREWING_STAND) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (plugin.<Boolean>getConfig("brewing_gui")) {
                Block block = event.getClickedBlock();
                BrewingStand stand = (BrewingStand) block.getState();
                brewingStandCache.put(player.getUniqueId(), stand);

                player.openInventory(new BrewingStandGUI().getInventory());
            } else {
                player.openWorkbench(null, true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BrewingStandGUI) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String name = clicked.getItemMeta().getDisplayName();
            if (name.contains("Crafting")) {
                player.closeInventory();
                player.openWorkbench(null, true);
            } else if (name.contains("Brewing")) {
                player.closeInventory();
                BrewingStand stand = brewingStandCache.get(player.getUniqueId());
                if (stand != null) {
                    player.openInventory(stand.getInventory());
                }
            }
        }
    }

    private void Emerald() {
        ItemStack firstTime = Emerald.createAugmented();
        ItemStack standard = Emerald.createRegular();
        this.firstTimeRewards.put("emerald", firstTime);
        this.standardResults.put("emerald", standard);
        registerRecipeFromConfig("emerald", standard);
    }

    private void Feather() {
        ItemStack firstTime = Feather.createAugmented();
        ItemStack standard = Feather.createRegular();
        this.firstTimeRewards.put("feather", firstTime);
        this.standardResults.put("feather", standard);
        registerRecipeFromConfig("feather", standard);
    }

    private void Fire() {
        ItemStack firstTime = Fire.createAugmented();
        ItemStack standard = Fire.createRegular();
        this.firstTimeRewards.put("fire", firstTime);
        this.standardResults.put("fire", standard);
        registerRecipeFromConfig("fire", standard);
    }

    private void Ender() {
        ItemStack firstTime = Ender.createAugmented();
        ItemStack standard = Ender.createRegular();
        this.firstTimeRewards.put("end_first", firstTime);
        this.standardResults.put("end_second", standard);

        registerRecipeFromConfig("end_first", firstTime);
        registerRecipeFromConfig("end_second", standard);
    }

    private void Frost() {
        ItemStack firstTime = Frost.createAugmented();
        ItemStack standard = Frost.createRegular();
        this.firstTimeRewards.put("frost", firstTime);
        this.standardResults.put("frost", standard);
        registerRecipeFromConfig("frost", standard);
    }

    private void Haste() {
        ItemStack firstTime = Haste.createAugmented();
        ItemStack standard = Haste.createRegular();
        this.firstTimeRewards.put("haste", firstTime);
        this.standardResults.put("haste", standard);
        registerRecipeFromConfig("haste", standard);
    }

    private void Heart() {
        ItemStack firstTime = Heart.createAugmented();
        ItemStack standard = Heart.createRegular();
        this.firstTimeRewards.put("heart", firstTime);
        this.standardResults.put("heart", standard);
        registerRecipeFromConfig("heart", standard);
    }

    private void Invis() {
        ItemStack firstTime = Invisibility.createAugmented();
        ItemStack standard = Invisibility.createRegular();
        this.firstTimeRewards.put("invis", firstTime);
        this.standardResults.put("invis", standard);
        registerRecipeFromConfig("invis", standard);
    }

    private void Ocean() {
        ItemStack firstTime = Ocean.createAugmented();
        ItemStack standard = Ocean.createRegular();
        this.firstTimeRewards.put("ocean", firstTime);
        this.standardResults.put("ocean", standard);
        registerRecipeFromConfig("ocean", standard);
    }

    private void Regen() {
        ItemStack firstTime = Regen.createAugmented();
        ItemStack standard = Regen.createRegular();
        this.firstTimeRewards.put("regen", firstTime);
        this.standardResults.put("regen", standard);
        registerRecipeFromConfig("regen", standard);
    }

    private void Speed() {
        ItemStack firstTime = Speed.createAugmented();
        ItemStack standard = Speed.createRegular();
        this.firstTimeRewards.put("speed", firstTime);
        this.standardResults.put("speed", standard);
        registerRecipeFromConfig("speed", standard);
    }

    private void Strength() {
        ItemStack firstTime = Strength.createAugmented();
        ItemStack standard = Strength.createRegular();
        this.firstTimeRewards.put("strength", firstTime);
        this.standardResults.put("strength", standard);
        registerRecipeFromConfig("strength", standard);
    }

    private void Thunder() {
        ItemStack firstTime = Thunder.createAugmented();
        ItemStack standard = Thunder.createRegular();
        this.firstTimeRewards.put("thunder", firstTime);
        this.standardResults.put("thunder", standard);
        registerRecipeFromConfig("thunder", standard);
    }

    private void Apophis() {
        ItemStack firstTime = Apophis.createAugmented();
        ItemStack standard = Apophis.createRegular();
        this.firstTimeRewards.put("apophis", firstTime);
        this.standardResults.put("apophis", standard);
        registerRecipeFromConfig("apophis", standard);
    }

    private void Thief() {
        ItemStack firstTime = Thief.createAugmented();
        ItemStack standard = Thief.createRegular();
        this.firstTimeRewards.put("thief", firstTime);
        this.standardResults.put("thief", standard);
        registerRecipeFromConfig("thief", standard);
    }
}