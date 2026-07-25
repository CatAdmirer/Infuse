package com.catadmirer.infuseSMP.util;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;

public abstract class RegionBlocker {
    private static RegionBlocker instance;

    public static void setInstance(RegionBlocker instance) {
        RegionBlocker.instance = instance;
    }

    public static RegionBlocker getInstance() {
        return instance;
    }

    public abstract boolean canUseSpark(Player player);

    public abstract boolean canBeTargetedBySpark(Entity entity);
    public abstract boolean canBeTargetedBySpark(Player player);
    
    public abstract Set<InfuseEffect> getAllowedEffects(Entity entity);
    public abstract Set<InfuseEffect> getAllowedEffects(Player player);
    public abstract Set<InfuseEffect> getAllowedEffects(Location loc);
    
    public abstract boolean isEffectAllowed(Entity entity, InfuseEffect effect);
    public abstract boolean isEffectAllowed(Player player, InfuseEffect effect);
    public abstract boolean isEffectAllowed(Location loc, InfuseEffect effect);

    // TODO: let people check for worldguard plugin
    // TODO: Make worldguardimpl a child of this class
    // TODO: make a child of this that uses blacklisted-worlds configs
    // TODO: make a way to use both blacklisted-worlds and worldguard at once.
}
