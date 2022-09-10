package com.archyx.aureliumskills.modifier;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.archyx.aureliumskills.util.item.NBTAPIUser;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Multipliers extends NBTAPIUser {

    public Multipliers(AureliumSkills plugin) {
        super(plugin);
    }

    public List<Multiplier> getMultipliers(ModifierType type, ItemStack item) {
        if (!OptionL.getBoolean(Option.MODIFIER_MULTIPLIER_ENABLED) || isNBTDisabled()) { // Return empty list if disabled
            return new ArrayList<>();
        }
        NBTItem nbtItem = new NBTItem(item);
        List<Multiplier> multipliers = new ArrayList<>();
        NBTCompound compound = ItemUtils.getMultipliersTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            double value = compound.getDouble(key);
            Skill skill = plugin.getSkillRegistry().getSkill(key); // Null if Global
            String skillName = getNBTName(skill);

            if (type == ModifierType.ITEM) {
                multipliers.add(new Multiplier("AureliumSkills.Multipliers.Item." + skillName, skill, value));
            } else if (type == ModifierType.ARMOR) {
                String slot = "Helmet";
                String mat = item.getType().toString();
                if (mat.contains("CHESTPLATE")) {
                    slot = "Chestplate";
                } else if (mat.contains("LEGGINGS")) {
                    slot = "Leggings";
                } else if (mat.contains("BOOTS")) {
                    slot = "Boots";
                }
                multipliers.add(new Multiplier("AureliumSkills.Multipliers.Armor." + slot + "." + skillName, skill, value));
            }
        }
        return multipliers;
    }

    public ItemStack addMultiplier(ModifierType type, ItemStack item, Skill skill, double value) {
        if (isNBTDisabled()) return item;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getMultipliersTypeCompound(nbtItem, type);
        compound.setDouble(getNBTName(skill), value);
        return nbtItem.getItem();
    }

    public ItemStack removeMultiplier(ModifierType type, ItemStack item, Skill skill) {
        if (isNBTDisabled()) return item;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getMultipliersTypeCompound(nbtItem, type);
        compound.removeKey(getNBTName(skill));
        ItemUtils.removeParentCompounds(compound);
        return nbtItem.getItem();
    }

    public ItemStack removeAllMultipliers(ModifierType type, ItemStack item) {
        if (isNBTDisabled()) return item;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getMultipliersTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            compound.removeKey(key);
        }
        ItemUtils.removeParentCompounds(compound);
        return nbtItem.getItem();
    }

    public void addLore(ModifierType type, ItemStack item, Skill skill, double value, Locale locale) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null)
                lore = new ArrayList<>();
            if (skill != null) { // Skill multiplier
                CommandMessage message;
                if (value >= 0) {
                    message = CommandMessage.valueOf(type.name() + "_MULTIPLIER_ADD_SKILL_LORE");
                } else {
                    message = CommandMessage.valueOf(type.name() + "_MULTIPLIER_ADD_SKILL_LORE_SUBTRACT");
                }
                if (lore.size() > 0) {
                    lore.add(" ");
                }
                lore.add(TextUtil.replace(Lang.getMessage(message, locale),
                        "{skill}", skill.getDisplayName(locale),
                        "{value}", NumberUtil.format1(Math.abs(value))));
            } else { // Global multiplier
                CommandMessage message;
                if (value >= 0) {
                    message = CommandMessage.valueOf(type.name() + "_MULTIPLIER_ADD_GLOBAL_LORE");
                } else {
                    message = CommandMessage.valueOf(type.name() + "_MULTIPLIER_ADD_GLOBAL_LORE_SUBTRACT");
                }
                if (lore.size() > 0) {
                    lore.add(" ");
                }
                lore.add(TextUtil.replace(Lang.getMessage(message, locale),
                        "{value}", NumberUtil.format1(Math.abs(value))));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    private String getNBTName(Skill skill) {
        if (skill != null) {
            return TextUtil.capitalize(skill.toString().toLowerCase(Locale.ROOT));
        } else {
            return "Global";
        }
    }

}
