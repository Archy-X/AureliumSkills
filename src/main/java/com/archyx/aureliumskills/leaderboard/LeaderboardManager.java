package com.archyx.aureliumskills.leaderboard;

import com.archyx.aureliumskills.skills.Skill;

import java.util.*;

public class LeaderboardManager {

    private final Map<Skill, List<SkillValue>> skillLeaderboards;
    private List<SkillValue> powerLeaderboard;
    private List<SkillValue> averageLeaderboard;
    private volatile boolean sorting = false;

    public LeaderboardManager() {
        this.skillLeaderboards = new HashMap<>();
        this.powerLeaderboard = new ArrayList<>();
        this.averageLeaderboard = new ArrayList<>();
    }

    public List<SkillValue> getLeaderboard(Skill skill) {
        List<SkillValue> leaderboard = skillLeaderboards.get(skill);
        if (leaderboard == null)
            throw new IllegalStateException("Invalid leaderboard skill index key: " + skill.name());
        return leaderboard;
    }

    public void setLeaderboard(Skill skill, List<SkillValue> leaderboard) {
        this.skillLeaderboards.put(skill, leaderboard);
    }

    public List<SkillValue> getLeaderboard(Skill skill, int page, int numPerPage) {
        List<SkillValue> leaderboard = skillLeaderboards.get(skill);
        if (leaderboard == null)
            throw new IllegalStateException("Invalid leaderboard skill index key: " + skill.name());
        int from = (Math.max(page, 1) - 1) * numPerPage;
        int to = from + numPerPage;
        return leaderboard.subList(Math.min(from, leaderboard.size()), Math.min(to, leaderboard.size()));
    }

    public List<SkillValue> getPowerLeaderboard() {
        return powerLeaderboard;
    }

    public List<SkillValue> getPowerLeaderboard(int page, int numPerPage) {
        int from = (Math.max(page, 1) - 1) * numPerPage;
        int to = from + numPerPage;
        return powerLeaderboard.subList(Math.min(from, powerLeaderboard.size()), Math.min(to, powerLeaderboard.size()));
    }

    public void setPowerLeaderboard(List<SkillValue> leaderboard) {
        this.powerLeaderboard = leaderboard;
    }

    public List<SkillValue> getAverageLeaderboard() {
        return averageLeaderboard;
    }

    public List<SkillValue> getAverageLeaderboard(int page, int numPerPage) {
        int from = (Math.max(page, 1) - 1) * numPerPage;
        int to = from + numPerPage;
        return averageLeaderboard.subList(Math.min(from, averageLeaderboard.size()), Math.min(to, averageLeaderboard.size()));
    }

    public void setAverageLeaderboard(List<SkillValue> leaderboard) {
        this.averageLeaderboard = leaderboard;
    }

    public int getSkillRank(Skill skill, UUID id) {
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
