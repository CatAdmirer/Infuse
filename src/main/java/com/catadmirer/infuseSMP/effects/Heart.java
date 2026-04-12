package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Heart extends InfuseEffect {
    public static final NamespacedKey heartBoost = new NamespacedKey("infuse", "heart_boost");
    public static final NamespacedKey heartSparkBoost = new NamespacedKey("infuse", "heart_spark_boost");

    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);
    
    public Heart() {
        super(EffectIds.HEART, "heart", false);
    }

    public Heart(boolean augmented) {
        super(EffectIds.HEART, "heart", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_HEART_NAME : MessageType.HEART_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_HEART_LORE : MessageType.HEART_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Heart(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Heart(false);
    }

    @Override
    public void equip(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        
        // Doubling the strength if the user already has the modifier active
        int healthBoost = 10;
        if (maxHealth.getModifier(heartBoost) != null) {
            healthBoost = 20;
            maxHealth.removeModifier(heartBoost);
        }

        AttributeModifier healthModifier = new AttributeModifier(heartBoost, healthBoost, Operation.ADD_NUMBER);
        
        maxHealth.addModifier(healthModifier);
        player.heal(healthBoost);
    }

    @Override
    public void unequip(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        AttributeModifier healthModifier = maxHealth.getModifier(heartBoost);

        // Ignoring players who dont have the modifier
        if (healthModifier == null) return;

        // Removing the modifier
        maxHealth.removeModifier(heartBoost);

        // Checking if the modifier was applied twice and adding it back if it was.
        if (healthModifier.getAmount() > 10) {
            healthModifier = new AttributeModifier(heartBoost, 10, Operation.ADD_NUMBER);
            maxHealth.addModifier(healthModifier);
        }
    }

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "heart")) return;

        // Applying effects for the heart spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute.getModifier(heartSparkBoost) == null) {
            attribute.addModifier(new AttributeModifier(heartSparkBoost, 10, Operation.ADD_NUMBER));
            player.heal(10);
        }

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "heart", duration, cooldown);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> attribute.removeModifier(heartSparkBoost), duration * 20);
    }

    private void showAndUpdateHealthAboveEntity(Infuse plugin, LivingEntity player) {
        Location ploc = player.getLocation().add(0, 2.5, 0);

        TextDisplay as = (TextDisplay) ploc.getWorld().spawn(ploc, TextDisplay.class);

        as.setGravity(false);
        as.setCustomNameVisible(true);
        as.customName();
        updateHealthDisplay(as, player);
        player.addPassenger(as);
        final BukkitRunnable updateTask = new BukkitRunnable() {
            public void run() {
                if (!player.isDead() && player.isValid()) {
                    Heart.this.updateHealthDisplay(as, player);
                } else {
                    this.cancel();
                    as.setCustomNameVisible(false);
                    as.customName(null);
                }
            }
        };

        updateTask.runTaskTimer(plugin, 0L, 10L);
        (new BukkitRunnable() {
            public void run() {
                updateTask.cancel();
                as.setCustomNameVisible(false);
                as.customName(null);
                player.removePassenger(as);
            }
        }).runTaskLater(plugin, 200L);
    }

    private void updateHealthDisplay(TextDisplay entity, LivingEntity player) {
        if (player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
            entity.customName(Message.toComponent(String.format("<yellow><b>%.1f ❤", player.getHealth()) + player.getAbsorptionAmount()));
        } else {
            entity.customName(Message.toComponent(String.format("<red><b>%.1f ❤", player.getHealth())));
        }
    }

    @EventHandler
    public void heartShowTargetHealth(TenHitEvent event) {
        Player attacker = event.getAttacker();
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        showAndUpdateHealthAboveEntity(plugin, event.getTarget());
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        ItemStack item = event.getItem();
        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 4));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 0));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AttributeInstance maxHealth = event.getPlayer().getAttribute(Attribute.MAX_HEALTH);
        maxHealth.removeModifier(heartBoost);
        maxHealth.removeModifier(heartSparkBoost);
    }
}