package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.DataManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;

public class Thunder implements Listener {
    
    private static final Set<UUID> activeSparks = new HashSet<>();
    private final Map<UUID, Long> entityLightningCooldowns = new HashMap<>();

    private static DataManager trustManager;

    private static Infuse plugin;

    public Thunder(Infuse plugin, DataManager trustManager) {
        Thunder.plugin = plugin;
        Thunder.trustManager = trustManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName("thunder"));
            meta.setLore(Infuse.getInstance().getEffectLore("thunder"));
            meta.setColor(Color.YELLOW);
            meta.setCustomModelData(13);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isEffect(ItemStack item) {
        if (item != null && item.getType() == Material.POTION && item.getItemMeta() != null) {
            return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 13;
        } else {
            return false;
        }
    }

    private boolean hasEffect(Player player) {
        String effect1 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "1");
        String effect2 = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), "2");
        String effectName = plugin.getEffectName("thunder");
        String effectName2 = plugin.getEffectName("aug_thunder");
        return effect1 != null && (effect1.equals(effectName) || effect2 != null && (effect2.equals(effectName2)));
    }

    @EventHandler
    public void onTridentHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Trident trident) {
            if (!trident.hasMetadata("thunderProcessed")) {
                trident.setMetadata("thunderProcessed", new FixedMetadataValue(plugin, true));
                if (trident.getShooter() instanceof Player attacker) {
                    if (this.hasEffect(attacker)) {
                        if (event.getEntity() instanceof LivingEntity target) {
                            target.getWorld().strikeLightningEffect(target.getLocation());
                            target.damage(4.0, attacker);
                            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0, new DustOptions(Color.YELLOW, 1.5F));
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
            boolean isPrimary = player.isSneaking() && this.hasThunderEquipped(player, "1");
            boolean isSecondary = !player.isSneaking() && this.hasThunderEquipped(player, "2");
            if (isPrimary || isSecondary) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "thunder")) {
                    event.setCancelled(true);
                    activateSpark(player);
                }
            }
        }
    }

    private boolean hasThunderEquipped(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = plugin.getEffectName("thunder");
        String effectName2 = plugin.getEffectName("aug_thunder");
        return currentEffect != null && (currentEffect.equals(effectName) || (currentEffect.equals(effectName2)));
    }

    public static void activateSpark(final Player caster) {
        final UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "thunder") && !activeSparks.contains(playerUUID)) {
            activeSparks.add(playerUUID);
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

            String augmentedName = ChatColor.stripColor(Infuse.getInstance().getEffectName("aug_thunder").toLowerCase());
            boolean isAugmented = augmentedName.equals(ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1").toLowerCase())) ||
                                  augmentedName.equals(ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2").toLowerCase()));

            long cooldown = Infuse.getInstance().getConfig(isAugmented ? "thunder.cooldown.augmented" : "thunder.cooldown.default");
            long duration = Infuse.getInstance().getConfig(isAugmented ? "thunder.duration.augmented" : "thunder.duration.default");

            CooldownManager.setDuration(playerUUID, "thunder", duration);
            CooldownManager.setCooldown(playerUUID, "thunder", cooldown);

            final long effectDuration = duration * 20;

            final double radius = 10.0;
            final World world = caster.getWorld();

            new BukkitRunnable() {
                int ticksElapsed = 0;

                public void run() {
                    if (this.ticksElapsed >= effectDuration) {
                        activeSparks.remove(playerUUID);
                        this.cancel();
                        return;
                    }

                    Location center = caster.getLocation();
                    for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                        if (!(entity instanceof LivingEntity target)) continue;
                        if (target.equals(caster)) continue;

                        if (target instanceof Player p) {
                            if (isTeammate(p, caster)) continue;
                        }

                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(4.0, caster);
                        world.spawnParticle(Particle.DUST, target.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0, new DustOptions(Color.YELLOW, 1.5F));
                    }

                    this.ticksElapsed += 20;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }


    private static boolean isTeammate(Player player, Player caster) {
        return trustManager.isTrusted(player, caster);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (this.hasEffect(attacker)) {
                if (event.getEntity() instanceof LivingEntity target) {
                    UUID targetUUID = target.getUniqueId();
                    long currentTime = System.currentTimeMillis();
                    if (this.entityLightningCooldowns.containsKey(targetUUID)) {
                        long lastStrikeTime = this.entityLightningCooldowns.get(targetUUID);
                        if (currentTime - lastStrikeTime < 2000L) {
                            return;
                        }
                    }

                    this.entityLightningCooldowns.put(targetUUID, currentTime);
                    List<Entity> nearbyEntities = target.getNearbyEntities(3.0, 3.0, 3.0);
                    Optional<Entity> nextChainTarget = nearbyEntities.stream().filter((e) -> {
                        return e instanceof LivingEntity && !e.equals(attacker);
                    }).findFirst();
                    if (nextChainTarget.isPresent()) {
                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(4.0, attacker);
                        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0, new DustOptions(Color.YELLOW, 1.5F));
                        this.chainLightning(target, attacker);
                    }

                }
            }
        }
    }

    private void chainLightning(Entity startEntity, final Player attacker) {
        final Set<Entity> processedEntities = new HashSet<>();
        final Queue<Entity> queue = new LinkedList<>();
        queue.add(startEntity);
        (new BukkitRunnable() {
            int strikes = 0;

            public void run() {
                if (!queue.isEmpty() && this.strikes < 5) {
                    Entity currentEntity = null;

                    while(!queue.isEmpty()) {
                        Entity candidate = queue.poll();
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
                            livingEntity.damage(4.0, attacker);
                            livingEntity.getWorld().spawnParticle(Particle.DUST, livingEntity.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0, new DustOptions(Color.YELLOW, 1.5F));
                            ++this.strikes;
                            for (Entity entity : livingEntity.getNearbyEntities(3.0, 3.0, 3.0)) {
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