package com.catadmirer.infuseSMP.worldguard;

import com.catadmirer.infuseSMP.Infuse;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class WorldGuardAPI {

    private static Boolean enabled;

    private StateFlag USE_SPARKS;
    private StateFlag SPARK_PASSTHROUGH;
    private StateFlag OCEAN_ENABLED;

    public void init() {
        this.USE_SPARKS = new StateFlag("region-use_sparks", true);
        this.SPARK_PASSTHROUGH = new StateFlag("region-spark_passthrough", true);
        this.OCEAN_ENABLED = new StateFlag("region-ocean_enabled", true);

        try {
            WorldGuard.getInstance().getFlagRegistry().registerAll(List.of(this.USE_SPARKS, this.SPARK_PASSTHROUGH, this.OCEAN_ENABLED));
            enabled = true;
        } catch (FlagConflictException ex) {
            Infuse.LOGGER.warn("A flag has conflicted with another plugin! Disabling worldguard hook...");
            enabled = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public StateFlag getUseSparksFlag() {
        return USE_SPARKS;
    }

    public StateFlag getSparkPassThroughFlag() {
        return SPARK_PASSTHROUGH;
    }

    public StateFlag getOceanEnabledFlag() {
        return OCEAN_ENABLED;
    }

    public static boolean isFlagEnabled(Player player, Location loc, StateFlag flag) {
        if (loc.getWorld() == null) return true;

        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (container == null) return true;

        final RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return true;

        return manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).testState(WorldGuardPlugin.inst().wrapPlayer(player), flag);
    }

}
