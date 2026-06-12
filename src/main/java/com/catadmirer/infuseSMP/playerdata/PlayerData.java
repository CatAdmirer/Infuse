package com.catadmirer.infuseSMP.playerdata;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PlayerData {
    private InfuseEffect lSlot;
    private InfuseEffect rSlot;
    private Set<OfflinePlayer> trusted;
    private boolean offhandControls;

    /**
     * Gets the set of the players that the player trusts.
     *
     * @return The set of players trusted by the player.
     */
    @Unmodifiable
    public Set<OfflinePlayer> getTrusted() {
        return Set.copyOf(trusted);
    }

    /**
     * Sets the players that the player trusts.
     *
     * @param trusted The set of players the truster should now trust
     */
    public void setTrusted(Set<OfflinePlayer> trusted) {
        this.trusted = new HashSet<>(trusted);
    }

    /**
     * Adds a player to the list of trusted people.
     *
     * @param trustedPlayer The person the truster now trusts.
     */
    public void addTrust(OfflinePlayer trustedPlayer) {
        this.trusted.add(trustedPlayer);
    }

    /**
     * Removes a player from this player's list of trusted people.
     *
     * @param untrusted The person to remove from the truster's trust.
     */
    public void removeTrust(OfflinePlayer untrusted) {
        trusted.remove(untrusted);
    }

    /**
     * Checks if a player is trusted by this player.
     *
     * @param trustedPlayer The player to check if the player trusts.
     *
     * @return True if the player trusts the other player, false otherwise
     */
    public boolean isTrusted(OfflinePlayer trustedPlayer) {
        return this.trusted.contains(trustedPlayer);
    }

    /**
     * Sets the infuse effect in a specific slot for the player.
     *
     * @param slot The slot to equip the effect in.
     * @param effect The {@link InfuseEffect} for the infuse effect.
     */
    public void setEffect(String slot, @Nullable InfuseEffect effect) {
        if (slot.equals("1")) {
            lSlot = effect;
        } else if (slot.equals("2")) {
            rSlot = effect;
        } else {
            Infuse.LOGGER.warn("Invalid slot id \"{}\"", slot);
        }
    }

    /**
     * Gets the infuse effect the player has in a specific slot.
     *
     * @return null if there is not an effect equipped there or if the InfuseEffect could not be deserialized.  Otherwise, it returns the deserialized InfuseEffect.
     */
    public @Nullable InfuseEffect getEffect(String slot) {
        if (slot.equals("1")) {
            return lSlot;
        } else if (slot.equals("2")) {
            return rSlot;
        } else {
            Infuse.LOGGER.warn("Invalid slot id \"{}\"", slot);
            return null;
        }
    }

    /**
     * Checks if the player has the infuse effect.
     * It checks both slots and doesn't differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    public boolean hasEffect(InfuseEffect effect) {
        return hasEffect(effect, false);
    }

    /**
     * Checks if the player has the infuse effect.
     * It checks both slots and doesn't differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    public boolean hasEffect(InfuseEffect effect, boolean differentiateAugmented) {
        return hasEffect(effect, differentiateAugmented, "1") || hasEffect(effect, differentiateAugmented, "2");
    }

    /**
     * Checks if the player has the infuse effect.
     * It checks both slots and doesn't differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    public boolean hasEffect(InfuseEffect effect, String slot) {
        return hasEffect(effect, false, slot);
    }

    /**
     * Checks if the player has the infuse effect equipped in a specific slot.
     *
     * @param differentiateAugmented Whether the search should differentiate between regular and augmented effects.
     *
     * @return True if the player has the effect equipped, false otherwise.
     */
    public boolean hasEffect(InfuseEffect effect, boolean differentiateAugmented, String slot) {
        InfuseEffect equipped = getEffect(slot);
        if (equipped == null) return false;

        if (differentiateAugmented) {
            return effect.equals(equipped);
        }

        return effect.getId() == equipped.getId();
    }

    /** Removes an infuse effect from a specific slot for the player. */
    public void removeEffect(String slot) {
        setEffect(slot, null);
    }

    /** Sets the control mode for the player. */
    void setControlMode(String controlMode) {
        if (controlMode.equals("offhand")) {
            offhandControls = true;
        } else if (controlMode.equals("command")) {
            offhandControls = false;
        } else {
            Infuse.LOGGER.warn("Invalid control mode \"{}\"", controlMode);
        }
    }

    /**
     * Gets the control mode of the player.
     *
     * @return Either "command" or "offhand".  Defaults to "offhand"
     */
    public String getControlMode() {
        return offhandControls ? "offhand" : "command";
    }
}
