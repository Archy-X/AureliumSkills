package com.archyx.aureliumskills.skills.alchemy;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BrewingStandData {

    private final @NotNull Map<Integer, Boolean> potionSlots;
    private final ItemStack ingredient;

    public BrewingStandData(ItemStack ingredient) {
        this.potionSlots = new HashMap<>();
        this.ingredient = ingredient;
    }

    public boolean isSlotBrewed(int slot) {
        @Nullable Boolean isBrewed = potionSlots.get(slot);
        if (isBrewed == null)
            throw new IndexOutOfBoundsException();
        return isBrewed;
    }

    public void setSlotBrewed(int slot, boolean isSlotBrewed) {
        potionSlots.put(slot, isSlotBrewed);
    }

    public ItemStack getIngredient() {
        return ingredient;
    }

}
