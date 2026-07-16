package com.catadmirer.infuseSMP.implementations;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WorldGuardImpl {

    private static boolean enabled = false;

    private static final Map<String, StateFlag> flags = new HashMap<>();

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
        if (!enabled) return true;

        final StateFlag flag = flags.get(flagName.toLowerCase());
        if (flag == null) return true;

        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (container == null) return true;

        final Location loc = entity.getLocation();
        final RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return true;

        if (entity instanceof final Player player) {
            return manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).testState(WorldGuardPlugin.inst().wrapPlayer(player), flag);
        } else {
            return manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).testState(null, flag);
        }
    }

    public static boolean isFlagEnabled(Location loc, String flagName) {
        if (!enabled) return true;

        final StateFlag flag = flags.get(flagName.toLowerCase());
        if (flag == null) return true;

        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (container == null) return true;

        final RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return true;

        return manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).testState(null, flag);
    }

}
