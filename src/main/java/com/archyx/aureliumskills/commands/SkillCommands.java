package com.archyx.aureliumskills.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.MessageType;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.menus.levelprogression.LevelProgressionOpener;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkillCommands {

    public static class SkillCommand extends BaseCommand {
        
        protected final @NotNull AureliumSkills plugin;
        protected final @NotNull Skill skill;
        
        public SkillCommand(@NotNull AureliumSkills plugin, @NotNull Skill skill) {
            this.plugin = plugin;
            this.skill = skill;
        }
        
        protected void openMenu(Player player, @NotNull PlayerData playerData) {
            if (OptionL.isEnabled(skill)) {
                new LevelProgressionOpener(plugin).open(player, playerData, skill);
            } else {
                plugin.getCommandManager().formatMessage(plugin.getCommandManager().getCommandIssuer(player), MessageType.ERROR, MessageKeys.UNKNOWN_COMMAND);
            }
        }
    }

    @CommandAlias("farming")
    public static class FarmingCommand extends SkillCommand {
        
        public FarmingCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.FARMING);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("foraging")
    public static class ForagingCommand extends SkillCommand {

        public ForagingCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.FORAGING);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("mining")
    public static class MiningCommand extends SkillCommand {

        public MiningCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.MINING);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("fishing")
    public static class FishingCommand extends SkillCommand {

        public FishingCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.FISHING);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("excavation")
    public static class ExcavationCommand extends SkillCommand {

        public ExcavationCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.EXCAVATION);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("archery")
    public static class ArcheryCommand extends SkillCommand {

        public ArcheryCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.ARCHERY);
        }
        
        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("defense")
    public static class DefenseCommand extends SkillCommand {

        public DefenseCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.DEFENSE);
        }
        
        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("fighting")
    public static class FightingCommand extends SkillCommand {

        public FightingCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.FIGHTING);
        }
        
        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("endurance")
    public static class EnduranceCommand extends SkillCommand {

        public EnduranceCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.ENDURANCE);
        }
        
        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("agility")
    public static class AgilityCommand extends SkillCommand {

        public AgilityCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.AGILITY);
        }
        
        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("alchemy")
    public static class AlchemyCommand extends SkillCommand {

        public AlchemyCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.ALCHEMY);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("enchanting")
    public static class EnchantingCommand extends SkillCommand {

        public EnchantingCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.ENCHANTING);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("sorcery")
    public static class SorceryCommand extends SkillCommand {

        public SorceryCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.SORCERY);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("healing")
    public static class HealingCommand extends SkillCommand {

        public HealingCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.HEALING);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
    @CommandAlias("forging")
    public static class ForgingCommand extends SkillCommand {

        public ForgingCommand(@NotNull AureliumSkills plugin) {
            super(plugin, Skills.FORGING);
        }

        @Default
        public void onCommand(@NotNull Player player) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                openMenu(player, playerData);
            }
        }
    }
}
