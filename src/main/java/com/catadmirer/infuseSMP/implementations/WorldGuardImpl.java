package com.catadmirer.infuseSMP.implementations;

import com.catadmirer.infuseSMP.Infuse;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WorldGuardImpl {

    private static boolean enabled = false;

    private static StateFlag USE_SPARKS;
    private static StateFlag SPARK_PASSTHROUGH;

    public static void load() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            final StateFlag flag = new StateFlag("use-sparks", true);
            registry.register(flag);
            USE_SPARKS = flag;
        } catch (FlagConflictException ex) {
            final Flag<?> existing = registry.get("use-sparks");
            if (existing instanceof StateFlag exist) {
                USE_SPARKS = exist;
                Infuse.LOGGER.warn("The flag 'use-sparks' may be buggy, this is due to another plugin already registering this as a custom flag.");
            } else {
                Infuse.LOGGER.error("The flag 'use-sparks' has failed to register, another plugin is currently conflicting. disabling WorldGuard hook.");
                setEnabled(false);
                USE_SPARKS = null;
            }
        }

        try {
            final StateFlag flag = new StateFlag("spark-passthrough", true);
            registry.register(flag);
            SPARK_PASSTHROUGH = flag;
        } catch (FlagConflictException ex) {
            final Flag<?> existing = registry.get("spark-passthrough");
            if (existing instanceof StateFlag exist) {
                SPARK_PASSTHROUGH = exist;
                Infuse.LOGGER.warn("The flag 'spark-passthrough' may be buggy, this is due to another plugin already registering this as a custom flag.");
            } else {
                Infuse.LOGGER.error("The flag 'spark-passthrough' has failed to register, another plugin is currently conflicting. disabling WorldGuard hook.");
                setEnabled(false);
                SPARK_PASSTHROUGH = null;
            }
        }

        if (enabled && USE_SPARKS != null && SPARK_PASSTHROUGH != null) Infuse.LOGGER.info("[INFUSE] Successfully hooked into WorldGuard and registered the custom flags.");
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

    public static boolean isFlagEnabled(LivingEntity entity, StateFlag flag) {
        if (!enabled) return true;

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

    public static boolean isFlagEnabled(Location loc, StateFlag flag) {
        if (!enabled) return true;

        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (container == null) return true;

        final RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return true;

        return manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).testState(null, flag);
    }

    public static StateFlag getUseSparks() {
        return USE_SPARKS;
    }

    public static StateFlag getSparkPassthrough() {
        return SPARK_PASSTHROUGH;
    }
}
