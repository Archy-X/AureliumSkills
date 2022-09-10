package com.archyx.aureliumskills.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ManaRegenerateEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

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

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
