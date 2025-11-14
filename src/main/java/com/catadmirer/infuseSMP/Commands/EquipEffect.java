package com.catadmirer.infuseSMP.Commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.ApophisManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EquipEffect implements Listener, CommandExecutor {
    private ApophisManager apophisCommand;

    public EquipEffect(ApophisManager apophisCommand) {
        this.apophisCommand = apophisCommand;
    }

    @EventHandler
    public void onFIrstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore() && Infuse.getInstance().<Boolean>getCanfig("join_effects_enabled")) {
            List<String> effects = Infuse.getInstance().getCanfig("join_effects");
            if (effects.isEmpty()) return;
            String chosenKey = effects.get(new Random().nextInt(effects.size()));
            String effectName = Infuse.getInstance().getEffect(chosenKey);
            if (effectName == null) return;
            equipHack(player, effectName, "2");
        }
    }

    public void handleLegendaryHack(Player player, ItemStack item, String hackName) {
        if (!this.equipHack(player, hackName, "1") && !this.equipHack(player, hackName, "2")) {
            player.performCommand("rdrain");
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugins()[0], () -> {
                this.equipHack(player, hackName, "2");
            }, 1L);
        }

    }

    private boolean equipHack(Player player, String hackName, String type) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (currentHack != null) {
            return false;
        } else {
            Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), type, hackName);
            String var10001 = String.valueOf(ChatColor.GREEN);
            hackName = applyHexColors(hackName);
            player.sendMessage(var10001 + "You have equipped " + hackName);
            this.consumeMainHandItem(player);
            return true;
        }
    }

    public static String applyHexColors(String input) {
        String regex = "(#(?:[0-9a-fA-F]{6}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String colorCode = ChatColor.of(hexCode).toString();
            matcher.appendReplacement(result, colorCode);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem != null && mainHandItem.getType() != Material.AIR) {
            EffectMapping hackMapping = EffectMapping.fromItem(mainHandItem);
            if (hackMapping != null) {
                if (player.getInventory().firstEmpty() == -1) {
                    event.setCancelled(true);
                    player.sendMessage(String.valueOf(ChatColor.RED) + "Your inventory is full! Make space before unequipping.");
                } else {
                    this.handleLegendaryHack(player, mainHandItem, hackMapping.getEffectName());
                    this.consumeMainHandItem(player);
                    String hackName = hackMapping.getEffectName();
                    if (hackName.equalsIgnoreCase(ChatColor.DARK_PURPLE + "Apohpis Effect") ||
                            hackName.equalsIgnoreCase(ChatColor.DARK_PURPLE + "Augmented Apohpis Effect")) {
                        apophisCommand.disguiseAsApophis(player);
                    }
                }
            }

        }
    }

    private void consumeMainHandItem(Player player) {
        Bukkit.getScheduler().runTaskLater(Infuse.getInstance(), () -> {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (mainHandItem != null && mainHandItem.getType() != Material.AIR) {
                if (mainHandItem.getAmount() > 1) {
                    mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand((ItemStack)null);
                }

            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String hack1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
        String hack2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
        String dropMode = Infuse.getInstance().getConfig().getString("effect_drops", "Random");
        Random rand = new Random();
        switch (dropMode.toLowerCase()) {
            case "1":
                if (hack1 != null) {
                    this.dropHackOnDeath(player, "1");
                }
                break;

            case "2":
                if (hack2 != null) {
                    this.dropHackOnDeath(player, "2");
                }
                break;

            case "none":
                break;

            case "random":
            default:
                if (hack1 != null && hack2 != null) {
                    String selectedHack = rand.nextBoolean() ? "1" : "2";
                    this.dropHackOnDeath(player, selectedHack);
                } else if (hack1 != null) {
                    this.dropHackOnDeath(player, "1");
                } else if (hack2 != null) {
                    this.dropHackOnDeath(player, "2");
                }
                break;
        }
        File disguiseFile = new File(
                Infuse.getInstance().getDataFolder(),
                "AphopisPlayers/" + player.getUniqueId() + ".yml"
        );

        if (disguiseFile.exists()) {
            disguiseFile.delete();
            Infuse.getInstance().resetSkinWithoutKick(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File disguiseFile = new File(Infuse.getInstance().getDataFolder(), "AphopisPlayers/" + player.getUniqueId() + ".yml");
        if (disguiseFile.exists()) {
            apophisCommand.disguiseAsApophis(player);
        }
    }

    private void dropHackOnDeath(Player player, String type) {
        String hackName = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (hackName != null) {
            Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), type);
            EffectMapping hackMapping = EffectMapping.fromEffectName(hackName);
            if (hackMapping != null) {
                ItemStack item = hackMapping.createItem();
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "Only players can use this command.");
            return true;
        } else {
            String hack1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
            String hack2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
            if (hack1 == null && hack2 == null) {
                player.sendMessage(String.valueOf(ChatColor.RED) + "You do not have any effects equipped to swap.");
                return true;
            } else {
                Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), "1", hack2);
                Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), "2", hack1);
                player.sendMessage(String.valueOf(ChatColor.GREEN) + "Your Effects have been swapped.");
                return true;
            }
        }
    }
}
