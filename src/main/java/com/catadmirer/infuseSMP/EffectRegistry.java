package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public class EffectRegistry {
    private static final Map<Key,InfuseEffect> EFFECTS = new HashMap<>();

    public static boolean has(Key key) {
        return EFFECTS.containsKey(key);
    }

    public static boolean register(InfuseEffect effect) {
        // Enforcing the id limit
        if (effect.getId() > 100) {
            Infuse.LOGGER.warn("Effect id {} for {} is invalid.  Effect ids cannot be >100.", effect.getId(), effect.key());
            return false;
        }

        if (EFFECTS.containsKey(effect.key())) {
            InfuseEffect existing = EFFECTS.get(effect.key());
            Infuse.LOGGER.warn("Effect key {} has already been taken by {}.  Cannot assign it to {}.", effect.key(), existing.key(), effect.key());
            return false;
        }

        // Attempting to register the effect
        EFFECTS.put(effect.getRegularVersion().key(), effect.getRegularVersion());
        EFFECTS.put(effect.getAugmentedVersion().key(), effect.getAugmentedVersion());

        // Registering event listeners in the effect
        Bukkit.getPluginManager().registerEvents(effect, Infuse.getInstance());

        return true;
    }

    /**
     * Gets a registered effect.
     * 
     * @param key The key of the effect.
     * 
     * @return The registered effect or null if no effect is registered under the specified key.
     */
    @Nullable
    public static InfuseEffect get(Key key) {
        return EFFECTS.get(key);
    }

    /**
     * Gets a registered effect.
     * 
     * @param item An item created by an effect.
     * 
     * @return The registered effect or null if the item does not come from a registered effect.
     */
    public static InfuseEffect get(@Nullable ItemStack item) {
        if (item == null) return null;
        if (item.getType() != Material.POTION) return null;

        String key = item.getPersistentDataContainer().get(InfuseEffect.EFFECT_KEY, PersistentDataType.STRING);
        if (key == null) return null;

        return get(Key.key("infuse", key));
    }

    /** Gets the list of registered effects. */
    @NonNull
    @Unmodifiable
    public static List<InfuseEffect> effects() {
        return List.copyOf(EFFECTS.values());
    }
}
