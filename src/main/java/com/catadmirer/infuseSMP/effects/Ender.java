package com.catadmirer.infuseSMP.effects;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Messages;
import com.catadmirer.infuseSMP.managers.CooldownManager;

import net.kyori.adventure.text.Component;

public class Ender extends InfuseEffect {
    public static final Component fireballName = Component.text("Cursing Projectile");
    public static final Set<UUID> cursedPlayers = new HashSet<>();

    public Ender() {
        super(EffectIds.ENDER, "ender", false);
    }

    public Ender(boolean augmented) {
        super(EffectIds.ENDER, "ender", augmented);
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
        return new Ender(false);
    }

    @Override
    public void equip(Infuse plugin, Player player) {}

    @Override
    public void unequip(Infuse plugin, Player player) {}

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

    public void cursePlayer(Infuse plugin, UUID playerUUID, long delayTicks) {
        cursedPlayers.add(playerUUID);

        Bukkit.getScheduler().runTaskLater(plugin, task -> cursedPlayers.remove(playerUUID), delayTicks);
    }

    public void applyGlowingToUntrusted(Infuse plugin, Player player) {
        double radius = 10;

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player nearby)) continue;
            if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
            if (plugin.getDataManager().isTrusted(nearby, player)) continue;
            nearby.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, false, false));
        }
    }

    public class Listeners implements Listener {
        private final Infuse plugin;
        private final Ender ender = new Ender();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
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
            if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;
            
            // Making sure the player has the ender effect
            Player player = event.getPlayer();
            if (!plugin.getDataManager().hasEffect(player, ender)) return;

            // Making sure the player used a bottle of dragons breath
            ItemStack item = player.getInventory().getItemInMainHand();
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

            if (!plugin.getDataManager().hasEffect(attacker, ender)) return;

            ender.cursePlayer(plugin, target.getUniqueId(), 1200);
        }

        @EventHandler
        public void onFireballDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof DragonFireball fireball)) return;
            if (!fireballName.equals(fireball.customName())) return;
            if (!(event.getEntity() instanceof Player target)) return;
            if (!(fireball.getShooter() instanceof Player shooter)) return;
            if (plugin.getDataManager().isTrusted(target, shooter)) return;

            ender.cursePlayer(plugin, target.getUniqueId(), 1200);

            event.setDamage(0);
        }

        @EventHandler
        public void onFireballHit(ProjectileHitEvent event) {
            if (!(event.getEntity() instanceof DragonFireball fireball)) return;
            if (!fireballName.equals(fireball.customName())) return;
            if (!(event.getHitEntity() instanceof Player target)) return;
            if (!(fireball.getShooter() instanceof Player shooter)) return;
            if (plugin.getDataManager().isTrusted(target, shooter)) return;

            ender.cursePlayer(plugin, target.getUniqueId(), 1200);
        }
    }
}