package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import com.catadmirer.infuseSMP.inventories.StationSelectionMenu;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

public class InfuseRecipeManager implements Listener {
    private final Infuse plugin;
    private final Map<String, ItemStack> firstTimeRewards;
    private final Map<String, ItemStack> standardResults;

    private net.kyori.adventure.bossbar.BossBar activeBossBar;
    private Location brewingStandLocation;

    FileConfiguration recipesConfig;
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

        for (EffectMapping mapping : EffectMapping.values()) {
            // Preventing double loading
            if (mapping.isAugmented()) continue;

            String recipeKey = mapping.getKey();
            boolean enabled = recipesSection.getBoolean(recipeKey + ".enabled", false);
            if (!enabled) {
                plugin.getLogger().info("Recipe " + recipeKey + " is disabled in config, skipping2.");
                continue;
            }

            firstTimeRewards.put(recipeKey, mapping.augmented().createItem());
            standardResults.put(recipeKey, mapping.createItem());
            registerRecipeFromConfig(recipeKey, mapping.createItem());
        }

        // Specifically loading the aug ender effect
        boolean enabled = recipesSection.getBoolean("aug_ender.enabled", false);
        if (!enabled) {
            plugin.getLogger().info("Recipe aug_ender is disabled in config, skipping.");
            return;
        }
        registerRecipeFromConfig("aug_ender", EffectMapping.AUG_ENDER.createItem());
    }

    private void spawnCustomBeam(Location brewingStandLocation, String recipeKey) {
        if (plugin.getConfigFile().brewingParticles()) {
            World world = brewingStandLocation.getWorld();
            Location crystalLoc = new Location(world, brewingStandLocation.getX(), -5000, brewingStandLocation.getZ());
            final EnderCrystal crystal = (EnderCrystal) world.spawnEntity(crystalLoc, EntityType.END_CRYSTAL);
            crystal.setShowingBottom(false);
            crystal.setInvulnerable(true);
            crystal.setInvisible(true);
            Location targetLoc = brewingStandLocation.clone().add(0, 600, 0);
            final ArmorStand marker = (ArmorStand) world.spawnEntity(targetLoc, EntityType.ARMOR_STAND);
            marker.setMarker(true);
            marker.setInvisible(true);
            marker.setInvulnerable(true);
            marker.setSilent(true);
            marker.setCustomNameVisible(false);
            crystal.setBeamTarget(marker.getLocation().toBlockLocation());
            int ritualDuration;
            if (recipeKey.equalsIgnoreCase("aug_ender")) {
                ritualDuration = plugin.getConfigFile().ritualDurationEnder();
            } else {
                ritualDuration = plugin.getConfigFile().ritualDuration();
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
        if (this.activeBossBar == null) return;
        event.getPlayer().showBossBar(activeBossBar);
    }

    private void startRitual(Player player, String recipeKey, Location playerLocation, final ItemStack craftedItem) {
        if (isRitualActive) {
            player.sendMessage(Messages.ERROR_RITUAL_ACTIVE.toComponent());
            return;
        }
        brewingStandLocation = this.findNearestBrewingStand(playerLocation);
        if (brewingStandLocation == null) {
            player.sendMessage(Messages.EFFECT_NOBREWING.toComponent());
            return;
        }

        isRitualActive = true;

        Component itemName = craftedItem.getItemMeta().displayName();
        TextColor itemColor = itemName.color();
        Component formattedItemName = Component.text("🧪 ", itemColor, TextDecoration.BOLD).append(itemName).append(Component.text(" 🧪"));

        BossBar.Color barColor = EffectMapping.fromEffectKey(recipeKey).getRitualColor();
        this.activeBossBar = BossBar.bossBar(formattedItemName, 1.0f, barColor, BossBar.Overlay.PROGRESS);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(activeBossBar);
        }

        String worldName = brewingStandLocation.getWorld().getName();
        String dimensionMessage;
        if (worldName.equalsIgnoreCase("world")) {
            dimensionMessage = "<green>Overworld";
        } else if (worldName.equalsIgnoreCase("world_end") || worldName.equalsIgnoreCase("world_the_end")) {
            dimensionMessage = "<dark_purple>End";
        } else if (worldName.equalsIgnoreCase("world_nether") || worldName.equalsIgnoreCase("world_the_nether")) {
            dimensionMessage = "<dark_red>Nether";
        } else {
            dimensionMessage = "<gray>" + worldName;
        }

        String messageTemplate = Messages.EFFECT_BROADCAST.getMessage();
        String discordTemplate = Messages.DISCORD_BROADCAST.getMessage();

        String x = String.valueOf(brewingStandLocation.getBlockX());
        String y = String.valueOf(brewingStandLocation.getBlockY());
        String z = String.valueOf(brewingStandLocation.getBlockZ());

        String formattedMessage = messageTemplate
                .replace("%player%", player.getName())
                .replace("%item%", MiniMessage.miniMessage().serialize(itemName))
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

        Bukkit.broadcast(Messages.toComponent(formattedMessage));
        if (plugin.getConfigFile().enableDiscordBroadcasts()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast " + formattedDiscordMessage);
        }
        String webhookUrl = plugin.getConfigFile().discordWebhookUrl();
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            sendToDiscord(webhookUrl, formattedDiscordMessage);
        }

        this.spawnCustomBeam(brewingStandLocation, recipeKey);
        int ritualDuration;

        if (recipeKey.equalsIgnoreCase("aug_ender")) {
            ritualDuration = plugin.getConfigFile().ritualDurationEnder();
        } else {
            ritualDuration = plugin.getConfigFile().ritualDuration();
        }

        new BukkitRunnable() {

            float progress = 1.0F;
            final double progressDecrement = 1.0 / (ritualDuration * 20.0);

            @Override
            public void run() {
                if (activeBossBar == null) { cancel(); return; }
                progress -= progressDecrement;
                activeBossBar = activeBossBar.progress(progress);

                if (progress == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.hideBossBar(activeBossBar);
                    }
                    String msg = Messages.EFFECT_FINISHED.getMessage();
                    msg = msg.replace("%item%", MiniMessage.miniMessage().serialize(itemName));
                    Bukkit.broadcast(Messages.toComponent(msg));
                    brewingStandLocation.getWorld().dropItemNaturally(brewingStandLocation, craftedItem);
                    isRitualActive = false;
                    activeBossBar = null;
                    cancel();
                }
            }
        }.runTaskTimer(this.plugin, 0, 1);
    }

    private void sendToDiscord(String webhookUrl, String message) {
        String payload = "{\"content\": \"" + message + "\"}";

        HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(payload)).build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<Void> response = client.send(request, BodyHandlers.discarding());

            // Checking the response status code
            int status = response.statusCode();
            if (status == 200) {
                plugin.getLogger().info("Message sent to Discord!");
            } else {
                plugin.getLogger().info("Error sending message to Discord: " + status);
            }
        } catch (IOException err) {
            plugin.getLogger().log(Level.SEVERE, "Could not send webhook message to discord.", err);
        } catch (InterruptedException err) {
            plugin.getLogger().log(Level.SEVERE, "Discord webhook request was interrupted!", err);
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
                            nearestLocation = checkLocation.setRotation(0, 0).toBlockLocation();
                        }
                    }
                }
            }
        }

        return nearestLocation;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getRecipe() instanceof ShapedRecipe shaped)) return;
        String recipeKey = shaped.getKey().getKey();
        if (!firstTimeRewards.containsKey(recipeKey) && !standardResults.containsKey(recipeKey)) return;
        Player player = (Player) event.getWhoClicked();
        if (recipeKey.equals("aug_ender")) {
            int endFirstAugLimit = plugin.getConfigFile().augmentedLimit("aug_ender.augmented_limit");
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
        int augmentedLimit = plugin.getConfigFile().augmentedLimit(recipeKey);
        int regularLimit = plugin.getConfigFile().regularLimit(recipeKey);

        ItemStack result = event.getCurrentItem();
        if (result == null) {
            event.setCancelled(true);
            return;
        }

        ItemStack augmentedItem = firstTimeRewards.get(recipeKey);

        boolean isAugmented = result.isSimilar(augmentedItem);

        if (isAugmented) {
            if (isRitualActive) {
                player.sendMessage(Messages.ERROR_RITUAL_ACTIVE.toComponent());
                event.setCancelled(true);
                return;
            }
            if (augmentedLimit <= 0 && !(augmentedLimit == -1)) {
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
            if (!(augmentedLimit == -1)) {
                plugin.getConfig().set("craft_limits." + recipeKey + ".augmented_limit", augmentedLimit - 1);
                plugin.saveConfig();
            }

            this.startRitual(player, recipeKey, player.getLocation(), augmentedItem);
            event.setCancelled(true);

        } else {
            if (regularLimit <= 0 && !(regularLimit == -1)) {
                event.setCancelled(true);
                return;
            }
            if (!(regularLimit == -1)) {
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

        Number augmentedLimitNumber = plugin.getConfigFile().augmentedLimit(recipeKey);
        Number regularLimitNumber = plugin.getConfigFile().regularLimit(recipeKey);

        if (augmentedLimitNumber == null || regularLimitNumber == null) {
            event.getInventory().setResult(null);
            return;
        }

        int augmentedLimit = augmentedLimitNumber.intValue();
        int regularLimit = regularLimitNumber.intValue();

        if (augmentedLimit > 0 || augmentedLimit == -1) {
            event.getInventory().setResult(firstTimeRewards.get(recipeKey));
        } else if (regularLimit > 0 || regularLimit == -1) {
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
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BREWING_STAND) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (plugin.getConfigFile().brewingGui()) {
                Block block = event.getClickedBlock();
                BrewingStand stand = (BrewingStand) block.getState();
                brewingStandCache.put(player.getUniqueId(), stand);

                player.openInventory(new StationSelectionMenu().getInventory());
            } else {
                MenuType.CRAFTING.create(player).open();
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory().getHolder() instanceof StationSelectionMenu) {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            if (event.getSlot() == 11) {
                player.closeInventory();
                MenuType.CRAFTING.create(player).open();
            } else if (event.getSlot() == 15) {
                player.closeInventory();
                BrewingStand stand = brewingStandCache.get(player.getUniqueId());
                if (stand != null) {
                    player.openInventory(stand.getInventory());
                }
            }
        }
    }

    @EventHandler
    public void onBrewingStandBreak(BlockBreakEvent event) {
        if (event.getBlock().getLocation().equals(brewingStandLocation)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBrewingStandExplode(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            if (block.getLocation().equals(brewingStandLocation)) {
                blocks.remove(block);
            }
        }
    }
}