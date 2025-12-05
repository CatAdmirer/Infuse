package com.catadmirer.infuseSMP.Effects;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.Particles.AlsoParticles;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Feather implements Listener {
    
    private final Plugin plugin;
    private final Map<UUID, Integer> hitCounter = new HashMap<>();

    private final Set<UUID> spark = new HashSet<>();

    private final DataManager trustManager;


    public Feather(Plugin plugin, DataManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void FeatherLand(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double radius = 4;
        UUID playerUUID = player.getUniqueId();
        if (player.isOnGround() && CooldownManager.isEffectActive(playerUUID, "feathermace")) {
            CooldownManager.setDuration(playerUUID, "feathermace", 0L);
            Location loc = player.getLocation();
            World world = player.getWorld();

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {

                if (!(entity instanceof LivingEntity target)) continue;
                if (target instanceof Player targetPlayer && isTeammate(player, targetPlayer)) continue;

                int damage = 8;
                target.damage(damage);
                Vector knockback = new Vector(0, 1, 0);
                target.setVelocity(target.getVelocity().add(knockback));
                Location anchor = target.getLocation();
                LivingEntity finalTarget = target;
                Bukkit.getRegionScheduler().run(Infuse.getInstance(), anchor, (task) -> {
                    finalTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 0, false, false, false));
                });
            }

            world.spawnParticle(Particle.CLOUD, loc, 50, 0, 0, 0, 2);
            world.playSound(loc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.5F, 1);
            Location anchor = player.getLocation();
            Bukkit.getRegionScheduler().runDelayed(Infuse.getInstance(), anchor, (task) -> {
                if (player.isOnline()) {
                    Vector dashDirection = player.getEyeLocation().getDirection().normalize();
                    Vector launchVector = dashDirection.multiply(5);
                    player.setVelocity(launchVector);
                }
            }, 1L);
        }
    }

    private boolean isTeammate(Player player, Player caster) {
        return trustManager.isTrusted(caster, player);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                if (!(damageByEntityEvent.getDamager() instanceof Player target)) return;
                if (!this.hasEffect(player, "1") && !this.hasEffect(player, "2")) {
                    return;
                }

                if (event.getCause() == DamageCause.FALL) {
                    return;
                }

                UUID uuid = player.getUniqueId();
                int count = this.hitCounter.getOrDefault(uuid, 0) + 1;
                this.hitCounter.put(uuid, count);
                if (count >= 10) {
                    this.hitCounter.put(uuid, 0);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 2));
                    Location chargeLocation = player.getLocation().add(0, 1, 0);
                    WindCharge windCharge = player.getWorld().spawn(chargeLocation, WindCharge.class);
                    Location targetLocation = player.getLocation().subtract(0, 1, 0);
                    Vector direction = targetLocation.toVector().subtract(chargeLocation.toVector()).normalize();
                    windCharge.setVelocity(direction.multiply(1));
                    windCharge.setShooter(player);
                    player.setVelocity(new Vector(0, 0.5, 0));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getCause() == DamageCause.FALL) {
                if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRightClickWindcharge(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.hasEffect(player, "1") || this.hasEffect(player, "2")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null && item.getType() == Material.WIND_CHARGE) {
                if (!player.hasCooldown(Material.WIND_CHARGE)) {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                        Location anchor = player.getLocation();
                        Bukkit.getRegionScheduler().runDelayed(this.plugin, anchor, (task) -> {
                            player.setCooldown(Material.WIND_CHARGE, 5);
                        }, 1L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWindChargeLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof WindCharge windCharge) {
            if (windCharge.getShooter() instanceof Player player) {
                Vector direction = player.getEyeLocation().getDirection().normalize().multiply(2);
                windCharge.setVelocity(direction);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (this.hasEffect(attacker, "1") || this.hasEffect(attacker, "2")) {
                double fallDistance = attacker.getFallDistance();
                if (fallDistance >= 7) {
                    attacker.getWorld().playSound(attacker.getLocation(), Sound.ITEM_MACE_SMASH_AIR, 1, 1);
                    Location startLoc = attacker.getLocation();
                    World world = startLoc.getWorld();
                    Location particleLoc = event.getDamager().getLocation();
                    world.spawnParticle(Particle.GUST_EMITTER_SMALL, particleLoc, 1, 0, 0, 0, 0);
                    attacker.setVelocity(new Vector(0, 1.8, 0));
                    double multiplier = 1.1;
                    event.setDamage(event.getDamage() * multiplier);
                }

            }
        }
    }

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getMessages().getString("feather.effect_name", "§#BEA3CAFeather Effect");
            effectName = applyHexColors(effectName);
            meta.setDisplayName(effectName);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getMessages().getStringList("feather.effect_lore"));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setColor(Color.fromRGB(255, 255, 255));
            meta.setLore(lore);
            meta.setCustomModelData(2);
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

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getMessages().getString("feather.effect_name", "§fFeather Effect");
        String effectName2 = Infuse.getInstance().getMessages().getString("aug_feather.effect_name", "§fAugmented Feather Effect");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
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
            if (!CooldownManager.isOnCooldown(playerUUID, "feather")) {
                if (player.isSneaking() && isPrimary || !player.isSneaking() && isSecondary) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }

            }
        }
    }

    public void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "feather")) {
            String effectName = Infuse.getInstance().getEffect("aug_feather");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            AlsoParticles.spawnEffect(player, Color.fromRGB(190, 163, 202));
            Vector dashDirection = player.getEyeLocation().getDirection().normalize();
            Vector launchVector = dashDirection.multiply(0).setY(1);
            player.setVelocity(launchVector);
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 10));
            boolean isAugmentedFeather =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            stripAllColors(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).equalsIgnoreCase(stripAllColors(effectName))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    stripAllColors(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).equalsIgnoreCase(stripAllColors(effectName)));
            long featherDefaultCooldown = Infuse.getInstance().getConfig("feather.cooldown.default");
            long featherAugmentedCooldown = Infuse.getInstance().getConfig("feather.cooldown.augmented");
            long featherCooldown = isAugmentedFeather ? featherAugmentedCooldown : featherDefaultCooldown;
            long featherDefaultDuration = Infuse.getInstance().getConfig("feather.duration.default");
            long featherAugmentedDuration = Infuse.getInstance().getConfig("feather.duration.augmented");
            long featherDuration = isAugmentedFeather ? featherAugmentedDuration : featherDefaultDuration;
            CooldownManager.setDuration(playerUUID, "feather", featherDuration);
            CooldownManager.setCooldown(playerUUID, "feather", featherCooldown);
            Location anchor = player.getLocation();
            Bukkit.getRegionScheduler().runDelayed(plugin, anchor, (task) -> {
                if (player.isOnline()) {
                    CooldownManager.setDuration(playerUUID, "feathermace", 5L);
                }
            }, 10L);

            spark.add(playerUUID);
        }
    }

    public static boolean isEffect(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 2;
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
}
