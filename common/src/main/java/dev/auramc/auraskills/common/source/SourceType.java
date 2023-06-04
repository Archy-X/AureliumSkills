package dev.auramc.auraskills.common.source;

import dev.auramc.auraskills.common.source.serializer.*;
import dev.auramc.auraskills.common.source.type.*;

import java.util.Locale;

public enum SourceType {

    ANVIL(AnvilSource.class, AnvilSourceSerializer.class),
    BLOCK(BlockSource.class, BlockSourceSerializer.class),
    BREWING(BrewingSource.class, BrewingSourceSerializer.class),
    DAMAGE(DamageSource.class, DamageSourceSerializer.class),
    ENCHANTING(EnchantingSource.class, EnchantingSourceSerializer.class),
    ENTITY(EntitySource.class, EntitySourceSerializer.class),
    GRINDSTONE(GrindstoneSource.class, GrindstoneSourceSerializer.class),
    ITEM_CONSUME(ItemConsumeSource.class, ItemConsumeSourceSerializer.class),
    JUMPING(JumpingSource.class, JumpingSourceSerializer.class),
    MANA_ABILITY_USE(ManaAbilityUseSource.class, ManaAbilityUseSourceSerializer.class),
    POTION_SPLASH(PotionSplashSource.class, PotionSplashSourceSerializer.class),
    STATISTIC(StatisticSource.class, StatisticSourceSerializer.class);

    private final Class<? extends Source> sourceClass;
    private final Class<? extends SourceSerializer<?>> serializerClass;

    SourceType(Class<? extends Source> sourceClass, Class<? extends SourceSerializer<?>> serializerClass) {
        this.sourceClass = sourceClass;
        this.serializerClass = serializerClass;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Class<? extends Source> getSourceClass() {
        return sourceClass;
    }

    public Class<? extends SourceSerializer<?>> getSerializerClass() {
        return serializerClass;
    }

}