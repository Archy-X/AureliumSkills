package com.archyx.aureliumskills.region;

import com.google.common.base.Objects;
import org.jetbrains.annotations.Nullable;

public class ChunkCoordinate {

    private final byte x;
    private final byte z;

    public ChunkCoordinate(byte x, byte z) {
        this.x = x;
        this.z = z;
    }

    public byte getX() {
        return x;
    }

    public byte getZ() {
        return z;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof ChunkCoordinate)) {
            return false;
        } else {
            ChunkCoordinate other = (ChunkCoordinate) obj;
            return this.x == other.x && this.z == other.z;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, z);
    }

}
