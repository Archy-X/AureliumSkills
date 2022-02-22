package com.archyx.aureliumskills.api.event;

import com.archyx.aureliumskills.mana.MAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a mana ability is activated by a player
 */
public class ManaAbilityActivateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final MAbility manaAbility;
    private int duration;
    private boolean isCancelled;

    public ManaAbilityActivateEvent(Player player, MAbility manaAbility, int duration) {
        this.player = player;
        this.manaAbility = manaAbility;
        this.duration = duration;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public MAbility getManaAbility() {
        return manaAbility;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
