package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final EffectManager trustManager;
    private final Map<UUID, Integer> meleeHitCounter = new HashMap();

    public Invisibility(Plugin plugin, EffectManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                Iterator var1 = Bukkit.getOnlinePlayers().iterator();

                while(var1.hasNext()) {
                    Player p = (Player)var1.next();
                    if (Invisibility.this.hasImmortalHackEquipped2(p, "1") || Invisibility.this.hasImmortalHackEquipped2(p, "2")) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
    }

    public static ItemStack createStealthGem() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("invis");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("invis");
            meta.setColor(Color.fromRGB(204, 51, 255));
            meta.setLore(lore);
            meta.setCustomModelData(7);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static boolean isStealthGem(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 7;
    }

    private void hideHealthForPlayer(final Player player, final int durationSeconds) {
        (new BukkitRunnable() {
            int elapsedTicks = 0;

            public void run() {
                WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(
                        20.0F,
                        20,
                        5.0F);
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
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player)event.getEntity().getShooter();
            if (this.hasImmortalHackEquipped2(shooter, "1") || this.hasImmortalHackEquipped2(shooter, "2")) {
                if (event.getEntity() instanceof Arrow) {
                    if (event.getHitEntity() instanceof Player) {
                        Player target = (Player)event.getHitEntity();
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
        if (event.getDamager() instanceof Player) {
            if (event.getEntity() instanceof Player) {
                Player attacker = (Player)event.getDamager();
                Player target = (Player)event.getEntity();
                if (this.hasImmortalHackEquipped2(attacker, "1") || this.hasImmortalHackEquipped2(attacker, "2")) {
                    int count = (Integer)this.meleeHitCounter.getOrDefault(attacker.getUniqueId(), 0) + 1;
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
                    target.getWorld().spawnParticle(Particle.SQUID_INK, target.getLocation().add(0.0D, 1.0D, 0.0D), 3, 0.5D, 0.5D, 0.5D, 0.0D);
                    this.ticksElapsed += 5;
                }
            }
        }).runTaskTimer(this.plugin, 0L, 5L);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player)event.getTarget();
            if (this.hasImmortalHackEquipped2(target, "1") || this.hasImmortalHackEquipped2(target, "1")) {
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
            boolean isLegendary = player.isSneaking() && this.hasImmortalHackEquipped2(player, "1");
            boolean isCommon = !player.isSneaking() && this.hasImmortalHackEquipped2(player, "2");
            if (isLegendary || isCommon) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "invis")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasImmortalHackEquipped2(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName2 = Infuse.getInstance().getEffect("aug_invis");
        String gemName = Infuse.getInstance().getEffect("invis");
        return currentHack != null && (currentHack.equals(gemName2) || currentHack.equals(gemName));
    }

    public void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();
        if (!CooldownManager.isOnCooldown(playerUUID, "invis")) {
            caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
            String gemName2 = Infuse.getInstance().getEffect("aug_invis");
            boolean isAugmentedInvis = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2)))
                    || (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2)));
            long invisDefaultCooldown = ((Integer) Infuse.getInstance().getCanfig("invisibility.cooldown.default")).longValue();
            long invisAugmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("invisibility.cooldown.augmented")).longValue();
            long invisCooldown = isAugmentedInvis ? invisAugmentedCooldown : invisDefaultCooldown;

            long invisDefaultDuration = ((Integer) Infuse.getInstance().getCanfig("invisibility.duration.default")).longValue();
            long invisAugmentedDuration = ((Integer) Infuse.getInstance().getCanfig("invisibility.duration.augmented")).longValue();
            long invisDuration = isAugmentedInvis ? invisAugmentedDuration : invisDefaultDuration;
            final double radius = 10.0D;
            final long durationTicks = invisDuration * 20;
            final World world = caster.getWorld();
            final Set<Player> vanishedPlayers = new HashSet();
            Iterator var9 = Bukkit.getOnlinePlayers().iterator();

            Player vanished;
            while(var9.hasNext()) {
                vanished = (Player)var9.next();
                if (vanished.getWorld().equals(world) && vanished.getLocation().distance(caster.getLocation()) <= radius && this.isTeammate(caster, vanished)) {
                    vanishedPlayers.add(vanished);
                }
            }

            var9 = vanishedPlayers.iterator();

            while(var9.hasNext()) {
                vanished = (Player)var9.next();
                Iterator var11 = Bukkit.getOnlinePlayers().iterator();

                while(var11.hasNext()) {
                    Player other = (Player)var11.next();
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
                        Iterator var16 = vanishedPlayers.iterator();

                        while(var16.hasNext()) {
                            Player vanished = (Player)var16.next();
                            Iterator var20 = Bukkit.getOnlinePlayers().iterator();

                            while(var20.hasNext()) {
                                Player other = (Player)var20.next();
                                other.showPlayer(Invisibility.this.plugin, vanished);
                            }
                        }

                    } else {
                        Location center = caster.getLocation();

                        for(int angle = 0; angle < 360; angle += 2) {
                            double rad = Math.toRadians((double)angle);
                            double baseX = center.getX() + radius * Math.cos(rad);
                            double baseZ = center.getZ() + radius * Math.sin(rad);
                            DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 0, 0), 15.0F);

                            for(int i = 0; i < 1; ++i) {
                                double offsetX = (Math.random() - 0.5D) * 0.3D;
                                double offsetZ = (Math.random() - 0.5D) * 0.3D;
                                Location particleLoc = new Location(world, baseX + offsetX, center.getY(), baseZ + offsetZ);
                                world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
                            }
                        }

                        Iterator var17 = Bukkit.getOnlinePlayers().iterator();

                        while(var17.hasNext()) {
                            Player p = (Player)var17.next();
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
