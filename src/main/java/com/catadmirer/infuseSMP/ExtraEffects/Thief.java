package com.catadmirer.infuseSMP.ExtraEffects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.Particles.AlsoParticles;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import org.bukkit.*;
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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Thief implements Listener, PacketListener {

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
                    boolean shouldBeHidden = hasEffect(p, "1") || hasEffect(p, "2");
                    for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                        if (shouldBeHidden) {
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

    @EventHandler
    public void handleOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("ability.use")) {
            boolean isPrimary = this.hasEffect(player, "1");
            boolean isSecondary = this.hasEffect(player, "2");
            UUID playerUUID = player.getUniqueId();
            if (!CooldownManager.isOnCooldown(playerUUID, "thief")) {
                if (player.isSneaking() && isPrimary || !player.isSneaking() && isSecondary) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }

            }
        }
    }

    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "thief")) {
            active.add(playerUUID);
            String effectName2 = Infuse.getInstance().getEffect("aug_thief");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            boolean isAugmentedEme = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)))
                    || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));

            long emeDefaultCooldown = Infuse.getInstance().getConfig("thief.cooldown.default");;
            long emeAugmentedCooldown = Infuse.getInstance().getConfig("thief.cooldown.augmented");;
            long emeCooldown = isAugmentedEme ? emeAugmentedCooldown : emeDefaultCooldown;
            CooldownManager.setDuration(playerUUID, "thief", 0);
            CooldownManager.setCooldown(playerUUID, "thief", emeCooldown);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (hasEffect(killer, "1") || hasEffect(killer, "2")) {
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
        bossBar.setProgress(1.0);
        bossBar.addPlayer(killer);
        shapeshiftedBossBars.put(killer.getUniqueId(), bossBar);
        shapeshiftTimeLeft.putIfAbsent(killer.getUniqueId(), shapeshiftTime);
        new BukkitRunnable() {
            @Override
            public void run() {
                Integer timeLeft = shapeshiftTimeLeft.get(killer.getUniqueId());

                if (timeLeft != null && timeLeft > 0) {
                    double progress = timeLeft / 3600.0;
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

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffect("thief");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("thief");
            meta.setColor(Color.fromRGB(255, 0, 0));
            meta.setLore(lore);
            meta.setCustomModelData(26);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isEffect(ItemStack item) {
        String effectName = Infuse.getInstance().getEffect("thief");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        String effectName = Infuse.getInstance().getEffect("thief");
        return currentEffect != null && (currentEffect.equals(effectName2) || currentEffect.equals(effectName));
    }

    public String removeAug(String key) {
        if (key.startsWith("aug_")) {
            return key.substring(4);
        }
        return key;
    }

    private final Set<UUID> active = new HashSet<>();

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            if (!(event.getDamager() instanceof Player player)) return;
            UUID playerUUID = player.getUniqueId();
            if (hasEffect(player, "1") || hasEffect(player, "2")) {
                if (active.contains(playerUUID)) {
                    String effect0 = Infuse.getInstance().getEffectManager().getEffect(victim.getUniqueId(), "1");
                    String effect01 = Infuse.getInstance().getEffectManager().getEffect(victim.getUniqueId(), "2");
                    String effect1 = Infuse.getInstance().getEffectReversed(effect0);
                    String effect2 = Infuse.getInstance().getEffectReversed(effect01);
                    effect1 = removeAug(effect1);
                    effect2 = removeAug(effect2);
                    Random rand = new Random();
                    if (effect1 != null && effect2 != null) {
                        String selectedEffect = rand.nextBoolean() ? effect1 : effect2;
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

    private void activateEffect(Player player, String effect, Entity victim) {
        if (effect != null) {
            switch (effect) {
                case "strength":
                    activateStrength(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §4Strength Effect");
                    break;
                case "speed":
                    activateSpeed(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §bSpeed Effect");
                    break;
                case "heart":
                    activateHeart(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §cHeart Effect");
                    break;
                case "emerald":
                    activateEmerald(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §aEmerald Effect");
                    break;
                case "fire":
                    activateFire(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §6Fire Effect");
                    break;
                case "feather":
                    activateFeather(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §fFeather Effect");
                    break;
                case "haste":
                    activateHaste(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §6Haste Effect");
                    break;
                case "ocean":
                    activateOcean(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §9Ocean Effect");
                    break;
                case "invis":
                    activateInvis(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §fInvisibility Effect");
                    break;
                case "frost":
                    activateFrost(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §bFrost Effect");
                    break;
                case "regen":
                    activateRegen(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §cRegeneration Effect");
                    break;
                case "thunder":
                    activateThunder(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §eThunder Effect");
                    break;
                case "ender":
                    activateEnder(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §5Ender Effect");
                    break;
                case "aug_ender":
                    activateEnder(player);
                    player.sendMessage("§eYou stole " + victim.getName() + "'s §5Ender Effect");
                    break;
                case "apophis":
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
        final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
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
            maxHealthAttribute.setBaseValue(40.0);
        }

        String effectName2 = Infuse.getInstance().getEffect("aug_thief");

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        boolean isAugmentedAph = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)))
                || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));

        long aphDefaultCooldown = Infuse.getInstance().getConfig("apophis.cooldown.default");;
        long aphAugmentedCooldown = Infuse.getInstance().getConfig("apophis.cooldown.augmented");;
        long aphCooldown = isAugmentedAph ? aphAugmentedCooldown : aphDefaultCooldown;

        long aphDefaultDuration = Infuse.getInstance().getConfig("apophis.duration.default");;
        long aphAugmentedDuration = Infuse.getInstance().getConfig("apophis.duration.augmented");;
        long aphDuration = isAugmentedAph ? aphAugmentedDuration : aphDefaultDuration;

        CooldownManager.setDuration(playerUUID, "thief", aphDuration);
        CooldownManager.setCooldown(playerUUID, "thief", aphCooldown * 2);
        (new BukkitRunnable() {
            public void run() {
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20.0);
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
                            double offsetX = 5.0 * Math.cos(rad);
                            double offsetZ = 5.0 * Math.sin(rad);
                            Location particleLoc = center.clone().add(offsetX, 0.1, offsetZ);
                            world.spawnParticle(Particle.LAVA, particleLoc, 10, 0.05, 0.05, 0.05, 0.01);
                        }

                        for (Player target : world.getPlayers()) {
                            if (!target.equals(caster) && target.getLocation().distance(center) <= 5.0) {
                                target.damage(8.0, caster);
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
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1"))
                        .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2)))) ||
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2"))
                                .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2))));
        long endFirstDuration = Infuse.getInstance().getConfig("ender.duration.augmented");;
        long endFirstCooldown = Infuse.getInstance().getConfig("ender.cooldown.augmented");;
        long endSecondDuration = Infuse.getInstance().getConfig("ender.duration.default");;
        long endSecondCooldown = Infuse.getInstance().getConfig("ender.cooldown.default");;

        long cooldown = isAugmented ? endFirstCooldown : endSecondCooldown;
        long duration = isAugmented ? endFirstDuration : endSecondDuration;

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
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1"))
                        .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2)))) ||
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2"))
                                .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2))));
        long defaultCooldown = Infuse.getInstance().getConfig("thunder.cooldown.default");;
        long augmentedCooldown = Infuse.getInstance().getConfig("thunder.cooldown.augmented");;
        long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;

        long defaultDuration = Infuse.getInstance().getConfig("thunder.duration.default");;
        long augmentedDuration = Infuse.getInstance().getConfig("thunder.duration.augmented");;
        long duration = isAugmented ? augmentedDuration : defaultDuration;
        final long effectDuration = duration * 20;

        CooldownManager.setDuration(playerUUID, "thunder", duration);
        CooldownManager.setCooldown(playerUUID, "thunder", cooldown);

        final double radius = 10.0;
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
                    target.damage(4.0, caster);
                    world.spawnParticle(Particle.DUST, target.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0, new Particle.DustOptions(Color.YELLOW, 1.5F));
                }

                this.ticksElapsed += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }


    public void activateRegen(final Player player) {
        final UUID playerUUID = player.getUniqueId();
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2))) ||
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));
        long defaultCooldown = Infuse.getInstance().getConfig("regen.cooldown.default");;

        long augmentedCooldown = Infuse.getInstance().getConfig("regen.cooldown.augmented");;

        long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;
        long defaultDuration = Infuse.getInstance().getConfig("regen.duration.default");;

        long augmentedDuration = Infuse.getInstance().getConfig("regen.duration.augmented");;

        long duration = isAugmented ? augmentedDuration : defaultDuration;

        CooldownManager.setCooldown(playerUUID, "thief", cooldown);
        CooldownManager.setDuration(playerUUID, "thief", duration * 2);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
    }

    private final Set<UUID> frozenAttackers = new HashSet<>();

    public void activateFrost(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        String effectName = Infuse.getInstance().getEffect("aug_thief");
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 300, 0));
        boolean isAugmentedFrost =
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName)) ||
                        (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName))));
        long frostDefaultCooldown = Infuse.getInstance().getConfig("frost.cooldown.default");;

        long frostAugmentedCooldown = Infuse.getInstance().getConfig("frost.cooldown.augmented");;

        long frostCooldown = isAugmentedFrost ? frostAugmentedCooldown : frostDefaultCooldown;

        long frostDefaultDuration = Infuse.getInstance().getConfig("frost.duration.default");;

        long frostAugmentedDuration = Infuse.getInstance().getConfig("frost.duration.augmented");;

        long frostDuration = isAugmentedFrost ? frostAugmentedDuration : frostDefaultDuration;

        CooldownManager.setDuration(playerUUID, "thief", frostDuration);
        CooldownManager.setCooldown(playerUUID, "thief", frostCooldown * 2);

        Location center = caster.getLocation();
        double radius = 5.0;
        World world = caster.getWorld();
        final Set<Player> affectedPlayers = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.equals(caster) && !this.isTeammate(player, caster)
                    && player.getWorld().equals(world)
                    && player.getLocation().distance(center) <= radius) {
                affectedPlayers.add(player);
                AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                if (jumpAttribute != null) {
                    jumpAttribute.setBaseValue(0.1);
                }
            }
        }

        this.frozenAttackers.add(caster.getUniqueId());
        new BukkitRunnable() {
            public void run() {
                for (Player player : affectedPlayers) {
                    AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                    if (jumpAttribute != null) {
                        jumpAttribute.setBaseValue(0.42);
                    }
                }
                frozenAttackers.remove(caster.getUniqueId());
            }
        }.runTaskLater(plugin, frostDuration * 20L);
    }

    public void activateStrength(Player player) {
        UUID playerUUID = player.getUniqueId();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2)))) ||
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2))));
        long defaultDuration = Infuse.getInstance().getConfig("strength.duration.default");;

        long augmentedDuration = Infuse.getInstance().getConfig("strength.duration.augmented");;

        long duration = isAugmented ? augmentedDuration : defaultDuration;

        long defaultCooldown = Infuse.getInstance().getConfig("strength.cooldown.default");;

        long augmentedCooldown = Infuse.getInstance().getConfig("strength.cooldown.augmented");;

        long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);
    }

    public void activateSpeed(final Player player) {
        UUID playerUUID = player.getUniqueId();

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final org.bukkit.util.Vector direction = player.getEyeLocation().getDirection().normalize();
        double dashMultiplier = plugin.getConfig().getDouble("speed.dashMultiplier", 20.0);
        org.bukkit.util.Vector dashVector = direction.clone().multiply(dashMultiplier);

        final Location startLocation = player.getLocation();
        final Location endLocation = startLocation.clone().add(dashVector);
        double playerVelocityMultiplier = plugin.getConfig().getDouble("speed.playerVelocityMultiplier", 2.0);
        player.setVelocity(direction.clone().multiply(playerVelocityMultiplier));

        player.getWorld().spawnParticle(Particle.CLOUD, startLocation, 50, 0.5, 0.5, 0.5, 0.1);

        new BukkitRunnable() {
            public void run() {
                double distance = startLocation.distance(endLocation);
                Vector step = direction.clone().multiply(0.5);
                for (double d = 0.0; d <= distance; d += step.length()) {
                    Location currentLocation = startLocation.clone().add(step.clone().multiply(d));
                    player.getWorld().spawnParticle(Particle.CLOUD, currentLocation, 5, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }.runTaskLater(this.plugin, 10L);

        long defaultDuration = Infuse.getInstance().getConfig("speed.duration.default");;

        CooldownManager.setDuration(playerUUID, "speed", defaultDuration);
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).equalsIgnoreCase(ChatColor.stripColor(effectName2))) ||
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).equalsIgnoreCase(ChatColor.stripColor(effectName2)));

        long defaultCooldown = Infuse.getInstance().getConfig("speed.cooldown.default");;

        long augmentedCooldown = Infuse.getInstance().getConfig("speed.cooldown.augmented");;

        long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;
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
                hitEntity.getWorld().spawnParticle(Particle.CRIT, hitEntity.getLocation().add(0.0, hitEntity.getHeight() / 2.0, 0.0), 10);
            }

        }
    }

    public void activateHeart(final Player player) {
        UUID playerUUID = player.getUniqueId();
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(40.0);
        }
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmentedHeart =
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2))) ||
                        (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));
        long defaultCooldown = Infuse.getInstance().getConfig("health.cooldown.default");;

        long augmentedCooldown = Infuse.getInstance().getConfig("health.cooldown.augmented");;

        long cooldown = isAugmentedHeart ? augmentedCooldown : defaultCooldown;

        long defaultDuration = Infuse.getInstance().getConfig("health.duration.default");;

        long augmentedDuration = Infuse.getInstance().getConfig("health.duration.augmented");;
        long duration = isAugmentedHeart ? augmentedDuration : defaultDuration;

        CooldownManager.setDuration(playerUUID, "thief", duration);
        CooldownManager.setCooldown(playerUUID, "thief", cooldown * 2);

        new BukkitRunnable() {
            public void run() {
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20.0);
                }
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    public void activateEmerald(Player player) {
        UUID playerUUID = player.getUniqueId();
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
        boolean isAugmentedEme = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)))
                || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));

        long emeDefaultCooldown = Infuse.getInstance().getConfig("emerald.cooldown.default");;
        long emeAugmentedCooldown = Infuse.getInstance().getConfig("emerald.cooldown.augmented");;
        long emeCooldown = isAugmentedEme ? emeAugmentedCooldown : emeDefaultCooldown;
        long emeDefaultDuration = Infuse.getInstance().getConfig("emerald.duration.default");;
        long emeAugmentedDuration = Infuse.getInstance().getConfig("emerald.duration.augmented");;
        long emeDuration = isAugmentedEme ? emeAugmentedDuration : emeDefaultDuration;

        CooldownManager.setDuration(playerUUID, "thief", emeDuration);
        CooldownManager.setCooldown(playerUUID, "thief", emeCooldown * 2);
    }

    public void activateFire(final Player player) {
        UUID playerUUID = player.getUniqueId();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);

        for (Entity entity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setFireTicks(100);
            }
        }

        this.spawnSparkEffect(player);
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        new BukkitRunnable() {
            public void run() {
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
            }
        }.runTaskLater(this.plugin, 20L);
        boolean isAugmentedFire =
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2))) ||
                        (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));

        long sparkDefaultCooldown = Infuse.getInstance().getConfig("fire.cooldown.default");;
        long sparkAugmentedCooldown = Infuse.getInstance().getConfig("fire.cooldown.augmented");;
        long sparkCooldown = isAugmentedFire ? sparkAugmentedCooldown : sparkDefaultCooldown;

        long sparkDefaultDuration = Infuse.getInstance().getConfig("fire.duration.default");;
        long sparkAugmentedDuration = Infuse.getInstance().getConfig("fire.duration.augmented");;
        long sparkDuration = isAugmentedFire ? sparkAugmentedDuration : sparkDefaultDuration;

        CooldownManager.setDuration(playerUUID, "thief", sparkDuration);
        CooldownManager.setCooldown(playerUUID, "thief", sparkCooldown * 2);
    }

    private void startDarkRedDustEffect(final Location startLoc, Player caster) {
        final World world = startLoc.getWorld();
        double explosionRadius = 5;
        
        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && target.getLocation().distance(startLoc) <= explosionRadius) {
                target.setVelocity(new Vector(0.0, 2.0, 0.0));
            }
        }

        world.playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 60) {
                    this.cancel();
                } else {
                    double baseRadius = 5.0;
                    double spreadFactor = this.tick * 0.1;
                    double circleRadius = baseRadius + spreadFactor;
                    double particleHeightOffset = this.tick * 3.0;
                    if (particleHeightOffset > 30.0) {
                        this.cancel();
                    } else {
                        for(int angle = 0; angle < 360; ++angle) {
                            double rad = Math.toRadians(angle);
                            double offsetX = circleRadius * Math.cos(rad);
                            double offsetZ = circleRadius * Math.sin(rad);
                            Location particleLoc = startLoc.clone().add(offsetX, particleHeightOffset, offsetZ);
                            world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0.0, 0.0, 0.0, 0.0, Material.REDSTONE_BLOCK.createBlockData());
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
            String effectName = Infuse.getInstance().getEffect("aug_thief");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            AlsoParticles.spawnEffect(player, Color.fromRGB(190, 163, 202));
            Vector dashDirection = player.getEyeLocation().getDirection().normalize();
            Vector launchVector = dashDirection.multiply(0).setY(1);
            player.setVelocity(launchVector);
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 10));
            boolean isAugmentedFeather =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).equalsIgnoreCase(ChatColor.stripColor(effectName))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).equalsIgnoreCase(ChatColor.stripColor(effectName)));
            long featherDefaultCooldown = Infuse.getInstance().getConfig("feather.cooldown.default");;
            long featherAugmentedCooldown = Infuse.getInstance().getConfig("feather.cooldown.augmented");;
            long featherCooldown = isAugmentedFeather ? featherAugmentedCooldown : featherDefaultCooldown;
            long featherDefaultDuration = Infuse.getInstance().getConfig("feather.duration.default");;
            long featherAugmentedDuration = Infuse.getInstance().getConfig("feather.duration.augmented");;
            long featherDuration = isAugmentedFeather ? featherAugmentedDuration : featherDefaultDuration;
            CooldownManager.setDuration(playerUUID, "feather", featherDuration);
            CooldownManager.setCooldown(playerUUID, "feather", featherCooldown * 2);
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
        double radius = 4.0;
        UUID playerUUID = player.getUniqueId();
        if (player.isOnGround() && CooldownManager.isEffectActive(playerUUID, "thiefmace")) {
            CooldownManager.setDuration(playerUUID, "thiefmace", 0L);
            Location loc = player.getLocation();
            World world = player.getWorld();

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
                livingEntity.damage(10.0);
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
        String effectName = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmentedHaste =
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName))) ||
                        (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName)));
        long hasteDefaultCooldown = Infuse.getInstance().getConfig("haste.cooldown.default");;
        long hasteAugmentedCooldown = Infuse.getInstance().getConfig("haste.cooldown.augmented");;
        long hasteCooldown = isAugmentedHaste ? hasteAugmentedCooldown : hasteDefaultCooldown;
        long hasteDefaultDuration = Infuse.getInstance().getConfig("haste.duration.default");;
        long hasteAugmentedDuration = Infuse.getInstance().getConfig("haste.duration.augmented");;
        long hasteDuration = isAugmentedHaste ? hasteAugmentedDuration : hasteDefaultDuration;
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 15, 3));
        CooldownManager.setDuration(playerUUID, "thief", hasteDuration);
        CooldownManager.setCooldown(playerUUID, "thief", hasteCooldown * 2);
    }

    public void activateOcean(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final double radius = 5.0;
        final World world = caster.getWorld();
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmented =
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2))) ||
                        (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));
        long defaultCooldown = Infuse.getInstance().getConfig("ocean.cooldown.default");;
        long augmentedCooldown = Infuse.getInstance().getConfig("ocean.cooldown.augmented");;
        long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;

        long defaultDuration = Infuse.getInstance().getConfig("ocean.duration.default");;
        long augmentedDuration = Infuse.getInstance().getConfig("ocean.duration.augmented");;
        long duration = isAugmented ? augmentedDuration : defaultDuration;

        CooldownManager.setDuration(playerUUID, "thief", duration * 2);
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
                                p.damage(2.0);
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
        String effectName2 = Infuse.getInstance().getEffect("aug_thief");
        boolean isAugmentedInvis = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)))
                || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));
        long invisDefaultCooldown = Infuse.getInstance().getConfig("invisibility.cooldown.default");;
        long invisAugmentedCooldown = Infuse.getInstance().getConfig("invisibility.cooldown.augmented");;
        long invisCooldown = isAugmentedInvis ? invisAugmentedCooldown : invisDefaultCooldown;

        long invisDefaultDuration = Infuse.getInstance().getConfig("invisibility.duration.default");;
        long invisAugmentedDuration = Infuse.getInstance().getConfig("invisibility.duration.augmented");;
        long invisDuration = isAugmentedInvis ? invisAugmentedDuration : invisDefaultDuration;
        final double radius = 10.0;
        final long durationTicks = invisDuration;
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
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 0, 0), 15);
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
                            hideHealthForPlayer(p, 2);
                        }
                    }

                    this.ticksElapsed += 10L;
                }
            }
        }).runTaskTimer(this.plugin, 0L, 10L);

        CooldownManager.setDuration(playerUUID, "thief", invisDuration);
        CooldownManager.setCooldown(playerUUID, "thief", invisCooldown * 2);
    }

    private boolean isTeammate(Player player, Player caster) {
        return dataManager.isTrusted(player, caster);
    }

    private void hideHealthForPlayer(final Player player, final int durationSeconds) {
        (new BukkitRunnable() {
            int elapsedTicks = 0;

            public void run() {
                WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(
                        20,
                        20,
                        5);
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
                this.elapsedTicks += 2;
                if (this.elapsedTicks >= durationSeconds * 20) {
                    this.cancel();
                }

            }
        }).runTaskTimer(this.plugin, 0L, 2L);
    }
}
