package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Ender extends InfuseEffect {
    public static final Component fireballName = Component.text("Cursing Projectile");
    public static final Set<UUID> cursedPlayers = new HashSet<>();

    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);

    public Ender() {
        super(EffectIds.ENDER, "ender", false);
    }

    public Ender(boolean augmented) {
        super(EffectIds.ENDER, "ender", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_ENDER_NAME : MessageType.ENDER_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_ENDER_LORE : MessageType.ENDER_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Ender(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Ender(false);
    }

    @Override
    public void equip(Player player) {}

    @Override
    public void unequip(Player player) {}

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;
        
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "ender", duration, cooldown);

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

    private boolean isSafeTeleportLocation(Location loc) {
        return loc.getBlock().getType().isAir() && loc.clone().add(0, 1, 0).getBlock().getType().isAir();
    }

    public void shootCursingFireball(Player player) {
        UUID uuid = player.getUniqueId();

        if (CooldownManager.isOnCooldown(player.getUniqueId(), "ender_fireball")) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            // Is this necessary?
            player.getInventory().setItemInMainHand(null);
        }

        Fireball fireball = player.launchProjectile(DragonFireball.class);
        fireball.setIsIncendiary(false);
        fireball.customName(fireballName);

        CooldownManager.setCooldown(uuid, "ender_fireball", 30);

        Vector velocity = fireball.getVelocity();
        velocity.multiply(2);
        fireball.setVelocity(velocity);
    }

    public void cursePlayer(UUID playerUUID, long delayTicks) {
        cursedPlayers.add(playerUUID);

        Bukkit.getScheduler().runTaskLater(plugin, task -> cursedPlayers.remove(playerUUID), delayTicks);
    }

    public void applyGlowingToUntrusted(Player player) {
        double radius = 10;

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player nearby)) continue;
            if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
            if (plugin.getDataManager().isTrusted(nearby, player)) continue;
            nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) return;

        // Making sure the damaged player is cursed
        UUID damagedUUID = damagedPlayer.getUniqueId();
        if (!cursedPlayers.contains(damagedUUID)) return;

        // Making sure the damage source isn't the one made by this plugin (prevents looping curse damage)
        if (event.getDamageSource().getDamageType() == DamageType.CAMPFIRE && event.getDamageSource().getDirectEntity() != null) return;
        
        // Making the fake damageSource
        DamageSource fakeSource = DamageSource.builder(DamageType.CAMPFIRE).withDirectEntity(damagedPlayer).build();

        // Sharing curse damage with all other cursed players
        for (UUID cursedUUID : cursedPlayers) {
            // Skipping the player who was hit
            if (cursedUUID == damagedUUID) continue;

            Player player = Bukkit.getPlayer(cursedUUID);
            player.damage(event.getDamage(), fakeSource);
        }
    }

    @EventHandler
    public void enderOnehitMobs(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (event.getEntity() instanceof Player) return;

        UUID attackerUUID = attacker.getUniqueId();
        if (CooldownManager.isEffectActive(attackerUUID, "ender")) {
            mob.setHealth(0);
        }
    }

    @EventHandler
    public void onUseDragonBreath(PlayerInteractEvent event) {
        // Listening for right clicks
        Action action = event.getAction();
        if (!action.isRightClick()) return;
        
        // Making sure the player has the ender effect
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        // Making sure the player used a bottle of dragons breath
        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getType() != Material.DRAGON_BREATH) return;

        // Making sure the cursing fireball isn't on cooldown
        if (CooldownManager.isOnCooldown(player.getUniqueId(), "ender_fireball")) return;

        shootCursingFireball(player);
        event.setCancelled(true);
    }

    @EventHandler
    public void curseOnHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        cursePlayer(target.getUniqueId(), 1200);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof DragonFireball fireball)) return;
        if (!fireballName.equals(fireball.customName())) return;
        if (!(event.getEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (plugin.getDataManager().isTrusted(target, shooter)) return;

        cursePlayer(target.getUniqueId(), 1200);

        event.setDamage(0);
    }

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof DragonFireball fireball)) return;
        if (!fireballName.equals(fireball.customName())) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (plugin.getDataManager().isTrusted(target, shooter)) return;

        cursePlayer(target.getUniqueId(), 1200);
    }
}