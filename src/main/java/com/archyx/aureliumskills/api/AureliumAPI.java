package com.archyx.aureliumskills.api;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class AureliumAPI {

    private static @Nullable AureliumSkills plugin;

    /**
     * Internal usage only.
     * Sets the {@link AureliumSkills} instance that will be used by all methods.
     * @param plugin AureliumSkills instance
     */
    public static void setPlugin(@NotNull AureliumSkills plugin) {
        if (AureliumAPI.plugin == null) {
            AureliumAPI.plugin = plugin;
        } else {
            throw new IllegalStateException("The AureliumSkills API is already registered");
        }
    }

    /**
     * Provides the {@link AureliumSkills} plugin instance.
     * Anything in the AureliumSkills instance is not an official API and could break
     * between versions without warning. Use at your own risk.
     * @return AureliumSkills instance.
     */
    public static @NotNull AureliumSkills getPlugin() {
        AureliumSkills plugin = AureliumAPI.plugin;
        if (plugin == null) {
            throw new IllegalStateException("The AureliumSkills API is not loaded yet");
        }
        return plugin;
    }

    /**
     * Gets the current mana of a player
     * @return the current mana of a player
     */
    public static double getMana(@NotNull Player player) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getMana();
        }
        return 0.0;
    }

    @Deprecated
    public static double getMana(@NotNull UUID playerId) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getMana();
        }
        return 0.0;
    }

    /**
     * Gets the max mana of a player
     * @return the max mana of a player
     */
    public static double getMaxMana(@NotNull Player player) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
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
    public static double getManaRegen(@NotNull Player player) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getManaRegen();
        } else {
            return OptionL.getDouble(Option.REGENERATION_BASE_MANA_REGEN);
        }
    }

    @Deprecated
    public static double getMaxMana(@NotNull UUID playerId) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getMaxMana();
        } else {
            return OptionL.getDouble(Option.BASE_MANA);
        }
    }

    /**
     * Sets a player's mana to an amount
     */
    public static void setMana(@NotNull Player player, double amount) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.setMana(amount);
        }
    }

    @Deprecated
    public static void setMana(@NotNull UUID playerId, double amount) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            playerData.setMana(amount);
        }
    }

    /**
     * Adds Skill XP to a player for a certain skill, and includes multiplier permissions
     */
    public static void addXp(@NotNull Player player, @NotNull Skill skill, double amount) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        plugin.getLeveler().addXp(player, skill, amount);
    }

    /**
     * Adds Skill XP to a player for a certain skill, without multipliers
     */
    public static void addXpRaw(@NotNull Player player, @NotNull Skill skill, double amount) {
        AureliumSkills plugin = AureliumAPI.plugin;
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
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
    public static boolean addXpOffline(@NotNull OfflinePlayer player, @NotNull Skill skill, double amount) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.addSkillXp(skill, amount);
            return true;
        }
        else {
            return false;
        }
    }

    @Deprecated
    public static boolean addXpOffline(@NotNull UUID playerId, @NotNull Skill skill, double amount) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
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
    public static int getSkillLevel(@NotNull Player player, @NotNull Skill skill) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getSkillLevel(skill);
        }
        else {
            return 1;
        }
    }

    @Deprecated
    public static int getSkillLevel(@NotNull UUID playerId, @NotNull Skill skill) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getSkillLevel(skill);
        }
        else {
            return 1;
        }
    }

    /**
     * Gets the skill xp of a player
     * @param player The player to get from
     * @param skill The skill to get
     * @return The current skill xp
     */
    public static double getXp(@NotNull Player player, @NotNull Skill skill) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getSkillXp(skill);
        }
        else {
            return 1;
        }
    }

    @Deprecated
    public static double getXp(@NotNull UUID playerId, @NotNull Skill skill) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
        if (playerData != null) {
            return playerData.getSkillXp(skill);
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
    public static double getStatLevel(@NotNull Player player, @NotNull Stat stat) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getStatLevel(stat);
        }
        else {
            return 0;
        }
    }

    @Deprecated
    public static double getStatLevel(@NotNull UUID playerId, @NotNull Stat stat) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
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
    public static double getBaseStatLevel(@NotNull Player player, @NotNull Stat stat) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            return playerData.getStatLevel(stat);
        }
        else {
            return 0;
        }
    }

    @Deprecated
    public static double getBaseStatLevel(@NotNull UUID playerId, @NotNull Stat stat) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
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
    public static boolean addStatModifier(@NotNull Player player, @NotNull String name, @NotNull Stat stat, double value) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.addStatModifier(new StatModifier(name, stat, value));
            return true;
        }
        return false;
    }

    @Deprecated
    public static boolean addStatModifier(@NotNull UUID playerId, @NotNull String name, @NotNull Stat stat, double value) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
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
    public static boolean removeStatModifier(@NotNull Player player, @NotNull String name) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            playerData.removeStatModifier(name);
            return true;
        }
        return false;
    }

    @Deprecated
    public static boolean removeStatModifier(@NotNull UUID playerId, @NotNull String name) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(playerId);
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
    public static @NotNull ItemStack addItemModifier(@NotNull ItemStack item, @NotNull Stat stat, double value, boolean lore) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
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
    public static @NotNull ItemStack addArmorModifier(@NotNull ItemStack item, @NotNull Stat stat, double value, boolean lore) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
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
    public static @NotNull ItemStack addItemMultiplier(@NotNull ItemStack item, @NotNull Skill skill, double value, boolean lore) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
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
    public static @NotNull ItemStack addArmorMultiplier(@NotNull ItemStack item, @NotNull Skill skill, double value, boolean lore) {
        Objects.requireNonNull(plugin, "The AureliumSkills API is not loaded yet");
        Multipliers multipliers = new Multipliers(plugin);
        ItemStack modifiedItem = multipliers.addMultiplier(ModifierType.ITEM, item, skill, value);
        if (lore) {
            multipliers.addLore(ModifierType.ARMOR, modifiedItem, skill, value, Lang.getDefaultLanguage());
        }
        return modifiedItem;
    }


}
