package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.util.MessageUtil;

import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Drop implements Listener {

    private final Infuse plugin;

    public Drop(Infuse plugin) {
        this.plugin = plugin;
    }

    private boolean isMace(ItemStack item) {
        return item != null && item.getType() == Material.POTION && item.getItemMeta().hasCustomModelData();
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (this.isMace(item)) {
            this.playDustEffect(event.getPlayer(), true, EffectMapping.fromItem(item), event.getItem().getLocation());
        }

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        final Item droppedItem = event.getItemDrop();
        ItemStack itemStack = droppedItem.getItemStack();
        if (this.isMace(itemStack)) {
            this.playDustEffectDrop(event.getPlayer(), false, EffectMapping.fromItem(itemStack), droppedItem.getLocation());
            (new BukkitRunnable() {
                public void run() {
                        droppedItem.setGlowing(true);
                }
            }).runTaskLater(this.plugin, 1L);
        }

    }

    private void playDustEffect(Player player, final boolean bottomToTop, EffectMapping itemName, Location location) {
        final Location base = location.add(0.0, 0.1, 0.0);
        final World world = location.getWorld();
        Color color = null;
        String itemthingy = itemName.getEffectName();
        String meowmeow = MessageUtil.stripAllColors(itemthingy);
        meowmeow = ChatColor.stripColor(meowmeow);
        String itemNameChanged = plugin.getEffectReversed(meowmeow);
        Integer abilityId = EffectMaps.getEffectNumber(itemNameChanged);
        switch (abilityId) {
            case 0, 1:
                color = Color.GREEN;
                break;
            case 2, 3:
                color = Color.fromRGB(190, 163, 202);
                break;
            case 4, 5:
                color = Color.fromRGB(252, 120, 3);
                break;
            case 6, 7:
                color = Color.fromRGB(0, 255, 255);
                break;
            case 8, 9:
                color = Color.fromRGB(185, 108, 0);
                break;
            case 10, 11:
                color = Color.fromRGB(0xFC0046);
                break;
            case 14, 15:
                color = Color.fromRGB(0x005AFC);
                break;
            case 16, 17:
                color = Color.fromRGB(0xFF03EF);
                break;
            case 18, 19:
                color = Color.fromRGB(209, 164, 75);
                break;
            case 20, 21:
                color = Color.fromRGB(139, 0, 0);
                break;
            case 22, 23:
                color = Color.fromRGB(252, 237, 0);
                break;
            case 24, 26:
                color = Color.PURPLE;
                break;
            case 25, 27:
                color = Color.fromRGB(69, 3, 62);
                break;
            case 28, 29:
                color = Color.fromRGB(255, 0, 0);
                break;
            default:
                break;
        }
        final Particle.DustOptions dust = new Particle.DustOptions(color, 0.7F);
        final int points = 16;
        final double radius = 0.6;
        (new BukkitRunnable() {
            double y = 0.0;

            public void run() {
                if (this.y > 2.0) {
                    this.cancel();
                } else {
                    double ringY = bottomToTop ? this.y : 2.0 - this.y;

                    for(int i = 0; i < points; ++i) {
                        double angle = Math.PI * 2 * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        world.spawnParticle(Particle.DUST, base.clone().add(x, ringY, z), 0, 0.0, 0.0, 0.0, 1.0, dust);
                    }

                    this.y += 0.15;
                }
            }
        }).runTaskTimer(Infuse.getInstance(), 0L, 1L);
        world.playSound(base, Sound.ENTITY_TURTLE_EGG_BREAK, 1.3F, 1.2F);
    }

    private void playDustEffectDrop(Player player, final boolean bottomToTop, EffectMapping itemName, Location location) {
        final Location base = location.add(0.0, -1.5, 0.0);
        final World world = location.getWorld();
        Color color = null;
        String itemthingy = itemName.getEffectName();
        String meowmeow = MessageUtil.stripAllColors(itemthingy);
        meowmeow = ChatColor.stripColor(meowmeow);
        String itemNameChanged = plugin.getEffectReversed(meowmeow);
        Integer abilityId = EffectMaps.getEffectNumber(itemNameChanged);
        switch (abilityId) {
            case 0, 1:
                color = Color.GREEN;
                break;
            case 2, 3:
                color = Color.fromRGB(190, 163, 202);
                break;
            case 4, 5:
                color = Color.fromRGB(252, 120, 3);
                break;
            case 6, 7:
                color = Color.fromRGB(0, 255, 255);
                break;
            case 8, 9:
                color = Color.fromRGB(185, 108, 0);
                break;
            case 10, 11:
                color = Color.fromRGB(0xFC0046);
                break;
            case 14, 15:
                color = Color.fromRGB(0x005AFC);
                break;
            case 16, 17:
                color = Color.fromRGB(0xFF03EF);
                break;
            case 18, 19:
                color = Color.fromRGB(209, 164, 75);
                break;
            case 20, 21:
                color = Color.fromRGB(139, 0, 0);
                break;
            case 22, 23:
                color = Color.fromRGB(252, 237, 0);
                break;
            case 24, 26:
                color = Color.PURPLE;
                break;
            case 25, 27:
                color = Color.fromRGB(69, 3, 62);
                break;
            case 28, 29:
                color = Color.fromRGB(255, 0, 0);
                break;
            default:
                break;
        }
        final Particle.DustOptions dust = new Particle.DustOptions(color, 0.7F);
        final int points = 16;
        final double radius = 0.6;
        (new BukkitRunnable() {
            double y = 0.0;

            public void run() {
                if (this.y > 2.0) {
                    this.cancel();
                } else {
                    double ringY = bottomToTop ? this.y : 2.0 - this.y;

                    for(int i = 0; i < points; ++i) {
                        double angle = Math.PI * 2 * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        world.spawnParticle(Particle.DUST, base.clone().add(x, ringY, z), 0, 0.0, 0.0, 0.0, 1.0, dust);
                    }

                    this.y += 0.15;
                }
            }
        }).runTaskTimer(Infuse.getInstance(), 0L, 1L);
        world.playSound(base, Sound.ENTITY_TURTLE_EGG_BREAK, 1.3F, 1.2F);
    }
}
