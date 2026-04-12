package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Ocean extends InfuseEffect {
    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);
    
    public Ocean() {
        super(EffectIds.OCEAN, "ocean", false);
    }

    public Ocean(boolean augmented) {
        super(EffectIds.OCEAN, "ocean", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_OCEAN_NAME : MessageType.OCEAN_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_OCEAN_LORE : MessageType.OCEAN_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Ocean(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Ocean(false);
    }

    @Override
    public void equip(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
    }

    @Override
    public void unequip(Player player) {}

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "ocean")) return;

        // Applying effects for the ocean spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final double radius = plugin.getMainConfig().oceanPullRadius();
        final World world = player.getWorld();
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "ocean", duration, cooldown);

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
                    double x = player.getLocation().getX() + radius * Math.cos(rad);
                    double z = player.getLocation().getZ() + radius * Math.sin(rad);
                    Location particleLoc = new Location(world, x, player.getLocation().getY(), z);
                    world.spawnParticle(Particle.FALLING_WATER, particleLoc, 1);
                }

                this.ticksElapsed += 10L;
            }
        }.runTaskTimer(plugin, 0L, 10L);

        // Ocean pull runnable
        new BukkitRunnable() {
            @Override
            public void run() {
                // Stopping when the spark has run out
                if (!CooldownManager.isEffectActive(player.getUniqueId(), "ocean")) {
                    cancel();
                    return;
                }

                Location holderLoc = player.getLocation();
                double strength = plugin.getMainConfig().oceanPullStrength();

                for (Player p : world.getPlayers()) {
                    if (p.equals(player)) continue;
                    if (plugin.getDataManager().isTrusted(player, p)) continue;
                    if (p.getLocation().distance(holderLoc) > radius) continue;

                    Vector direction = holderLoc.toVector().subtract(p.getLocation().toVector());
                    if (direction.lengthSquared() > 0.0001) {
                        Vector pullVector = direction.normalize().multiply(strength);
                        if (Double.isFinite(pullVector.getX()) && Double.isFinite(pullVector.getY()) && Double.isFinite(pullVector.getZ())) {
                            p.setVelocity(pullVector);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, plugin.getMainConfig().oceanPullInterval());
    }
}