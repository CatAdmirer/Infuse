package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Pattern;

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

    public String stripAllColors(String input) {
        if (input == null) return null;
        Pattern pattern = Pattern.compile(
                "(§#[0-9a-fA-F]{6})" +
                        "|(§x(§[0-9a-fA-F]){6})" +
                        "|(§[0-9a-fk-orA-FK-OR])" +
                        "|(�x(�[0-9a-fA-F]){6})" +
                        "|�"
        );
        return pattern.matcher(input).replaceAll("");
    }

    private void playDustEffect(Player player, final boolean bottomToTop, EffectMapping itemName, Location location) {
        final Location base = location.add(0.0, 0.1, 0.0);
        final World world = location.getWorld();
        Color color = null;
        String itemthingy = itemName.getEffectName();
        String meowmeow = stripAllColors(itemthingy);
        meowmeow = ChatColor.stripColor(meowmeow);
        String itemNameChanged = plugin.getEffectReversed(meowmeow);
        EffectMapping mapping = EffectMapping.fromEffectKey(itemNameChanged);
        if (mapping == null) return;
        switch (mapping.getEffectId()) {
            case 0, 1:
                color = Color.GREEN;
                break;
            case 2, 3:
                color = Color.fromRGB(0xBEA3CA);
                break;
            case 4, 5:
                color = Color.fromRGB(0xFC7803);
                break;
            case 6, 7:
                color = Color.AQUA;
                break;
            case 8, 9:
                color = Color.fromRGB(0xB96C00);
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
                color = Color.fromRGB(0xD1A44B);
                break;
            case 20, 21:
                color = Color.fromRGB(0x8B0000);
                break;
            case 22, 23:
                color = Color.fromRGB(0xFCED00);
                break;
            case 24, 26:
                color = Color.PURPLE;
                break;
            case 25, 27:
                color = Color.fromRGB(0x45033E);
                break;
            case 28, 29:
                color = Color.RED;
                break;
            default:
                return;
        }
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
        }).runTaskTimer(Infuse.getInstance(), 0, 1);
        world.playSound(base, Sound.ENTITY_TURTLE_EGG_BREAK, 1.3F, 1.2F);
    }

    private void playDustEffectDrop(Player player, final boolean bottomToTop, EffectMapping itemName, Location location) {
        final Location base = location.add(0, -1.5, 0);
        final World world = location.getWorld();
        Color color = null;
        String itemthingy = itemName.getEffectName();
        String meowmeow = stripAllColors(itemthingy);
        meowmeow = ChatColor.stripColor(meowmeow);
        String itemNameChanged = plugin.getEffectReversed(meowmeow);
        EffectMapping mapping = EffectMapping.fromEffectKey(itemNameChanged);
        if (mapping == null) return;
        switch (mapping.getEffectId()) {
            case 0, 1:
                color = Color.GREEN;
                break;
            case 2, 3:
                color = Color.fromRGB(0xBEA3CA);
                break;
            case 4, 5:
                color = Color.fromRGB(0xFC7803);
                break;
            case 6, 7:
                color = Color.AQUA;
                break;
            case 8, 9:
                color = Color.fromRGB(0xB96C00);
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
                color = Color.fromRGB(0xD1A44B);
                break;
            case 20, 21:
                color = Color.fromRGB(0x8B0000);
                break;
            case 22, 23:
                color = Color.fromRGB(0xFCED00);
                break;
            case 24, 26:
                color = Color.PURPLE;
                break;
            case 25, 27:
                color = Color.fromRGB(0x45033E);
                break;
            case 28, 29:
                color = Color.RED;
                break;
            default:
                return;
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
