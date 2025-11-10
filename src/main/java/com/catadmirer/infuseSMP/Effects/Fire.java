package com.catadmirer.infuseSMP.Effects;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectManager;
import com.github.retrooper.packetevents.event.PacketListener;
import org.bukkit.*;
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
                    if (Fire.this.hasImmortalHackEquipped(player, "1") || Fire.this.hasImmortalHackEquipped(player, "2")) {
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

    public static ItemStack createFIRE() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("fire");
            gemName = applyHexColors(gemName);
            meta.setDisplayName(gemName);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("fire"));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setColor(Color.fromRGB(255, 165, 0));
            meta.setLore(lore);
            meta.setCustomModelData(3);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static String applyHexColors(String input) {
        String regex = "(#(?:[0-9a-fA-F]{6}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String colorCode = net.md_5.bungee.api.ChatColor.of(hexCode).toString();
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
            if (inLava && Fire.this.hasImmortalHackEquipped(player, "1") || inLava && Fire.this.hasImmortalHackEquipped(player, "2")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inLava = player.isInLava();
        Vector direction = player.getLocation().getDirection().normalize();
        if (inLava && Fire.this.hasImmortalHackEquipped(player, "1") || inLava && Fire.this.hasImmortalHackEquipped(player, "2")) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            double boostStrength = 0.6;
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        }
    }


    private boolean hasImmortalHackEquipped(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = Infuse.getInstance().getEffect("fire");
        String gemName2 = Infuse.getInstance().getEffect("aug_fire");
        return currentHack != null && (currentHack.equals(gemName) || currentHack.equals(gemName2));
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            if (this.hasImmortalHackEquipped(player, "1") || this.hasImmortalHackEquipped(player, "2")) {
                if (event.getForce() >= 1.0F && event.getProjectile() instanceof Projectile) {
                    Projectile projectile = (Projectile)event.getProjectile();
                    projectile.setFireTicks(100);
                }

            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == DamageCause.FALL) {
                Player player = (Player)event.getEntity();
                if (this.hasImmortalHackEquipped(player, "1") || this.hasImmortalHackEquipped(player, "2")) {
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
        if (event.getDamager() instanceof Player) {
            Player player = (Player)event.getDamager();
            if (this.hasImmortalHackEquipped(player, "1") || this.hasImmortalHackEquipped(player, "2")) {
                UUID uuid = player.getUniqueId();
                int count = (Integer)this.hitCounter.getOrDefault(uuid, 0) + 1;
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
            boolean isLegendary = player.isSneaking() && this.hasImmortalHackEquipped(player, "1");
            boolean isCommon = !player.isSneaking() && this.hasImmortalHackEquipped(player, "2");
            if (isLegendary || isCommon) {
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
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);

            for (Entity entity : player.getNearbyEntities(5.0D, 5.0D, 5.0D)) {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setFireTicks(100);
                }
            }

            this.spawnSparkEffect(player);
            String gemName2 = Infuse.getInstance().getEffect("aug_fire");
            new BukkitRunnable() {
                public void run() {
                    player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
                }
            }.runTaskLater(this.plugin, 20L);
            boolean isAugmentedFire =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            stripAllColors(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(stripAllColors(gemName2))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    stripAllColors(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(stripAllColors(gemName2)));

            long sparkDefaultCooldown = ((Integer) Infuse.getInstance().getCanfig("fire.cooldown.default")).longValue();
            long sparkAugmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("fire.cooldown.augmented")).longValue();
            long sparkCooldown = isAugmentedFire ? sparkAugmentedCooldown : sparkDefaultCooldown;

            long sparkDefaultDuration = ((Integer) Infuse.getInstance().getCanfig("fire.duration.default")).longValue();
            long sparkAugmentedDuration = ((Integer) Infuse.getInstance().getCanfig("fire.duration.augmented")).longValue();
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
                        world.playSound(center, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0F, 1.0F);

                        for(int angle = 0; angle < 360; angle += 20) {
                            double rad = Math.toRadians((double)angle);
                            double offsetX = 5.0D * Math.cos(rad);
                            double offsetZ = 5.0D * Math.sin(rad);
                            Location particleLoc = center.clone().add(offsetX, 0.1D, offsetZ);
                            world.spawnParticle(Particle.LAVA, particleLoc, 10, 0.05D, 0.05D, 0.05D, 0.01D);
                        }

                        Iterator var11 = world.getPlayers().iterator();

                        while(var11.hasNext()) {
                            Player target = (Player)var11.next();
                            if (!target.equals(caster) && target.getLocation().distance(center) <= 5.0D) {
                                target.damage(8.0D, caster);
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
        double explosionRadius = 5.0D;
        Iterator var6 = world.getPlayers().iterator();

        while(var6.hasNext()) {
            Player target = (Player)var6.next();
            if (!target.equals(caster) && target.getLocation().distance(startLoc) <= explosionRadius) {
                target.setVelocity(new Vector(0.0D, 2.0D, 0.0D));
            }
        }

        world.playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 60) {
                    this.cancel();
                } else {
                    double baseRadius = 5.0D;
                    double spreadFactor = (double)this.tick * 0.1D;
                    double circleRadius = baseRadius + spreadFactor;
                    double particleHeightOffset = (double)this.tick * 3.0D;
                    if (particleHeightOffset > 30.0D) {
                        this.cancel();
                    } else {
                        for(int angle = 0; angle < 360; ++angle) {
                            double rad = Math.toRadians((double)angle);
                            double offsetX = circleRadius * Math.cos(rad);
                            double offsetZ = circleRadius * Math.sin(rad);
                            Location particleLoc = startLoc.clone().add(offsetX, particleHeightOffset, offsetZ);
                            world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0.0D, 0.0D, 0.0D, 0.0D, Material.REDSTONE_BLOCK.createBlockData());
                        }

                        ++this.tick;
                    }
                }
            }
        }).runTaskTimer(this.plugin, 0L, 1L);
    }

    public static boolean isStrengthGem(ItemStack item) {
        if (item != null && item.getType() == Material.POTION && item.getItemMeta() != null) {
            return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 3;
        } else {
            return false;
        }
    }
}
