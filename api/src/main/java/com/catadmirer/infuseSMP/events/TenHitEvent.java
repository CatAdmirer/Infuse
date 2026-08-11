package com.catadmirer.infuseSMP.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TenHitEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player attacker;
    private final Player target;

    public TenHitEvent(Player attacker, Player target) {
        this.attacker = attacker;
        this.target = target;
    }

    public Player getAttacker() {
        return attacker;
    }

    public Player getTarget() {
        return target;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
