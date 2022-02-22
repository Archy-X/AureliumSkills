package com.archyx.aureliumskills.api.event;

import com.archyx.aureliumskills.menu.MenuType;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MenuInitializeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final MenuType type;
    private final InventoryContents contents;

    public MenuInitializeEvent(Player player, MenuType type, InventoryContents contents) {
        this.player = player;
        this.type = type;
        this.contents = contents;
    }

    public Player getPlayer() {
        return player;
    }

    public MenuType getType() {
        return type;
    }

    public InventoryContents getContents() {
        return contents;
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
