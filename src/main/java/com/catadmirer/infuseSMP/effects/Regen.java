package com.catadmirer.infuseSMP.effects;

import java.util.UUID;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.catadmirer.infuseSMP.EffectIds;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Regen extends InfuseEffect {
    private final Infuse plugin = JavaPlugin.getPlugin(Infuse.class);
    
    public Regen() {
        super(EffectIds.REGEN, "regen", false);
    }

    public Regen(boolean augmented) {
        super(EffectIds.REGEN, "regen", augmented);
    }

    @Override
    public Message getItemName() {
        return new Message(augmented ? MessageType.AUG_REGEN_NAME : MessageType.REGEN_NAME);
    }

    @Override
    public Message getItemLore() {
        return new Message(augmented ? MessageType.AUG_REGEN_LORE : MessageType.REGEN_LORE);
    }

    @Override
    public InfuseEffect getAugmentedForm() {
        return new Regen(true);
    }

    @Override
    public InfuseEffect getRegularForm() {
        return new Regen(false);
    }

    @Override
    public void equip(Player player) {}

    @Override
    public void unequip(Player player) {}

    @Override
    public void activateSpark(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Making sure the player isn't on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "regen")) return;

        // Applying effects for the regen spark
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "regen", duration, cooldown);
    }

    public static class Listeners implements Listener {
        private final Infuse plugin;
        private final Regen effect = new Regen();

        public Listeners(Infuse plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void regenCanAlwaysEat(PlayerInteractEvent event) {
            if (!(event.getAction().isRightClick())) return;
            Player player = event.getPlayer();

            // Filtering an empty hand
            if (event.getItem() == null) return;
            
            // Filtering inedible items
            if (!event.getItem().getType().isEdible()) return;
            
            // Filtering always edible items
            if (new ItemStack(event.getItem().getType()).getItemMeta().getFood().canAlwaysEat()) return;

            // Making the food always edible only if the player has the regen effect.  Makes food not always edible otherwise
            if (plugin.getDataManager().hasEffect(player, effect)) {
                event.getItem().editMeta(meta -> {
                    FoodComponent foodComp = meta.getFood();
                    foodComp.setCanAlwaysEat(true);
                    meta.setFood(foodComp);
                });
            } else {
                event.getItem().editMeta(meta -> {
                    meta.setFood(null);
                });
            }
        }

        @EventHandler
        public void regenRegenerateOnHit(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) return;
            if (!plugin.getDataManager().hasEffect(player, effect)) return;

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false));
            if (CooldownManager.isEffectActive(player.getUniqueId(), "regen")) {
                for (Entity loopentity : player.getNearbyEntities(5, 5, 5)) {
                    if (loopentity instanceof Player otherplayer) {
                        if (plugin.getDataManager().isTrusted(player, otherplayer)) {
                            otherplayer.heal(event.getDamage()/2);
                        }
                    }
                }
            }
        }
    }
}