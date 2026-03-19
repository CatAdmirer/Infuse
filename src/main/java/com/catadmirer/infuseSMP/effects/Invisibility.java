package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Invisibility implements Listener {
    public static final MiniMessage mm = MiniMessage.miniMessage();

    private static Infuse plugin;

    public Invisibility(Infuse plugin) {
        Invisibility.plugin = plugin;
    }

    public static void applyPassiveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        String victimName;
        if (plugin.getMainConfig().invisHideDeaths() && plugin.getDataManager().hasEffect(killer, EffectMapping.INVIS)) {
            victimName = "<gray><obf>Someone";
        } else {
            victimName = mm.serialize(victim.displayName());
        }
        
        String killerName;
        if (plugin.getMainConfig().invisHideKills() && plugin.getDataManager().hasEffect(killer, EffectMapping.INVIS)) {
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
        if (!plugin.getDataManager().hasEffect(shooter, EffectMapping.INVIS)) return;
        if (!(event.getEntity() instanceof Arrow)) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
        this.spawnBlackParticles(target, 4);
    }

    @EventHandler
    public void onTenHits(TenHitEvent event) {
        Player attacker = event.getAttacker();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.INVIS)) return;

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
        if (!plugin.getDataManager().hasEffect(target, EffectMapping.INVIS)) return;

        event.setCancelled(true);
    }

    public static void activateSpark(Boolean isAugmented, Player caster) {
        UUID playerUUID = caster.getUniqueId();
        if (CooldownManager.isOnCooldown(playerUUID, "invis")) return;

        caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
                    
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_INVIS : EffectMapping.INVIS);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_INVIS : EffectMapping.INVIS);

        CooldownManager.setDuration(playerUUID, "invis", duration);
        CooldownManager.setCooldown(playerUUID, "invis", cooldown);

        final double radius = 10;
        final long durationTicks = duration * 20;
        final World world = caster.getWorld();
        final Set<Player> vanishedPlayers = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(world) && player.getLocation().distance(caster.getLocation()) <= radius && isTeammate(caster, player)) {
                vanishedPlayers.add(player);
            }
        }

        for (Player vanished : vanishedPlayers) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(vanished) && !isTeammate(other, vanished)) {
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
                    Location center = caster.getLocation();

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
                        if (p.getWorld().equals(world) && p.getLocation().distance(center) <= radius && !isTeammate(p, caster)) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                        }
                    }

                    this.ticksElapsed += 10L;
                }
            }
        }).runTaskTimer(plugin, 0L, 10L);
    }

    private static boolean isTeammate(Player player, Player caster) {
        return plugin.getDataManager().isTrusted(player, caster);
    }
}
