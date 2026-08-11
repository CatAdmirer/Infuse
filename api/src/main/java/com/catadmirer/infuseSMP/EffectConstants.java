package com.catadmirer.infuseSMP;

import java.awt.Color;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;

public class EffectConstants {
    public enum Id {
        EMERALD,
        ENDER,
        FEATHER,
        FIRE,
        FROST,
        HASTE,
        HEART,
        INVIS,
        OCEAN,
        REGEN,
        SPEED,
        STRENGTH,
        THUNDER,
        APOPHIS,
        THIEF;

        public int value() {
            return ordinal();
        }
    }

    public enum BackgroundColor {
        EMERALD(Material.LIME_STAINED_GLASS_PANE),
        ENDER(Material.PURPLE_STAINED_GLASS_PANE),
        FEATHER(Material.WHITE_STAINED_GLASS_PANE),
        FIRE(Material.ORANGE_STAINED_GLASS_PANE),
        FROST(Material.LIGHT_BLUE_STAINED_GLASS_PANE),
        HASTE(Material.ORANGE_STAINED_GLASS_PANE),
        HEART(Material.RED_STAINED_GLASS_PANE),
        INVIS(Material.LIGHT_GRAY_STAINED_GLASS_PANE),
        OCEAN(Material.BLUE_STAINED_GLASS_PANE),
        REGEN(Material.RED_STAINED_GLASS_PANE),
        SPEED(Material.LIGHT_BLUE_STAINED_GLASS_PANE),
        STRENGTH(Material.RED_STAINED_GLASS_PANE),
        THUNDER(Material.YELLOW_STAINED_GLASS_PANE),
        APOPHIS(Material.MAGENTA_STAINED_GLASS_PANE),
        THIEF(Material.RED_STAINED_GLASS_PANE);

        private final Material material;

        BackgroundColor(Material material) {
            this.material = material;
        }

        public Material value() {
            return material;
        }
    }

    public enum PotionColor {
        EMERALD(Color.GREEN),
        ENDER(new Color(0x800080)),
        FEATHER(new Color(0xBEA3CA)),
        FIRE(new Color(0xEE5522)),
        FROST(new Color(0x55FFFF)),
        HASTE(new Color(0xFFCC33)),
        HEART(Color.RED),
        INVIS(new Color(0xAA00AA)),
        OCEAN(new Color(0x0066FF)),
        REGEN(new Color(0xFF5555)),
        SPEED(new Color(0xEEBB77)),
        STRENGTH(new Color(0x800000)),
        THUNDER(Color.YELLOW),
        APOPHIS(new Color(0x440044)),
        THIEF(new Color(0xAA0000));

        private final Color color;

        PotionColor(Color color) {
            this.color = color;
        }

        public Color value() {
            return color;
        }
    }

    public enum RitualColor {
        EMERALD(BossBar.Color.GREEN),
        ENDER(BossBar.Color.PURPLE),
        FEATHER(BossBar.Color.WHITE),
        FIRE(BossBar.Color.RED),
        FROST(BossBar.Color.BLUE),
        HASTE(BossBar.Color.YELLOW),
        HEART(BossBar.Color.RED),
        INVIS(BossBar.Color.PURPLE),
        OCEAN(BossBar.Color.BLUE),
        REGEN(BossBar.Color.PINK),
        SPEED(BossBar.Color.YELLOW),
        STRENGTH(BossBar.Color.RED),
        THUNDER(BossBar.Color.RED),
        APOPHIS(BossBar.Color.PURPLE),
        THIEF(BossBar.Color.YELLOW);

        private final BossBar.Color color;

        RitualColor(BossBar.Color color) {
            this.color = color;
        }

        public BossBar.Color value() {
            return color;
        }
    }
}
