package dev.aurelium.auraskills.common.source.serializer;

import dev.aurelium.auraskills.api.item.ItemFilter;
import dev.aurelium.auraskills.api.source.type.BrewingXpSource;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.source.type.BrewingSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;

public class BrewingSourceSerializer extends SourceSerializer<BrewingSource> {

    public BrewingSourceSerializer(AuraSkillsPlugin plugin, String sourceName) {
        super(plugin, sourceName);
    }

    @Override
    public BrewingSource deserialize(Type type, ConfigurationNode source) throws SerializationException {
        ItemFilter ingredients = required(source, "ingredient").get(ItemFilter.class);
        BrewingXpSource.BrewTriggers trigger = required(source, "trigger").get(BrewingXpSource.BrewTriggers.class);

        return new BrewingSource(plugin, getId(), getXp(source), ingredients, trigger);
    }
}