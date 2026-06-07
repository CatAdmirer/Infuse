package com.catadmirer.infuseSMP.worldguard;

import com.catadmirer.infuseSMP.Infuse;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;

public class WorldGuardHandler {

    private final Infuse plugin;

    public static StateFlag USE_SPARKS;
    public static StateFlag SPARK_PASSTHROUGH;
    public static StateFlag OCEAN_ENABLED;

    public WorldGuardHandler(Infuse plugin) {
        this.plugin = plugin;

        this.USE_SPARKS = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("regionspark-enabled");
        this.SPARK_PASSTHROUGH = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("regionspark-passthrough");
        this.OCEAN_ENABLED = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("regionocean-enabled");
    }

    public static boolean isProtected(Location loc, StateFlag flag) {
        return false;
    }

}
