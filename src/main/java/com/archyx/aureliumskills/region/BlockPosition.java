package com.archyx.aureliumskills.region;

import com.google.common.base.Objects;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockPosition {

    private final int x;
    private final int y;
    private final int z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public static @NotNull BlockPosition fromBlock(@NotNull Block block) {
        return new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof BlockPosition)) {
            return false;
        } else {
            BlockPosition other = (BlockPosition) obj;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, z);
    }

}
