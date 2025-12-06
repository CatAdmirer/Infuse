package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import com.catadmirer.infuseSMP.util.EffectUtil;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Ocean implements Listener {
    private final DataManager dataManager;

    public Ocean(Plugin plugin, DataManager dataManager) {
        this.dataManager = dataManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (Ocean.this.hasEffect(p, "1") || (Ocean.this.hasEffect(p, "2"))) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
        (new BukkitRunnable() {
            public void run() {
                for (Player effectHolder : Bukkit.getOnlinePlayers()) {
                    if (!Ocean.this.hasEffect(effectHolder, "1") || (!Ocean.this.hasEffect(effectHolder, "2"))) continue;

                    for (Player p : effectHolder.getWorld().getPlayers()) {
                        if (!p.equals(effectHolder) && p.getLocation().distance(effectHolder.getLocation()) <= 5 && p.getLocation().getBlock().isLiquid()) {
                            int currentAir = p.getRemainingAir();
                            int newAir = Math.max(currentAir - 5, -20);
                            p.setRemainingAir(newAir);
                            if (newAir <= 0) {
                                p.damage(1);
                            }
                        }
                    }
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player effectHolder : Bukkit.getOnlinePlayers()) {
                    if (!CooldownManager.isEffectActive(effectHolder.getUniqueId(), "ocean")) {
                        continue;
                    }
                    if (!hasEffect(effectHolder, "1") || (!hasEffect(effectHolder, "2"))) continue;
                    World world = effectHolder.getWorld();
                    Location holderLoc = effectHolder.getLocation();
                    double radius = Infuse.getInstance().getConfig("ocean_pulling.pull.radius");
                    double strength = Infuse.getInstance().getConfig("ocean_pulling.pull.strength");

                    for (Player p : world.getPlayers()) {
                        if (p.equals(effectHolder)) continue;
                        if (isTrusted(effectHolder, p)) continue;
                        if (p.getLocation().distance(holderLoc) <= radius) {
                            Vector direction = holderLoc.toVector().subtract(p.getLocation().toVector());
                            if (direction.lengthSquared() > 0.0001) {
                                Vector pullVector = direction.normalize().multiply(strength);
                                if (Double.isFinite(pullVector.getX()) && Double.isFinite(pullVector.getY()) && Double.isFinite(pullVector.getZ())) {
                                    p.setVelocity(pullVector);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, Infuse.getInstance().getConfig("ocean_pulling.pull.interval"));

    }

    private boolean isTrusted(Player player, Player caster) {
        return dataManager.isTrusted(caster, player);
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
            meta.setDisplayName(Infuse.getInstance().getEffectName(augmented ? "aug_ocean" : "ocean"));
            meta.setLore(Infuse.getInstance().getEffectLore(augmented ? "aug_ocean" : "ocean"));
            meta.setColor(Color.BLUE);

            if (augmented) meta.setCustomModelData(999);
            meta.getPersistentDataContainer().set(Infuse.EFFECT_ID, PersistentDataType.INTEGER, augmented ? 15 : 14);

            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isRegular(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 14;
    }

    public static boolean isAugmented(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 15;
    }

    public static boolean isEffect(ItemStack item) {
        return isRegular(item) || isAugmented(item);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (Infuse.getInstance().<Boolean>getConfig("invis_deaths")) {
            if (killer != null && killer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                String msg = Infuse.getInstance().getConfig("invis.kill_invis");
                msg = msg.replace("%victim%", victim.getName())
                        .replace("%killer%", "§7§kSomeone");
                event.deathMessage(Component.text(msg));
            } else if (victim.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                if (killer != null) {
                    String msg = Infuse.getInstance().getConfig("invis.death_invis");
                    msg = msg.replace("%victim%", "§7§kSomeone")
                            .replace("%killer%", killer.getName());
                    event.deathMessage(Component.text(msg));
                }
            }
        }
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffectName("ocean");
        String effectName2 = Infuse.getInstance().getEffectName("aug_ocean");
        return currentEffect != null && (currentEffect.equals(effectName) || currentEffect.equals((effectName2)));
    }

    public static void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "ocean")) {
            caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

            final double radius = 5;
            final World world = caster.getWorld();
            String effectName2 = Infuse.getInstance().getEffectName("aug_ocean");
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

            CooldownManager.setDuration(playerUUID, "ocean", duration);
            CooldownManager.setCooldown(playerUUID, "ocean", cooldown);

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
            }.runTaskTimer(Infuse.getInstance(), 0L, 10L);
        }
    }
}
