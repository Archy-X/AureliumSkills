package com.archyx.aureliumskills.support;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.leaderboard.SkillValue;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.math.BigNumber;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class PlaceholderSupport extends PlaceholderExpansion {

    private final AureliumSkills plugin;
    private final String[] xpIdentifiers = {"xp_required_formatted_", "xp_required_", "xp_progress_int_", "xp_progress_1_", "xp_progress_", "xp_int_", "xp_formatted_", "xp_"};

    public PlaceholderSupport(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getIdentifier() {
        return "aureliumskills";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        //Gets total combined skill level
        if (identifier.equals("power")) {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                return String.valueOf(playerData.getPowerLevel());
            }
        }

        //Gets HP with scaling as an integer
        if (identifier.equals("hp")) {
            return String.valueOf(Math.round(player.getHealth() * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING)));
        }

        //Gets HP with scaling with 1 decimal
        if (identifier.equals("hp_1")) {
            return NumberUtil.format1(player.getHealth() * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING));
        }

        //Gets max hp
        if (identifier.equals("hp_max")) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) {
                return String.valueOf(Math.round(attribute.getValue() * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING)));
            }
            else {
                return "";
            }
        }

        //Gets HP with scaling with 2 decimal
        if (identifier.equals("hp_2")) {
            return NumberUtil.format2(player.getHealth() * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING));
        }

        //Gets HP Percent as an integer
        if (identifier.equals("hp_percent")) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) {
                return String.valueOf(Math.round(player.getHealth() / attribute.getValue()));
            }
            else {
                return "";
            }
        }

        //Gets mana
        if (identifier.equals("mana")) {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                return String.valueOf(playerData.getMana());
            }
        }

        // Gets mana rounded to an integer
        if (identifier.equals("mana_int")) {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                return String.valueOf(Math.round(playerData.getMana()));
            }
        }

        // Gets max mana
        if (identifier.equals("mana_max")) {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                return String.valueOf(playerData.getMaxMana());
            }
        }

        // Gets max mana rounded to an integer
        if (identifier.equals("mana_max_int")) {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                return String.valueOf(Math.round(playerData.getMaxMana()));
            }
        }

        //Gets stat values
        for (Stat stat : plugin.getStatRegistry().getStats()) {
            String name = stat.name();
            if (identifier.equals(name.toLowerCase(Locale.ENGLISH))) {
                PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData != null) {
                    return String.valueOf(playerData.getStatLevel(stat));
                }
            } else if (identifier.equals(name.toLowerCase(Locale.ROOT) + "_int")) {
                PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData != null) {
                    return String.valueOf(Math.round(playerData.getStatLevel(stat)));
                }
            }
        }

        //Gets skill levels
        for (Skill skill : plugin.getSkillRegistry().getSkills()) {
            if (identifier.equals(skill.name().toLowerCase(Locale.ENGLISH))) {
                PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData != null) {
                    return String.valueOf(playerData.getSkillLevel(skill));
                }
            }
            else if (identifier.equals(skill.name().toLowerCase(Locale.ENGLISH) + "_roman")) {
                PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData != null) {
                    return RomanNumber.toRoman(playerData.getSkillLevel(skill));
                }
            }
        }

        if (identifier.startsWith("lb_")) {
            String leaderboardType = TextUtil.replace(identifier, "lb_", "");
            if (leaderboardType.startsWith("power_")) {
                int place = NumberUtil.toInt(TextUtil.replace(leaderboardType, "power_", ""));
                if (place > 0) {
                    List<SkillValue> list = plugin.getLeaderboardManager().getPowerLeaderboard(place, 1);
                    if (list.size() > 0) {
                        SkillValue skillValue = list.get(0);
                        String name = Bukkit.getOfflinePlayer(skillValue.getId()).getName();
                        return (name != null ? name : "?") + " - " + skillValue.getLevel();
                    }
                    else return "";
                }
                else {
                    if (identifier.endsWith("name")) {
                        int namePlace = NumberUtil.toInt(TextUtil.replace(leaderboardType, "power_", "", "_name", ""));
                        if (namePlace > 0) {
                            List<SkillValue> list = plugin.getLeaderboardManager().getPowerLeaderboard(namePlace, 1);
                            if (list.size() > 0) {
                                SkillValue skillValue = list.get(0);
                                String name = Bukkit.getOfflinePlayer(skillValue.getId()).getName();
                                return name != null ? name : "?";
                            }
                            else return "";
                        }
                    }
                    else if (identifier.endsWith("value")) {
                        int valuePlace = NumberUtil.toInt(TextUtil.replace(leaderboardType, "power_", "", "_value", ""));
                        if (valuePlace > 0) {
                            List<SkillValue> list = plugin.getLeaderboardManager().getPowerLeaderboard(valuePlace, 1);
                            if (list.size() > 0) {
                                SkillValue playerSkill = list.get(0);
                                return String.valueOf(playerSkill.getLevel());
                            }
                            else return "";
                        }
                    }
                }
            }
            else {
                for (Skill skill : plugin.getSkillRegistry().getSkills()) {
                    if (leaderboardType.startsWith(skill.name().toLowerCase(Locale.ENGLISH) + "_")) {
                        int place = NumberUtil.toInt(TextUtil.replace(leaderboardType, skill.name().toLowerCase(Locale.ENGLISH) + "_", ""));
                        if (place > 0) {
                            List<SkillValue> list = plugin.getLeaderboardManager().getLeaderboard(skill, 1, 1);
                            if (list.size() > 0) {
                                SkillValue skillValue = list.get(0);
                                String name = Bukkit.getOfflinePlayer(skillValue.getId()).getName();
                                return (name != null ? name : "?") + " - " + skillValue.getLevel();
                            }
                            else return "";
                        }
                        else {
                            if (identifier.endsWith("name")) {
                                int namePlace = NumberUtil.toInt(TextUtil.replace(leaderboardType, skill.name().toLowerCase(Locale.ENGLISH) + "_", "", "_name", ""));
                                if (namePlace > 0) {
                                    List<SkillValue> list = plugin.getLeaderboardManager().getLeaderboard(skill, namePlace, 1);
                                    if (list.size() > 0) {
                                        SkillValue skillValue = list.get(0);
                                        String name = Bukkit.getOfflinePlayer(skillValue.getId()).getName();
                                        return name != null ? name : "?";
                                    }
                                    else return "";
                                }
                            }
                            else if (identifier.endsWith("value")) {
                                int valuePlace = NumberUtil.toInt(TextUtil.replace(leaderboardType, skill.name().toLowerCase(Locale.ENGLISH) + "_", "", "_value", ""));
                                if (valuePlace > 0) {
                                    List<SkillValue> list = plugin.getLeaderboardManager().getLeaderboard(skill, valuePlace, 1);
                                    if (list.size() > 0) {
                                        SkillValue skillValue = list.get(0);
                                        return String.valueOf(skillValue.getLevel());
                                    }
                                    else return "";
                                }
                            }
                        }
                    }
                }
            }
        }

        if (identifier.equals("rank")) {
            return String.valueOf(plugin.getLeaderboardManager().getPowerRank(player.getUniqueId()));
        }

        if (identifier.startsWith("rank_")) {
            String skillName = TextUtil.replace(identifier, "rank_", "");
            Skill skill = plugin.getSkillRegistry().getSkill(skillName);
            if (skill != null) {
                return String.valueOf(plugin.getLeaderboardManager().getSkillRank(skill, player.getUniqueId()));
            }
        }

        for (String id : xpIdentifiers) {
            if (identifier.startsWith(id)) {
                String skillName = TextUtil.replace(identifier, id, "");
                Skill skill = plugin.getSkillRegistry().getSkill(skillName);
                if (skill != null) {
                    PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData != null) {
                        switch (id) {
                            case "xp_required_formatted_":
                                return BigNumber.withSuffix(plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1));
                            case "xp_required_":
                                return String.valueOf(plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1));
                            case "xp_progress_int_":
                                return String.valueOf(Math.round(playerData.getSkillXp(skill) / (double) (plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1)) * 100));
                            case "xp_progress_1_":
                                return NumberUtil.format1(playerData.getSkillXp(skill) / (double) (plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1)) * 100);
                            case "xp_progress_":
                                return String.valueOf(playerData.getSkillXp(skill) / (double) (plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1)) * 100);
                            case "xp_int_":
                                return String.valueOf(Math.round(playerData.getSkillXp(skill)));
                            case "xp_formatted_":
                                return BigNumber.withSuffix(Math.round(playerData.getSkillXp(skill)));
                            case "xp_":
                                return String.valueOf(playerData.getSkillXp(skill));
                        }
                    }
                }
            }
        }

        if (identifier.startsWith("multiplier")) {
            if (identifier.equals("multiplier")) {
                return NumberUtil.format2(plugin.getLeveler().getMultiplier(player));
            }
            String skillName = TextUtil.replace(identifier, "multiplier_", "");
            Skill skill = plugin.getSkillRegistry().getSkill(skillName);
            if (skill != null) {
                return NumberUtil.format2(plugin.getLeveler().getMultiplier(player, skill));
            }
        }

        if (identifier.startsWith("multiplier_percent")) {
            if (identifier.equals("multiplier_percent")) {
                return String.valueOf(Math.round((plugin.getLeveler().getMultiplier(player) - 1) * 100));
            }
            String skillName = TextUtil.replace(identifier, "multiplier_percent_", "");
            Skill skill = plugin.getSkillRegistry().getSkill(skillName);
            if (skill != null) {
                return String.valueOf(Math.round((plugin.getLeveler().getMultiplier(player, skill) - 1) * 100));
            }
        }

        return null;
    }

}
