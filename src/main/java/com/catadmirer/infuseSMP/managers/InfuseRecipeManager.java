package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.inventories.StationSelectionMenu;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

public class InfuseRecipeManager implements Listener {
    private final Infuse plugin;
    private final Map<String, ItemStack> firstTimeRewards;
    private final Map<String, ItemStack> standardResults;
    private final FileConfiguration recipesConfig;

    private BossBar ritualBossBar;

    public InfuseRecipeManager(Infuse plugin) {
        this.plugin = plugin;
        this.firstTimeRewards = new HashMap<>();
        this.standardResults = new HashMap<>();

        File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false);
        }

        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);

        ConfigurationSection recipesSection = recipesConfig.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return;
        }

        // Loading the recipes from the config.
        for (String recipeKey : recipesSection.getKeys(false)) {
            // Making sure the recipe is enabled
            boolean enabled = recipesConfig.getBoolean("recipes." + recipeKey + ".enabled", false);
            if (!enabled) {
                plugin.getLogger().info("Recipe " + recipeKey + " is disabled in config, skipping.");
                continue;
            }

            EffectMapping mapping = EffectMapping.fromEffectKey(recipeKey);

            // Handling the augmented ender effect
            if (mapping != null && mapping.isAugmented()) {
                registerRecipeFromConfig("aug_ender", EffectMapping.AUG_ENDER.createItem());
                continue;
            }

            firstTimeRewards.put(recipeKey, mapping.augmented().createItem());
            standardResults.put(recipeKey, mapping.createItem());
            registerRecipeFromConfig(recipeKey, mapping.createItem());
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.ritualBossBar == null)
            return;
        event.getPlayer().showBossBar(ritualBossBar);
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

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack craftedItem = event.getInventory().getResult();
        EffectMapping effect = EffectMapping.fromItem(craftedItem);

        // Making sure the item being crafted is an Infuse effect and not crafting it if
        // it isn't
        if (effect == null) return;

        // Not allowing the player to shift click effects
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        // Making sure the brewing stand is still placed
        Location brewerLocation = event.getInventory().getLocation();
        if (brewerLocation.getBlock().getType() != Material.BREWING_STAND) {
            event.setCancelled(true);
            return;
        }

        HumanEntity player = event.getWhoClicked();

        // Checking craft limits
        int craftLimit = plugin.getMainConfig().getCraftLimit(effect);
        int numCrafted = plugin.getDataManager().getCrafted(effect);

        if (numCrafted == craftLimit) {
            event.setCancelled(true);
            return;
        }
        // Incrementing the number of effects crafted.
        plugin.getDataManager().setCrafted(effect, numCrafted + 1);
        Component itemName = Messages.toComponent(effect.getName());

        // If the effect is not augmented, just let the item be crafted
        if (!effect.isAugmented())  {
            event.setCancelled(true);
            CraftingInventory inv = event.getInventory();
            ItemStack[] matrix = inv.getMatrix();

            for(int i = 0; i < matrix.length; ++i) {
                if (matrix[i] != null && matrix[i].getType() != Material.AIR) {
                    int newAmt = matrix[i].getAmount() - 1;
                    matrix[i] = newAmt > 0 ? new ItemStack(matrix[i].getType(), newAmt) : null;
                }
            }

            inv.setMatrix(matrix);
            player.closeInventory();
            brewerLocation.getWorld().dropItemNaturally(brewerLocation, craftedItem);
            if (plugin.getMainConfig().regularBroadcast()) {
                Environment worldEnv = brewerLocation.getWorld().getEnvironment();
                String worldName = switch(worldEnv) {
                    case NORMAL -> "<green><b>Overworld";
                    case NETHER -> "<dark_red><b>Nether";
                    case THE_END -> "<dark_purple><b>End";
                    default -> "<gray>" + brewerLocation.getWorld().getName();
                };

                String x = String.valueOf(brewerLocation.getBlockX());
                String y = String.valueOf(brewerLocation.getBlockY());
                String z = String.valueOf(brewerLocation.getBlockZ());

                String formattedMessage = Messages.REGULAR_BROADCAST.getMessage()
                        .replace("%player%", player.getName())
                        .replace("%item%", MiniMessage.miniMessage().serialize(itemName))
                        .replace("%x%", x)
                        .replace("%y%", y)
                        .replace("%z%", z)
                        .replace("%dimension%", worldName);
                Bukkit.broadcast(Messages.toComponent(formattedMessage));
            }
            return;
        }

        // Clearing the ingredients
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();


        for(int i = 0; i < matrix.length; ++i) {
            if (matrix[i] != null && matrix[i].getType() != Material.AIR) {
                int newAmt = matrix[i].getAmount() - 1;
                matrix[i] = newAmt > 0 ? new ItemStack(matrix[i].getType(), newAmt) : null;
            }
        }

        inv.setMatrix(matrix);

        // Closing the inventory
        player.closeInventory();

        // Cancelling the event
        event.setCancelled(true);

        // Starting the ritual for the augmented effect
        // Making sure there isn't a ritual active already
        if (ritualBossBar != null) {
            player.sendMessage(Messages.ERROR_RITUAL_ACTIVE.toComponent());
            event.setCancelled(true);
            return;
        }

        // Creating the bossbar
        this.ritualBossBar = BossBar.bossBar(MiniMessage.miniMessage()
                .deserialize("🧪 <b>" + effect.getName() + "</b><reset> 🧪").color(itemName.color()), 1.0f,
                effect.getRitualColor(), BossBar.Overlay.PROGRESS);

        // Adding every player online to the bossbar
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(ritualBossBar);
        }

        // Getting the duration of the ritual
        int ritualDuration;
        if (effect == EffectMapping.AUG_ENDER) {
            ritualDuration = plugin.getMainConfig().ritualDurationEnder();
        } else {
            ritualDuration = plugin.getMainConfig().ritualDuration();
        }

        // Spawning the ender crystal if the config allows
        if (plugin.getMainConfig().ritualBeacon()) {
            Location startLoc = brewerLocation.clone().add(0.5, 0, 0.5);
            startLoc.setY(-100);
            Location targetLoc = brewerLocation.clone().add(0.5, 0, 0.5);
            targetLoc.setY(500);
            
            EnderCrystal crystal = (EnderCrystal) brewerLocation.getWorld().spawnEntity(startLoc, EntityType.END_CRYSTAL);
            crystal.setShowingBottom(false);
            crystal.setInvulnerable(true);
            crystal.setInvisible(true);
            crystal.setBeamTarget(targetLoc);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // TODO: Test if i need to remove beam or not
                crystal.setBeamTarget(null);
                crystal.remove();
            }, ritualDuration * 20L);
        }

        Environment worldEnv = brewerLocation.getWorld().getEnvironment();
        String worldName = switch(worldEnv) {
            case NORMAL -> "<green><b>Overworld";
            case NETHER -> "<dark_red><b>Nether";
            case THE_END -> "<dark_purple><b>End";
            default -> "<gray>" + brewerLocation.getWorld().getName();
        };

        String x = String.valueOf(brewerLocation.getBlockX());
        String y = String.valueOf(brewerLocation.getBlockY());
        String z = String.valueOf(brewerLocation.getBlockZ());

        String formattedMessage = Messages.EFFECT_BROADCAST.getMessage()
                .replace("%player%", player.getName())
                .replace("%item%", MiniMessage.miniMessage().serialize(itemName))
                .replace("%x%", x)
                .replace("%y%", y)
                .replace("%z%", z)
                .replace("%dimension%", worldName);

        String formattedDiscordMessage = Messages.DISCORD_BROADCAST.getMessage()
                .replace("%player%", player.getName())
                .replace("%item%", PlainTextComponentSerializer.plainText().serialize(itemName))
                .replace("%x%", x)
                .replace("%y%", y)
                .replace("%z%", z)
                .replace("%dimension%", MiniMessage.miniMessage().stripTags(worldName));

        // Broadcasting that the ritual has started
        Bukkit.broadcast(Messages.toComponent(formattedMessage));
        if (plugin.getMainConfig().enableDiscordBroadcasts()) {
            String webhookUrl = plugin.getMainConfig().discordWebhookUrl();
            if (webhookUrl != null && !webhookUrl.isEmpty()) {
                sendToDiscord(webhookUrl, formattedDiscordMessage);
            }
        }

        // Preventing the brewing stand from being broken
        ImmortalBrewerListeners brewerListeners = new ImmortalBrewerListeners(brewerLocation);
        Bukkit.getPluginManager().registerEvents(brewerListeners, plugin);
        // Starting the ritual progress bar
        new BukkitRunnable() {
            float progress = 1.0f;
            final double progressDecrement = 1.0 / (ritualDuration * 20.0);
            @Override
            public void run() {
                if (ritualBossBar == null) {
                    cancel();
                    return;
                }
                progress -= progressDecrement;
                if (progress <= 0) {
                    ritualBossBar.progress(0);
                    cancel();
                    return;
                }
                ritualBossBar.progress(progress);
            }

        }.runTaskTimer(this.plugin, 0, 1);
        // Scheduling the task that ends and cleans up the ritual
        Bukkit.getScheduler().runTaskLater(plugin, task -> {
            // Removing the bossbar from view
            ritualBossBar.viewers().forEach(viewer -> {
                if (viewer instanceof Audience audience) {
                    audience.hideBossBar(ritualBossBar);
                }
            });

            // Allowing the brewing stand to be broken
            HandlerList.unregisterAll(brewerListeners);

            // Broadcasting that the effect has been brewed
            String msg = Messages.EFFECT_FINISHED.getMessage();
            msg = msg.replace("%item%", MiniMessage.miniMessage().serialize(itemName));
            Bukkit.broadcast(Messages.toComponent(msg));

            // Dropping the item
            brewerLocation.getWorld().dropItemNaturally(brewerLocation, effect.createItem());
            ritualBossBar = null;
            task.cancel();
        }, ritualDuration * 20L);
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
        if (result.getType() == Material.POTION) {
            event.setCancelled(true);
        }
    }

    // TODO: fix this
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        // Ignoring non-infuse items
        if (event.getRecipe() == null) return;
        ItemStack item = event.getRecipe().getResult();
        EffectMapping effect = EffectMapping.fromItem(item);
        if (effect == null) return;
        // Checking the limits for the effect
        EffectMapping augForm = effect.augmented();
        int augLimit = plugin.getMainConfig().getCraftLimit(augForm);
        int augCrafted = plugin.getDataManager().getCrafted(augForm);
        if (augLimit > augCrafted) {
            event.getInventory().setResult(effect.augmented().createItem());
            return;
        }
        int regLimit = plugin.getMainConfig().getCraftLimit(effect);
        int regCrafted = plugin.getDataManager().getCrafted(effect);
        if (regLimit > regCrafted) {
            event.getInventory().setResult(effect.createItem());
            return;
        }
        
        event.getInventory().setResult(null);
    }

    public static final Component effectCraftingMenu = Component.text("Effect Crafting");

    @EventHandler
    public void onBrewingStandInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;
        if (block.getType() != Material.BREWING_STAND)
            return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        if (plugin.getMainConfig().brewingGui()) {
            player.openInventory(new StationSelectionMenu(block.getLocation()).getInventory());
        } else {
            // Opening the menu for crafting effects
            MenuType.CRAFTING.builder().location(block.getLocation()).title(effectCraftingMenu).build(player).open();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getHolder() instanceof StationSelectionMenu menu) {
            event.setCancelled(true);
            HumanEntity player = event.getWhoClicked();

            // Making sure the block is still a brewing stand
            Block block = menu.getStandLocation().getBlock();
            if (block.getType() != Material.BREWING_STAND)
                return;

            if (event.getSlot() == 11) {
                // Closing the StationSelectionMenu
                player.closeInventory();

                // Opening the menu for crafting effects
                MenuType.CRAFTING.builder().location(block.getLocation()).title(effectCraftingMenu).build(player).open();
            } else if (event.getSlot() == 15) {
                // Closing the StationSelectionMenu
                player.closeInventory();

                // Opening the brewing stand
                BrewingStand data = (BrewingStand) block.getState();
                player.openInventory(data.getInventory());
            }
        }
    }

    public static class ImmortalBrewerListeners implements Listener {
        private final Location brewerLocation;

        public ImmortalBrewerListeners(Location brewerLocation) {
            this.brewerLocation = brewerLocation;
        }

        @EventHandler
        public void onBrewingStandBreak(BlockBreakEvent event) {
            if (event.getBlock().getLocation().equals(brewerLocation)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onBrewingStandExplode(EntityExplodeEvent event) {
            List<Block> blocks = event.blockList();
            for (Block block : blocks) {
                if (block.getLocation().equals(brewerLocation)) {
                    blocks.remove(block);
                }
            }
        }
    }
}