package com.archyx.aureliumskills.util.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class BlockFaceUtil {

    public static BlockFace[] getBlockSides() {
        return new BlockFace[] {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
    }

    public static List<Block> getSurroundingBlocks(Block block) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 1; y >= -1; y--) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    blocks.add(block.getRelative(x, y, z));
                }
            }
        }
        return blocks;
    }

}
