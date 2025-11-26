package com.catadmirer.infuseSMP.effects;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.particles.AlsoParticles;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Speed implements Listener {
    
    private static Plugin plugin;
    private final Map<UUID, Integer> speedLevels = new HashMap<>();
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private final Map<UUID, Long> bowPullStartTime = new HashMap<>();

    public Speed(Plugin plugin) {
        Speed.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!Speed.this.hasSpeed(p, "1") && !Speed.this.hasSpeed(p, "2")) continue;

                    UUID uuid = p.getUniqueId();
                    long lastHit = Speed.this.lastHitTime.getOrDefault(uuid, 0L);
                    if (System.currentTimeMillis() - lastHit > 1000L) {
                        Speed.this.speedLevels.put(uuid, 1);
                    }

                    int currentLevel = Speed.this.speedLevels.getOrDefault(uuid, 1);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, Math.max(0, currentLevel - 1), false, false, false));
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (this.hasSpeed(player, "1") || this.hasSpeed(player, "2")) {
                long startTime = this.bowPullStartTime.getOrDefault(player.getUniqueId(), 0L);
                long pullTimeMs = System.currentTimeMillis() - startTime;
                double adjustedPullTimeMs = pullTimeMs * 1.8;
                float pullFraction = (float)Math.min(adjustedPullTimeMs / 1000.0, 1.0);
                event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(pullFraction));
                this.bowPullStartTime.remove(player.getUniqueId());
            }
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

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (this.hasSpeed(player, "1") || this.hasSpeed(player, "2")) {
                UUID uuid = player.getUniqueId();
                long currentTime = System.currentTimeMillis();
                long lastHit = this.lastHitTime.getOrDefault(uuid, 0L);
                if (currentTime - lastHit >= 50L) {
                    this.lastHitTime.put(uuid, currentTime);
                    this.speedLevels.put(uuid, this.speedLevels.getOrDefault(uuid, 1) + 1);
                    if (event.getEntity() instanceof LivingEntity target) {
                        int currentNoDamageTicks = target.getNoDamageTicks();
                        target.setNoDamageTicks(currentNoDamageTicks / 2);
                    }

                }
            }
        }
    }

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();

        if (meta != null) {
            String effectName = Infuse.getInstance().getEffect("speed");
            effectName = applyHexColors(effectName);
            meta.setDisplayName(effectName);
            meta.setColor(Color.AQUA);
            List<String> lore = new ArrayList<>(Infuse.getInstance().getEffectLore("speed"));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, applyHexColors(lore.get(i)));
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setLore(lore);
            meta.setCustomModelData(10);
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

    private boolean hasSpeed(Player player, String tier) {
        String effectName = Infuse.getInstance().getEffect("speed");
        String effectName2 = Infuse.getInstance().getEffect("aug_speed");
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
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
                if (!CooldownManager.isOnCooldown(playerUUID, "speed")) {
                    event.setCancelled(true);
                    activateSpark(player);
                }
            }
        }
    }

    private boolean hasEffect(Player player, String tier) {
        String effectName = Infuse.getInstance().getEffect("speed");
        String effectName2 = Infuse.getInstance().getEffect("aug_speed");
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals(effectName2));
    }

    public static void activateSpark(final Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "speed")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            AlsoParticles.spawnEffect(player, Color.fromRGB(0xD1A44B));
            final Vector direction = player.getEyeLocation().getDirection().normalize();
            double playerVelocityMultiplier = Infuse.getInstance().getConfig("speed.playerVelocityMultiplier");
            player.setVelocity(direction.clone().multiply(playerVelocityMultiplier));
            final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0xE6DCAA), 1.5F);
            final Location[] previousLocation = new Location[]{player.getLocation().clone()};
            final int[] ticksPassed = new int[]{0};
            final Location anchor = player.getLocation();
            Bukkit.getRegionScheduler().runAtFixedRate(plugin, anchor, (task) -> {
                if (!player.isOnline()) {
                    task.cancel();
                    return;
                }

                Location currentLocation = player.getLocation();
                double distance = previousLocation[0].distance(currentLocation);

                if (distance > 0.1) {
                    Vector step = currentLocation.toVector().subtract(previousLocation[0].toVector()).normalize().multiply(0.3);
                    Location particleLocation = previousLocation[0].clone();

                    for (double d = 0.0; d <= distance; d += step.length()) {
                        particleLocation.add(step);
                        player.getWorld().spawnParticle(Particle.DUST, particleLocation, 5, 0.1, 0.05, 0.1, 0.05, dustOptions);
                    }

                    previousLocation[0] = currentLocation.clone();
                }

                if (ticksPassed[0] >= 3 && player.isOnGround()) {
                    task.cancel();
                }

                ticksPassed[0]++;
            }, 1L, 1L);
            
            String augmentedName = ChatColor.stripColor(Infuse.getInstance().getEffect("aug_speed").toLowerCase());
            boolean isAugmented = augmentedName.equals(ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").toLowerCase())) ||
                                  augmentedName.equals(ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").toLowerCase()));

            long cooldown = Infuse.getInstance().getConfig(isAugmented ? "speed.cooldown.augmented" : "speed.cooldown.default");
            long duration = Infuse.getInstance().getConfig(isAugmented ? "speed.duration.augmented" : "speed.duration.default");

            CooldownManager.setDuration(playerUUID, "speed", duration);
            CooldownManager.setCooldown(playerUUID, "speed", cooldown);
        }
    }




    public static boolean isEffect(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 10;
    }
}