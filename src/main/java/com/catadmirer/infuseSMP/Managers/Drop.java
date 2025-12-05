package com.catadmirer.infuseSMP.Managers;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.util.MessageUtil;
import org.bukkit.ChatColor;
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

    private void playDustEffect(Player player, final boolean bottomToTop, EffectMapping effect, Location location) {
        final Location base = location.add(0, 0.1, 0);
        final World world = location.getWorld();
        String effectName = effect.getEffectName();
        String strippedName = MessageUtil.stripAllColors(effectName);
        strippedName = ChatColor.stripColor(strippedName);
        String itemNameChanged = plugin.getEffectReversed(strippedName);
        Integer abilityId = EffectMaps.getEffectId(itemNameChanged);

        Color color = switch (abilityId) {
            case 0, 1 -> Color.GREEN;
            case 2, 3 -> Color.fromRGB(0xBEA3CA);
            case 4, 5 -> Color.fromRGB(0xFC7803);
            case 6, 7 -> Color.AQUA;
            case 8, 9 -> Color.fromRGB(0xB96C00);
            case 10, 11 -> Color.fromRGB(0xFC0046);
            case 14, 15 -> Color.fromRGB(0x005AFC);
            case 16, 17 -> Color.fromRGB(0xFF03EF);
            case 18, 19 -> Color.fromRGB(0xD1A44B);
            case 20, 21 -> Color.fromRGB(0x8B0000);
            case 22, 23 -> Color.fromRGB(0xFCED00);
            case 24, 25 -> Color.PURPLE;
            case 26, 27 -> Color.fromRGB(0x45033E);
            case 28, 29 -> Color.RED;
            default -> null;
        };

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
        }).runTaskTimer(Infuse.getInstance(), 0L, 1L);
        world.playSound(base, Sound.ENTITY_TURTLE_EGG_BREAK, 1.3F, 1.2F);
    }

    private void playDustEffectDrop(Player player, final boolean bottomToTop, EffectMapping itemName, Location location) {
        final Location base = location.add(0, -1.5, 0);
        final World world = location.getWorld();
        Color color = null;
        String itemthingy = itemName.getEffectName();
        String meowmeow = MessageUtil.stripAllColors(itemthingy);
        meowmeow = ChatColor.stripColor(meowmeow);
        String itemNameChanged = plugin.getEffectReversed(meowmeow);
        Integer abilityId = EffectMaps.getEffectId(itemNameChanged);
        switch (abilityId) {
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
            case 24, 25:
                color = Color.PURPLE;
                break;
            case 26, 27:
                color = Color.fromRGB(0x45033E);
                break;
            case 28, 29:
                color = Color.RED;
                break;
            default:
                break;
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
        }).runTaskTimer(Infuse.getInstance(), 0L, 1L);
        world.playSound(base, Sound.ENTITY_TURTLE_EGG_BREAK, 1.3F, 1.2F);
    }
}
