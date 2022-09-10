package com.archyx.aureliumskills.source;

import com.archyx.aureliumskills.lang.CustomMessageKey;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.lootmanager.loot.context.LootContext;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public interface Source extends LootContext {

    Skill getSkill();

    String name();

    default String getPath() {
        return getSkill().toString().toLowerCase(Locale.ROOT) + "." + toString().toLowerCase(Locale.ROOT);
    }

    default String getDisplayName(Locale locale) {
        Skill skill = getSkill();
        String messagePath = "sources." + skill.toString().toLowerCase(Locale.ROOT) + "." + toString().toLowerCase(Locale.ROOT);
        if (skill == Skills.ARCHERY || skill == Skills.FIGHTING) {
            messagePath = "sources.mobs." + toString().toLowerCase(Locale.ROOT);
        }
        CustomMessageKey key = new CustomMessageKey(messagePath);
        String message = messagePath;
        try {
            message = Lang.getMessage(key, locale);
        }
        catch (IllegalStateException ex) {
            // No custom message exists when using the message as a key
            Bukkit.getLogger().warning("[AureliumSkills] Unknown custom message with path: " + key);
        }
        return message;
    }

    /**
     * Only gets the message identifier, not the localized actual name.
     * @return The message identifier, null if the source has no unit
     */
    default String getUnitName() {
        return null;
    }

    /**
     * Gets the item to be displayed in the sources menu
     * @return The item or null if the source does not exist in this version
     */
    ItemStack getMenuItem();

    @Override
    default String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
