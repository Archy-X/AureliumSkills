package com.archyx.aureliumskills.leveler;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.source.SourceManager;
import com.archyx.aureliumskills.source.SourceTag;
import com.archyx.aureliumskills.support.WorldGuardFlags;
import com.archyx.aureliumskills.support.WorldGuardSupport;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public abstract class SkillLeveler {

    public final @NotNull AureliumSkills plugin;
    private final SourceManager sourceManager;
    private @Nullable Ability ability;
    private final @NotNull String skillName;

    public SkillLeveler(@NotNull AureliumSkills plugin, @NotNull Skill skill) {
        this.plugin = plugin;
        this.skillName = skill.toString().toLowerCase(Locale.ENGLISH);
        this.sourceManager = plugin.getSourceManager();
    }

    public SkillLeveler(@NotNull AureliumSkills plugin, @NotNull Ability ability) {
        this.plugin = plugin;
        this.ability = ability;
        this.skillName = ability.getSkill().toString().toLowerCase(Locale.ENGLISH);
        this.sourceManager = plugin.getSourceManager();
    }

    public double getXp(@NotNull Source source) {
        return sourceManager.getXp(source);
    }

    public double getXp(@NotNull Player player, @NotNull Source source) {
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            double output = getXp(source);
            @Nullable Ability ability = this.ability;
            if (ability != null) {
                if (plugin.getAbilityManager().isEnabled(ability)) {
                    double modifier = 1;
                    modifier += plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability)) / 100;
                    output *= modifier;
                }
            }
            return output;
        }
        return 0.0;
    }

    public double getXp(@NotNull Player player, double input) {
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            double output = input;
            @Nullable Ability ability = this.ability;
            if (ability != null) {
                if (plugin.getAbilityManager().isEnabled(ability)) {
                    double modifier = 1;
                    modifier += plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability)) / 100;
                    output *= modifier;
                }
            }
            return output;
        }
        return 0.0;
    }

    public double getXp(@NotNull Player player, double input, @NotNull Ability ability) {
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            double output = input;
            if (plugin.getAbilityManager().isEnabled(ability)) {
                double modifier = 1;
                modifier += plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability)) / 100;
                output *= modifier;
            }
            return output;
        }
        return 0.0;
    }

    protected boolean hasTag(@NotNull Source source, @NotNull SourceTag tag) {
        for (Source sourceWithTag : sourceManager.getTag(tag)) {
            if (source == sourceWithTag) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public void checkCustomBlocks(@NotNull Player player, @NotNull Block block, @NotNull Skill skill) {
        // Check custom blocks
        Map<XMaterial, Double> customBlocks = sourceManager.getCustomBlocks(skill);
        if (customBlocks != null) {
            for (Map.Entry<XMaterial, Double> entry : customBlocks.entrySet()) {
                if (XMaterial.isNewVersion()) {
                    if (entry.getKey().parseMaterial() == block.getType()) {
                        if (OptionL.getBoolean(Option.CHECK_BLOCK_REPLACE) && plugin.getRegionManager().isPlacedBlock(block)) {
                            return;
                        }
                        plugin.getLeveler().addXp(player, skill, getXp(player, entry.getValue()));
                        break;
                    }
                }
                else {
                    if (entry.getKey().parseMaterial() == block.getType() && block.getData() == entry.getKey().getData()) {
                        if (OptionL.getBoolean(Option.CHECK_BLOCK_REPLACE) && plugin.getRegionManager().isPlacedBlock(block)) {
                            return;
                        }
                        plugin.getLeveler().addXp(player, skill, getXp(player, entry.getValue()));
                        break;
                    }
                }
            }
        }
    }

    public boolean blockXpGain(@NotNull Player player) {
        //Checks if in blocked world
        Location location = player.getLocation();
        if (plugin.getWorldManager().isInBlockedWorld(location)) {
            return true;
        }
        //Check for permission
        if (!player.hasPermission("aureliumskills." + skillName)) {
            return true;
        }
        //Check creative mode disable
        if (OptionL.getBoolean(Option.DISABLE_IN_CREATIVE_MODE)) {
            return player.getGameMode().equals(GameMode.CREATIVE);
        }
        //Checks if in blocked region
        WorldGuardSupport worldGuardSupport = plugin.getWorldGuardSupport();
        if (plugin.isWorldGuardEnabled() && worldGuardSupport != null) {
            if (worldGuardSupport.isInBlockedRegion(location)) {
                return true;
            }
            // Check if blocked by flags
            else
                return worldGuardSupport.blockedByFlag(location, player, WorldGuardFlags.FlagKey.XP_GAIN);
        }
        return false;
    }

    public boolean blockXpGainLocation(@NotNull Location location, @NotNull Player player) {
        //Checks if in blocked world
        if (plugin.getWorldManager().isInBlockedWorld(location)) {
            return true;
        }
        //Checks if in blocked region
        WorldGuardSupport worldGuardSupport = plugin.getWorldGuardSupport();
        if (plugin.isWorldGuardEnabled() && worldGuardSupport != null) {
            if (worldGuardSupport.isInBlockedRegion(location)) {
                return true;
            }
            // Check if blocked by flags
            else
                return worldGuardSupport.blockedByFlag(location, player, WorldGuardFlags.FlagKey.XP_GAIN);
        }
        return false;
    }

    public boolean blockXpGainPlayer(@NotNull Player player) {
        //Check for permission
        if (!player.hasPermission("aureliumskills." + skillName)) {
            return true;
        }
        //Check creative mode disable
        if (OptionL.getBoolean(Option.DISABLE_IN_CREATIVE_MODE)) {
            return player.getGameMode().equals(GameMode.CREATIVE);
        }
        return false;
    }

    public boolean blockAbility(@NotNull Player player) {
        if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
            return true;
        }
        if (!player.hasPermission("aureliumskills." + skillName)) {
            return true;
        }
        if (OptionL.getBoolean(Option.DISABLE_IN_CREATIVE_MODE)) {
            return player.getGameMode().equals(GameMode.CREATIVE);
        }
        return false;
    }

    public boolean blockDisabled(@NotNull Ability ability) {
        if (!OptionL.isEnabled(ability.getSkill())) {
            return true;
        }
        return !plugin.getAbilityManager().isEnabled(ability);
    }

    public double getValue(@NotNull Ability ability, @NotNull PlayerData playerData) {
        return plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability));
    }

    protected boolean hasSilkTouch(@NotNull Player player) {
        return player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
    }

}
