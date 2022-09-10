package com.archyx.aureliumskills.api.event;

import com.archyx.aureliumskills.skills.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class XpGainEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final @NotNull Player player;
    private final @NotNull Skill skill;
    private double amount;
    private boolean isCancelled;

    public XpGainEvent(@NotNull Player player, @NotNull Skill skill, double amount) {
        this.player = player;
        this.skill = skill;
        this.amount = amount;
        this.isCancelled = false;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Skill getSkill() {
        return skill;
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
