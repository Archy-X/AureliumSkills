package com.archyx.aureliumskills.rewards.builder;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.commands.CommandExecutor;
import com.archyx.aureliumskills.rewards.CommandReward;
import com.archyx.aureliumskills.rewards.Reward;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CommandRewardBuilder extends MessagedRewardBuilder {

    private @NotNull CommandExecutor executor;
    private @Nullable String command;
    private CommandExecutor revertExecutor;
    private @Nullable String revertCommand;

    public CommandRewardBuilder(@NotNull AureliumSkills plugin) {
        super(plugin);
        this.executor = CommandExecutor.CONSOLE;
    }

    public @NotNull CommandRewardBuilder executor(@NotNull CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public @NotNull CommandRewardBuilder command(@NotNull String command) {
        this.command = command;
        return this;
    }

    public @NotNull CommandRewardBuilder revertCommand(@NotNull String revertCommand) {
        this.revertCommand = revertCommand;
        return this;
    }

    public @NotNull CommandRewardBuilder revertExecutor(@NotNull CommandExecutor revertExecutor) {
        this.revertExecutor = revertExecutor;
        return this;
    }

    @Override
    public @NotNull Reward build() {
        Objects.requireNonNull(command, "You must specify a command");
        return new CommandReward(plugin, menuMessage, chatMessage, executor, command, revertExecutor, revertCommand);
    }
}
