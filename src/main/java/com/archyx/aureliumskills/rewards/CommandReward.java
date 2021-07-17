package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.commands.CommandExecutor;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.text.TextUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

public class CommandReward extends MessagedReward {

    private final CommandExecutor executor;
    private final String command;
    private final CommandExecutor revertExecutor;
    private final String revertCommand;

    public CommandReward(AureliumSkills plugin, String menuMessage, String chatMessage, CommandExecutor executor, String command, CommandExecutor revertExecutor, String revertCommand) {
        super(plugin, menuMessage, chatMessage);
        this.executor = executor;
        this.command = command;
        this.revertExecutor = revertExecutor;
        this.revertCommand = revertCommand;
    }

    @Override
    public void giveReward(Player player, Skill skill, int level) {
        executeCommand(executor, command, player, skill, level);
    }

    public void executeRevert(Player player, Skill skill, int level) {
        if (revertCommand != null) {
            executeCommand(revertExecutor != null ? revertExecutor : CommandExecutor.CONSOLE, command, player, skill, level);
        }
    }

    private void executeCommand(CommandExecutor executor, String command, Player player, Skill skill, int level) {
        String executedCommand = TextUtil.replace(command, "{player}", player.getName(),
                "{skill}", skill.toString().toLowerCase(Locale.ROOT),
                "{level}", String.valueOf(level));
        if (plugin.isPlaceholderAPIEnabled()) {
            executedCommand = PlaceholderAPI.setPlaceholders(player, executedCommand);
        }
        executedCommand = TextUtil.replaceNonEscaped(executedCommand, "&", "§");
        // Executes the commands
        if (executor == CommandExecutor.CONSOLE) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), executedCommand);
        } else {
            player.performCommand(executedCommand);
        }
    }

}
