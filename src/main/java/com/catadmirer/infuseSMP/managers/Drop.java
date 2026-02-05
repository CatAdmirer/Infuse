package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class Drop implements Listener {
    private final Infuse plugin;

    public Drop(Infuse plugin) {
        this.plugin = plugin;
    }

    private boolean isPotion(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().hasCustomModelData();
    }

    public void onPickup(EntityPickupItemEvent event) {
        // Making sure the entity is a player
        if (!(event.getEntity() instanceof Player player)) return;

        // Making sure the item has been picked up.
        if (event.isCancelled()) return;

        ItemStack item = event.getItem().getItemStack();
        if (this.isPotion(item)) {
            this.playDustEffect(player, true, EffectMapping.fromItem(item), event.getItem().getLocation());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        final Item droppedItem = event.getItemDrop();
        ItemStack itemStack = droppedItem.getItemStack();
        if (this.isPotion(itemStack)) {
            this.playDustEffectDrop(event.getPlayer(), false, EffectMapping.fromItem(itemStack), droppedItem.getLocation());
            (new BukkitRunnable() {
                public void run() {
                        droppedItem.setGlowing(true);
                }
            }).runTaskLater(this.plugin, 1L);
        }
    }

    private void playDustEffect(Player player, final boolean bottomToTop, @NotNull EffectMapping effect, Location location) {
        final Location base = location.add(0, 0.1, 0);
        final World world = location.getWorld();
        Color color = Color.fromRGB(effect.getColor().getRGB());
        final Particle.DustOptions dust = new Particle.DustOptions(color, 0.7F);
        final int points = 16;
        final double radius = 0.6;
        (new BukkitRunnable() {
            double y = 0;

            public void run() {
                if (this.y > 2) {
                    this.cancel();
                } else {
                    double ringY = bottomToTop ? this.y : 2 - this.y;

                    for(int i = 0; i < points; ++i) {
                        double angle = Math.PI * 2 * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        world.spawnParticle(Particle.DUST, base.clone().add(x, ringY, z), 0, 0, 0, 0, 1, dust);
                    }

                    this.y += 0.15;
                }
            }
        }).runTaskTimer(plugin, 0, 1);
        world.playSound(base, Sound.ENTITY_TURTLE_EGG_BREAK, 1.3F, 1.2F);
    }

    private void playDustEffectDrop(Player player, final boolean bottomToTop, EffectMapping effect, Location location) {
        final Location base = location.add(0, -1.5, 0);
        final World world = location.getWorld();
        Color color = Color.fromRGB(effect.getColor().getRGB());
        final Particle.DustOptions dust = new Particle.DustOptions(color, 0.7F);
        final int points = 16;
        final double radius = 0.6;
        (new BukkitRunnable() {
            double y = 0;

            public void run() {
                if (this.y > 2) {
                    this.cancel();
                } else {
                    double ringY = bottomToTop ? this.y : 2 - this.y;

                    for(int i = 0; i < points; ++i) {
                        double angle = Math.PI * 2 * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        world.spawnParticle(Particle.DUST, base.clone().add(x, ringY, z), 0, 0, 0, 0, 1, dust);
                    }

                    this.y += 0.15;
                }
            }
        }).runTaskTimer(plugin, 0L, 1L);
        world.playSound(base, Sound.ENTITY_TURTLE_EGG_BREAK, 1.3F, 1.2F);
    }
}
