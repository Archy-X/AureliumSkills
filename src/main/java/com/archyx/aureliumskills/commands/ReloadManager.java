package com.archyx.aureliumskills.commands;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.stats.Luck;
import com.archyx.aureliumskills.support.WorldGuardSupport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ReloadManager {

    private final @NotNull AureliumSkills plugin;
    private final @NotNull Lang lang;

    public ReloadManager(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    public void reload(@NotNull CommandSender sender) {
        @Nullable Locale locale = plugin.getLang().getLocale(sender);
        // Load config
        plugin.reloadConfig();
        plugin.saveDefaultConfig();
        plugin.getOptionLoader().loadOptions();
        plugin.getRequirementManager().load();
        // Load sources_config
        plugin.getSourceManager().loadSources();
        plugin.getCheckBlockReplace().reloadCustomBlocks();
        // Load rewards
        plugin.getRewardManager().loadRewards();
        // Load language files
        lang.loadLanguageFiles();
        lang.loadEmbeddedMessages(plugin.getCommandManager());
        lang.loadLanguages(plugin.getCommandManager());
        // Load menus
        plugin.getMenuFileManager().generateDefaultFiles();
        plugin.getMenuFileManager().loadMenus();
        // Load ability_config
        plugin.getAbilityManager().loadOptions();

        plugin.getLeveler().loadLevelRequirements();
        // Load loot tables
        plugin.getLootTableManager().loadLootTables();
        // Load worlds and regions
        plugin.getWorldManager().loadWorlds();
        WorldGuardSupport worldGuardSupport = plugin.getWorldGuardSupport();
        if (plugin.isWorldGuardEnabled() && worldGuardSupport != null) {
            worldGuardSupport.loadRegions();
        }
        // Recalculate health and luck stats
        Luck luck = new Luck(plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getHealth().reload(player);
            luck.reload(player);
        }
        // Resets all action bars
        plugin.getActionBar().resetActionBars();
        // Load boss bars
        plugin.getBossBar().loadOptions();
        sender.sendMessage(AureliumSkills.getPrefix(locale) + ChatColor.GREEN + Lang.getMessage(CommandMessage.RELOAD, locale));
    }

}
