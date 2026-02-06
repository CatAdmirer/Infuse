package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.kyori.adventure.text.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Ender implements Listener {
    private static Infuse plugin;

    private final HashMap<UUID, Integer> dragonBreathCooldowns = new HashMap<>();

    private final Set<UUID> cursedPlayers = new HashSet<>();

    private final Set<UUID> processingDamage = new HashSet<>();

    private DataManager dataManager;

    private final Set<UUID> curseChain = new HashSet<>();

    public static final Component fireballName = Component.text("Cursing Projectile");

    public Ender(DataManager dataManager, Infuse plugin) {
        this.dataManager = dataManager;
        Ender.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                dragonBreathCooldowns.replaceAll((uuid, time) -> time > 0 ? time - 1 : 0);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (EffectMapping.ENDER.hasEffect(player)) {
                        applyGlowingToUntrusted(player);
                    }

                    if (cursedPlayers.contains(player.getUniqueId())) {
                        player.getWorld().spawnParticle(
                                Particle.WITCH,
                                player.getLocation().add(0, 1, 0),
                                10,
                                0.3, 0.5, 0.3,
                                0.01
                        );
                    }
                }

            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) return;

        UUID damagedUUID = damagedPlayer.getUniqueId();
        if (!cursedPlayers.contains(damagedUUID)) return;
        if (processingDamage.contains(damagedUUID) || curseChain.contains(damagedUUID)) return;

        double damage = event.getDamage();
        processingDamage.add(damagedUUID);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    curseChain.add(damagedUUID);

                    for (UUID cursedUUID : new HashSet<>(cursedPlayers)) {
                        if (!cursedUUID.equals(damagedUUID)) {
                            if (processingDamage.contains(cursedUUID) || curseChain.contains(cursedUUID)) continue;

                            Player cursed = Bukkit.getPlayer(cursedUUID);
                            if (cursed != null && cursed.isOnline() && !cursed.isDead()) {
                                processingDamage.add(cursedUUID);
                                curseChain.add(cursedUUID);
                                cursed.damage(damage, damagedPlayer);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        processingDamage.remove(cursedUUID);
                                        curseChain.remove(cursedUUID);
                                    }
                                }.runTaskLater(plugin, 5L);
                            }
                        }
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            processingDamage.remove(damagedUUID);
                            curseChain.remove(damagedUUID);
                        }
                    }.runTaskLater(plugin, 5L);

                } catch (Exception e) {
                    e.printStackTrace();
                    processingDamage.remove(damagedUUID);
                    curseChain.remove(damagedUUID);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private boolean isTeammate(Player player, Player caster) {
        return dataManager.isTrusted(player, caster);
    }


    public static void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;
        
        // Applying cooldowns and durations for the effect
        boolean isAugmented = plugin.getDataManager().getEffect(playerUUID, "1") == EffectMapping.AUG_ENDER || plugin.getDataManager().getEffect(playerUUID, "2") == EffectMapping.AUG_ENDER;
        long cooldown = plugin.getConfigFile().cooldown(isAugmented ? EffectMapping.AUG_ENDER : EffectMapping.ENDER);
        long duration = plugin.getConfigFile().duration(isAugmented ? EffectMapping.AUG_ENDER : EffectMapping.ENDER);

        CooldownManager.setDuration(playerUUID, "ender", duration);
        CooldownManager.setCooldown(playerUUID, "ender", cooldown);

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

    private static boolean isSafeTeleportLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        Location feet = loc.clone();
        Location head = loc.clone().add(0, 1, 0);

        return !feet.getBlock().getType().isSolid() && !head.getBlock().getType().isSolid();
    }


    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        UUID attackerUUID = attacker.getUniqueId();
        long sparkActive1 = CooldownManager.getEffectTimeLeft(attackerUUID, "ender");
        long sparkActive2 = CooldownManager.getEffectTimeLeft(attackerUUID, "aug_ender");
        long sparkActive = Math.max(sparkActive1, sparkActive2);
        if (event.getEntity() instanceof LivingEntity mob && !(event.getEntity() instanceof Player)) {
            if (sparkActive > 0) {
                mob.setHealth(0);
            }
        }
    }

    @EventHandler
    public void onUseDragonBreath(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;
        if (EffectMapping.ENDER.hasEffect(player) || EffectMapping.AUG_ENDER.hasEffect(player)) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.DRAGON_BREATH) return;

            shootCursingFireball(player);
            event.setCancelled(true);
        }
    }


    public void shootCursingFireball(Player player) {
        UUID uuid = player.getUniqueId();

        int cooldown = dragonBreathCooldowns.getOrDefault(uuid, 0);
        if (cooldown > 0) {
            return;
        }
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        Fireball fireball = player.launchProjectile(DragonFireball.class);
        fireball.setIsIncendiary(false);
        fireball.customName(fireballName);
        dragonBreathCooldowns.put(uuid, 30);

        Vector velocity = fireball.getVelocity();
        velocity.multiply(2.0);
        fireball.setVelocity(velocity);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof DragonFireball fireball)) return;
        if (!fireballName.equals(fireball.customName())) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (isTeammate(target, shooter)) return;

        cursePlayer(target.getUniqueId(), 1200);

        event.setDamage(0);
    }


    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof DragonFireball fireball)) return;
        if (!fireballName.equals(fireball.customName())) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (isTeammate(target, shooter)) return;

        cursePlayer(target.getUniqueId(), 1200);
    }

    public void cursePlayer(UUID playerUUID, long delayTicks) {
        cursedPlayers.add(playerUUID);

        Bukkit.getScheduler().runTaskLater(plugin, task -> cursedPlayers.remove(playerUUID), delayTicks);
    }

    public void applyGlowingToUntrusted(Player player) {
        if (!EffectMapping.ENDER.hasEffect(player) && !EffectMapping.AUG_ENDER.hasEffect(player)) return;

        double radius = 10;

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player nearby)) continue;
            if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
            if (dataManager.isTrusted(nearby, player)) continue;
            nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false));
        }
    }
}