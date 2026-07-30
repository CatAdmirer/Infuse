package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.platform.BossBarColor;
import com.catadmirer.infuseSMP.platform.Player;

import java.awt.Color;

public interface InfuseEffect {
    public int id();
    public String plainKey();
    public String key();
    public boolean augmented();
    public Color potionColor();
    public BossBarColor ritualColor();

    public void equip(Player owner);
    public void unequip(Player owner);
    @Deprecated(since = "2.4.5") public void applyPassives(Player owner);
    public void activateSpark(Player owner);

    public InfuseEffect getRegularVersion();
    public InfuseEffect getAugmentedVersion();

    public Message getName();
    public Message lore();

    public default char getIcon() {
        return (char) Integer.parseInt("E" + (augmented() ? 2 : 0) + String.format("%02d", id()), 16);
    }

    public default char getActiveIcon() {
        return (char) Integer.parseInt("E" + (augmented() ? 3 : 1) + String.format("%02d", id()), 16);
    }

    /** Serializes an InfuseEffect into an int */
    public default int serialize() {
        return (augmented() ? 100 : 0) + id();
    }
}
