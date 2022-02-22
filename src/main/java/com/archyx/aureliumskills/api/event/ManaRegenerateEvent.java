package com.archyx.aureliumskills.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Calls when a player regenerates any amount of mana
 */
public class ManaRegenerateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private double amount;
    private boolean isCancelled;

    public ManaRegenerateEvent(Player player, double amount) {
        this.player = player;
        this.amount = amount;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
