package com.catadmirer.infuseSMP.effects;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.github.retrooper.packetevents.event.PacketListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Fire implements Listener, PacketListener {
    
    private final Plugin plugin;
    private final Map<UUID, Integer> hitCounter = new HashMap<>();

    public Fire(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                Bukkit.getOnlinePlayers().forEach((player) -> {
                    if (Fire.this.hasEffect(player, "1") || Fire.this.hasEffect(player, "2")) {
                        Fire.this.applyFireResistance(player);
                        Fire.this.handleSwim(player);
                    }
                });
            }
        }).runTaskTimer(plugin, 0L, 10L);
    }

    private void applyFireResistance(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false));
    }

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffect("fire");
            effectName = applyHexColors(effectName);
            meta.setDisplayName(effectName);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("fire"));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setColor(Color.fromRGB(0xFFA500));
            meta.setLore(lore);
            meta.setCustomModelData(3);
            effect.setItemMeta(meta);
        }

        return effect;
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

    public void handleSwim(Player player) {
        boolean inLava = player.isInLava();
        if (inLava) {
            player.setGliding(true);
        } else if (player.getLocation().getBlock().getType() == Material.POWDER_SNOW) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        boolean inLava = player.isInLava();
        if (!event.isGliding()) {
            if (inLava && Fire.this.hasEffect(player, "1") || inLava && Fire.this.hasEffect(player, "2")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inLava = player.isInLava();
        Vector direction = player.getLocation().getDirection().normalize();
        if (inLava && Fire.this.hasEffect(player, "1") || inLava && Fire.this.hasEffect(player, "2")) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            double boostStrength = 0.6;
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        }
    }


    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffect("fire");
        String effectName2 = Infuse.getInstance().getEffect("aug_fire");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
                if (event.getForce() >= 1 && event.getProjectile() instanceof Projectile projectile) {
                    projectile.setFireTicks(100);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getCause() == DamageCause.FALL) {
                if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
                    Material blockType = player.getLocation().getBlock().getType();
                    if (blockType == Material.LAVA || blockType == Material.LAVA_CAULDRON) {
                        event.setCancelled(true);
                    }

                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
                UUID uuid = player.getUniqueId();
                int count = this.hitCounter.getOrDefault(uuid, 0) + 1;
                if (count >= 20) {
                    event.getEntity().setFireTicks(100);
                    count = 0;
                }

                this.hitCounter.put(uuid, count);
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        this.handleOffhand(event);
    }

    public void handleOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("ability.use")) {
            boolean isPrimary = player.isSneaking() && this.hasEffect(player, "1");
            boolean isSecondary = !player.isSneaking() && this.hasEffect(player, "2");
            if (isPrimary || isSecondary) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "fire")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    public void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "fire")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setFireTicks(100);
                }
            }

            this.spawnSparkEffect(player);
            String effectName2 = Infuse.getInstance().getEffect("aug_fire");
            new BukkitRunnable() {
                public void run() {
                    player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
                }
            }.runTaskLater(this.plugin, 20L);
            boolean isAugmentedFire =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            stripAllColors(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(stripAllColors(effectName2))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    stripAllColors(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(stripAllColors(effectName2)));

            long sparkDefaultCooldown = Infuse.getInstance().getCanfig("fire.cooldown.default");
            long sparkAugmentedCooldown = Infuse.getInstance().getCanfig("fire.cooldown.augmented");
            long sparkCooldown = isAugmentedFire ? sparkAugmentedCooldown : sparkDefaultCooldown;

            long sparkDefaultDuration = Infuse.getInstance().getCanfig("fire.duration.default");
            long sparkAugmentedDuration = Infuse.getInstance().getCanfig("fire.duration.augmented");
            long sparkDuration = isAugmentedFire ? sparkAugmentedDuration : sparkDefaultDuration;

            CooldownManager.setDuration(playerUUID, "fire", sparkDuration);
            CooldownManager.setCooldown(playerUUID, "fire", sparkCooldown);
        }
    }

    public String stripAllColors(String input) {
        if (input == null) return null;
        Pattern pattern = Pattern.compile(
                "(§#[0-9a-fA-F]{6})" +
                        "|(§x(§[0-9a-fA-F]){6})" +
                        "|(§[0-9a-fk-orA-FK-OR])"
        );
        return pattern.matcher(input).replaceAll("");
    }


    private void spawnSparkEffect(final Player caster) {
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 100) {
                    Fire.this.startDarkRedDustEffect(caster.getLocation(), caster);
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

    private void startDarkRedDustEffect(final Location startLoc, Player caster) {
        final World world = startLoc.getWorld();
        double explosionRadius = 5.0;
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
                            world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0.0, 0.0, 0.0, 0.0, Material.REDSTONE_BLOCK.createBlockData());
                        }

                        ++this.tick;
                    }
                }
            }
        }).runTaskTimer(this.plugin, 0L, 1L);
    }

    public static boolean isEffect(ItemStack item) {
        if (item != null && item.getType() == Material.POTION && item.getItemMeta() != null) {
            return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 3;
        } else {
            return false;
        }
    }
}
