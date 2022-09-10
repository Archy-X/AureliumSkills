package com.archyx.aureliumskills.skills.foraging;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

public enum ForagingSource implements Source {

    OAK_LOG("LOG", new byte[] {0, 4, 8, 12}, true, "OAK_WOOD"),
    SPRUCE_LOG("LOG", new byte[] {1, 5, 9, 13}, true, "SPRUCE_WOOD"),
    BIRCH_LOG("LOG", new byte[] {2, 6, 10, 14}, true, "BIRCH_WOOD"),
    JUNGLE_LOG("LOG", new byte[] {3, 7, 11, 15}, true, "JUNGLE_WOOD"),
    ACACIA_LOG("LOG_2", new byte[] {0, 4, 8, 12}, true, "ACACIA_WOOD"),
    DARK_OAK_LOG("LOG_2", new byte[] {1, 5, 9, 13}, true, "DARK_OAK_WOOD"),
    MANGROVE_LOG(new String[] {"MANGROVE_WOOD"}, true),
    OAK_LEAVES("LEAVES", new byte[] {0, 8}, true),
    SPRUCE_LEAVES("LEAVES", new byte[] {1, 9}, true),
    BIRCH_LEAVES("LEAVES", new byte[] {2, 10}, true),
    JUNGLE_LEAVES("LEAVES", new byte[] {3, 11}, true),
    ACACIA_LEAVES("LEAVES_2", new byte[] {0, 8}, true),
    DARK_OAK_LEAVES("LEAVES_2", new byte[] {1, 9}, true),
    MANGROVE_LEAVES(false, true),
    CRIMSON_STEM(new String[] {"CRIMSON_HYPHAE"}, true),
    WARPED_STEM(new String[] {"WARPED_HYPHAE"}, true),
    NETHER_WART_BLOCK(false, true),
    WARPED_WART_BLOCK(false, true),
    MOSS_BLOCK,
    MOSS_CARPET(true),
    AZALEA(true),
    FLOWERING_AZALEA(true),
    AZALEA_LEAVES(false, true),
    FLOWERING_AZALEA_LEAVES(false, true),
    MANGROVE_ROOTS(false, true);

    private String[] alternateMaterials;
    private String legacyMaterial;
    private byte[] legacyData = {};
    private boolean requiresBlockBelow;
    private boolean isTrunk;
    private boolean isLeaf;

    ForagingSource() {

    }

    ForagingSource(boolean requiresBlockBelow) {
        this.requiresBlockBelow = requiresBlockBelow;
    }

    ForagingSource(boolean requiresBlockBelow, boolean isLeaf) {
        this(requiresBlockBelow);
        this.isLeaf = isLeaf;
    }

    ForagingSource(String legacyMaterial) {
        this.legacyMaterial = legacyMaterial;
    }

    ForagingSource(String[] alternateMaterials) {
        this.alternateMaterials = alternateMaterials;
    }

    ForagingSource(String[] alternateMaterials, boolean isTrunk) {
        this(alternateMaterials);
        this.isTrunk = isTrunk;
    }

    ForagingSource(String legacyMaterial, byte[] legacyData) {
        this(legacyMaterial);
        this.legacyData = legacyData;
    }

    ForagingSource(String legacyMaterial, byte[] legacyData, boolean isLeaf) {
        this(legacyMaterial, legacyData);
        this.isLeaf = isLeaf;
    }

    ForagingSource(String legacyMaterial, byte[] legacyData, String... alternateMaterials) {
        this(legacyMaterial, legacyData);
        this.alternateMaterials = alternateMaterials;
    }

    ForagingSource(String legacyMaterial, byte[] legacyData, boolean isTrunk, String... alternateMaterials) {
        this(legacyMaterial, legacyData, alternateMaterials);
        this.isTrunk = isTrunk;
    }

    public String getLegacyMaterial() {
        return legacyMaterial;
    }

    public byte[] getLegacyData() {
        return legacyData;
    }

    public boolean requiresBlockBelow() {
        return requiresBlockBelow;
    }

    public String[] getAlternateMaterials() {
        return alternateMaterials;
    }

    public boolean isTrunk() {
        return isTrunk;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @SuppressWarnings("deprecation")
    public boolean isMatch(BlockState blockState) {
        boolean matched = false;
        String materialName = blockState.getType().toString();
        String legacyMaterial = getLegacyMaterial();
        if (XMaterial.isNewVersion() || legacyMaterial == null) { // Standard block handling
            if (toString().equalsIgnoreCase(materialName)) {
                matched = true;
            } else if (getAlternateMaterials() != null) {
                for (String alternate : getAlternateMaterials()) {
                    if (alternate != null) {
                        if (alternate.equalsIgnoreCase(materialName)) {
                            matched = true;
                            break;
                        }
                    }
                }
            }
        } else { // Legacy block handling
            if (getLegacyData().length == 0) { // No data value
                if (legacyMaterial.equalsIgnoreCase(materialName)) {
                    matched = true;
                }
            } else { // With data value
                if (legacyMaterial.equalsIgnoreCase(materialName) && byteArrayContains(legacyData, blockState.getRawData())) {
                    matched = true;
                }
            }
        }
        return matched;
    }

    public boolean isMatch(Block block) {
        return isMatch(block.getState());
    }

    private boolean byteArrayContains(byte[] array, byte input) {
        for (byte b : array) {
            if (b == input) return true;
        }
        return false;
    }

    @Override
    public Skill getSkill() {
        return Skills.FORAGING;
    }

    public static ForagingSource getSource(BlockState blockState) {
        for (ForagingSource source : values()) {
            if (source.isMatch(blockState)) {
                return source;
            }
        }
        return null;
    }

    public static ForagingSource getSource(Block block) {
        return getSource(block.getState());
    }

    @Override
    public ItemStack getMenuItem() {
        return ItemUtils.parseItem(this.toString());
    }
}
