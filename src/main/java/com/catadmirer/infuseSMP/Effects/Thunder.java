package com.catadmirer.infuseSMP.Effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.util.EffectUtil;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class Thunder implements Listener {

    private final static Set<UUID> activeSparks = new HashSet<>();
    private final Map<UUID, Long> entityLightningCooldowns = new HashMap<>();

    private final Infuse plugin;

    public Thunder(Infuse plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static ItemStack createRegular() {
        return createEffect(false);
    }

    public static ItemStack createAugmented() {
        return createEffect(true);
    }

    public static ItemStack createEffect(boolean augmented) {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) effect.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Infuse.getInstance().getEffectName(augmented ? "aug_thunder" : "thunder"));
            meta.setLore(Infuse.getInstance().getEffectLore(augmented ? "aug_thunder" : "thunder"));
            meta.setColor(Color.YELLOW);

            if (augmented) meta.setCustomModelData(999);
            meta.getPersistentDataContainer().set(Infuse.EFFECT_ID, PersistentDataType.INTEGER, augmented ? 23 : 22);

            effect.setItemMeta(meta);
        }

        return effect;
    }

    public static boolean isRegular(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 22;
    }

    public static boolean isAugmented(ItemStack item) {
        return EffectUtil.getIdFromItem(item) == 23;
    }

    public static boolean isEffect(ItemStack item) {
        return isRegular(item) || isAugmented(item);
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
                            target.damage(4, attacker);
                            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
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
            String effectName2 = Infuse.getInstance().getEffectName("aug_thunder");
            boolean isAugmented = (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1"))
                            .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2)))) ||
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2"))
                                    .equalsIgnoreCase(ChatColor.stripColor(ChatColor.stripColor(effectName2))));
            long defaultCooldown = Infuse.getInstance().getConfig("thunder.cooldown.default");
            long augmentedCooldown = Infuse.getInstance().getConfig("thunder.cooldown.augmented");
            long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;

            long defaultDuration = Infuse.getInstance().getConfig("thunder.duration.default");
            long augmentedDuration = Infuse.getInstance().getConfig("thunder.duration.augmented");
            long duration = isAugmented ? augmentedDuration : defaultDuration;
            final long effectDuration = duration * 20;

            CooldownManager.setDuration(playerUUID, "thunder", duration);
            CooldownManager.setCooldown(playerUUID, "thunder", cooldown);

            final double radius = 10;
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
                        target.damage(4, caster);
                        world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                    }

                    this.ticksElapsed += 20;
                }
            }.runTaskTimer(Infuse.getInstance(), 0L, 20L);
        }
    }


    private static boolean isTeammate(Player player, Player caster) {
        return Infuse.getInstance().getEffectManager().isTrusted(player, caster);
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
                    List<Entity> nearbyEntities = target.getNearbyEntities(3, 3, 3);
                    Optional<Entity> nextChainTarget = nearbyEntities.stream().filter((e) -> {
                        return e instanceof LivingEntity && !e.equals(attacker);
                    }).findFirst();
                    if (nextChainTarget.isPresent()) {
                        target.getWorld().strikeLightningEffect(target.getLocation());
                        target.damage(4, attacker);
                        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
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
                            livingEntity.damage(4, attacker);
                            livingEntity.getWorld().spawnParticle(Particle.DUST, livingEntity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new DustOptions(Color.YELLOW, 1.5F));
                            ++this.strikes;
                            for (Entity entity : livingEntity.getNearbyEntities(3, 3, 3)) {
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