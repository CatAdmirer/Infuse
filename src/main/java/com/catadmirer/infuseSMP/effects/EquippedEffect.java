package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;

public abstract class EquippedEffect implements Listener {
    protected final OfflinePlayer owner;
    protected final InfuseEffect effect;
    protected final int slot;
    protected final Infuse plugin;

    protected boolean active = false;

    public EquippedEffect(OfflinePlayer owner, InfuseEffect effect, int slot, Infuse plugin) {
        this.owner = owner;
        this.effect = effect;
        this.slot = slot;
        this.plugin = plugin;
    }

    public abstract void equip();
    public abstract void unequip();

    public abstract void applyPassives();
    public abstract void activateSpark();
}
