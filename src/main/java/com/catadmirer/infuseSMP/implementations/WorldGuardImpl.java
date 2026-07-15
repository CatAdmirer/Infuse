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

        InfuseEffect.getRegisteredEffects().values().stream().map(s -> new StateFlag("allow-" + s.getKey(), true)).forEach(flag -> {
            final String name = flag.getName().toLowerCase();
            try {
                registry.register(flag);
                flags.put(name.replace("allow-", ""), flag);
            } catch (FlagConflictException ex) {
                final Flag<?> existing = registry.get(flag.getName());

                if (existing instanceof StateFlag exist) {
                    Infuse.LOGGER.warn("The flag 'allow-%s' may be buggy, this is due to another plugin already registering this as a custom flag.".formatted(name));
                    flags.put(name.replace("allow-", ""), exist);
                } else {
                    Infuse.LOGGER.error("The flag 'allow-%s' has failed to register, another plugin is currently conflicting. This flag will not work.".formatted(name));
                    flags.put(name.replace("allow-", ""), null);
                }
            }
        });

        Stream.of("use-sparks", "spark-passthrough").map(s -> new StateFlag(s, true)).forEach(flag -> {
            final String name = flag.getName().toLowerCase();
            try {
                registry.register(flag);
                flags.put(name, flag);
            } catch (FlagConflictException ex) {
                final Flag<?> existing = registry.get(flag.getName());

                if (existing instanceof StateFlag exist) {
                    Infuse.LOGGER.warn("The flag 'allow-%s' may be buggy, this is due to another plugin already registering this as a custom flag.".formatted(name));
                    flags.put(name, exist);
                } else {
                    Infuse.LOGGER.error("The flag 'allow-%s' has failed to register, another plugin is currently conflicting. This flag will not work.".formatted(name));
                    flags.put(name, null);
                }
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
