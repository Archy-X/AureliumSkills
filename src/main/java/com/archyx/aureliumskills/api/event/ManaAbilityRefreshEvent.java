package com.archyx.aureliumskills.api.event;

import com.archyx.aureliumskills.mana.MAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ManaAbilityRefreshEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final Player player;
    private final MAbility manaAbility;

    public ManaAbilityRefreshEvent(Player player, MAbility manaAbility) {
        this.player = player;
        this.manaAbility = manaAbility;
    }


    public Player getPlayer() {
        return player;
    }

    public MAbility getManaAbility() {
        return manaAbility;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
