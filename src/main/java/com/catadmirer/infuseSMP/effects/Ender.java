package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.kyori.adventure.text.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.slf4j.LoggerFactory;

public class Ender extends InfuseEffect {
    public static final Component fireballName = Component.text("Cursing Projectile");
    public static final Set<UUID> cursedPlayers = new HashSet<>();

    public Ender() {
        this(false);
    }

    public Ender(boolean augmented) {
        super(EffectIds.ENDER, "ender", augmented);
    }

    public Ender(DataManager dataManager, Infuse plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getDataManager().hasEffect(player, new Ender())) {
                    applyGlowingToUntrusted(player);
                }

                // Spawning particles on cursed players
                if (cursedPlayers.contains(player.getUniqueId())) {
                    player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);
                }
            }
        }, 0L, 20L);
    }

    @Override
    public Component getItemName() {
        return augmented ? Messages.AUG_ENDER_NAME.toComponent() : Messages.ENDER_NAME.toComponent();
    }

    @Override
    public List<Component> getItemLore() {
        return augmented ? Messages.AUG_ENDER_LORE.getComponentList() : Messages.ENDER_LORE.getComponentList();
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Ender(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Ender();
    }

    @Override
    public void activateSpark(Infuse plugin, Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getConfigFile().cooldown(this);
        long duration = plugin.getConfigFile().duration(this);

        CooldownManager.setDuration(playerUUID, "ender", duration);
        CooldownManager.setCooldown(playerUUID, "ender", cooldown);

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Teleporting the player in the direction they're looking
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
        return loc.getBlock().getType().isAir() && loc.clone().add(0, 1, 0).getBlock().getType().isAir();
    }

    public void cursePlayer(Player toCurse) {

    }

    

    public void shootCursingFireball(Player player) {
        UUID uuid = player.getUniqueId();

        if (CooldownManager.isOnCooldown(player.getUniqueId(), "ender_fireball")) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        Fireball fireball = player.launchProjectile(DragonFireball.class);
        fireball.setIsIncendiary(false);
        fireball.customName(fireballName);

        CooldownManager.setCooldown(uuid, "ender_fireball", 30);

        Vector velocity = fireball.getVelocity();
        velocity.multiply(2.0);
        fireball.setVelocity(velocity);
    }

    public static void cursePlayer(Infuse plugin, UUID playerUUID, long durationTicks) {
        cursedPlayers.add(playerUUID);

        Bukkit.getScheduler().runTaskLater(plugin, task -> cursedPlayers.remove(playerUUID), durationTicks);
    }

    public void applyGlowingToUntrusted(Infuse plugin) {
        if (owner == null) {
            plugin.getLogger().log(Level.SEVERE, "Cannot use Ender#applyGlowingToUntrusted without setting an owner for the effect.", new IllegalStateException());
            return;
        }

        if (!owner.isOnline()) {
            plugin.getLogger().log(Level.SEVERE, "Cannot use Ender#applyGlowingToUntrusted with effects whose owner is offline.", new IllegalStateException());
            return;
        }

        Player player = owner.getPlayer();
        
        if (!plugin.getDataManager().hasEffect(player, new Ender())) return;

        double radius = 10;

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player nearby)) continue;
            if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
            if (plugin.getDataManager().isTrusted(nearby, player)) continue;
            nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false));
        }
    }
}