package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InfuseDebug;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import java.util.EnumSet;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Frost extends InfuseEffect {
    private final static Set<UUID> frozenAttackers = new HashSet<>();

    private static Infuse plugin;

    public Frost(DataManager dataManager, Infuse plugin) {
        Frost.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        if (plugin.getDataManager().hasEffect(player, new Frost()) && !(player.getVelocity().lengthSquared() < 0.01)) {
            if (player.isInPowderedSnow()) {
                player.setGliding(true);
            }

            Material blockType = player.getLocation().subtract(0, 1, 0).getBlock().getType();
            if (MaterialSetTag.ICE.isTagged(blockType)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
            }
        }
    }

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (event.isGliding()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, new Frost())) return;

        if (player.isInPowderedSnow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        Vector direction = player.getLocation().getDirection().normalize();
        if (inFrost && plugin.getDataManager().hasEffect(player, new Frost())) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            double boostStrength = 0.6;
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        }
    }

    @EventHandler
    public void onPlayerInteractWithWindCharge(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.WIND_CHARGE) {
            if (player.getFreezeTicks() > 1) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onTenthAttack(TenHitEvent event) {
        InfuseDebug.log("[Frost] Recieved TenHitEvent");
        InfuseDebug.log("[Frost] TenHitEvent Attacker: {}", event.getAttacker().getName());
        InfuseDebug.log("[Frost] TenHitEvent Target: {}", event.getTarget().getName());
        
        if (!plugin.getDataManager().hasEffect(event.getAttacker(), new Frost())) return;

        InfuseDebug.log("[Frost] Attacker has frost effect");

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
        long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_FROST : EffectMapping.FROST);
        long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_FROST : EffectMapping.FROST);

        CooldownManager.setDuration(playerUUID, "frost", duration);
        CooldownManager.setCooldown(playerUUID, "frost", cooldown);

        Location center = caster.getLocation();
        double radius = 5;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
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
        PotionEffect effect = attacker.getPotionEffect(PotionEffectType.UNLUCK);
        if (effect.getAmplifier() >= 0 && frozenAttackers.contains(attacker.getUniqueId()) && event.getEntity() instanceof Player target) {
            target.setFreezeTicks(200);
        }
    }
}