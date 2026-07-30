package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.platform.BossBarColor;

import java.awt.Color;

public class EffectConstants {
    /**
     * Gets the color for the glass pane to use in the background of this effect's AugOrRegChooser menu.
     *
     * @param effectId The id of the infuse effect.
     *
     * @return the color for the effect.
     */
    public static String menuBackgroundColor(int effectId) {
        return switch (effectId) {
            case EffectIds.EMERALD -> "LIME";
            case EffectIds.ENDER -> "PURPLE";
            case EffectIds.FEATHER -> "WHITE";
            case EffectIds.FIRE, EffectIds.HASTE -> "ORANGE";
            case EffectIds.FROST, EffectIds.SPEED -> "LIGHT_BLUE";
            case EffectIds.HEART, EffectIds.REGEN, EffectIds.STRENGTH, EffectIds.THIEF -> "RED";
            case EffectIds.INVIS -> "LIGHT_GRAY";
            case EffectIds.OCEAN -> "BLUE";
            case EffectIds.THUNDER -> "YELLOW";
            case EffectIds.APOPHIS -> "MAGENTA";
            default -> null;
        };
    }

    /**
     * Gets the {@link Color} for this effect's potion and related text.
     *
     * @param effectId The id of the infuse effect.
     *
     * @return the {@link Color} for the effect.
     */
    public static Color potionColor(int effectId) {
        return switch (effectId) {
            case EffectIds.EMERALD -> Color.GREEN;
            case EffectIds.ENDER -> new Color(0x800080);
            case EffectIds.FEATHER -> new Color(0xBEA3CA);
            case EffectIds.FIRE -> new Color(0xEE5522);
            case EffectIds.FROST -> new Color(0x55FFFF);
            case EffectIds.HASTE -> new Color(0xFFCC33);
            case EffectIds.HEART -> Color.RED;
            case EffectIds.INVIS -> new Color(0xAA00AA);
            case EffectIds.OCEAN -> new Color(0x0066FF);
            case EffectIds.REGEN -> new Color(0xFF5555);
            case EffectIds.SPEED -> new Color(0xEEBB77);
            case EffectIds.STRENGTH -> new Color(0x800000);
            case EffectIds.THUNDER -> Color.YELLOW;
            case EffectIds.APOPHIS -> new Color(0x440044);
            case EffectIds.THIEF -> new Color(0xAA0000);
            default -> null;
        };
    }

    /**
     * Gets the {@link BossBarColor} for this effect's ritual.
     *
     * @param effectId The id of the infuse effect.
     *
     * @return the {@link BossBarColor} for the effect.
     */
    public static BossBarColor ritualColor(int effectId) {
        return switch (effectId) {
            case EffectIds.EMERALD -> BossBarColor.GREEN;
            case EffectIds.ENDER, EffectIds.INVIS, EffectIds.APOPHIS -> BossBarColor.PURPLE;
            case EffectIds.FEATHER -> BossBarColor.WHITE;
            case EffectIds.FIRE, EffectIds.HEART, EffectIds.STRENGTH, EffectIds.THUNDER -> BossBarColor.RED;
            case EffectIds.FROST, EffectIds.OCEAN -> BossBarColor.BLUE;
            case EffectIds.HASTE, EffectIds.SPEED, EffectIds.THIEF -> BossBarColor.YELLOW;
            case EffectIds.REGEN -> BossBarColor.PINK;
            default -> null;
        };
    }
}
