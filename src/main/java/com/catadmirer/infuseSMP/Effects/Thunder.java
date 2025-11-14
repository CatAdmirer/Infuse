package com.catadmirer.infuseSMP.Effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class Thunder implements Listener {
    
    private final Set<UUID> activeSparks = new HashSet();
    private final Map<UUID, Long> entityLightningCooldowns = new HashMap();

    private DataManager trustManager;

    private final Infuse plugin;

    public Thunder(Infuse plugin, DataManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static ItemStack createTHUNDER() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("thunder");
            meta.setDisplayName(gemName);
            meta.setColor(Color.fromRGB(255, 255, 0));
            List<String> lore = Infuse.getInstance().getEffectLore("thunder");
            meta.setLore(lore);
            meta.setCustomModelData(13);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    public static boolean isInvincibilityGem(ItemStack item) {
        if (item != null && item.getType() == Material.POTION && item.getItemMeta() != null) {
            return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 13;
        } else {
            return false;
        }
    }

    private boolean hasImmortalHackEquipped(Player player) {
        String hack1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
        String hack2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
        String gemName = plugin.getEffect("thunder");
        String gemName2 = plugin.getEffect("aug_thunder");
        return hack1 != null && (hack1.equals(gemName) || hack2 != null && (hack2.equals(gemName2)));
    }

    @EventHandler
    public void onTridentHit(EntityDamageByEntityEvent event) {
        Entity var3 = event.getDamager();
        if (var3 instanceof Trident) {
            Trident trident = (Trident)var3;
            if (!trident.hasMetadata("thunderProcessed")) {
                trident.setMetadata("thunderProcessed", new FixedMetadataValue(plugin, true));
                ProjectileSource var4 = trident.getShooter();
                if (var4 instanceof Player) {
                    Player attacker = (Player)var4;
                    if (this.hasImmortalHackEquipped(attacker)) {
                        Entity var5 = event.getEntity();
                        if (var5 instanceof LivingEntity) {
                            LivingEntity target = (LivingEntity)var5;
                            target.getWorld().strikeLightningEffect(target.getLocation());
                            target.damage(4.0D, attacker);
                            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0.0D, 1.0D, 0.0D), 10, 0.5D, 0.5D, 0.5D, 0.0D, new DustOptions(Color.YELLOW, 1.5F));
                        }
                    }
                }
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
            boolean isLegendary = player.isSneaking() && this.hasThunderEquipped(player, "1");
            boolean isCommon = !player.isSneaking() && this.hasThunderEquipped(player, "2");
            if (isLegendary || isCommon) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "thunder")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasThunderEquipped(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = plugin.getEffect("thunder");
        String gemName2 = plugin.getEffect("aug_thunder");
        return currentHack != null && (currentHack.equals(gemName) || (currentHack.equals(gemName2)));
    }

    public void activateSpark(final Player caster) {
        final UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "thunder") && !this.activeSparks.contains(playerUUID)) {
            this.activeSparks.add(playerUUID);
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
            String gemName2 = plugin.getEffect("aug_thunder");
            boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1"))
                            .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(gemName2)))) ||
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2"))
                                    .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(gemName2))));
            long defaultCooldown = ((Integer) Infuse.getInstance().getCanfig("thunder.cooldown.default")).longValue();
            long augmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("thunder.cooldown.augmented")).longValue();
            long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;

            long defaultDuration = ((Integer) Infuse.getInstance().getCanfig("thunder.duration.default")).longValue();
            long augmentedDuration = ((Integer) Infuse.getInstance().getCanfig("thunder.duration.augmented")).longValue();
            long duration = isAugmented ? augmentedDuration : defaultDuration;
            final int effectDuration = (int) (duration * 20L);

            CooldownManager.setDuration(playerUUID, "thunder", duration);
            CooldownManager.setCooldown(playerUUID, "thunder", cooldown);

            final double radius = 10.0D;
            final World world = caster.getWorld();

            new BukkitRunnable() {
                int ticksElapsed = 0;

                public void run() {
                    if (this.ticksElapsed >= effectDuration) {
                        Thunder.this.activeSparks.remove(playerUUID);
                        this.cancel();
                        return;
                    }

                    Location center = caster.getLocation();
                    for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                        if (!(entity instanceof LivingEntity)) continue;
                        LivingEntity target = (LivingEntity) entity;
                        if (target.equals(caster)) continue;

                        if (target instanceof Player) {
                            Player p = (Player) target;
                            if (Thunder.this.isTeammate(p, caster)) continue;
                        }

                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(4.0D, caster);
                        world.spawnParticle(Particle.DUST, target.getLocation().add(0.0D, 1.0D, 0.0D), 10, 0.5D, 0.5D, 0.5D, 0.0D, new DustOptions(Color.YELLOW, 1.5F));
                    }

                    this.ticksElapsed += 20;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }


    private boolean isTeammate(Player player, Player caster) {
        return trustManager.isTrusted(player, caster);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player)event.getDamager();
            if (this.hasImmortalHackEquipped(attacker)) {
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity)event.getEntity();
                    UUID targetUUID = target.getUniqueId();
                    long currentTime = System.currentTimeMillis();
                    if (this.entityLightningCooldowns.containsKey(targetUUID)) {
                        long lastStrikeTime = (Long)this.entityLightningCooldowns.get(targetUUID);
                        if (currentTime - lastStrikeTime < 2000L) {
                            return;
                        }
                    }

                    this.entityLightningCooldowns.put(targetUUID, currentTime);
                    List<Entity> nearbyEntities = target.getNearbyEntities(3.0D, 3.0D, 3.0D);
                    Optional<Entity> nextChainTarget = nearbyEntities.stream().filter((e) -> {
                        return e instanceof LivingEntity && !e.equals(attacker);
                    }).findFirst();
                    if (nextChainTarget.isPresent()) {
                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(4.0D, attacker);
                        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0.0D, 1.0D, 0.0D), 10, 0.5D, 0.5D, 0.5D, 0.0D, new DustOptions(Color.YELLOW, 1.5F));
                        this.chainLightning(target, attacker);
                    }

                }
            }
        }
    }

    private void chainLightning(Entity startEntity, final Player attacker) {
        final Set<Entity> processedEntities = new HashSet();
        final Queue<Entity> queue = new LinkedList();
        queue.add(startEntity);
        (new BukkitRunnable() {
            int strikes = 0;

            public void run() {
                if (!queue.isEmpty() && this.strikes < 5) {
                    Entity currentEntity = null;

                    while(!queue.isEmpty()) {
                        Entity candidate = (Entity)queue.poll();
                        if (candidate instanceof LivingEntity && !processedEntities.contains(candidate)) {
                            currentEntity = candidate;
                            break;
                        }
                    }

                    if (currentEntity != null) {
                        processedEntities.add(currentEntity);
                        LivingEntity livingEntity = (LivingEntity)currentEntity;
                        if (!livingEntity.equals(attacker)) {
                            livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());
                            livingEntity.damage(4.0D, attacker);
                            livingEntity.getWorld().spawnParticle(Particle.DUST, livingEntity.getLocation().add(0.0D, 1.0D, 0.0D), 10, 0.5D, 0.5D, 0.5D, 0.0D, new DustOptions(Color.YELLOW, 1.5F));
                            ++this.strikes;
                            Iterator var3 = livingEntity.getNearbyEntities(3.0D, 3.0D, 3.0D).iterator();

                            while(var3.hasNext()) {
                                Entity entity = (Entity)var3.next();
                                if (entity instanceof LivingEntity && !processedEntities.contains(entity)) {
                                    queue.add(entity);
                                }
                            }

                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
    }
}