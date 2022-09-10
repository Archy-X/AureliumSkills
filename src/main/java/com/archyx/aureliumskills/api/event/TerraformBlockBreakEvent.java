package com.archyx.aureliumskills.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class TerraformBlockBreakEvent extends BlockBreakEvent {

    public TerraformBlockBreakEvent(Block theBlock, Player player) {
        super(theBlock, player);
    }

}
