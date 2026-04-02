package com.catadmirer.infuseSMP.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerHitEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player lastAttacker;
    private final Player target;
    private final int hitCount;

    public PlayerHitEvent(Player lastAttacker, Player target, int hitCount) {
        this.lastAttacker = lastAttacker;
        this.target = target;
        this.hitCount = hitCount;
    }

    public Player getLastAttacker() {
        return lastAttacker;
    }

    public Player getTarget() {
        return target;
    }

    public int getHitCount() {
        return hitCount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}