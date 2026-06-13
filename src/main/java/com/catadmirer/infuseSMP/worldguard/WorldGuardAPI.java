package com.catadmirer.infuseSMP.worldguard;

import com.catadmirer.infuseSMP.Infuse;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
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
        } catch (FlagConflictException ex) {
            Infuse.LOGGER.warn("A flag has conflicted with another plugin! Disabling worldguard hook...");
            enabled = false;
            return;
        }

        enabled = true;
        Infuse.LOGGER.info("Successfully loaded custom WorldGuard flags.");
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

    public static boolean isFlagEnabled(Player player, StateFlag flag) {
        final ApplicableRegionSet set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        return set.queryState(WorldGuardPlugin.inst().wrapPlayer(player), flag) != StateFlag.State.DENY;
    }


}
