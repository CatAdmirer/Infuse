package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.events.EffectEquipEvent;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.destroystokyo.paper.MaterialSetTag;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Frost implements Listener {

    private final static Set<UUID> frozenAttackers = new HashSet<>();
    private final static Set<Location> powdered = new HashSet<>();
    private static FixedMetadataValue blockData = null;

    private static Infuse plugin;

    public Frost(Infuse plugin) {
        Frost.plugin = plugin;

        if (blockData != null) return;
        blockData = new FixedMetadataValue(plugin, 0);
    }

    public static void applyPassiveEffects(Player player) {
        if (plugin.getDataManager().hasEffect(player, EffectMapping.FROST) && !(player.getVelocity().lengthSquared() < 0.01)) {
            if (player.isInPowderedSnow()) {
                player.setGliding(true);
            }

            Material blockType = player.getLocation().subtract(0, 1, 0).getBlock().getType();
            if (MaterialSetTag.ICE.isTagged(blockType)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
            }
        }
    }

    public void changeToSnow(Player player) {
        final int frostSnowRadius = plugin.getMainConfig().frostPassiveSnowChangingRadius();
        Location center = player.getLocation();

        for (int dx = -frostSnowRadius; dx <= frostSnowRadius; dx++) {
            for (int dy = -frostSnowRadius; dy <= frostSnowRadius; dy++) {
                for (int dz = -frostSnowRadius; dz <= frostSnowRadius; dz++) {
                    // Getting the block in the radius
                    Block powderSnowBlock = center.toBlockLocation().add(dx, dy, dz).getBlock();

                    // Skipping non-powdered snow blocks
                    if (powderSnowBlock.getType() != Material.POWDER_SNOW) continue;

                    // Skipping if there is a block above this one
                    if (powderSnowBlock.getRelative(BlockFace.UP).getType() != Material.AIR) continue;

                    // Changing the block to regular snow
                    powderSnowBlock.setType(Material.SNOW_BLOCK);
                    
                    powdered.add(powderSnowBlock.getLocation());
                    Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                        // Skipping if the player is too close to the block
                        if (powderSnowBlock.getLocation().distance(player.getLocation()) <= frostSnowRadius) return;

                        // Resetting the block to powdered snow
                        powderSnowBlock.setType(Material.POWDER_SNOW);
                        powdered.remove(powderSnowBlock.getLocation());
                        task.cancel();
                    }, 10, 10);
                }
            }
        }
    }

    @EventHandler
    public void onShutdown(PluginDisableEvent event) {
        powdered.forEach(location -> location.getWorld().getBlockAt(location).setType(Material.POWDER_SNOW));
        powdered.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getDataManager().hasEffect(event.getPlayer(), EffectMapping.FROST)) return;
        changeToSnow(event.getPlayer());
    }

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (event.isGliding()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FROST)) return;

        if (player.isInPowderedSnow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FROST)) return;

        final boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        final Vector direction = player.getLocation().getDirection().normalize();
        if (inFrost && event.getFrom().distanceSquared(event.getTo()) > 0.01) {
            final double boostStrength = plugin.getMainConfig().frostPassiveWalkSpeed();
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        } else {
            changeToSnow(player);
        }
    }

    @EventHandler
    public void onPlayerInteractWithWindCharge(PlayerInteractEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WIND_CHARGE && event.getPlayer().getFreezeTicks() > 1) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTenthAttack(TenHitEvent event) {
        Infuse.LOGGER.debug("[Frost] Recieved TenHitEvent");
        Infuse.LOGGER.debug("[Frost] TenHitEvent Attacker: {}", event.getAttacker().getName());
        Infuse.LOGGER.debug("[Frost] TenHitEvent Target: {}", event.getTarget().getName());
        
        if (!plugin.getDataManager().hasEffect(event.getAttacker(), EffectMapping.FROST)) return;

        Infuse.LOGGER.debug("[Frost] Attacker has frost effect");

        (new BukkitRunnable() {
            int ticksElapsed = 0;
            final int freezeDuration = 200;

            public void run() {
                if (this.ticksElapsed >= freezeDuration) {
                    event.getTarget().setFreezeTicks(0);
                    this.cancel();
                } else {
                    int currentFreezeTicks = event.getTarget().getFreezeTicks();
                    event.getTarget().setFreezeTicks(currentFreezeTicks + 2);
                    this.ticksElapsed += 2;
                }
            }
        }).runTaskTimer(plugin, 0L, 2L);
    }

    public static void activateSpark(Boolean isAugmented, Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "frost")) return;

        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 300, 0));
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_FROST : EffectMapping.FROST);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_FROST : EffectMapping.FROST);

        CooldownManager.setTimes(playerUUID, "frost", duration, cooldown);

        Location center = caster.getLocation();
        final double radius = plugin.getMainConfig().frostAbilityRadius();
        World world = caster.getWorld();
        final Set<Player> affectedPlayers = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(caster) && !isTeammate(player, caster)
                    && player.getWorld().equals(world)
                    && player.getLocation().distance(center) <= radius) {
                affectedPlayers.add(player);
                AttributeInstance jumpAttribute = player.getAttribute(Attribute.JUMP_STRENGTH);
                if (jumpAttribute != null) {
                    jumpAttribute.setBaseValue(0.1);
                }
            }
        }

        frozenAttackers.add(caster.getUniqueId());

        new BukkitRunnable() {
            public void run() {
                for (Player player : affectedPlayers) {
                    AttributeInstance jumpAttribute = player.getAttribute(Attribute.JUMP_STRENGTH);
                    if (jumpAttribute != null) {
                        jumpAttribute.setBaseValue(0.42);
                    }
                }
                frozenAttackers.remove(caster.getUniqueId());
            }
        }.runTaskLater(plugin, duration * 20L);
    }


    @EventHandler
    public void onPlayerJoin(EffectEquipEvent event) {
        Player player = event.getPlayer();
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (jumpAttribute != null && jumpAttribute.getBaseValue() == 0.1) {
            jumpAttribute.setBaseValue(0.42);
        }
    }

    private static boolean isTeammate(Player player, Player caster) {
        return plugin.getDataManager().isTrusted(player, caster);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!attacker.hasPotionEffect(PotionEffectType.UNLUCK)) return;

        final PotionEffect effect = attacker.getPotionEffect(PotionEffectType.UNLUCK);
        if (effect == null || effect.getAmplifier() < 0) return;
        if (!(frozenAttackers.contains(attacker.getUniqueId())) || !(event.getEntity() instanceof Player)) return;

        event.getEntity().setFreezeTicks(200);
    }
}