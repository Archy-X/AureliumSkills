package com.archyx.aureliumskills.api;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.modifier.ModifierType;
import com.archyx.aureliumskills.modifier.Modifiers;
import com.archyx.aureliumskills.modifier.Multipliers;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Stat;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

public class AureliumAPI {

    private static AureliumSkills plugin;

    /**
     * Internal usage only.
     * Sets the {@link AureliumSkills} instance that will be used by all methods.
     * @param plugin AureliumSkills instance
     */
    public static void setPlugin(AureliumSkills plugin) {
        if (AureliumAPI.plugin == null) {
            AureliumAPI.plugin = plugin;
        } else {
            throw new IllegalStateException("The AureliumSkills API is already registered");
        }
    }

    /**
     * Sets player level as if calling the @Command setSkillLevel
     * @param player player to have level set
     * @param skill null for all skills
     * @param level must be greater than 0
     * @param messagePlayer sends the player a message detailing level change
     */
    public static void setSkillLevel(Player player, @Nullable Skill skill, int level, boolean messagePlayer){
        Locale locale = plugin.getLang().getLocale(player);
        Collection<Skill> skills;
        String message = AureliumSkills.getPrefix(locale) + Lang.getMessage(CommandMessage.SKILL_SETLEVEL_SET, locale)
                .replace("{level}", String.valueOf(level))
                .replace("{player}", player.getName());
        if(skill == null) {
            skills = plugin.getSkillRegistry().getSkills();
            message = message.replace("{skill}", "*");
        }else {
            skills = Collections.singletonList(skill);
            message = message.replace("{skill}", skill.getDisplayName(locale));
        }
        if (level > 0) {
            skills.forEach(s -> {
                PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData == null) return;
                int oldLevel = playerData.getSkillLevel(s);
                playerData.setSkillLevel(s, level);
                playerData.setSkillXp(s, 0);
                plugin.getLeveler().updateStats(player);
                plugin.getLeveler().updatePermissions(player);
                plugin.getLeveler().applyRevertCommands(player, s, oldLevel, level);
                plugin.getLeveler().applyLevelUpCommands(player, s, oldLevel, level);
                plugin.getModifierManager().reloadPlayer(player);
            });
            if(messagePlayer) {
                player.sendMessage(message);
            }
        } else {
            if(messagePlayer) {
                player.sendMessage(AureliumSkills.getPrefix(locale) + Lang.getMessage(CommandMessage.SKILL_SETLEVEL_AT_LEAST_ONE, locale));
            }
        }
    }

    /**
     * Provides the {@link AureliumSkills} plugin instance.
     * Anything in the AureliumSkills instance is not an official API and could break
     * between versions without warning. Use at your own risk.
     * @return AureliumSkills instance.
     */
    public static AureliumSkills getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("The AureliumSkills API is not loaded yet");
        }
        return plugin;
    }

    /**
     * Gets the current mana of a player
     * @return the current mana of a player
     */
    public static double getMana(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getMana();
        }
        return 0.0;
    }

    @Deprecated
    public static double getMana(UUID playerId) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getMana();
        }
        return 0.0;
    }

    /**
     * Gets the max mana of a player
     * @return the max mana of a player
     */
    public static double getMaxMana(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getMaxMana();
        } else {
            return OptionL.getDouble(Option.BASE_MANA);
        }
    }

    /**
     * Gets the amount of mana a player regenerates every second
     * @return the mana regeneration per second of a player
     */
    public static double getManaRegen(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getManaRegen();
        } else {
            return OptionL.getDouble(Option.REGENERATION_BASE_MANA_REGEN);
        }
    }

    @Deprecated
    public static double getMaxMana(UUID playerId) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getMaxMana();
        } else {
            return OptionL.getDouble(Option.BASE_MANA);
        }
    }

    /**
     * Sets a player's mana to an amount
     */
    public static void setMana(Player player, double amount) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.setMana(amount);
        }
    }

    @Deprecated
    public static void setMana(UUID playerId, double amount) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            playerData.setMana(amount);
        }
    }

    /**
     * Adds Skill XP to a player for a certain skill, and includes multiplier permissions
     */
    public static void addXp(Player player, Skill skill, double amount) {
        plugin.getLeveler().addXp(player, skill, amount);
    }

    /**
     * Adds Skill XP to a player for a certain skill, without multipliers
     */
    public static void addXpRaw(Player player, Skill skill, double amount) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.addSkillXp(skill, amount);
            plugin.getLeveler().checkLevelUp(player, skill);
        }
    }

    /**
     * Adds Skill XP to an offline player for a certain skill
     * No longer works in beta, offline players are not stored in memory anymore.
     */
    @Deprecated
    public static boolean addXpOffline(OfflinePlayer player, Skill skill, double amount) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.addSkillXp(skill, amount);
            return true;
        }
        else {
            return false;
        }
    }

    @Deprecated
    public static boolean addXpOffline(UUID playerId, Skill skill, double amount) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            playerData.addSkillXp(skill, amount);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Gets the skill level of a player
     * @return the skill level of a player, or 1 if player does not have a skills profile
     */
    public static int getSkillLevel(Player player, Skill skill) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getSkillLevel(skill);
        }
        else {
            return 1;
        }
    }

    @Deprecated
    public static int getSkillLevel(UUID playerId, Skill skill) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getSkillLevel(skill);
        }
        else {
            return 1;
        }
    }

    /**
     * Gets the total skill level of a player
     * @return the total skill level of a player, will display number of skills available if player does not have a skills profile
     */
    public static int getTotalLevel(Player player) {
        int totalLevel = 0;
        for (Skill skill : plugin.getSkillRegistry().getSkills()) {
            totalLevel += getSkillLevel(player, skill);
        }
        return totalLevel;
    }

    /**
     * Gets the skill xp of a player
     * @param player The player to get from
     * @param skill The skill to get
     * @return The current skill xp
     */
    public static double getXp(Player player, Skill skill) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getSkillXp(skill);
        }
        else {
            return 1;
        }
    }

    @Deprecated
    public static double getXp(UUID playerId, Skill skill) {
        PlayerData playerSkill = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerSkill != null) {
            return playerSkill.getSkillXp(skill);
        }
        else {
            return 1;
        }
    }

    /**
     * Gets the stat level of a player
     * @param player The player to get from
     * @param stat The stat to get
     * @return The stat level
     */
    public static double getStatLevel(Player player, Stat stat) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getStatLevel(stat);
        }
        else {
            return 0;
        }
    }

    @Deprecated
    public static double getStatLevel(UUID playerId, Stat stat) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getStatLevel(stat);
        }
        else {
            return 0;
        }
    }

    /**
     * Gets the base stat level of a player, without modifiers
     * @param player The player to get from
     * @param stat The stat to get
     * @return The stat level without modifiers
     */
    @Deprecated
    public static double getBaseStatLevel(Player player, Stat stat) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getStatLevel(stat);
        }
        else {
            return 0;
        }
    }

    @Deprecated
    public static double getBaseStatLevel(UUID playerId, Stat stat) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getStatLevel(stat);
        }
        else {
            return 0;
        }
    }

    /**
     * Adds a stat modifier to a player
     * @param player The player to add to
     * @param name The name of the stat modifier
     * @param stat The stat to add to
     * @param value The value of the modifier
     * @return true if a modifier was added, false if the player does not have a skills profile
     */
    public static boolean addStatModifier(Player player, String name, Stat stat, double value) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.addStatModifier(new StatModifier(name, stat, value));
            return true;
        }
        return false;
    }

    @Deprecated
    public static boolean addStatModifier(UUID playerId, String name, Stat stat, double value) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            playerData.addStatModifier(new StatModifier(name, stat, value));
            return true;
        }
        return false;
    }

    /**
     * Removes a stat modifier from a player
     * @param player The player to remove from
     * @param name The name of the stat modifier
     * @return true if the operation was successful, false if the stat modifier was not found or the player does not have a skills profile
     */
    public static boolean removeStatModifier(Player player, String name) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.removeStatModifier(name);
            return true;
        }
        return false;
    }

    @Deprecated
    public static boolean removeStatModifier(UUID playerId, String name) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            playerData.removeStatModifier(name);
            return true;
        }
        return false;
    }

    /**
     * Adds an item modifier to an item, with optional lore. This does NOT change the item passed in directly,
     * you must use the returned ItemStack. This means the original ItemStack passed in is not changed at all, a new
     * one is created.
     * @param item The original item, will not be changed by the method
     * @param stat The stat to add (Use Stats enum for default stats)
     * @param value The value of the stat to add
     * @param lore Whether to add lore. Added lore will use the default language.
     * @return A new ItemStack with the item modifier
     */
    public static ItemStack addItemModifier(ItemStack item, Stat stat, double value, boolean lore) {
        Modifiers modifiers = new Modifiers(plugin);
        ItemStack modifiedItem = modifiers.addModifier(ModifierType.ITEM, item, stat, value);
        if (lore) {
            modifiers.addLore(ModifierType.ITEM, modifiedItem, stat, value, Lang.getDefaultLanguage());
        }
        return modifiedItem;
    }

    /**
     * Adds an armor modifier to an item, with optional lore. This does NOT change the item passed in directly,
     * you must use the returned ItemStack. This means the original ItemStack passed in is not changed at all, a new
     * one is created.
     * @param item The original item, will not be changed by the method
     * @param stat The stat to add (Use Stats enum for default stats)
     * @param value The value of the stat to add
     * @param lore Whether to add lore. Added lore will use the default language.
     * @return A new ItemStack with the armor modifier
     */
    public static ItemStack addArmorModifier(ItemStack item, Stat stat, double value, boolean lore) {
        Modifiers modifiers = new Modifiers(plugin);
        ItemStack modifiedItem = modifiers.addModifier(ModifierType.ARMOR, item, stat, value);
        if (lore) {
            modifiers.addLore(ModifierType.ARMOR, modifiedItem, stat, value, Lang.getDefaultLanguage());
        }
        return modifiedItem;
    }

    /**
     * Adds an item multiplier to an item, with optional lore. This does NOT change the item passed in directly,
     * you must use the returned ItemStack. This means the original ItemStack passed in is not changed at all, a new
     * one is created.
     * @param item The original item, will not be changed by the method
     * @param skill The skill to add (Use Skills enum for default skills)
     * @param value The value of the multiplier (in percentage points) to add
     * @param lore Whether to add lore. Added lore will use the default language.
     * @return A new ItemStack with the item modifier
     */
    public static ItemStack addItemMultiplier(ItemStack item, Skill skill, double value, boolean lore) {
        Multipliers multipliers = new Multipliers(plugin);
        ItemStack modifiedItem = multipliers.addMultiplier(ModifierType.ITEM, item, skill, value);
        if (lore) {
            multipliers.addLore(ModifierType.ITEM, modifiedItem, skill, value, Lang.getDefaultLanguage());
        }
        return modifiedItem;
    }

    /**
     * Adds an armor multiplier to an item, with optional lore. This does NOT change the item passed in directly,
     * you must use the returned ItemStack. This means the original ItemStack passed in is not changed at all, a new
     * one is created.
     * @param item The original item, will not be changed by the method
     * @param skill The skill to add (Use Skills enum for default skills)
     * @param value The value of the multiplier (in percentage points) to add
     * @param lore Whether to add lore. Added lore will use the default language.
     * @return A new ItemStack with the armor modifier
     */
    public static ItemStack addArmorMultiplier(ItemStack item, Skill skill, double value, boolean lore) {
        Multipliers multipliers = new Multipliers(plugin);
        ItemStack modifiedItem = multipliers.addMultiplier(ModifierType.ITEM, item, skill, value);
        if (lore) {
            multipliers.addLore(ModifierType.ARMOR, modifiedItem, skill, value, Lang.getDefaultLanguage());
        }
        return modifiedItem;
    }


}
