package com.catadmirer.infuseSMP.playerdata;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.Set;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DataManager {
    /**
     * Reloads the player data.
     *
     * @return True if the data was loaded successfully, false otherwise.
     */
    public boolean load();

    /**
     * Gets the amount of times an effect has been crafted.
     * 
     * @param effect The effect
     */
    public int getCrafted(EffectMapping effect);

    /**
     * Sets the amount of times an effect has been crafted.
     * 
     * @param effect The effect to adjust the crafted count for
     * @param crafted The number of this effect that has been crafted
     */
    public void setCrafted(EffectMapping effect, int crafted);

    /**
     * Gets a set of the players that the truster trusts.
     * 
     * @param truster
     * 
     * @return The set of players trusted by the truster.
     */
    @NotNull
    public Set<OfflinePlayer> getTrusted(@NotNull OfflinePlayer truster);

    /**
     * Sets the players that the truster trusts.
     * 
     * @param truster The player to modify
     * @param trusted The set of players the truster now trusts
     */
    public void setTrusted(@NotNull OfflinePlayer truster, @NotNull Set<OfflinePlayer> trusted);

    /**
     * Adds a player to the list of trusted people.
     * 
     * @param truster The person whose trusted list to modify.
     * @param toTrust The person the truster now trusts.
     */
    public default void addTrust(@NotNull OfflinePlayer truster, @NotNull OfflinePlayer toTrust) {
        Set<OfflinePlayer> trusted = getTrusted(truster);
        trusted.add(toTrust);
        setTrusted(truster, trusted);
    }

    /**
     * Removes a player from another player's list of trusted people.
     * 
     * @param truster The player whose trusted list to modify.
     * @param toRemove The person to remove from the truster's trust.
     */
    public default void removeTrust(@NotNull OfflinePlayer truster, @NotNull OfflinePlayer toRemove) {
        Set<OfflinePlayer> trusted = getTrusted(truster);
        trusted.remove(toRemove);
        setTrusted(truster, trusted);
    }

    /**
     * Checks if a player is trusted by another player.
     * 
     * @param truster The player whose trusted list to check.
     * @param toCheck The player to check if truster trusts.
     * 
     * @return True if the truster trusts the toCheck player, false otherwise
     */
    public boolean isTrusted(@NotNull OfflinePlayer truster, @NotNull OfflinePlayer toCheck);

    /**
     * Sets the infuse effect in a specific slot for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param slot The slot to equip the effect in.
     * @param effect The {@link EffectMapping} for the infuse effect.
     */
    public void setEffect(@NotNull UUID playerUUID, @NotNull String slot, @NotNull EffectMapping effect);

    /**
     * Gets the infuse effect a player has in a specific slot.
     * 
     * @param playerUUID The UUID of the player.
     * @param slot The slot to get the effect from.
     * 
     * @return null if there is not an effect equipped there or if the EffectMapping could not be deserialized.  Otherwise, it returns the deserialized EffectMapping.
     */
    @Nullable
    public EffectMapping getEffect(@NotNull UUID playerUUID, @NotNull String slot);

    public default boolean hasEffect(OfflinePlayer player, EffectMapping effect) {
        return hasEffect(player, effect, false);
    }

    public default boolean hasEffect(OfflinePlayer player, EffectMapping effect, boolean differentiateAugmented) {
        return hasEffect(player, effect, differentiateAugmented, "1") || hasEffect(player, effect, differentiateAugmented, "2");        
    }

    public default boolean hasEffect(OfflinePlayer player, EffectMapping effect, String slot) {
        return hasEffect(player, effect, false, slot);
    }

    public boolean hasEffect(OfflinePlayer player, EffectMapping effect, boolean differentiateAugmented, String slot);

    /**
     * Removes an infuse effect from a specific slot for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param slot The slot to remove an effect from.
     */
    public void removeEffect(@NotNull UUID playerUUID, @NotNull String slot);

    /**
     * Sets the control mode for a player.
     * 
     * @param playerUUID The UUID of the player.
     * @param defaultMode The new control mode to use.
     */
    public void setControlMode(@NotNull UUID playerUUID, @NotNull String defaultMode);

    /**
     * Gets the control mode of a player.
     * 
     * @param playerUUID The UUID of the player.
     * 
     * @return Either "command" or "offhand".  Defaults to "offhand"
     */
    @NotNull
    public String getControlMode(@NotNull UUID playerUUID);
}