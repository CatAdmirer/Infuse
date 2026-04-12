package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;

import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Invis extends InfuseEffect {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);
    
    public Invis() {
        super(EffectIds.INVIS, "invis", false);
    }

    public Invis(boolean augmented) {
        super(EffectIds.INVIS, "invis", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_INVIS_NAME : MessageType.INVIS_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_INVIS_LORE : MessageType.INVIS_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Invis(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Invis(false);
    }

    @Override
    public void equip(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, -1, 0, false, false));
    }

    @Override
    public void unequip(Player player) {}

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "invis")) return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
                    
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "invis", duration, cooldown);

        final double radius = 10;
        final long durationTicks = duration * 20;
        final World world = player.getWorld();
        final Set<Player> vanishedPlayers = new HashSet<>();

        for (Player p : player.getWorld().getPlayers()) {
            if (player.getLocation().distance(p.getLocation()) <= radius && plugin.getDataManager().isTrusted(player, p)) {
                vanishedPlayers.add(player);
            }
        }

        for (Player vanished : vanishedPlayers) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(vanished) && !plugin.getDataManager().isTrusted(other, vanished)) {
                    other.hidePlayer(plugin, vanished);
                }
            }
        }

        (new BukkitRunnable() {
            long ticksElapsed = 0L;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    for (Player vanished : vanishedPlayers) {
                        for (Player other : Bukkit.getOnlinePlayers()) {
                            other.showPlayer(plugin, vanished);
                        }
                    }

                } else {
                    Location center = player.getLocation();

                    for(int angle = 0; angle < 360; angle += 2) {
                        double rad = Math.toRadians(angle);
                        double baseX = center.getX() + radius * Math.cos(rad);
                        double baseZ = center.getZ() + radius * Math.sin(rad);
                        DustOptions dustOptions = new DustOptions(Color.BLACK, 15);

                        for(int i = 0; i < 1; ++i) {
                            double offsetX = (Math.random() - 0.5) * 0.3;
                            double offsetZ = (Math.random() - 0.5) * 0.3;
                            Location particleLoc = new Location(world, baseX + offsetX, center.getY(), baseZ + offsetZ);
                            world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
                        }
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getWorld().equals(world) && p.getLocation().distance(center) <= radius && !plugin.getDataManager().isTrusted(p, player)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                        }
                    }

                    this.ticksElapsed += 10L;
                }
            }
        }).runTaskTimer(plugin, 0L, 10L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        String victimName;
        if (plugin.getMainConfig().invisHideDeaths() && plugin.getDataManager().hasEffect(killer, this)) {
            victimName = "<gray><obf>Someone";
        } else {
            victimName = mm.serialize(victim.displayName());
        }
        
        String killerName;
        if (plugin.getMainConfig().invisHideKills() && plugin.getDataManager().hasEffect(killer, this)) {
            killerName = "<gray><obf>Someone";
        } else {
            killerName = mm.serialize(killer.displayName());
        }

        Message msg = new Message(MessageType.DEATH_MESSAGE);
        msg.applyPlaceholder("victim", victimName);
        msg.applyPlaceholder("killer", killerName);
        event.deathMessage(msg.toComponent());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (!plugin.getDataManager().hasEffect(shooter, this)) return;
        if (!(event.getEntity() instanceof Arrow)) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
        this.spawnBlackParticles(target, 4);
    }

    @EventHandler
    public void onTenHits(TenHitEvent event) {
        Player attacker = event.getAttacker();
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        Player target = event.getTarget();
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
        this.spawnBlackParticles(target, 4);
    }

    private void spawnBlackParticles(final Player target, final int durationInSeconds) {
        (new BukkitRunnable() {
            int ticksElapsed = 0;
            final int maxTicks = durationInSeconds * 20;

            public void run() {
                if (this.ticksElapsed >= this.maxTicks) {
                    this.cancel();
                } else {
                    target.getWorld().spawnParticle(Particle.SQUID_INK, target.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0);
                    this.ticksElapsed += 5;
                }
            }
        }).runTaskTimer(plugin, 0L, 5L);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player target)) return;
        if (!plugin.getDataManager().hasEffect(target, this)) return;

        event.setCancelled(true);
    }
}