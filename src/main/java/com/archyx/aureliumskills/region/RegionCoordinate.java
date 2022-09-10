package com.archyx.aureliumskills.region;

import com.google.common.base.Objects;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegionCoordinate {

    private final World world;
    private final int x;
    private final int z;

    public RegionCoordinate(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof RegionCoordinate)) {
            return false;
        } else {
            RegionCoordinate other = (RegionCoordinate) obj;
            return this.x == other.x && this.z == other.z && this.world == other.world;
        }
    }

    @Override
    public @NotNull String toString() {
        return world.getName() + ", " + x + ", " + z;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(world.getName(), x, z);
    }

}
