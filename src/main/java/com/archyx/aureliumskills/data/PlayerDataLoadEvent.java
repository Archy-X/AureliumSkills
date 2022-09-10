package com.archyx.aureliumskills.data;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerDataLoadEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final @NotNull PlayerData playerData;

    public PlayerDataLoadEvent(@NotNull PlayerData playerData) {
        this.playerData = playerData;
    }

    public @NotNull PlayerData getPlayerData() {
        return playerData;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
