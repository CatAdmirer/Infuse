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

    private DataManager trustManager;

    private final Infuse plugin;

    public Frost(DataManager trustManager, Infuse plugin) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                Bukkit.getOnlinePlayers().forEach((player) -> {
                    if (Frost.this.hasImmortalHackEquipped2(player, "2") && !(player.getVelocity().lengthSquared() < 0.01D) || (Frost.this.hasImmortalHackEquipped2(player, "1") && !(player.getVelocity().lengthSquared() < 0.01D))) {
                        Frost.this.handleSwim(player);
                        Material blockType = player.getLocation().subtract(0.0D, 1.0D, 0.0D).getBlock().getType();
                        if (Frost.ICE_BLOCKS.contains(blockType)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
                        }

                    }
                });
            }
        }).runTaskTimer(plugin, 0L, 10L);
    }

    public static ItemStack createFrost() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("frost");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("frost");
            meta.setColor(Color.fromRGB(0, 255, 255));
            meta.setLore(lore);
            meta.setCustomModelData(4);
            gem.setItemMeta(meta);
        }

        return gem;
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
            if (inFrost && this.hasImmortalHackEquipped2(player, "1") || inFrost && hasImmortalHackEquipped2(player, "2")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        Vector direction = player.getLocation().getDirection().normalize();
        if (inFrost && hasImmortalHackEquipped2(player, "1") || inFrost && hasImmortalHackEquipped2(player, "2")) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            double boostStrength = 0.6;
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        }
    }

    public static boolean isStealthGem(ItemStack item) {
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
        if (event.getDamager() instanceof Player) {
            if (event.getEntity() instanceof Player) {
                Player attacker = (Player)event.getDamager();
                final Player target = (Player)event.getEntity();
                if (this.hasImmortalHackEquipped2(attacker, "1") || (this.hasImmortalHackEquipped2(attacker, "2"))) {
                    int count = (Integer)this.meleeHitCounter.getOrDefault(attacker.getUniqueId(), 0) + 1;
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
            boolean isLegendary = player.isSneaking() && this.hasImmortalHackEquipped2(player, "1");
            boolean isCommon = !player.isSneaking() && this.hasImmortalHackEquipped2(player, "2");
            if (isLegendary || isCommon) {
                UUID playerUUID = player.getUniqueId();
                if (!CooldownManager.isOnCooldown(playerUUID, "frost")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasImmortalHackEquipped2(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = Infuse.getInstance().getEffect("frost");
        String gemName2 = Infuse.getInstance().getEffect("aug_frost");
        return currentHack != null && currentHack.equals(gemName) || currentHack != null && currentHack.equals(gemName2);
    }

    public void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "frost")) {
            String gemName = Infuse.getInstance().getEffect("aug_frost");
            caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
            caster.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 300, 0));
            boolean isAugmentedFrost =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName)) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName))));
            long frostDefaultCooldown = ((Integer) Infuse.getInstance().getCanfig("frost.cooldown.default")).longValue();
            long frostAugmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("frost.cooldown.augmented")).longValue();
            long frostCooldown = isAugmentedFrost ? frostAugmentedCooldown : frostDefaultCooldown;

            long frostDefaultDuration = ((Integer) Infuse.getInstance().getCanfig("frost.duration.default")).longValue();
            long frostAugmentedDuration = ((Integer) Infuse.getInstance().getCanfig("frost.duration.augmented")).longValue();
            long frostDuration = isAugmentedFrost ? frostAugmentedDuration : frostDefaultDuration;

            CooldownManager.setDuration(playerUUID, "frost", frostDuration);
            CooldownManager.setCooldown(playerUUID, "frost", frostCooldown);

            Location center = caster.getLocation();
            double radius = 5.0D;
            World world = caster.getWorld();
            final Set<Player> affectedPlayers = new HashSet<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(caster) && !this.isTeammate(player, caster)
                        && player.getWorld().equals(world)
                        && player.getLocation().distance(center) <= radius) {
                    affectedPlayers.add(player);
                    AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                    if (jumpAttribute != null) {
                        jumpAttribute.setBaseValue(0.1D);
                    }
                }
            }

            this.frozenAttackers.add(caster.getUniqueId());

            new BukkitRunnable() {
                public void run() {
                    for (Player player : affectedPlayers) {
                        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                        if (jumpAttribute != null) {
                            jumpAttribute.setBaseValue(0.42D);
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
        if (jumpAttribute != null && jumpAttribute.getBaseValue() == 0.1D) {
            jumpAttribute.setBaseValue(0.42D);
        }

    }

    private boolean isTeammate(Player player, Player caster) {
        return trustManager.isTrusted(player, caster);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player)event.getDamager();
            if (attacker.hasPotionEffect(PotionEffectType.UNLUCK)) {
                PotionEffect effect = attacker.getPotionEffect(PotionEffectType.UNLUCK);
                if (effect != null && effect.getAmplifier() >= 0 && this.frozenAttackers.contains(attacker.getUniqueId()) && event.getEntity() instanceof Player) {
                    Player target = (Player)event.getEntity();
                    target.setFreezeTicks(200);
                }
            }

        }
    }

    static {
        ICE_BLOCKS = EnumSet.of(Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE);
    }
}
