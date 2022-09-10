package com.archyx.aureliumskills.source;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SourceRegistry {

    private final @NotNull Map<@NotNull Skill, Class<?>> registry;
    private final @NotNull Map<@NotNull Skill, @NotNull Source @NotNull []> sources;

    public SourceRegistry() {
        registry = new HashMap<>();
        sources = new HashMap<>();
        try {
            for (Skill skill : Skills.values()) {
                String className = TextUtil.capitalize(skill.toString().toLowerCase(Locale.ROOT)) + "Source";
                Class<?> sourceClass = Class.forName("com.archyx.aureliumskills.skills." + skill.toString().toLowerCase(Locale.ROOT) + "." + className);
                register(skill, sourceClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Class<?> getSourceClass(Skill skill) {
        return registry.get(skill);
    }

    /**
     * The registered class must be an enum or a class that has a values method that returns an array of Source objects.
     * Any class must implement
     */
    public void register(@NotNull Skill skill, @NotNull Class<?> sourceClass) {
        registry.put(skill, sourceClass);
        try {
            Method method = sourceClass.getMethod("values");
            Object object = method.invoke(null);
            if (object instanceof Source[]) {
                sources.put(skill, (@NotNull Source[]) object);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public @NotNull Source @NotNull [] values(@NotNull Skill skill) {
        return sources.get(skill);
    }

    public @NotNull Set<Source> values() {
        Set<Source> sourceSet = new HashSet<>();
        for (Source[] skillSources : sources.values()) {
            sourceSet.addAll(Arrays.asList(skillSources));
        }
        return sourceSet;
    }

    public @Nullable Source valueOf(@NotNull String sourceString) {
        for (Source source : values()) {
            if (source.toString().equals(sourceString.toUpperCase(Locale.ROOT))) {
                return source;
            }
        }
        return null;
    }

}
