package com.archyx.aureliumskills.modifier;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.requirement.Requirements;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.StatLeveler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ModifierManager {

    private final @NotNull AureliumSkills plugin;
    private final @NotNull StatLeveler statLeveler;

    public ModifierManager(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.statLeveler = new StatLeveler(plugin);
    }

    public void reloadPlayer(@NotNull Player player) {
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        Requirements requirements = new Requirements(plugin);
        Modifiers modifiers = new Modifiers(plugin);
        Multipliers multipliers = new Multipliers(plugin);
        if (playerData != null) {
            Set<@NotNull Stat> statsToReload = new HashSet<>();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!(item.getType() == Material.AIR)) {
                for (StatModifier modifier : modifiers.getModifiers(ModifierType.ITEM, item)) {
                    playerData.removeStatModifier(modifier.getName());
                    statsToReload.add(modifier.getStat());
                }
                for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ITEM, item)) {
                    playerData.removeMultiplier(multiplier.getName());
                }
                if (requirements.meetsRequirements(ModifierType.ITEM, item, player)) {
                    for (StatModifier modifier : modifiers.getModifiers(ModifierType.ITEM, item)) {
                        playerData.addStatModifier(modifier, false);
                        statsToReload.add(modifier.getStat());
                    }
                    for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ITEM, item)) {
                        playerData.addMultiplier(multiplier);
                    }
                }
            }
            ItemStack itemOffHand = player.getInventory().getItemInOffHand();
            if (!(itemOffHand.getType() == Material.AIR)) {
                for (StatModifier modifier : modifiers.getModifiers(ModifierType.ITEM, itemOffHand)) {
                    playerData.removeStatModifier(modifier.getName() + ".Offhand");
                    statsToReload.add(modifier.getStat());
                }
                for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ITEM, itemOffHand)) {
                    playerData.removeMultiplier(multiplier.getName() + ".Offhand");
                }
                if (requirements.meetsRequirements(ModifierType.ITEM, itemOffHand, player)) {
                    for (StatModifier modifier : modifiers.getModifiers(ModifierType.ITEM, itemOffHand)) {
                        StatModifier offHandModifier = new StatModifier(modifier.getName() + ".Offhand", modifier.getStat(), modifier.getValue());
                        playerData.addStatModifier(offHandModifier, false);
                        statsToReload.add(modifier.getStat());
                    }
                    for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ITEM, itemOffHand)) {
                        Multiplier offHandMultiplier = new Multiplier(multiplier.getName() + ".Offhand", multiplier.getSkill(), multiplier.getValue());
                        playerData.addMultiplier(offHandMultiplier);
                    }
                }
            }
            EntityEquipment equipment = player.getEquipment();
            if (equipment != null) {
                for (ItemStack armor : equipment.getArmorContents()) {
                    if (armor != null) {
                        if (!(armor.getType() == Material.AIR)) {
                            for (StatModifier modifier : modifiers.getModifiers(ModifierType.ARMOR, armor)) {
                                playerData.removeStatModifier(modifier.getName());
                                statsToReload.add(modifier.getStat());
                            }
                            for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ARMOR, armor)) {
                                playerData.removeMultiplier(multiplier.getName());
                            }
                            if (requirements.meetsRequirements(ModifierType.ARMOR, armor, player)) {
                                for (StatModifier modifier : modifiers.getModifiers(ModifierType.ARMOR, armor)) {
                                    playerData.addStatModifier(modifier, false);
                                    statsToReload.add(modifier.getStat());
                                }
                                for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ARMOR, armor)) {
                                    playerData.addMultiplier(multiplier);
                                }
                            }
                        }
                    }
                }
            }
            for (Stat stat : statsToReload) {
                statLeveler.reloadStat(player, stat);
            }
        }
    }
}
