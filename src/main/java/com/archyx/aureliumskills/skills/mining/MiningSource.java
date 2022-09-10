package com.archyx.aureliumskills.skills.mining;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.BlockSource;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MiningSource implements Source, BlockSource {

    STONE("STONE", 0),
    COBBLESTONE,
    GRANITE("STONE", 1),
    DIORITE("STONE", 2),
    ANDESITE("STONE", 5),
    COAL_ORE,
    IRON_ORE,
    NETHER_QUARTZ_ORE("QUARTZ_ORE"),
    REDSTONE_ORE("GLOWING_REDSTONE_ORE", true),
    GOLD_ORE,
    LAPIS_ORE,
    DIAMOND_ORE,
    EMERALD_ORE,
    TERRACOTTA("HARD_CLAY"),
    WHITE_TERRACOTTA("STAINED_CLAY", 0),
    ORANGE_TERRACOTTA("ORANGE_TERRACOTTA", 1),
    YELLOW_TERRACOTTA("STAINED_CLAY", 4),
    LIGHT_GRAY_TERRACOTTA("STAINED_CLAY", 8),
    BROWN_TERRACOTTA("STAINED_CLAY", 12),
    RED_TERRACOTTA("STAINED_CLAY", 14),
    NETHERRACK,
    BLACKSTONE,
    BASALT,
    MAGMA_BLOCK("MAGMA"),
    NETHER_GOLD_ORE,
    ANCIENT_DEBRIS,
    END_STONE("ENDER_STONE"),
    OBSIDIAN,
    DEEPSLATE,
    COPPER_ORE,
    TUFF,
    CALCITE,
    SMOOTH_BASALT,
    AMETHYST_BLOCK,
    AMETHYST_CLUSTER,
    DEEPSLATE_COAL_ORE,
    DEEPSLATE_IRON_ORE,
    DEEPSLATE_COPPER_ORE,
    DEEPSLATE_GOLD_ORE,
    DEEPSLATE_REDSTONE_ORE,
    DEEPSLATE_EMERALD_ORE,
    DEEPSLATE_LAPIS_ORE,
    DEEPSLATE_DIAMOND_ORE,
    DRIPSTONE_BLOCK,
    ICE(true),
    PACKED_ICE(true),
    BLUE_ICE(true),
    REINFORCED_DEEPSLATE;

    private final String legacyMaterial;
    private final byte legacyData;
    private final boolean allowBothIfLegacy;
    private boolean requiresSilkTouch;

    MiningSource() {
        this(null, -1, false);
    }

    MiningSource(boolean requiresSilkTouch) {
        this(null, -1, false);
        this.requiresSilkTouch = requiresSilkTouch;
    }

    MiningSource(String legacyMaterial) {
        this(legacyMaterial, -1, false);
    }

    MiningSource(String legacyMaterial, int legacyData) {
        this(legacyMaterial, legacyData, false);
    }

    MiningSource(String legacyMaterial, boolean allowBothIfLegacy) {
        this(legacyMaterial, -1, allowBothIfLegacy);
    }

    MiningSource(String legacyMaterial, int legacyData, boolean allowBothIfLegacy) {
        this.legacyMaterial = legacyMaterial;
        this.legacyData = (byte) legacyData;
        this.allowBothIfLegacy = allowBothIfLegacy;
    }

    @Override
    public @Nullable String getLegacyMaterial() {
        return legacyMaterial;
    }

    @Override
    public byte getLegacyData() {
        return legacyData;
    }

    @Override
    public boolean allowBothIfLegacy() {
        return allowBothIfLegacy;
    }

    public boolean requiresSilkTouch() {
        return requiresSilkTouch;
    }

    @Override
    public @NotNull Skill getSkill() {
        return Skills.MINING;
    }

    public static @Nullable MiningSource getSource(@NotNull Block block) {
        for (MiningSource source : values()) {
            if (source.isMatch(block)) {
                return source;
            }
        }
        return null;
    }

    @Override
    public @Nullable ItemStack getMenuItem() {
        return ItemUtils.parseItem(this.toString());
    }
}
