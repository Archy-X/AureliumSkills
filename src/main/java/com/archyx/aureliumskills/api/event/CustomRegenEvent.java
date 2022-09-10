package com.archyx.aureliumskills.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomRegenEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final @NotNull Player player;
    private double amount;
    private boolean isCancelled;
    private final @NotNull RegenReason reason;

    public CustomRegenEvent(@NotNull Player player, double amount, @NotNull RegenReason reason) {
        this.player = player;
        this.amount = amount;
        this.reason = reason;
        this.isCancelled = false;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public @NotNull RegenReason getReason() {
        return reason;
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
