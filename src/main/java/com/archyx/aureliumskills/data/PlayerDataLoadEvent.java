package com.archyx.aureliumskills.data;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDataLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final PlayerData playerData;

    public PlayerDataLoadEvent(PlayerData playerData) {
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
