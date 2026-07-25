package com.catadmirer.infuseSMP.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

public class BasicRegionBlocker extends RegionBlocker {

    @Override
    public boolean canUseSpark(Player player) {
        return true;
    }

    @Override
    public boolean canBeTargetedBySpark(Entity entity) {
        return true;
    }

    @Override
    public boolean canBeTargetedBySpark(Player player) {
        return true;
    }

    @Override
    public Set<InfuseEffect> getAllowedEffects(Entity entity) {
        return getAllowedEffects(entity.getLocation());
        
    }

    @Override
    public Set<InfuseEffect> getAllowedEffects(Player player) {
        return getAllowedEffects(player.getLocation());
    }

    @Override
    public Set<InfuseEffect> getAllowedEffects(Location loc) {
        return InfuseEffect.getRegisteredEffects()
            .values()
            .stream()
            .filter(e -> {
                List<NamespacedKey> worlds = Infuse.getInstance().getMainConfig().getBlacklistedWorlds(e);

                return !worlds.contains(loc.getWorld().getKey());
            })
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isEffectAllowed(Entity entity, InfuseEffect effect) {
        return isEffectAllowed(entity.getLocation(), effect);
    }

    @Override
    public boolean isEffectAllowed(Player player, InfuseEffect effect) {
        return isEffectAllowed(player.getLocation(), effect);
    }

    @Override
    public boolean isEffectAllowed(Location loc, InfuseEffect effect) {
        List<NamespacedKey> worlds = Infuse.getInstance().getMainConfig().getBlacklistedWorlds(effect);

        return !worlds.contains(loc.getWorld().getKey());
    }
    
}
