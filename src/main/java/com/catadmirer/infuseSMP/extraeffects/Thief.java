package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class Thief implements Listener {
    private static Infuse plugin;

    private Map<UUID, UUID> shapeshiftedPlayers = new HashMap<>();
    private Map<UUID, BossBar> shapeshiftedBossBars = new HashMap<>();
    private Map<UUID, Integer> shapeshiftTimeLeft = new HashMap<>();

    public Thief(Infuse plugin) {
        Thief.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                        if (EffectMapping.THIEF.hasEffect(p)) {
                            otherPlayer.unlistPlayer(p);
                        } else {
                            if (otherPlayer.canSee(p)) {
                                otherPlayer.listPlayer(p);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "thief")) {
            active.add(playerUUID);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            
            // Applying cooldowns and durations for the effect
            boolean isAugmented = plugin.getDataManager().getEffect(playerUUID, "1").isAugmented() || plugin.getDataManager().getEffect(playerUUID, "2").isAugmented();
            long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_THIEF : EffectMapping.THIEF);
            long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_THIEF : EffectMapping.THIEF);

            CooldownManager.setDuration(playerUUID, "thief", duration);
            CooldownManager.setCooldown(playerUUID, "thief", cooldown);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (EffectMapping.THIEF.hasEffect(killer)) {
            shapeshiftedPlayers.put(killer.getUniqueId(), deadPlayer.getUniqueId());
            shapeshift(killer, deadPlayer);
            startShapeshiftTimer(killer);
        } else if (shapeshiftedPlayers.containsKey(deadPlayer.getUniqueId())) {
            revertShapeshift(deadPlayer);
        }
    }

    private void startShapeshiftTimer(Player killer) {
        int shapeshiftTime = 3600;
        BossBar bossBar = Bukkit.createBossBar("Shapeshift", BarColor.PINK, BarStyle.SOLID);
        bossBar.setProgress(1);
        bossBar.addPlayer(killer);
        shapeshiftedBossBars.put(killer.getUniqueId(), bossBar);
        shapeshiftTimeLeft.putIfAbsent(killer.getUniqueId(), shapeshiftTime);
        new BukkitRunnable() {
            @Override
            public void run() {
                Integer timeLeft = shapeshiftTimeLeft.get(killer.getUniqueId());

                if (timeLeft != null && timeLeft > 0) {
                    double progress = timeLeft / 3600;
                    bossBar.setProgress(progress);
                    shapeshiftTimeLeft.put(killer.getUniqueId(), timeLeft - 1);
                } else {
                    revertShapeshift(killer);
                    bossBar.removePlayer(killer);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void shapeshift(Player killer, Player deadPlayer) {
        killer.customName(deadPlayer.customName());
        killer.displayName(deadPlayer.displayName());
        killer.setCustomNameVisible(true);
        PlayerTextures skinTexture = deadPlayer.getPlayerProfile().getTextures();;
        PlayerProfile profile = Bukkit.createProfile(killer.getUniqueId(), killer.getName());
        profile.setTextures(skinTexture);
        killer.setPlayerProfile(profile);
    }

    private void revertShapeshift(Player player) {
        if (!shapeshiftedPlayers.containsKey(player.getUniqueId())) return;

        UUID originalUUID = shapeshiftedPlayers.get(player.getUniqueId());
        Player originalPlayer = Bukkit.getPlayer(originalUUID);

        if (originalPlayer != null) {
            player.customName(originalPlayer.customName());
            player.displayName(originalPlayer.displayName());
            PlayerTextures skinTexture = originalPlayer.getPlayerProfile().getTextures();
            PlayerProfile profile = Bukkit.createProfile(originalUUID, originalPlayer.getName());
            profile.setTextures(skinTexture);
            player.setPlayerProfile(profile);
        }

        shapeshiftedPlayers.remove(player.getUniqueId());
        shapeshiftedBossBars.remove(player.getUniqueId());
        shapeshiftTimeLeft.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (shapeshiftedPlayers.containsKey(player.getUniqueId())) {
            revertShapeshift(player);
        }
    }

    private final static Set<UUID> active = new HashSet<>();

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            if (!(event.getDamager() instanceof Player player)) return;
            UUID playerUUID = player.getUniqueId();
            if (EffectMapping.THIEF.hasEffect(player)) {
                if (active.contains(playerUUID)) {
                    EffectMapping effect1 = plugin.getDataManager().getEffect(victim.getUniqueId(), "1");
                    EffectMapping effect2 = plugin.getDataManager().getEffect(victim.getUniqueId(), "2");
                    
                    Random rand = new Random();
                    if (effect1 != null && effect2 != null) {
                        EffectMapping selectedEffect = rand.nextBoolean() ? effect1 : effect2;
                        activateEffect(player, selectedEffect, victim);
                        active.remove(playerUUID);
                    } else if (effect1 != null) {
                        activateEffect(player, effect1, victim);
                        active.remove(playerUUID);
                    } else if (effect2 != null) {
                        activateEffect(player, effect2, victim);
                        active.remove(playerUUID);
                    }
                }
            }
        }
    }

    private void activateEffect(Player player, @NotNull EffectMapping effect, Entity victim) {
        String msg = Messages.THIEF_STEAL.getMessage();
        msg = msg.replace("%player%", victim.getName());
        msg = msg.replace("%effect_name%", effect.getName());
        player.sendMessage(Messages.toComponent(msg));

        // Activating the stolen spark.
        effect.activateSpark(player);

        UUID playerUUID = player.getUniqueId();
        
        // Removing cooldowns from the stolen spark
        CooldownManager.clearSpecificCooldown(playerUUID, effect.regular().getKey());
        CooldownManager.clearSpecificDuration(playerUUID, effect.regular().getKey());
        
        // Applying cooldowns for the thief effect
        long cooldown = plugin.getConfigFile().cooldown(effect);
        long duration = plugin.getConfigFile().duration(effect);

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (CooldownManager.isEffectActive(player.getUniqueId(), "thief") && !event.isCritical()) {
                double originalDamage = event.getDamage();
                double critDamage = originalDamage * 1.35;
                event.setDamage(critDamage);
                Entity hitEntity = event.getEntity();
                hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                hitEntity.getWorld().spawnParticle(Particle.CRIT, hitEntity.getLocation().add(0, hitEntity.getHeight() / 2, 0), 10);
            }
        }
    }
}