package com.archyx.aureliumskills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.archyx.aureliumskills.AureliumSkills;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("stats")
public class StatsCommand extends BaseCommand {

	private final @NotNull AureliumSkills plugin;

	public StatsCommand(@NotNull AureliumSkills plugin) {
		this.plugin = plugin;
	}

	@Default
	@CommandPermission("aureliumskills.stats")
	@Description("Opens the Stats menu where you can see current stat levels and descriptions.")
	public void onStats(@NotNull Player player) {
		plugin.getMenuManager().openMenu(player, "stats");
	}
	
}
