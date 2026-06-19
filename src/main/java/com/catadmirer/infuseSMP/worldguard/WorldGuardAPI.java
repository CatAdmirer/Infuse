package com.catadmirer.infuseSMP.worldguard;

import com.catadmirer.infuseSMP.Infuse;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardAPI {

    private static Boolean loaded;
    private static Boolean enabled;

    private static StateFlag USE_SPARKS;
    private static StateFlag SPARK_PASSTHROUGH;
    private static StateFlag OCEAN_ENABLED;

    public void init() {
        USE_SPARKS = new StateFlag("region-use_sparks", true);
        SPARK_PASSTHROUGH = new StateFlag("region-spark_passthrough", true);
        OCEAN_ENABLED = new StateFlag("region-ocean_enabled", true);

        try {
            WorldGuard.getInstance().getFlagRegistry().registerAll(List.of(USE_SPARKS, SPARK_PASSTHROUGH, OCEAN_ENABLED));
        } catch (FlagConflictException ex) {
            Infuse.LOGGER.warn("A flag has conflicted with another plugin! Disabling worldguard hook...");
            loaded = false;
            return;
        }

        loaded = true;
        Infuse.LOGGER.info("Successfully loaded custom WorldGuard flags.");
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean status) {
        enabled = status;
    }

    public static StateFlag getUseSparksFlag() {
        return USE_SPARKS;
    }

    public static StateFlag getSparkPassThroughFlag() {
        return SPARK_PASSTHROUGH;
    }

    public static StateFlag getOceanEnabledFlag() {
        return OCEAN_ENABLED;
    }

    public static boolean isFlagEnabled(Entity entity, StateFlag flag) {
        if (!enabled) return true;

        final RegionQuery set = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        if (entity instanceof Player) {
            return set.getApplicableRegions(BukkitAdapter.adapt(entity.getLocation())).queryState(WorldGuardPlugin.inst().wrapPlayer((Player) entity), flag) != StateFlag.State.DENY;
        } else {
            return set.queryState(BukkitAdapter.adapt(entity.getLocation()), null, flag) != StateFlag.State.DENY;
        }
    }

    public static List<Entity> getAllEntitiesWhereFlagIsAllowed(Player player, double radius, StateFlag flag) {
        return new ArrayList<>(player.getNearbyEntities(radius, radius, radius).stream().filter(entity -> isFlagEnabled(entity, flag)).toList());
    }

    public static List<Player> getAllPlayersWhereFlagIsAllowed(Player player, double radius, StateFlag flag) {
        final List<Player> players = new ArrayList<>();

        player.getNearbyEntities(radius, radius, radius).stream().filter(plr -> plr instanceof Player && isFlagEnabled(plr, flag)).toList()
                .forEach(target -> players.add((Player) target));

        return players;
    }


}
