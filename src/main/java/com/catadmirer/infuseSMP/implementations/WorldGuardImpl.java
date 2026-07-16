package com.catadmirer.infuseSMP.implementations;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WorldGuardImpl {

    private static boolean enabled = false;

    public static void load() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        Stream.concat(
                InfuseEffect.getRegisteredEffects().values().stream().map(e -> "allow-" + e.getKey()),
                Stream.of("use-sparks", "spark-passthrough"))
            .map(s -> new StateFlag(s, true))
            .forEach(
                flag -> {
                    try {
                        registry.register(flag);
                    } catch (FlagConflictException err) {
                        Infuse.LOGGER.warn("Another plugin has already registered the flag \"%s\".  Cannot register the flag.".formatted(flag.getName()));
                    }
                });

        if (enabled) Infuse.LOGGER.info("[Infuse] Successfully hooked into WorldGuard and registered the custom flags.");
    }

    public static boolean canEnable() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        WorldGuardImpl.enabled = enabled;
    }

    public static boolean isFlagEnabled(LivingEntity entity, String flagName) {
        return isFlagEnabled(entity.getLocation(), flagName, null);
    }

    public static boolean isFlagEnabled(Player player, String flagName) {
        return isFlagEnabled(player.getLocation(), flagName, WorldGuardPlugin.inst().wrapPlayer(player));
    }

    public static boolean isFlagEnabled(Location loc, String flagName) {
        return isFlagEnabled(loc, flagName, null);
    }

    public static boolean isFlagEnabled(Location loc, String flagName, RegionAssociable assoc) {
        if (!enabled) return true;

        final Flag<?> flag = WorldGuard.getInstance().getFlagRegistry().get(flagName.toLowerCase());
        if (!(flag instanceof StateFlag)) return true;

        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return true;

        return manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).testState(assoc, (StateFlag) flag);
    }
}
