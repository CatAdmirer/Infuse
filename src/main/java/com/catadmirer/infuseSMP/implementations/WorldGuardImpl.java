package com.catadmirer.infuseSMP.implementations;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.util.EffectFlag;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class WorldGuardImpl {
    private static final SetFlag<InfuseEffect> ALLOWED_EFFECTS = new SetFlag<>("allowed-effects", new EffectFlag(null));
    private static final StateFlag USE_SPARKS = new StateFlag("use-sparks", true);
    private static final StateFlag SPARK_PASSTHROUGH = new StateFlag("spark_passthrough", true);

    private static boolean enabled = false;

    public static void enable() {
        if (enabled) return;

        enabled = true;

        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        Stream.of(ALLOWED_EFFECTS, USE_SPARKS, SPARK_PASSTHROUGH)
            .forEach(flag -> {
                try {
                    registry.register(flag);
                } catch (FlagConflictException err) {
                    Infuse.LOGGER.warn("Another plugin has already registered the flag \"{}\".  Cannot register the flag.", flag.getName());
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

    public static boolean canUseSpark(Player player) {
        if (!enabled) return true;

        return queryValue(player.getLocation(), USE_SPARKS, WorldGuardPlugin.inst().wrapPlayer(player)) == StateFlag.State.ALLOW;
    }

    public static boolean canBeTargetedBySpark(Entity entity) {
        if (!enabled) return true;

        return queryValue(entity.getLocation(), SPARK_PASSTHROUGH, null) == StateFlag.State.ALLOW;
    }

    public static boolean canBeTargetedBySpark(Player player) {
        if (!enabled) return true;

        return queryValue(player.getLocation(), SPARK_PASSTHROUGH, WorldGuardPlugin.inst().wrapPlayer(player)) == StateFlag.State.ALLOW;
    }

    public static Set<InfuseEffect> getAllowedEffects(Entity entity) {
        return getAllowedEffects(entity.getLocation(), null);
    }

    public static Set<InfuseEffect> getAllowedEffects(Player player) {
        return getAllowedEffects(player.getLocation(), WorldGuardPlugin.inst().wrapPlayer(player));
    }

    public static Set<InfuseEffect> getAllowedEffects(Location loc) {
        return getAllowedEffects(loc, null);
    }

    public static Set<InfuseEffect> getAllowedEffects(Location loc, RegionAssociable assoc) {
        return queryValue(loc, ALLOWED_EFFECTS, assoc);
    }

    public static boolean isEffectAllowed(Entity entity, InfuseEffect effect) {
        return isEffectAllowed(entity.getLocation(), null, effect);
    }

    public static boolean isEffectAllowed(Player player, InfuseEffect effect) {
        return isEffectAllowed(player.getLocation(), WorldGuardPlugin.inst().wrapPlayer(player), effect);
    }

    public static boolean isEffectAllowed(Location loc, InfuseEffect effect) {
        return isEffectAllowed(loc, null, effect);
    }

    public static boolean isEffectAllowed(Location loc, RegionAssociable assoc, InfuseEffect effect) {
        return getAllowedEffects(loc, assoc).stream()
            .filter(e -> e.getId() == effect.getId())
            .findAny()
            .isPresent();
    }

    @Nullable
    private static <T> T queryValue(Location loc, Flag<T> flag, RegionAssociable assoc) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null) return null;

        return manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).queryValue(assoc, flag);
    }
}
