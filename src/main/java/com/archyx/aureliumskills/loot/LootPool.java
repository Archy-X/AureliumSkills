package com.archyx.aureliumskills.loot;

import java.util.List;

public class LootPool {

    private final String name;
    private final List<Loot> loot;
    private final double baseChance;
    private final double chancePerLuck;
    private final int selectionPriority;
    private final boolean overrideVanillaLoot;

    public LootPool(String name, List<Loot> loot, double baseChance, double chancePerLuck, int selectionPriority, boolean overrideVanillaLoot) {
        this.name = name;
        this.loot = loot;
        this.baseChance = baseChance;
        this.chancePerLuck = chancePerLuck;
        this.selectionPriority = selectionPriority;
        this.overrideVanillaLoot = overrideVanillaLoot;
    }

    public String getName() {
        return name;
    }

    public List<Loot> getLoot() {
        return loot;
    }

    public double getBaseChance() {
        return baseChance;
    }

    public double getChancePerLuck() {
        return chancePerLuck;
    }

    public int getSelectionPriority() {
        return selectionPriority;
    }

    public boolean overridesVanillaLoot() {
        return overrideVanillaLoot;
    }

}
