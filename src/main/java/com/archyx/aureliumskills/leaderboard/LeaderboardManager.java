package com.archyx.aureliumskills.leaderboard;

import com.archyx.aureliumskills.skills.Skill;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LeaderboardManager {

    private final @NotNull Map<@NotNull Skill, List<SkillValue>> skillLeaderboards;
    private @NotNull List<SkillValue> powerLeaderboard;
    private @NotNull List<SkillValue> averageLeaderboard;
    private volatile boolean sorting = false;

    public LeaderboardManager() {
        this.skillLeaderboards = new HashMap<>();
        this.powerLeaderboard = new ArrayList<>();
        this.averageLeaderboard = new ArrayList<>();
    }

    public @NotNull List<SkillValue> getLeaderboard(@NotNull Skill skill) {
        List<SkillValue> leaderboard = skillLeaderboards.get(skill);
        if (leaderboard == null)
            throw new IllegalStateException("Invalid leaderboard skill index key: " + skill.name());
        return leaderboard;
    }

    public void setLeaderboard(@NotNull Skill skill, List<SkillValue> leaderboard) {
        this.skillLeaderboards.put(skill, leaderboard);
    }

    public @NotNull List<SkillValue> getLeaderboard(@NotNull Skill skill, int page, int numPerPage) {
        List<SkillValue> leaderboard = skillLeaderboards.get(skill);
        if (leaderboard == null)
            throw new IllegalStateException("Invalid leaderboard skill index key: " + skill.name());
        int from = (Math.max(page, 1) - 1) * numPerPage;
        int to = from + numPerPage;
        return leaderboard.subList(Math.min(from, leaderboard.size()), Math.min(to, leaderboard.size()));
    }

    public @NotNull List<SkillValue> getPowerLeaderboard() {
        return powerLeaderboard;
    }

    public @NotNull List<SkillValue> getPowerLeaderboard(int page, int numPerPage) {
        int from = (Math.max(page, 1) - 1) * numPerPage;
        int to = from + numPerPage;
        return powerLeaderboard.subList(Math.min(from, powerLeaderboard.size()), Math.min(to, powerLeaderboard.size()));
    }

    public void setPowerLeaderboard(@NotNull List<SkillValue> leaderboard) {
        this.powerLeaderboard = leaderboard;
    }

    public @NotNull List<SkillValue> getAverageLeaderboard() {
        return averageLeaderboard;
    }

    public @NotNull List<SkillValue> getAverageLeaderboard(int page, int numPerPage) {
        int from = (Math.max(page, 1) - 1) * numPerPage;
        int to = from + numPerPage;
        return averageLeaderboard.subList(Math.min(from, averageLeaderboard.size()), Math.min(to, averageLeaderboard.size()));
    }

    public void setAverageLeaderboard(@NotNull List<SkillValue> leaderboard) {
        this.averageLeaderboard = leaderboard;
    }

    public int getSkillRank(@NotNull Skill skill, @NotNull UUID id) {
        List<SkillValue> leaderboard = skillLeaderboards.get(skill);
        if (leaderboard == null)
            throw new IllegalStateException("Invalid leaderboard skill index key: " + skill.name());
        for (SkillValue skillValue : leaderboard) {
            if (skillValue.getId().equals(id)) {
                return leaderboard.indexOf(skillValue) + 1;
            }
        }
        return 0;
    }

    public int getPowerRank(UUID id) {
        for (SkillValue skillValue : powerLeaderboard) {
            if (skillValue.getId().equals(id)) {
                return powerLeaderboard.indexOf(skillValue) + 1;
            }
        }
        return 0;
    }

    public int getAverageRank(UUID id) {
        for (SkillValue skillValue : averageLeaderboard) {
            if (skillValue.getId().equals(id)) {
                return averageLeaderboard.indexOf(skillValue) + 1;
            }
        }
        return 0;
    }

    public boolean isNotSorting() {
        return !sorting;
    }

    public void setSorting(boolean sorting) {
        this.sorting = sorting;
    }

}
