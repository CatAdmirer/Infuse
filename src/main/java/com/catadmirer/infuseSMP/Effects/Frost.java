package com.catadmirer.infuseSMP.Effects;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.DataManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Frost implements Listener {
    private final Set<UUID> frozenAttackers = new HashSet<>();
    
    private final Map<UUID, Integer> meleeHitCounter = new HashMap<>();
    private static final Set<Material> ICE_BLOCKS;

    private DataManager dataManager;

    private final Infuse plugin;

    public Frost(DataManager dataManager, Infuse plugin) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                Bukkit.getOnlinePlayers().forEach((player) -> {
                    if (Frost.this.hasEffect(player, "2") && !(player.getVelocity().lengthSquared() < 0.01) || (Frost.this.hasEffect(player, "1") && !(player.getVelocity().lengthSquared() < 0.01))) {
                        Frost.this.handleSwim(player);
                        Material blockType = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType();
                        if (Frost.ICE_BLOCKS.contains(blockType)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
                        }

                    }
                });
            }
        }).runTaskTimer(plugin, 0L, 10L);
    }

    public static ItemStack createEffect() {
        ItemStack effect = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)effect.getItemMeta();
        if (meta != null) {
            String effectName = Infuse.getInstance().getEffect("frost");
            meta.setDisplayName(effectName);
            List<String> lore = Infuse.getInstance().getEffectLore("frost");
            meta.setColor(Color.fromRGB(0, 255, 255));
            meta.setLore(lore);
            meta.setCustomModelData(4);
            effect.setItemMeta(meta);
        }

        return effect;
    }

    public void handleSwim(Player player) {
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        if (inFrost) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        if (!event.isGliding()) {
            if (inFrost && this.hasEffect(player, "1") || inFrost && hasEffect(player, "2")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        Vector direction = player.getLocation().getDirection().normalize();
        if (inFrost && hasEffect(player, "1") || inFrost && hasEffect(player, "2")) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            double boostStrength = 0.6;
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        }
    }

    public static boolean isEffect(ItemStack item) {
        if (item != null && item.getType() == Material.POTION && item.getItemMeta() != null) {
            return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 4;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onPlayerInteractWithWindCharge(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.WIND_CHARGE) {
            if (player.getFreezeTicks() > 1) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (event.getEntity() instanceof Player target) {
                if (this.hasEffect(attacker, "1") || (this.hasEffect(attacker, "2"))) {
                    int count = this.meleeHitCounter.getOrDefault(attacker.getUniqueId(), 0) + 1;
                    this.meleeHitCounter.put(attacker.getUniqueId(), count);
                    if (count >= 20) {
                        this.meleeHitCounter.put(attacker.getUniqueId(), 0);
                        (new BukkitRunnable() {
                            int ticksElapsed = 0;
                            final int freezeDuration = 200;

                            public void run() {
                                if (this.ticksElapsed >= freezeDuration) {
                                    target.setFreezeTicks(0);
                                    this.cancel();
                                } else {
                                    int currentFreezeTicks = target.getFreezeTicks();
                                    target.setFreezeTicks(currentFreezeTicks + 2);
                                    this.ticksElapsed += 2;
                                }
                            }
                        }).runTaskTimer(plugin, 0L, 2L);
                    }

                }
            }
        }
    }
    
    @EventHandler
    public void handleOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("ability.use")) {
            boolean isPrimary = player.isSneaking() && this.hasEffect(player, "1");
            boolean isSecondary = !player.isSneaking() && this.hasEffect(player, "2");
            if (isPrimary || isSecondary) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "frost")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasEffect(Player player, String tier) {
        String currentEffect = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String effectName = Infuse.getInstance().getEffect("frost");
        String effectName2 = Infuse.getInstance().getEffect("aug_frost");
        return currentEffect != null && currentEffect.equals(effectName) || currentEffect != null && currentEffect.equals(effectName2);
    }

    public void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "frost")) {
            String effectName = Infuse.getInstance().getEffect("aug_frost");
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
            caster.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 300, 0));
            boolean isAugmentedFrost =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName)) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(effectName))));
            long frostDefaultCooldown = Infuse.getInstance().getConfig("frost.cooldown.default");
            long frostAugmentedCooldown = Infuse.getInstance().getConfig("frost.cooldown.augmented");
            long frostCooldown = isAugmentedFrost ? frostAugmentedCooldown : frostDefaultCooldown;

            long frostDefaultDuration = Infuse.getInstance().getConfig("frost.duration.default");
            long frostAugmentedDuration = Infuse.getInstance().getConfig("frost.duration.augmented");
            long frostDuration = isAugmentedFrost ? frostAugmentedDuration : frostDefaultDuration;

            CooldownManager.setDuration(playerUUID, "frost", frostDuration);
            CooldownManager.setCooldown(playerUUID, "frost", frostCooldown);

            Location center = caster.getLocation();
            double radius = 5.0;
            World world = caster.getWorld();
            final Set<Player> affectedPlayers = new HashSet<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(caster) && !this.isTeammate(player, caster)
                        && player.getWorld().equals(world)
                        && player.getLocation().distance(center) <= radius) {
                    affectedPlayers.add(player);
                    AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                    if (jumpAttribute != null) {
                        jumpAttribute.setBaseValue(0.1);
                    }
                }
            }

            this.frozenAttackers.add(caster.getUniqueId());

            new BukkitRunnable() {
                public void run() {
                    for (Player player : affectedPlayers) {
                        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                        if (jumpAttribute != null) {
                            jumpAttribute.setBaseValue(0.42);
                        }
                    }
                    Frost.this.frozenAttackers.remove(caster.getUniqueId());
                }
            }.runTaskLater(plugin, frostDuration * 20L);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null && jumpAttribute.getBaseValue() == 0.1) {
            jumpAttribute.setBaseValue(0.42);
        }

    }

    private boolean isTeammate(Player player, Player caster) {
        return dataManager.isTrusted(player, caster);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (attacker.hasPotionEffect(PotionEffectType.UNLUCK)) {
                PotionEffect effect = attacker.getPotionEffect(PotionEffectType.UNLUCK);
                if (effect != null && effect.getAmplifier() >= 0 && this.frozenAttackers.contains(attacker.getUniqueId()) && event.getEntity() instanceof Player target) {
                    target.setFreezeTicks(200);
                }
            }

        }
    }

    static {
        ICE_BLOCKS = EnumSet.of(Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE);
    }
}
