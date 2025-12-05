package com.catadmirer.infuseSMP.ExtraEffects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Apophis implements Listener {
    private Infuse plugin;

    public Apophis(Infuse plugin) {
        this.plugin = plugin;
        this.startHealthCheckTask();
        (new BukkitRunnable() {
            public void run() {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!Apophis.this.hasEffect(onlinePlayer, "1") && !Apophis.this.hasEffect(onlinePlayer, "2")) continue;

                    ItemStack mainHand = onlinePlayer.getInventory().getItemInMainHand();
                    Apophis.this.applyPassiveEffects(onlinePlayer);
                    if (Apophis.this.isSword(mainHand) && mainHand.getEnchantmentLevel(Enchantment.LOOTING) < 5) {
                        mainHand.addUnsafeEnchantment(Enchantment.LOOTING, 5);
                    }
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    private void applyPassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 9, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 2, false, false));
    }

    private boolean isSword(ItemStack item) {
        if (item == null) {
            return false;
        } else {
            Material type = item.getType();
            return type == Material.WOODEN_SWORD || type == Material.STONE_SWORD || type == Material.IRON_SWORD || type == Material.GOLDEN_SWORD || type == Material.DIAMOND_SWORD || type == Material.NETHERITE_SWORD;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        (new BukkitRunnable() {
            public void run() {
                AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20.0);
                }

            }
        }).runTaskLater(Infuse.getInstance(), 15L);
    }

    private void startHealthCheckTask() {
        (new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealthAttribute == null) continue;
                    double currentMaxHealth = maxHealthAttribute.getBaseValue();

                    if (!Apophis.this.hasEffect(player, "1") && !Apophis.this.hasEffect(player, "2")) continue;

                    if (currentMaxHealth == 20) {
                        maxHealthAttribute.setBaseValue(30);
                    }
                }
            }
        }).runTaskTimer(Infuse.getInstance(), 0, 20);
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffect("apophis");
        String effectName2 = Infuse.getInstance().getEffect("aug_apophis");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
    }

    public static boolean isEffect(ItemStack item) {
        String effectName = Infuse.getInstance().getEffect("apophis");
        return item != null && item.getType() == Material.POTION && item.getItemMeta().getDisplayName().equals(effectName);
    }

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffect("apophis");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("apophis");
            meta.setColor(Color.fromRGB(0x45033E));
            meta.setLore(lore);
            meta.setCustomModelData(25);
            effect.setItemMeta(meta);
        }

        return effect;
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
            if (!CooldownManager.isOnCooldown(playerUUID, "apophis") || CooldownManager.isOnCooldown(playerUUID, "apophis")) {
                if (player.isSneaking() && isPrimary || !player.isSneaking() && isSecondary) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }

            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        UUID attackerUUID = attacker.getUniqueId();

        long sparkActive = CooldownManager.getEffectTimeLeft(attackerUUID, "apophis");

        if (event.getEntity() instanceof Player target) {
            if (sparkActive > 0) {
                target.sendTitle("\uE090", "", 0, 60, 0);
            }
        }
    }
    public void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "apophis")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));
            final AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            for (Entity entity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setFireTicks(100);
                }
            }

            this.spawnSparkEffect(player);
            (new BukkitRunnable() {
                public void run() {
                    player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
                }
            }).runTaskLater(this.plugin, 20L);
            if (maxHealthAttribute != null) {
                maxHealthAttribute.setBaseValue(40.0);
            }

            String effectName2 = Infuse.getInstance().getEffect("aug_apophis");

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

            CooldownManager.setDuration(playerUUID, "apophis", aphDuration);
            CooldownManager.setCooldown(playerUUID, "apophis", aphCooldown);
            (new BukkitRunnable() {
                public void run() {
                    if (maxHealthAttribute != null) {
                        maxHealthAttribute.setBaseValue(20.0);
                    }
                }
            }).runTaskLater(Infuse.getInstance(), 1200L);
        }
    }

    private void spawnSparkEffect(final Player caster) {
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 100) {
                    Apophis.this.startDarkRedDustEffect(caster.getLocation(), caster);
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
}

