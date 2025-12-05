package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Invisibility implements Listener, PacketListener {
    
    private final Plugin plugin;
    private final DataManager trustManager;
    private final Map<UUID, Integer> meleeHitCounter = new HashMap<>();

    public Invisibility(Plugin plugin, DataManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (Invisibility.this.hasEffect(p, "1") || Invisibility.this.hasEffect(p, "2")) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffect("invis");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("invis");
            meta.setColor(Color.fromRGB(204, 51, 255));
            meta.setLore(lore);
            meta.setCustomModelData(7);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isEffect(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 7;
    }

    private void hideHealthForPlayer(final Player player, final int durationSeconds) {
        (new BukkitRunnable() {
            int elapsedTicks = 0;

            public void run() {
                WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(
                        20,
                        20,
                        5);
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
                this.elapsedTicks += 2;
                if (this.elapsedTicks >= durationSeconds * 20) {
                    this.cancel();
                }

            }
        }).runTaskTimer(this.plugin, 0L, 2L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player shooter) {
            if (this.hasEffect(shooter, "1") || this.hasEffect(shooter, "2")) {
                if (event.getEntity() instanceof Arrow) {
                    if (event.getHitEntity() instanceof Player target) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
                        this.hideHealthForPlayer(target, 4);
                        this.spawnBlackParticles(target, 4);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (event.getEntity() instanceof Player target) {
                if (this.hasEffect(attacker, "1") || this.hasEffect(attacker, "2")) {
                    int count = this.meleeHitCounter.getOrDefault(attacker.getUniqueId(), 0) + 1;
                    this.meleeHitCounter.put(attacker.getUniqueId(), count);
                    if (count >= 20) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
                        this.hideHealthForPlayer(target, 4);
                        this.spawnBlackParticles(target, 4);
                        this.meleeHitCounter.put(attacker.getUniqueId(), 0);
                    }
                }
            }
        }
    }

    private void spawnBlackParticles(final Player target, final int durationInSeconds) {
        (new BukkitRunnable() {
            int ticksElapsed = 0;
            final int maxTicks = durationInSeconds * 20;

            public void run() {
                if (this.ticksElapsed >= this.maxTicks) {
                    this.cancel();
                } else {
                    target.getWorld().spawnParticle(Particle.SQUID_INK, target.getLocation().add(0.0, 1.0, 0.0), 3, 0.5, 0.5, 0.5, 0.0);
                    this.ticksElapsed += 5;
                }
            }
        }).runTaskTimer(this.plugin, 0L, 5L);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player target) {
            if (this.hasEffect(target, "1") || this.hasEffect(target, "1")) {
                event.setCancelled(true);
            }
        }

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
                if (!CooldownManager.isOnCooldown(playerUUID, "invis")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName2 = Infuse.getInstance().getEffect("aug_invis");
        String effectName = Infuse.getInstance().getEffect("invis");
        return currentEffect != null && (currentEffect.equals(effectName2) || currentEffect.equals(effectName));
    }

    public void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "invis")) {
            caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            String effectName2 = Infuse.getInstance().getEffect("aug_invis");
            boolean isAugmentedInvis = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)))
                    || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName2)));
            long invisDefaultCooldown = Infuse.getInstance().getConfig("invisibility.cooldown.default");;
            long invisAugmentedCooldown = Infuse.getInstance().getConfig("invisibility.cooldown.augmented");;
            long invisCooldown = isAugmentedInvis ? invisAugmentedCooldown : invisDefaultCooldown;

            long invisDefaultDuration = Infuse.getInstance().getConfig("invisibility.duration.default");;
            long invisAugmentedDuration = Infuse.getInstance().getConfig("invisibility.duration.augmented");;
            long invisDuration = isAugmentedInvis ? invisAugmentedDuration : invisDefaultDuration;
            final double radius = 10.0;
            final long durationTicks = invisDuration * 20;
            final World world = caster.getWorld();
            final Set<Player> vanishedPlayers = new HashSet<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(world) && player.getLocation().distance(caster.getLocation()) <= radius && this.isTeammate(caster, player)) {
                    vanishedPlayers.add(player);
                }
            }

            for (Player vanished : vanishedPlayers) {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!other.equals(vanished) && !this.isTeammate(other, vanished)) {
                        other.hidePlayer(this.plugin, vanished);
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
                                other.showPlayer(Invisibility.this.plugin, vanished);
                            }
                        }

                    } else {
                        Location center = caster.getLocation();

                        for(int angle = 0; angle < 360; angle += 2) {
                            double rad = Math.toRadians(angle);
                            double baseX = center.getX() + radius * Math.cos(rad);
                            double baseZ = center.getZ() + radius * Math.sin(rad);
                            DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 0, 0), 15);

                            for(int i = 0; i < 1; ++i) {
                                double offsetX = (Math.random() - 0.5) * 0.3;
                                double offsetZ = (Math.random() - 0.5) * 0.3;
                                Location particleLoc = new Location(world, baseX + offsetX, center.getY(), baseZ + offsetZ);
                                world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
                            }
                        }

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getWorld().equals(world) && p.getLocation().distance(center) <= radius && !Invisibility.this.isTeammate(p, caster)) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                                Invisibility.this.hideHealthForPlayer(p, 2);
                            }
                        }

                        this.ticksElapsed += 10L;
                    }
                }
            }).runTaskTimer(this.plugin, 0L, 10L);

            CooldownManager.setDuration(playerUUID, "invis", invisDuration);
            CooldownManager.setCooldown(playerUUID, "invis", invisCooldown);
        }
    }

    private boolean isTeammate(Player player, Player caster) {
        return trustManager.isTrusted(player, caster);
    }
}
