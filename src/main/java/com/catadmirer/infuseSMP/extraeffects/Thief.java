package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.particles.Particles;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Thief implements Listener {

    private final Plugin plugin;


    private Map<UUID, UUID> shapeshiftedPlayers = new HashMap<>();
    private Map<UUID, BossBar> shapeshiftedBossBars = new HashMap<>();
    private Map<UUID, Integer> shapeshiftTimeLeft = new HashMap<>();

    private final DataManager dataManager;

    public Thief(DataManager dataManager, Infuse plugin) {
        this.plugin = plugin;
        this.dataManager = dataManager;
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
            boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
            long cooldown = Infuse.getInstance().getConfig("emerald.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = Infuse.getInstance().getConfig("emerald.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "emerald", duration);
            CooldownManager.setCooldown(playerUUID, "emerald", cooldown);
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
        killer.setCustomName(deadPlayer.getName());
        killer.setDisplayName(deadPlayer.getName());
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
            player.setCustomName(originalPlayer.getName());
            player.setDisplayName(originalPlayer.getName());
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

    public String removeAug(String key) {
        if (key.startsWith("aug_")) {
            return key.substring(4);
        }
        return key;
    }

    private final static Set<UUID> active = new HashSet<>();

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            if (!(event.getDamager() instanceof Player player)) return;
            UUID playerUUID = player.getUniqueId();
            if (EffectMapping.THIEF.hasEffect(player)) {
                if (active.contains(playerUUID)) {
                    EffectMapping effect1 = Infuse.getInstance().getEffectManager().getEffect(victim.getUniqueId(), "1");
                    EffectMapping effect2 = Infuse.getInstance().getEffectManager().getEffect(victim.getUniqueId(), "2");
                    
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

    private void activateEffect(Player player, EffectMapping effect, Entity victim) {
        if (effect != null) {
            switch (effect) {
                case STRENGTH, AUG_STRENGTH:
                    activateStrength(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §4Strength Effect");
                    break;
                case SPEED, AUG_SPEED:
                    activateSpeed(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §bSpeed Effect");
                    break;
                case HEART, AUG_HEART:
                    activateHeart(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §cHeart Effect");
                    break;
                case EMERALD, AUG_EMERALD:
                    activateEmerald(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §aEmerald Effect");
                    break;
                case FIRE, AUG_FIRE:
                    activateFire(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §6Fire Effect");
                    break;
                case FEATHER, AUG_FEATHER:
                    activateFeather(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §fFeather Effect");
                    break;
                case HASTE, AUG_HASTE:
                    activateHaste(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §6Haste Effect");
                    break;
                case OCEAN, AUG_OCEAN:
                    activateOcean(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §9Ocean Effect");
                    break;
                case INVIS, AUG_INVIS:
                    activateInvis(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §fInvisibility Effect");
                    break;
                case FROST, AUG_FROST:
                    activateFrost(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §bFrost Effect");
                    break;
                case REGEN, AUG_REGEN:
                    activateRegen(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §cRegeneration Effect");
                    break;
                case THUNDER, AUG_THUNDER:
                    activateThunder(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §eThunder Effect");
                    break;
                case ENDER, AUG_ENDER:
                    activateEnder(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §5Ender Effect");
                    break;
                case APOPHIS, AUG_APOPHIS:
                    activateApohpis(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §5Apohpis Effect");
                    break;
                default:
                    break;
            }
        }
    }

    public void activateApohpis(final Player player) {
        UUID playerUUID = player.getUniqueId();
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
        final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setFireTicks(100);
            }
        }

        spawnSparkEffect(player);
        (new BukkitRunnable() {
            public void run() {
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
            }
        }).runTaskLater(this.plugin, 20L);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(40);
        }

        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("apophis.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("apophis.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        (new BukkitRunnable() {
            public void run() {
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20);
                }
            }
        }).runTaskLater(Infuse.getInstance(), 1200L);
    }

    private void spawnSparkEffect(final Player caster) {
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 100) {
                    startDarkRedDustEffect(caster.getLocation(), caster);
                    this.cancel();
                } else {
                    Location center = caster.getLocation();
                    World world = center.getWorld();
                    if (this.tick > 0 && this.tick % 20 == 0) {
                        world.playSound(center, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);

                        for(int angle = 0; angle < 360; angle += 20) {
                            double rad = Math.toRadians(angle);
                            double offsetX = 5 * Math.cos(rad);
                            double offsetZ = 5 * Math.sin(rad);
                            Location particleLoc = center.clone().add(offsetX, 0.1, offsetZ);
                            world.spawnParticle(Particle.LAVA, particleLoc, 10, 0.05, 0.05, 0.05, 0.01);
                        }

                        for (Player target : world.getPlayers()) {
                            if (!target.equals(caster) && target.getLocation().distance(center) <= 5) {
                                target.damage(8, caster);
                            }
                        }
                    }

                    ++this.tick;
                }
            }
        }).runTaskTimer(this.plugin, 0L, 1L);
    }

    public void activateEnder(Player player) {
        UUID playerUUID = player.getUniqueId();
       
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("ender.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("ender.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();
        int maxDistance = 15;

        Location targetLoc = null;

        for (int i = 1; i <= maxDistance; i++) {
            Location checkLoc = startLoc.clone().add(direction.clone().multiply(i));
            if (isSafeTeleportLocation(checkLoc)) {
                targetLoc = checkLoc;
            } else {
                break;
            }
        }

        if (targetLoc != null) {
            Location finalLoc = targetLoc.clone();
            finalLoc.setYaw(player.getLocation().getYaw());
            finalLoc.setPitch(player.getLocation().getPitch());
            player.teleport(finalLoc);
        }
    }

    private boolean isSafeTeleportLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        Location feet = loc.clone();
        Location head = loc.clone().add(0, 1, 0);

        return !feet.getBlock().getType().isSolid() && !head.getBlock().getType().isSolid();
    }

    private final Set<UUID> activeSparks = new HashSet<>();

    public void activateThunder(final Player caster) {
        final UUID playerUUID = caster.getUniqueId();
        this.activeSparks.add(playerUUID);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("thunder.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("thunder.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);
        
        final long effectDuration = duration * 20;

        final double radius = 10;
        final World world = caster.getWorld();

        new BukkitRunnable() {
            int ticksElapsed = 0;

            public void run() {
                if (this.ticksElapsed >= effectDuration) {
                    activeSparks.remove(playerUUID);
                    this.cancel();
                    return;
                }

                Location center = caster.getLocation();
                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(caster)) continue;
                    if (target instanceof Player p) {
                        if (isTeammate(p, caster)) continue;
                    }

                    target.getWorld().strikeLightningEffect(target.getLocation());
                    target.damage(4, caster);
                    world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.YELLOW, 1.5F));
                }

                this.ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }


    public void activateRegen(final Player player) {
        final UUID playerUUID = player.getUniqueId();
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("regen.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("regen.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
    }

    private final Set<UUID> frozenAttackers = new HashSet<>();

    public void activateFrost(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 300, 0));
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("frost.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("frost.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        Location center = caster.getLocation();
        double radius = 5;
        World world = caster.getWorld();
        final Set<Player> affectedPlayers = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(caster) && !this.isTeammate(player, caster)
                    && player.getWorld().equals(world)
                    && player.getLocation().distance(center) <= radius) {
                affectedPlayers.add(player);
                AttributeInstance jumpAttribute = player.getAttribute(Attribute.JUMP_STRENGTH);
                if (jumpAttribute != null) {
                    jumpAttribute.setBaseValue(0.1);
                }
            }
        }

        this.frozenAttackers.add(caster.getUniqueId());
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

    public void activateStrength(Player player) {
        UUID playerUUID = player.getUniqueId();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("strength.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("strength.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);
    }

    public void activateSpeed(final Player player) {
        UUID playerUUID = player.getUniqueId();

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final org.bukkit.util.Vector direction = player.getEyeLocation().getDirection().normalize();
        double dashMultiplier = plugin.getConfig().getDouble("speed.dashMultiplier", 20);
        org.bukkit.util.Vector dashVector = direction.clone().multiply(dashMultiplier);

        final Location startLocation = player.getLocation();
        final Location endLocation = startLocation.clone().add(dashVector);
        double playerVelocityMultiplier = plugin.getConfig().getDouble("speed.playerVelocityMultiplier", 2);
        player.setVelocity(direction.clone().multiply(playerVelocityMultiplier));

        player.getWorld().spawnParticle(Particle.CLOUD, startLocation, 50, 0.5, 0.5, 0.5, 0.1);

        new BukkitRunnable() {
            public void run() {
                double distance = startLocation.distance(endLocation);
                Vector step = direction.clone().multiply(0.5);
                for (double d = 0; d <= distance; d += step.length()) {
                    Location currentLocation = startLocation.clone().add(step.clone().multiply(d));
                    player.getWorld().spawnParticle(Particle.CLOUD, currentLocation, 5, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }.runTaskLater(this.plugin, 10L);

        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("speed.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("speed.duration." + (isAugmented ? "augmented" : "default"));

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

    public void activateHeart(final Player player) {
        UUID playerUUID = player.getUniqueId();
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(40);
        }
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("health.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("health.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        new BukkitRunnable() {
            public void run() {
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20);
                }
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    public void activateEmerald(Player player) {
        UUID playerUUID = player.getUniqueId();
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("emerald.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("emerald.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);
    }

    public void activateFire(final Player player) {
        UUID playerUUID = player.getUniqueId();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);

        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setFireTicks(100);
            }
        }

        this.spawnSparkEffect(player);
        new BukkitRunnable() {
            public void run() {
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
            }
        }.runTaskLater(this.plugin, 20L);
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("fire.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("fire.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);
    }

    private void startDarkRedDustEffect(final Location startLoc, Player caster) {
        final World world = startLoc.getWorld();
        double explosionRadius = 5;

        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && target.getLocation().distance(startLoc) <= explosionRadius) {
                target.setVelocity(new Vector(0, 2, 0));
            }
        }

        world.playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 60) {
                    this.cancel();
                } else {
                    double baseRadius = 5;
                    double spreadFactor = this.tick * 0.1;
                    double circleRadius = baseRadius + spreadFactor;
                    double particleHeightOffset = this.tick * 3;
                    if (particleHeightOffset > 30) {
                        this.cancel();
                    } else {
                        for(int angle = 0; angle < 360; ++angle) {
                            double rad = Math.toRadians(angle);
                            double offsetX = circleRadius * Math.cos(rad);
                            double offsetZ = circleRadius * Math.sin(rad);
                            Location particleLoc = startLoc.clone().add(offsetX, particleHeightOffset, offsetZ);
                            world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());
                        }

                        ++this.tick;
                    }
                }
            }
        }).runTaskTimer(this.plugin, 0L, 1L);
    }

    private final Set<UUID> spark = new HashSet<>();

    public void activateFeather(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "aug_thief")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            Particles.spawnEffectCloud(player, Color.fromRGB(0xBEA3CA));
            Vector dashDirection = player.getEyeLocation().getDirection().normalize();
            Vector launchVector = dashDirection.multiply(0).setY(1);
            player.setVelocity(launchVector);
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 10));
            
            // Applying cooldowns and durations for the effect
            boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
            long cooldown = Infuse.getInstance().getConfig("feather.cooldown." + (isAugmented ? "augmented" : "default"));
            long duration = Infuse.getInstance().getConfig("feather.duration." + (isAugmented ? "augmented" : "default"));

            CooldownManager.setDuration(playerUUID, "thief", duration);
            CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

            Location anchor = player.getLocation();
            Bukkit.getRegionScheduler().runDelayed(plugin, anchor, (task) -> {
                if (player.isOnline()) {
                    CooldownManager.setDuration(playerUUID, "thiefmace", 5L);
                }
            }, 10L);

            spark.add(playerUUID);
        }
    }

    @EventHandler
    public void FeatherLand(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double radius = 4;
        UUID playerUUID = player.getUniqueId();
        if (player.isOnGround() && CooldownManager.isEffectActive(playerUUID, "thiefmace")) {
            CooldownManager.setDuration(playerUUID, "thiefmace", 0L);

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof LivingEntity target)) continue;
                if (target.equals(player)) continue;
                if (target instanceof Player p && isTeammate(player, p)) continue;

                int damage = 5;
                target.damage(damage);
                Vector knockback = new Vector(0, 1, 0);
                target.setVelocity(target.getVelocity().add(knockback));
                Location anchor = target.getLocation();
                Bukkit.getRegionScheduler().run(Infuse.getInstance(), anchor, (task) -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 0, false, false, false));
                });
            }
        }
    }

    private void applyNextAttackBonus(Player player) {
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity livingEntity) {
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1, 1);
                livingEntity.damage(10);
            }
        }
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            UUID playerUUID = player.getUniqueId();
            if (spark.contains(playerUUID)) {
                applyNextAttackBonus(player);
                spark.remove(playerUUID);
            }
        }
    }

    public void activateHaste(Player player) {
        UUID playerUUID = player.getUniqueId();
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("haste.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("haste.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 15, 3));
    }

    public void activateOcean(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final double radius = 5;
        final World world = caster.getWorld();
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("ocean.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("ocean.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        final long durationTicks = duration * 20L;
        new BukkitRunnable() {
            long ticksElapsed = 0L;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    return;
                }

                for (int angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    double x = caster.getLocation().getX() + radius * Math.cos(rad);
                    double z = caster.getLocation().getZ() + radius * Math.sin(rad);
                    Location particleLoc = new Location(world, x, caster.getLocation().getY(), z);
                    world.spawnParticle(Particle.FALLING_WATER, particleLoc, 1);
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(caster) &&
                            p.getWorld().equals(world) &&
                            p.getLocation().distance(caster.getLocation()) <= radius) {

                        Vector direction = caster.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                        p.setVelocity(direction.multiply(0.5));

                        if (p.getLocation().getBlock().isLiquid()) {
                            int newOxygen = Math.max(p.getRemainingAir() - 20, -20);
                            p.setRemainingAir(newOxygen);
                            if (newOxygen <= 0) {
                                p.damage(2);
                            }
                        }
                    }
                }

                this.ticksElapsed += 10L;
            }
        }.runTaskTimer(this.plugin, 0L, 10L);
    }

    public void activateInvis(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").isAugmented() || Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").isAugmented();
        long cooldown = Infuse.getInstance().getConfig("invis.cooldown." + (isAugmented ? "augmented" : "default"));
        long duration = Infuse.getInstance().getConfig("invis.duration." + (isAugmented ? "augmented" : "default"));

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        final double radius = 10;
        final long durationTicks = duration * 20;
        final World world = caster.getWorld();
        final Set<Player> vanishedPlayers = new HashSet<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(world) && p.getLocation().distance(caster.getLocation()) <= radius && this.isTeammate(caster, p)) {
                vanishedPlayers.add(p);
            }
        }

        for (Player vanished : vanishedPlayers) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(vanished) && !this.isTeammate(other, vanished)) {
                    other.hidePlayer(this.plugin, vanished);
                }
            }
        }

        (new BukkitRunnable() {
            long ticksElapsed = 0L;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    for (Player vanished : vanishedPlayers) {
                        for (Player other : Bukkit.getOnlinePlayers()) {
                            other.showPlayer(plugin, vanished);
                        }
                    }

                } else {
                    Location center = caster.getLocation();

                    for(int angle = 0; angle < 360; angle += 2) {
                        double rad = Math.toRadians(angle);
                        double baseX = center.getX() + radius * Math.cos(rad);
                        double baseZ = center.getZ() + radius * Math.sin(rad);
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.BLACK, 15);
                        for(int i = 0; i < 1; ++i) {
                            double offsetX = (Math.random() - 0.5) * 0.3;
                            double offsetZ = (Math.random() - 0.5) * 0.3;
                            Location particleLoc = new Location(world, baseX + offsetX, center.getY(), baseZ + offsetZ);
                            world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
                        }
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getWorld().equals(world) && p.getLocation().distance(center) <= radius && !isTeammate(p, caster)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                        }
                    }

                    this.ticksElapsed += 10L;
                }
            }
        }).runTaskTimer(this.plugin, 0L, 10L);
    }

    private boolean isTeammate(Player player, Player caster) {
        return dataManager.isTrusted(player, caster);
    }

}
