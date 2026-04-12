package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.catadmirer.infuseSMP.EffectIds;

public class Frost extends InfuseEffect {
    private static final Set<UUID> frozenAttackers = new HashSet<>();

    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);

    public Frost() {
        super(EffectIds.FROST, "frost", false);
    }

    public Frost(boolean augmented) {
        super(EffectIds.FROST, "frost", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_FROST_NAME : MessageType.FROST_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_FROST_LORE : MessageType.FROST_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Frost(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Frost(false);
    }

    @Override
    public void equip(Player player) {
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (jumpAttribute != null && jumpAttribute.getBaseValue() == 0.1) {
            jumpAttribute.setBaseValue(0.42);
        }
    }

    @Override
    public void unequip(Player player) {}

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "frost")) return;

        // Applying effects for the frost spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "frost", duration, cooldown);

        Location center = player.getLocation();
        double radius = 5;
        World world = player.getWorld();
        final Set<Player> affectedPlayers = new HashSet<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && !plugin.getDataManager().isTrusted(p, player)
                    && p.getWorld().equals(world)
                    && p.getLocation().distance(center) <= radius) {
                affectedPlayers.add(p);
                AttributeInstance jumpAttribute = p.getAttribute(Attribute.JUMP_STRENGTH);
                if (jumpAttribute != null) {
                    jumpAttribute.setBaseValue(0.1);
                }
            }
        }

        frozenAttackers.add(player.getUniqueId());

        new BukkitRunnable() {
            public void run() {
                for (Player p : affectedPlayers) {
                    AttributeInstance jumpAttribute = p.getAttribute(Attribute.JUMP_STRENGTH);
                    if (jumpAttribute != null) {
                        jumpAttribute.setBaseValue(0.42);
                    }
                }
                frozenAttackers.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    @EventHandler
    public void onTenthAttack(TenHitEvent event) {
        Infuse.LOGGER.debug("[Frost] Recieved TenHitEvent");
        Infuse.LOGGER.debug("[Frost] TenHitEvent Attacker: {}", event.getAttacker().getName());
        Infuse.LOGGER.debug("[Frost] TenHitEvent Target: {}", event.getTarget().getName());
        
        if (!plugin.getDataManager().hasEffect(event.getAttacker(), this)) return;

        Infuse.LOGGER.debug("[Frost] Attacker has frost effect");

        (new BukkitRunnable() {
            int ticksElapsed = 0;
            final int freezeDuration = 200;

            public void run() {
                if (this.ticksElapsed >= freezeDuration) {
                    event.getTarget().setFreezeTicks(0);
                    this.cancel();
                } else {
                    int currentFreezeTicks = event.getTarget().getFreezeTicks();
                    event.getTarget().setFreezeTicks(currentFreezeTicks + 2);
                    this.ticksElapsed += 2;
                }
            }
        }).runTaskTimer(plugin, 0L, 2L);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!attacker.hasPotionEffect(PotionEffectType.UNLUCK)) return;
        PotionEffect effect = attacker.getPotionEffect(PotionEffectType.UNLUCK);
        if (effect.getAmplifier() >= 0 && Frost.frozenAttackers.contains(attacker.getUniqueId()) && event.getEntity() instanceof Player target) {
            target.setFreezeTicks(200);
        }
    }
}