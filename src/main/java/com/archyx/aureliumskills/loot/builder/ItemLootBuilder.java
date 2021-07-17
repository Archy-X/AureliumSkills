package com.archyx.aureliumskills.loot.builder;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.loot.type.ItemLoot;
import com.archyx.aureliumskills.util.misc.Validate;
import org.bukkit.inventory.ItemStack;

public class ItemLootBuilder extends LootBuilder {

    protected ItemStack item;
    protected int minAmount;
    protected int maxAmount;

    @Override
    public Loot build() {
        Validate.notNull(item, "You must specify an item");
        return new ItemLoot(plugin, weight, message, xp, sources, item, minAmount, maxAmount);
    }

    public ItemLootBuilder(AureliumSkills plugin) {
        super(plugin);
        this.minAmount = 1;
        this.maxAmount = 1;
    }

    public ItemLootBuilder item(ItemStack item) {
        this.item = item;
        return this;
    }

    public ItemLootBuilder minAmount(int minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    public ItemLootBuilder maxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
        return this;
    }

}
