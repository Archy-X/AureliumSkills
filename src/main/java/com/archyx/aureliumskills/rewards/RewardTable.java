package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.support.LuckPermsSupport;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardTable {

    private final AureliumSkills plugin;
    private final List<Stat> statsLeveled;
    private final Map<Integer, List<Reward>> rewards;

    public RewardTable(AureliumSkills plugin) {
        this.plugin = plugin;
        this.rewards = new HashMap<>();
        this.statsLeveled = new ArrayList<>();
    }

    public ImmutableList<Reward> getRewards(int level) {
        return ImmutableList.copyOf(rewards.get(level));
    }

    public Map<Integer, List<Reward>> getRewardsMap() {
        return rewards;
    }

    public void addReward(Reward reward, int level) {
        List<Reward> rewards = this.rewards.computeIfAbsent(level, k -> new ArrayList<>());
        rewards.add(reward);
        if (reward instanceof StatReward) {
            StatReward statReward = (StatReward) reward;
            if (!statsLeveled.contains(statReward.getStat())) {
                statsLeveled.add(statReward.getStat());
            }
        }
    }

    public ImmutableList<Stat> getStatsLeveled() {
        return ImmutableList.copyOf(statsLeveled);
    }

    /**
     * Searches rewards for all rewards of a certain type
     * @param type The class of the type of reward to search
     * @param <T> The reward type
     * @return A map of each level to a list of rewards of that type
     */
    public <T extends Reward> Map<Integer, ImmutableList<T>> searchRewards(Class<T> type) {
        Map<Integer, ImmutableList<T>> rewardMap = new HashMap<>();
        for (Map.Entry<Integer, List<Reward>> entry : rewards.entrySet()) {
            List<T> rewardList = new ArrayList<>();
            for (Reward reward : entry.getValue()) {
                if (type.isInstance(reward)) {
                    rewardList.add(type.cast(reward));
                }
            }
            rewardMap.put(entry.getKey(), ImmutableList.copyOf(rewardList));
        }
        return rewardMap;
    }

    /**
     * Searches all rewards of a certain type at a single level
     */
    public <T extends Reward> ImmutableList<T> searchRewards(Class<T> type, int level) {
        ImmutableList<Reward> levelRewards = getRewards(level);
        List<T> rewardList = new ArrayList<>();
        for (Reward reward : levelRewards) {
            if (type.isInstance(reward)) {
                rewardList.add(type.cast(reward));
            }
        }
        return ImmutableList.copyOf(rewardList);
    }

    public void applyStats(PlayerData playerData, int level) {
        Map<Integer, ImmutableList<StatReward>> statRewardMap = searchRewards(StatReward.class);
        for (int i = 2; i <= level; i++) {
            ImmutableList<StatReward> statRewardList = statRewardMap.get(i);
            if (statRewardList != null) {
                for (StatReward statReward : statRewardList) {
                    playerData.addStatLevel(statReward.getStat(), statReward.getValue());
                }
            }
        }
    }

    public void applyPermissions(Player player, int level) {
        Map<Integer, ImmutableList<PermissionReward>> permissionRewardMap = searchRewards(PermissionReward.class);
        for (Map.Entry<Integer, ImmutableList<PermissionReward>> entry : permissionRewardMap.entrySet()) {
            int entryLevel = entry.getKey();
            for (PermissionReward reward : entry.getValue()) {
                LuckPermsSupport luckPermsSupport = plugin.getLuckPermsSupport();
                if (plugin.isLuckPermsEnabled() && luckPermsSupport != null) {
                    // Add permission if unlocked
                    if (level >= entryLevel) {
                        plugin.getLuckPermsSupport().addPermission(player, reward.getPermission(), reward.getValue());
                    }
                    // Remove permission if not unlocked
                    else {
                        plugin.getLuckPermsSupport().removePermission(player, reward.getPermission(), reward.getValue());
                    }
                }
            }
        }
    }

}
