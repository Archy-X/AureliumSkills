package com.archyx.aureliumskills.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CustomMessageKey implements MessageKey {

    private final @NotNull String path;

    public CustomMessageKey(@NotNull String path) {
        this.path = path;
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomMessageKey that = (CustomMessageKey) o;
        return Objects.equals(path, that.path);
    }
}
