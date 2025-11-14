package com.catadmirer.infuseSMP.Effects;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Managers.CooldownManager;
import com.catadmirer.infuseSMP.Managers.EffectManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Ocean implements Listener {
    
    private final Plugin plugin;

    private final EffectManager trustManager;

    public Ocean(Plugin plugin, EffectManager trustManager) {
        this.plugin = plugin;
        this.trustManager = trustManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        (new BukkitRunnable() {
            public void run() {
                Iterator var1 = Bukkit.getOnlinePlayers().iterator();

                while(var1.hasNext()) {
                    Player p = (Player)var1.next();
                    if (Ocean.this.hasImmortalHackEquipped2(p, "1") || (Ocean.this.hasImmortalHackEquipped2(p, "2"))) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
                    }
                }

            }
        }).runTaskTimer(plugin, 0L, 20L);
        (new BukkitRunnable() {
            public void run() {
                Iterator var1 = Bukkit.getOnlinePlayers().iterator();

                while(true) {
                    Player gemHolder;
                    do {
                        if (!var1.hasNext()) {
                            return;
                        }

                        gemHolder = (Player)var1.next();
                    } while(!Ocean.this.hasImmortalHackEquipped2(gemHolder, "1") || (!Ocean.this.hasImmortalHackEquipped2(gemHolder, "2")));

                    Iterator var3 = gemHolder.getWorld().getPlayers().iterator();

                    while(var3.hasNext()) {
                        Player p = (Player)var3.next();
                        if (!p.equals(gemHolder) && p.getLocation().distance(gemHolder.getLocation()) <= 5.0D && p.getLocation().getBlock().isLiquid()) {
                            int currentAir = p.getRemainingAir();
                            int newAir = Math.max(currentAir - 5, -20);
                            p.setRemainingAir(newAir);
                            if (newAir <= 0) {
                                p.damage(1.0D);
                            }
                        }
                    }
                }
            }
        }).runTaskTimer(plugin, 0L, 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player gemHolder : Bukkit.getOnlinePlayers()) {
                    if (!CooldownManager.isEffectActive(gemHolder.getUniqueId(), "ocean")) {
                        continue;
                    }
                    if (!hasImmortalHackEquipped2(gemHolder, "1") || (!hasImmortalHackEquipped2(gemHolder, "2"))) continue;
                    World world = gemHolder.getWorld();
                    Location holderLoc = gemHolder.getLocation();
                    double radius = Infuse.getInstance().getCanfig("ocean_pulling.pull.radius");
                    double strength = Infuse.getInstance().getCanfig("ocean_pulling.pull.strength");

                    for (Player p : world.getPlayers()) {
                        if (p.equals(gemHolder)) continue;
                        if (isTrusted(gemHolder, p)) continue;
                        if (p.getLocation().distance(holderLoc) <= radius) {
                            Vector direction = holderLoc.toVector().subtract(p.getLocation().toVector());
                            if (direction.lengthSquared() > 0.0001) {
                                Vector pullVector = direction.normalize().multiply(strength);
                                if (Double.isFinite(pullVector.getX()) && Double.isFinite(pullVector.getY()) && Double.isFinite(pullVector.getZ())) {
                                    p.setVelocity(pullVector);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, ( (Integer) Infuse.getInstance().getCanfig("ocean_pulling.pull.interval")).longValue());

    }

    private boolean isTrusted(Player player, Player caster) {
        return trustManager.isTrusted(caster, player);
    }


    public static ItemStack createOcean() {
        ItemStack gem = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)gem.getItemMeta();
        if (meta != null) {
            String gemName = Infuse.getInstance().getEffect("ocean");
            meta.setDisplayName(gemName);
            List<String> lore = Infuse.getInstance().getEffectLore("ocean");
            meta.setColor(Color.fromRGB(0, 0, 255));
            meta.setLore(lore);
            meta.setCustomModelData(8);
            gem.setItemMeta(meta);
        }

        return gem;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (Infuse.getInstance().<Boolean>getCanfig("invis_deaths")) {
            if (killer != null && killer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                String msg = Infuse.getInstance().getCanfig("invis.kill_invis");
                msg = msg.replace("%victim%", victim.getName())
                        .replace("%killer%", ChatColor.GRAY + "" + TextDecoration.OBFUSCATED + ("Someone"));
                event.deathMessage(Component.text(msg));
            } else if (victim.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                if (killer != null) {
                    String msg = Infuse.getInstance().getCanfig("invis.death_invis");
                    msg = msg.replace("%victim%", ChatColor.GRAY + "" + TextDecoration.OBFUSCATED + ("Someone"))
                            .replace("%killer%", killer.getName());
                    event.deathMessage(Component.text(msg));
                }
            }
        }
    }

    public static boolean isInventoryGlitchGem(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 8;
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
                if (!CooldownManager.isOnCooldown(playerUUID, "frost")) {
                    event.setCancelled(true);
                    this.activateSpark(player);
                }
            }
        }
    }

    private boolean hasImmortalHackEquipped2(Player player, String tier) {
        String currentHack = Infuse.getInstance().getEffectManager().getEffect(player.getUniqueId(), tier);
        String gemName = Infuse.getInstance().getEffect("ocean");
        String gemName2 = Infuse.getInstance().getEffect("aug_ocean");
        return currentHack != null && (currentHack.equals(gemName) || currentHack.equals((gemName2)));
    }

    public void activateSpark(final Player caster) {
        UUID playerUUID = caster.getUniqueId();

        if (!CooldownManager.isOnCooldown(playerUUID, "ocean")) {
            caster.playSound(caster.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);

            final double radius = 5.0D;
            final World world = caster.getWorld();
            String gemName2 = Infuse.getInstance().getEffect("aug_ocean");
            boolean isAugmented =
                    (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1") != null &&
                            ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "1")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2))) ||
                            (Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2") != null &&
                                    ChatColor.stripColor(Infuse.getInstance().getEffectManager().getEffect(playerUUID, "2")).toLowerCase().equalsIgnoreCase(ChatColor.stripColor(gemName2)));
            long defaultCooldown = ((Integer) Infuse.getInstance().getCanfig("ocean.cooldown.default")).longValue();
            long augmentedCooldown = ((Integer) Infuse.getInstance().getCanfig("ocean.cooldown.augmented")).longValue();
            long cooldown = isAugmented ? augmentedCooldown : defaultCooldown;

            long defaultDuration = ((Integer) Infuse.getInstance().getCanfig("ocean.duration.default")).longValue();
            long augmentedDuration = ((Integer) Infuse.getInstance().getCanfig("ocean.duration.augmented")).longValue();
            long duration = isAugmented ? augmentedDuration : defaultDuration;

            CooldownManager.setDuration(playerUUID, "ocean", duration);
            CooldownManager.setCooldown(playerUUID, "ocean", cooldown);

            final long durationTicks = duration * 20L;

            new BukkitRunnable() {
                long ticksElapsed = 0L;

                public void run() {
                    if (this.ticksElapsed >= durationTicks) {
                        this.cancel();
                        return;
                    }

                    for (int angle = 0; angle < 360; angle += 10) {
                        double rad = Math.toRadians(angle);
                        double x = caster.getLocation().getX() + radius * Math.cos(rad);
                        double z = caster.getLocation().getZ() + radius * Math.sin(rad);
                        Location particleLoc = new Location(world, x, caster.getLocation().getY(), z);
                        world.spawnParticle(Particle.FALLING_WATER, particleLoc, 1);
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.equals(caster) &&
                                p.getWorld().equals(world) &&
                                p.getLocation().distance(caster.getLocation()) <= radius) {

                            Vector direction = caster.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                            p.setVelocity(direction.multiply(0.5D));

                            if (p.getLocation().getBlock().isLiquid()) {
                                int newOxygen = Math.max(p.getRemainingAir() - 20, -20);
                                p.setRemainingAir(newOxygen);
                                if (newOxygen <= 0) {
                                    p.damage(2.0D);
                                }
                            }
                        }
                    }

                    this.ticksElapsed += 10L;
                }
            }.runTaskTimer(this.plugin, 0L, 10L);
        }
    }
}
