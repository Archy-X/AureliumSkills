package com.archyx.aureliumskills.menus.abilities;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityManager;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractManaAbilityItem extends AbstractItem implements TemplateItemProvider<@NotNull MAbility> {

    protected final @NotNull ManaAbilityManager manager;

    public AbstractManaAbilityItem(@NotNull AureliumSkills plugin) {
        super(plugin);
        this.manager = plugin.getManaAbilityManager();
    }

    @Override
    public @NotNull Class<@NotNull MAbility> getContext() {
        return MAbility.class;
    }

    @Override
    public @Nullable ItemStack onItemModify(@NotNull ItemStack baseItem, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull MAbility mAbility) {
        // Hide disabled mana abilities
        if (!plugin.getAbilityManager().isEnabled(mAbility)) {
            return null;
        }
        return baseItem;
    }
}
