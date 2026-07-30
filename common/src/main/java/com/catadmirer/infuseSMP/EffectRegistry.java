package com.catadmirer.infuseSMP;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.catadmirer.infuseSMP.effects.InfuseEffect;

public class EffectRegistry {
    private static final Map<Integer,InfuseEffect> REGISTERED_EFFECTS = new HashMap<>();

    public static boolean isRegistered(InfuseEffect effect) {
        return REGISTERED_EFFECTS.containsKey(effect.id());
    }

    public static boolean register(InfuseEffect effect) {
        if (effect.id() > 100) {
            Infuse.LOGGER.warn("Effect id {} for {} is invalid.  Effect ids cannot be >100.", effect.id(), effect.plainKey());
            return false;
        }

        InfuseEffect existing = REGISTERED_EFFECTS.get(effect.id());
        if (existing != null) {
            Infuse.LOGGER.warn("Effect id {} has already been taken by {}.  Cannot assign it to {}.", effect.id(), existing.plainKey(), effect.plainKey());
            return false;
        }

        REGISTERED_EFFECTS.put(effect.id(), effect);
        return true;
    }

    @NonNull
    @Unmodifiable
    public static Map<Integer,InfuseEffect> getRegisteredEffects() {
        return Map.copyOf(REGISTERED_EFFECTS);
    }

    public static InfuseEffect fromString(@Nullable String key) {
        if (key == null) return null;

        // Checking if the effect is augmented
        boolean augmented = key.startsWith("aug_");
        if (augmented) {
            key = key.substring(4);
        }

        // Searching for a matching registered effect
        for (InfuseEffect effect : REGISTERED_EFFECTS.values()) {
            if (!effect.plainKey().equals(key)) continue;

            return augmented ? effect.getAugmentedVersion() : effect.getRegularVersion();
        }

        Infuse.LOGGER.warn("No effect found for string '{}'.", key);
        return null;
    }

    /**
     * Deserializes an InfuseEffect from an int
     * <br>
     * The first two digits of an infuse effect are the effect id.  IDs 0-12 are taken by the base Effects.
     * If the number is >= 100, then the effect will be converted to its augmented form.
     *
     * @param serialized The serialized int
     */
    public static InfuseEffect deserialize(int serialized) {
        if (!REGISTERED_EFFECTS.containsKey(serialized % 100)) {
            Infuse.LOGGER.warn("Could not find an effect registered to id {}", serialized % 100);
            return null;
        }

        boolean augmented = serialized > 99;
        int id = serialized % 100;
        InfuseEffect effect = REGISTERED_EFFECTS.get(id);

        return augmented ? effect.getAugmentedVersion() : effect.getRegularVersion();
    }
}
