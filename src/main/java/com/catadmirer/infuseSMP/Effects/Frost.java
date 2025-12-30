package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.Managers.EffectMapping;
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

public class Frost implements Listener {
    private final static Set<UUID> frozenAttackers = new HashSet<>();

    private final Map<UUID, Integer> meleeHitCounter = new HashMap<>();
    private static final Set<Material> ICE_BLOCKS;

    private final Infuse plugin;

    public Frost(DataManager dataManager, Infuse plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                Bukkit.getOnlinePlayers().forEach((player) -> {
                    if (EffectMapping.FROST.hasEffect(player) && !(player.getVelocity().lengthSquared() < 0.01)) {
                        Frost.this.handleSwim(player);
                        Material blockType = player.getLocation().subtract(0, 1, 0).getBlock().getType();
                        if (Frost.ICE_BLOCKS.contains(blockType)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
                        }

                    }
                });
            }
        }).runTaskTimer(plugin, 0L, 10L);
    }

    public void handleSwim(Player player) {
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        if (inFrost) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        if (!event.isGliding()) {
            if (inFrost && EffectMapping.FROST.hasEffect(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        Vector direction = player.getLocation().getDirection().normalize();
        if (inFrost && EffectMapping.FROST.hasEffect(player)) {
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
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (event.getEntity() instanceof Player target) {
                if (EffectMapping.FROST.hasEffect(attacker)) {
                    int count = this.meleeHitCounter.getOrDefault(attacker.getUniqueId(), 0) + 1;
                    this.meleeHitCounter.put(attacker.getUniqueId(), count);
                    if (count >= 20) {
                        this.meleeHitCounter.put(attacker.getUniqueId(), 0);
                        (new BukkitRunnable() {
                            int ticksElapsed = 0;
                            final int freezeDuration = 200;

                            public void run() {
                                if (this.ticksElapsed >= freezeDuration) {
                                    target.setFreezeTicks(0);
                                    this.cancel();
                                } else {
                                    int currentFreezeTicks = target.getFreezeTicks();
                                    target.setFreezeTicks(currentFreezeTicks + 2);
                                    this.ticksElapsed += 2;
                                }
                            }
                        }).runTaskTimer(plugin, 0L, 2L);
                    }

                }
            }
        }
    }

    public static void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "frost")) {
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            caster.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 300, 0));
            
            // Applying cooldowns and durations for the effect
            boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
            long cooldown = Infuse.getInstance().getConfig("frost.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = Infuse.getInstance().getConfig("frost.duration." + (isAugmented ? "augmented" : "default"));

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
            }.runTaskLater(Infuse.getInstance(), duration * 20L);
        }
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
        return Infuse.getInstance().getEffectManager().isTrusted(player, caster);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (attacker.hasPotionEffect(PotionEffectType.UNLUCK)) {
                PotionEffect effect = attacker.getPotionEffect(PotionEffectType.UNLUCK);
                if (effect != null && effect.getAmplifier() >= 0 && frozenAttackers.contains(attacker.getUniqueId()) && event.getEntity() instanceof Player target) {
                    target.setFreezeTicks(200);
                }
            }

        }
    }

    static {
        ICE_BLOCKS = EnumSet.of(Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE);
    }
}
