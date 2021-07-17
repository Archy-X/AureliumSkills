package com.archyx.aureliumskills.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player regenerates health with custom regen mechanics turned on in
 * the config
 */
public class CustomRegenEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private double amount;
    private boolean isCancelled;
    private final RegenReason reason;

    public CustomRegenEvent(Player player, double amount, RegenReason reason) {
        this.player = player;
        this.amount = amount;
        this.reason = reason;
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

    public RegenReason getReason() {
        return reason;
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
