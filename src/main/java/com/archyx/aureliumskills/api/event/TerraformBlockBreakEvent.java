package com.archyx.aureliumskills.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a block is broken due to terraform. Wrapper to the BlockBreakEvent to distinguish
 * it from a regular block break. Plugins listening to the BlockBreakEvent will still get this event,
 * so it is advised to check the event object's class is actually BlockBreakEvent instead of a subclass
 * to prevent infinite event loops and crashes.
 */
public class TerraformBlockBreakEvent extends BlockBreakEvent {

    public TerraformBlockBreakEvent(@NotNull Block theBlock, @NotNull Player player) {
        super(theBlock, player);
    }

}
