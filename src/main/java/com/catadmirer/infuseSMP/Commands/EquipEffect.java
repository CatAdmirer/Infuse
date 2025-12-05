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
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore() && Infuse.getInstance().<Boolean>getConfig("join_effects_enabled")) {
            List<String> effects = Infuse.getInstance().getConfig("join_effects");
            if (effects.isEmpty()) return;
            String chosenKey = effects.get(new Random().nextInt(effects.size()));
            String effectName = Infuse.getInstance().getEffectName(chosenKey);
            if (effectName == null) return;
            equipEffect(player, effectName, "2");
        }
    }

    public void drainSecondaryEffect(Player player, ItemStack item, String effectName) {
        if (!this.equipEffect(player, effectName, "1") && !this.equipEffect(player, effectName, "2")) {
            player.performCommand("rdrain");
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugins()[0], () -> {
                this.equipEffect(player, effectName, "2");
            }, 1L);
        }

    }

    private boolean equipEffect(Player player, String effectName, String type) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (currentEffect != null) {
            return false;
        } else {
            Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), type, effectName);
            effectName = applyHexColors(effectName);
            player.sendMessage("§aYou have equipped " + effectName);
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
            EffectMapping effect = EffectMapping.fromItem(mainHandItem);
            if (effect != null) {
                if (player.getInventory().firstEmpty() == -1) {
                    event.setCancelled(true);
                    player.sendMessage("§cYour inventory is full! Make space before unequipping.");
                } else {
                    this.drainSecondaryEffect(player, mainHandItem, effect.getEffectName());
                    this.consumeMainHandItem(player);
                    String effectName = effect.getEffectName();
                    if (effectName.equalsIgnoreCase("§5Apohpis Effect") ||
                            effectName.equalsIgnoreCase("§5Augmented Apohpis Effect")) {
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
        String effect1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
        String effect2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
        String dropMode = Infuse.getInstance().getConfig().getString("effect_drops", "Random");
        Random rand = new Random();
        switch (dropMode.toLowerCase()) {
            case "1":
                if (effect1 != null) {
                    this.dropEffectOnDeath(player, "1");
                }
                break;

            case "2":
                if (effect2 != null) {
                    this.dropEffectOnDeath(player, "2");
                }
                break;

            case "none":
                break;

            case "random":
            default:
                if (effect1 != null && effect2 != null) {
                    String selectedEffect = rand.nextBoolean() ? "1" : "2";
                    this.dropEffectOnDeath(player, selectedEffect);
                } else if (effect1 != null) {
                    this.dropEffectOnDeath(player, "1");
                } else if (effect2 != null) {
                    this.dropEffectOnDeath(player, "2");
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

    private void dropEffectOnDeath(Player player, String type) {
        String effectName = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), type);
        if (effectName != null) {
            Infuse.getInstance().getEffectManager().removeEffect(player.getUniqueId(), type);
            EffectMapping effect = EffectMapping.fromEffectName(effectName);
            if (effect != null) {
                ItemStack item = effect.createItem();
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        } else {
            String effect1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
            String effect2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
            if (effect1 == null && effect2 == null) {
                player.sendMessage("§cYou do not have any effects equipped to swap.");
                return true;
            } else {
                Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), "1", effect2);
                Infuse.getInstance().getEffectManager().setEffect(player.getUniqueId(), "2", effect1);
                player.sendMessage("§aYour Effects have been swapped.");
                return true;
            }
        }
    }
}
