package com.archyx.aureliumskills.requirement;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.modifier.ModifierType;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.archyx.aureliumskills.util.item.NBTAPIUser;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Requirements extends NBTAPIUser {

    private final RequirementManager manager;

    public Requirements(@NotNull AureliumSkills plugin) {
        super(plugin);
        this.manager = plugin.getRequirementManager();
    }

    public @NotNull Map<Skill, Integer> getRequirements(@NotNull ModifierType type, @NotNull ItemStack item) {
        if (isNBTDisabled()) return new HashMap<>();
        NBTItem nbtItem = new NBTItem(item);
        Map<Skill, Integer> requirements = new HashMap<>();
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            try {
                Skill skill = plugin.getSkillRegistry().getSkill(key);
                if (skill != null) {
                    Integer value = compound.getInteger(key);
                    requirements.put(skill, value);
                }
            }
            catch (Exception ignored) { }
        }
        return requirements;
    }

    @SuppressWarnings("deprecation")
    public @NotNull Map<Skill, Integer> getGlobalRequirements(@NotNull ModifierType type, @NotNull ItemStack item) {
        Map<Skill, Integer> requirements = new HashMap<>();
        for (GlobalRequirement global : manager.getGlobalRequirementsType(type)) {
            if (XMaterial.isNewVersion()) {
                if (global.getMaterial().parseMaterial() == item.getType()) {
                    requirements.putAll(global.getRequirements());
                }
            } else {
                if (global.getMaterial().parseMaterial() == item.getType()) {
                    if (!ItemUtils.isDurable(item.getType())) {
                        MaterialData materialData = item.getData();
                        if (materialData != null) {
                            if (item.getDurability() == global.getMaterial().getData())
                                requirements.putAll(global.getRequirements());
                        }
                    }
                } else {
                    requirements.putAll(global.getRequirements());
                }
            }
        }
        return requirements;
    }

    public @NotNull ItemStack addRequirement(@NotNull ModifierType type, @NotNull ItemStack item, @NotNull Skill skill, int level) {
        if (isNBTDisabled()) return item;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        compound.setInteger(getName(skill), level);
        return nbtItem.getItem();
    }

    public @NotNull ItemStack removeRequirement(@NotNull ModifierType type, @NotNull ItemStack item, @NotNull Skill skill) {
        if (isNBTDisabled()) return item;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            if (key.equals(getName(skill))) {
                compound.removeKey(key);
            }
        }
        ItemUtils.removeParentCompounds(compound);
        return nbtItem.getItem();
    }

    public @NotNull ItemStack removeAllRequirements(@NotNull ModifierType type, @NotNull ItemStack item) {
        if (isNBTDisabled()) return item;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        compound.getKeys().forEach(compound::removeKey);
        ItemUtils.removeParentCompounds(compound);
        return nbtItem.getItem();
    }

    public boolean hasRequirement(@NotNull ModifierType type, @NotNull ItemStack item, @NotNull Skill skill) {
        if (isNBTDisabled()) return false;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
        for (String key : compound.getKeys()) {
            if (key.equals(getName(skill))) {
                return true;
            }
        }
        return false;
    }

    public @NotNull ItemStack convertFromLegacy(@NotNull ItemStack item) {
        if (isNBTDisabled()) return item;
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound oldRequirementsCompound = nbtItem.getCompound("skillRequirements");
        if (oldRequirementsCompound != null) {
            for (ModifierType type : ModifierType.values()) {
                NBTCompound oldTypeCompound = oldRequirementsCompound.getCompound(type.toString().toLowerCase(Locale.ENGLISH));
                if (oldTypeCompound != null) {
                    NBTCompound compound = ItemUtils.getRequirementsTypeCompound(nbtItem, type);
                    for (String key : oldTypeCompound.getKeys()) {
                        compound.setInteger(TextUtil.capitalize(key), oldTypeCompound.getInteger(key));
                    }
                }
            }
        }
        nbtItem.removeKey("skillRequirements");
        return nbtItem.getItem();
    }

    public void addLore(@NotNull ModifierType type, @NotNull ItemStack item, @NotNull Skill skill, int level, @Nullable Locale locale) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String text = TextUtil.replace(Lang.getMessage(CommandMessage.valueOf(type.name() + "_REQUIREMENT_ADD_LORE"), locale), "{skill}", skill.getDisplayName(locale), "{level}", String.valueOf(level));
            @Nullable List<@NotNull String> lore = meta.getLore();
            if (lore != null) {
                lore.add(text);
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
    }

    public void removeLore(@NotNull ItemStack item, @NotNull Skill skill) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            @Nullable List<@NotNull String> lore = meta.getLore();
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.contains("Requires") && line.contains(TextUtil.capitalize(skill.name().toLowerCase(Locale.ENGLISH)))) {
                        lore.remove(line);
                    }
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
    }

    public boolean meetsRequirements(@NotNull ModifierType type, @NotNull ItemStack item, @NotNull Player player) {
        if (!OptionL.getBoolean(Option.REQUIREMENT_ENABLED)) return true;
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return true;
        // Check global requirements
        for (Map.Entry<Skill, Integer> entry : getGlobalRequirements(type, item).entrySet()) {
            if (playerData.getSkillLevel(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        for (Map.Entry<Skill, Integer> entry : getRequirements(type, item).entrySet()) {
            if (playerData.getSkillLevel(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private @NotNull String getName(@NotNull Skill skill) {
        return TextUtil.capitalize(skill.name().toLowerCase(Locale.ENGLISH));
    }

}
