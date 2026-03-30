package com.catadmirer.infuseSMP.events;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EffectEquipEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final EffectMapping effect;
    private final String slot;

    public EffectEquipEvent(Player player, EffectMapping effect, String slot) {
        this.player = player;
        this.effect = effect;
        this.slot = slot;
    }

    public Player getPlayer() {
        return player;
    }

    public EffectMapping getEffect() {
        return effect;
    }

    public String getSlot() {
        return slot;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}