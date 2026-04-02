package com.catadmirer.infuseSMP.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerHitEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player attacker;
    private final Player target;
    private final int hitCount;

    public PlayerHitEvent(Player attacker, Player target, int hitCount) {
        this.attacker = attacker;
        this.target = target;
        this.hitCount = hitCount;
    }

    public Player getAttacker() {
        return attacker;
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