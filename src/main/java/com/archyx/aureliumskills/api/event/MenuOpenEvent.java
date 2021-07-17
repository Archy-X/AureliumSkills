package com.archyx.aureliumskills.api.event;

import com.archyx.aureliumskills.menu.MenuType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event when a player opens an Aurelium Skills menu, either through commands or switching from another.
 * Calls before the menu is actually opened. Will not call when changing pages in the same menu type.
 */
public class MenuOpenEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final MenuType type;
    private boolean cancelled;

    public MenuOpenEvent(Player player, MenuType type) {
        this.player = player;
        this.type = type;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public MenuType getType() {
        return type;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
