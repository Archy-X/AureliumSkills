package com.archyx.aureliumskills.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an ability or luck causes extra items to drop from an action. Does not include the original or normal items dropped except for Fishing Lucky Catch.
 */
public class PlayerLootDropEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final Player player;
    private @NotNull ItemStack itemStack;
    private @NotNull Location location;
    private final LootDropCause cause;
    private boolean isCancelled;

    public PlayerLootDropEvent(Player player, @NotNull ItemStack itemStack, @NotNull Location location, LootDropCause cause) {
        this.player = player;
        this.itemStack = itemStack;
        this.location = location;
        this.cause = cause;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    public LootDropCause getCause() {
        return cause;
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
