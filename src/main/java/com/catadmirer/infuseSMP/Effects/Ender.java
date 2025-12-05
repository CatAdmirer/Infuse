package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.util.EffectUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Ender implements Listener {
    private final Infuse plugin;

    private final HashMap<UUID, Integer> dragonBreathCooldowns = new HashMap<>();

    private final Set<UUID> cursedPlayers = new HashSet<>();

    private final Set<UUID> processingDamage = new HashSet<>();

    private DataManager dataManager;

    private final Set<UUID> curseChain = new HashSet<>();

    public Ender(DataManager dataManager, Infuse plugin) {
        this.dataManager = dataManager;
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                dragonBreathCooldowns.replaceAll((uuid, time) -> time > 0 ? time - 1 : 0);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean isPrimary = hasEffect(player, "1");
                    boolean isSecondary = hasEffect(player, "2");

                    if (isPrimary || isSecondary) {
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





    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        this.handleOffhand(event);
    }

    public void handleOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("ability.use")) {
            boolean isPrimary = this.hasEffect(player, "1");
            boolean isSecondary = this.hasEffect(player, "2");
            UUID playerUUID = player.getUniqueId();
            if (!CooldownManager.isOnCooldown(playerUUID, "ender")) {
                if (player.isSneaking() && isPrimary || !player.isSneaking() && isSecondary) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }

            }
        }
    }

    private boolean isTeammate(Player player, Player caster) {
        return dataManager.isTrusted(player, caster);
    }


    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;
        String effectName = Infuse.getInstance().getEffect("aug_ender");
        boolean isAugEnder =
                (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                        ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).equalsIgnoreCase(ChatColor.stripColor(effectName))) ||
                        (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).equalsIgnoreCase(ChatColor.stripColor(effectName)));

        long featherDefaultCooldown = Infuse.getInstance().getConfig("feather.cooldown.default");
        long featherAugmentedCooldown = Infuse.getInstance().getConfig("feather.cooldown.augmented");
        long endCooldown = isAugEnder ? featherAugmentedCooldown : featherDefaultCooldown;
        long featherDefaultDuration = Infuse.getInstance().getConfig("feather.duration.default");
        long featherAugmentedDuration = Infuse.getInstance().getConfig("feather.duration.augmented");
        long endDuration = isAugEnder ? featherAugmentedDuration : featherDefaultDuration;

        CooldownManager.setDuration(playerUUID, "ender", endDuration);
        CooldownManager.setCooldown(playerUUID, "ender", endCooldown);
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

    public static ItemStack createRegular() {
        return createEffect(false);
    }

    public static ItemStack createAugmented() {
        return createEffect(true);
    }

    public static ItemStack createEffect(boolean augmented) {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffect(augmented ? "aug_ender" : "ender"));
            meta.setLore(Infuse.getInstance().getEffectLore(augmented ? "aug_ender" : "ender"));
            meta.setColor(Color.fromRGB(0x871277));

            if (augmented) meta.setCustomModelData(999);
            meta.getPersistentDataContainer().set(Infuse.EFFECT_ID, PersistentDataType.INTEGER, augmented ? 26 : 24);

            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isRegular(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 24;
    }

    public static boolean isAugmented(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 26;
    }

    public static boolean isEffect(ItemStack item) {
        return isRegular(item) || isAugmented(item);
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffect("ender");
        String effectName2 = Infuse.getInstance().getEffect("aug_ender");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
    }

    @EventHandler
    public void onUseDragonBreath(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;
        boolean isPrimary = hasEffect(player, "1");
        boolean isSecondary = hasEffect(player, "2");
        if (isPrimary || isSecondary) {
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
            player.sendMessage(ChatColor.RED + "You must wait " + cooldown + " seconds before using Dragon's Breath again!");
            return;
        }
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setIsIncendiary(false);
        fireball.setYield(4);
        fireball.setCustomName("Cursing Projectile");
        dragonBreathCooldowns.put(uuid, 30);

        player.sendMessage(ChatColor.GREEN + "You shot a cursing fireball! Cooldown started.");
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Fireball fireball)) return;
        if (!"Cursing Projectile".equals(fireball.getCustomName())) return;
        event.setDamage(0);
    }


    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) return;
        if (!"Cursing Projectile".equals(fireball.getCustomName())) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (isTeammate(target, shooter)) return;
        cursedPlayers.add(target.getUniqueId());
        target.sendMessage(ChatColor.RED + "You have been cursed!");
        removeCurseLater(target.getUniqueId(), 20 * 60);
    }

    public void removeCurseLater(UUID playerUUID, long delayTicks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                cursedPlayers.remove(playerUUID);
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.GREEN + "The curse has worn off.");
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }

    public void applyGlowingToUntrusted(Player player) {
        boolean isPrimary = hasEffect(player, "1");
        boolean isSecondary = hasEffect(player, "2");
        double radius = 10;

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player nearby)) continue;
            if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
            if (!dataManager.isTrusted(nearby, player)) {
                if (isPrimary || isSecondary) {
                    if (!nearby.hasPotionEffect(PotionEffectType.GLOWING)) {
                        nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false));
                    }
                }
            }
        }
    }
}