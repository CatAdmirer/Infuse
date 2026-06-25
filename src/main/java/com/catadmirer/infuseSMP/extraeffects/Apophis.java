package com.catadmirer.infuseSMP.extraeffects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.effects.Emerald.FoodAndExpLock;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.events.TenHitEvent;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.util.ItemUtil;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.view.CraftEnchantmentView;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Apophis extends InfuseEffect {
    public static final NamespacedKey LOOTING_KEY = new NamespacedKey("infuse", "apophis_looting");
    public static final NamespacedKey APOPHIS_BOOST = new NamespacedKey("infuse", "apophis_boost");
    public static final NamespacedKey APOPHIS_SPARK_BOOST = new NamespacedKey("infuse", "apophis_spark_boost");

    private final Infuse plugin;

    public Apophis() {
        this(false);
    }

    public Apophis(boolean augmented) {
        super("apophis", EffectIds.APOPHIS, augmented, EffectConstants.potionColor(EffectIds.APOPHIS), EffectConstants.ritualColor(EffectIds.APOPHIS));

        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(Player owner) {
        AttributeInstance attribute = owner.getAttribute(Attribute.MAX_HEALTH);
        attribute.addModifier(new AttributeModifier(APOPHIS_BOOST, 10, Operation.ADD_NUMBER));
        owner.heal(10);

        owner.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, -1, 9, false, false));
        owner.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, -1, 2, false, false));
        owner.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, -1, 2, false, false));
    }

    @Override
    public void unequip(Player owner) {
        AttributeInstance attribute = owner.getAttribute(Attribute.MAX_HEALTH);
        attribute.removeModifier(APOPHIS_BOOST);

        owner.removePotionEffect(PotionEffectType.LUCK);
        owner.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        owner.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);

        // Removing enchanted items from the owner's inventory
        for (ItemStack item : owner.getInventory()) {
            if (item == null || item.getType() == Material.AIR) continue;

            ItemUtil.removeSpecialEnchant(item, LOOTING_KEY, Enchantment.LOOTING);
        }
    }

    @Override
    public void activateSpark(Player owner) {
        UUID playerUUID = owner.getUniqueId();

        // Stopping if the spark is on cooldown
        if (CooldownManager.isOnCooldown(playerUUID, "apophis")) return;

        owner.playSound(owner.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        owner.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 600, 254));

        for (Entity entity : owner.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != owner) {
                entity.setFireTicks(100);
            }
        }

        // particles
        spawnSparkEffect(owner);
        Bukkit.getScheduler().runTaskLater(plugin, t -> owner.getWorld().spawnParticle(Particle.EXPLOSION, owner.getLocation(), 1), 20L);

        AttributeInstance attribute = owner.getAttribute(Attribute.MAX_HEALTH);
        if (attribute.getModifier(APOPHIS_SPARK_BOOST) == null) {
            attribute.addModifier(new AttributeModifier(APOPHIS_SPARK_BOOST, 10, Operation.ADD_NUMBER));
            owner.heal(10);
        }

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "apophis", duration, cooldown);

        Bukkit.getScheduler().runTaskLater(plugin, () -> attribute.removeModifier(APOPHIS_SPARK_BOOST), duration * 20);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Apophis();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Apophis(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? Message.MessageType.AUG_APOPHIS_NAME : Message.MessageType.APOPHIS_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? Message.MessageType.AUG_APOPHIS_LORE : Message.MessageType.APOPHIS_LORE);
    }

    private void spawnSparkEffect(final Player caster) {
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 100) {
                    startDarkRedDustEffect(caster.getLocation(), caster);
                    this.cancel();
                    return;
                }

                Location center = caster.getLocation();
                World world = center.getWorld();
                if (this.tick > 0 && this.tick % 20 == 0) {
                    world.playSound(center, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);

                    for (int angle = 0; angle < 360; angle += 20) {
                        double rad = Math.toRadians(angle);
                        double offsetX = 5 * Math.cos(rad);
                        double offsetZ = 5 * Math.sin(rad);
                        Location particleLoc = center.clone().add(offsetX, 0.1, offsetZ);
                        world.spawnParticle(Particle.LAVA, particleLoc, 10, 0.05, 0.05, 0.05, 0.01);
                    }

                    for (Player target : world.getPlayers()) {
                        if (!target.equals(caster) && target.getLocation().distance(center) <= 5) {
                            target.damage(8, caster);
                        }
                    }
                }

                this.tick++;
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }

    private void startDarkRedDustEffect(final Location startLoc, Player caster) {
        final World world = startLoc.getWorld();
        double explosionRadius = 5;
        for (Player target : world.getPlayers()) {
            if (!target.equals(caster) && target.getLocation().distance(startLoc) <= explosionRadius) {
                target.setVelocity(new Vector(0, 2, 0));
            }
        }

        world.playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        (new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (this.tick >= 60) {
                    this.cancel();
                    return;
                }

                double baseRadius = 5;
                double spreadFactor = this.tick * 0.1;
                double circleRadius = baseRadius + spreadFactor;
                double particleHeightOffset = this.tick * 3;
                if (particleHeightOffset > 30) {
                    this.cancel();
                    return;
                }

                for(int angle = 0; angle < 360; ++angle) {
                    double rad = Math.toRadians(angle);
                    double offsetX = circleRadius * Math.cos(rad);
                    double offsetZ = circleRadius * Math.sin(rad);
                    Location particleLoc = startLoc.clone().add(offsetX, particleHeightOffset, offsetZ);
                    world.spawnParticle(Particle.DUST_PILLAR, particleLoc, 3, 0, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());
                }

                ++this.tick;
            }
        }).runTaskTimer(plugin, 0, 1);
    }

    //// Listeners ////
    //// These are only registered once, so they need to be able to handle being used for every player, no matter what effects they actually have

    @EventHandler
    public void enchantHeldItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isSword(item)) {
            ItemUtil.applySpecialEnchantment(item, LOOTING_KEY, Enchantment.LOOTING, plugin.getMainConfig().apophisLootingLevel());
        }
    }

    @EventHandler
    public void removeLootingWhenStored(InventoryCloseEvent event) {
        if (event.getView().getType() == InventoryType.PLAYER) return;

        for (ItemStack item : event.getView().getTopInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            ItemUtil.removeSpecialEnchant(item, LOOTING_KEY, Enchantment.LOOTING);
        }
    }

    @EventHandler
    public void removeLootingWhenDropped(PlayerDropItemEvent event) {
        ItemUtil.removeSpecialEnchant(event.getItemDrop().getItemStack(), LOOTING_KEY, Enchantment.LOOTING);
    }

    @EventHandler
    public void tenHitEvent(TenHitEvent event) {
        Infuse.LOGGER.debug("[Apophis] Received TenHitEvent");
        Infuse.LOGGER.debug("[Apophis] Attacker: {}", event.getAttacker().getName());
        Infuse.LOGGER.debug("[Apophis] Target: {}", event.getTarget().getName());

        if (!plugin.getDataManager().hasEffect(event.getTarget(), this)) return;

        Infuse.LOGGER.debug("[Apophis] Target has apophis effect");
        Infuse.LOGGER.debug("[Apophis] Locking attacker's food and Exp");

        new FoodAndExpLock(plugin, event.getAttacker(), plugin.getMainConfig().apophisLockDurationSeconds());
    }

    @EventHandler
    public void apophisExpMultiplier(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getDataManager().hasEffect(player, this)) return;

        ExperienceOrb orb = event.getExperienceOrb();
        int amount = orb.getExperience();

        double multiplier = 2;
        if (CooldownManager.isEffectActive(player.getUniqueId(), "apophis")) {
            multiplier = 4;
        }

        int newAmount = (int) Math.round(amount * multiplier);
        orb.setExperience(newAmount);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void apophisEnchantBonus(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();

        // Skipping non-enchantable items
        if (!item.hasData(DataComponentTypes.ENCHANTABLE)) return;

        // Skipping already enchanted items
        if (!item.getEnchantments().isEmpty()) return;

        // Making sure the enchanter has the apophis effect
        Player player = event.getEnchanter();
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        EnchantmentOffer[] offers = event.getOffers();
        Random random = new Random(player.getEnchantmentSeed());

        // Calculating the costs
        for (int    k = 0; k < 3; k++) {
            int cost;

            Enchantable enchantable = item.getData(DataComponentTypes.ENCHANTABLE);
            if (enchantable == null) {
                offers[k] = null;
                continue;
            }

            int i = random.nextInt(1, 9) + 7 + random.nextInt(0, 16);

            // Calculating cose
            if (k == 0) {
                cost = Math.max(i / 3, 1);
            } else if (k == 1) {
                cost = i * 2 / 3 + 1;
            } else {
                cost = Math.max(i, 30);
            }

            if (cost < k + 1) {
                offers[k] = null;
                continue;
            }

            try {
                EnchantmentMenu menu = (EnchantmentMenu) ((CraftEnchantmentView) event.getView()).getHandle();

                Method getEnchantmentList = menu.getClass().getDeclaredMethod("getEnchantmentList", RegistryAccess.class, net.minecraft.world.item.ItemStack.class, int.class, int.class);
                getEnchantmentList.setAccessible(true);

                List<?> list = (List<?>) getEnchantmentList.invoke(menu, ((CraftWorld) player.getWorld()).getHandle().registryAccess(), CraftItemStack.asNMSCopy(item), k, cost);
                if (!list.isEmpty()) {
                    EnchantmentInstance enchantmentinstance = (EnchantmentInstance) list.get(random.nextInt(list.size()));

                    Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment = null;
                    int level;

                    Class<EnchantmentInstance> clazz = EnchantmentInstance.class;

                    if (!clazz.isRecord()) {
                        // Handling pre-1.21.5
                        enchantment = (Holder) clazz.getField("enchantment").get(enchantmentinstance);
                        level = (int) clazz.getField("level").get(enchantmentinstance);
                    } else {
                        RecordComponent[] components = clazz.getRecordComponents();
                        enchantment = (Holder) components[0].getAccessor().invoke(enchantmentinstance);
                        level = (int) components[1].getAccessor().invoke(enchantmentinstance);
                    }
                    offers[k] = new EnchantmentOffer(CraftEnchantment.minecraftHolderToBukkit(enchantment), level, cost);
                }
                getEnchantmentList.setAccessible(false);
            } catch (NoSuchMethodException e) {
                Infuse.LOGGER.error("Could not find the \"getEnchantmentList\" method in the EnchantmentMenu class");
            } catch (Exception e) {
                Infuse.LOGGER.error("Error while calculating enchantments:", e);
            }
        }
    }

    @EventHandler
    public void stealExp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player attacker)) return;
        if (!plugin.getDataManager().hasEffect(attacker, this)) return;

        // Getting configs
        int exp = damaged.getTotalExperience();
        int expPerHit = plugin.getMainConfig().apophisExpPerHit();

        // Updating the xp of the players
        damaged.setTotalExperience(exp - expPerHit);

        int toGain = (int) (expPerHit * plugin.getMainConfig().apophisExpPercent());
        attacker.setTotalExperience(attacker.getTotalExperience() + toGain);

        // Calling the exp change event to allow for sharing if the spark is active
        new PlayerExpChangeEvent(attacker, toGain).callEvent();
    }

    @EventHandler
    public void apophisPreserveConsumables(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Making sure the player has the apophis effect
        if (!plugin.getDataManager().hasEffect(player, this)) return;

        ItemStack consumedItem = event.getItem();

        // Not allowing potions to be be preserved
        if (consumedItem.getType() == Material.POTION) return;

        // Getting the chance for the item to not be consumed
        double chance = 0.5;
        if (CooldownManager.isEffectActive(player.getUniqueId(), "apophis")) chance = 0.75;

        // Rolling the dice
        if (Math.random() > chance) return;

        // Refunding the item
        consumedItem.add(1);
        event.setItem(consumedItem);

        // Playing a noise
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 3, 1.5, 0.5, 0.5, 0.01);
    }

    @EventHandler
    public void expShare(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (!CooldownManager.isEffectActive(player.getUniqueId(), "apophis")) return;

        for (OfflinePlayer trusted : plugin.getDataManager().getTrusted(player)) {
            Player trustedPlayer = trusted.getPlayer();
            if (trustedPlayer == null) continue;

            int toGain = (int) (event.getAmount() * plugin.getMainConfig().apophisPercentExpToShare());
            trustedPlayer.setTotalExperience(trustedPlayer.getTotalExperience() + toGain);

            // Not calling PlayerExpChangeEvent to prevent infinite looping
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        UUID attackerUUID = attacker.getUniqueId();

        if (event.getEntity() instanceof Player target) {
            if (CooldownManager.isEffectActive(attackerUUID, "apophis")) {
                target.showTitle(Title.title(Component.text("\uE090"), Component.empty(), Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)));
            }
        }
    }
}