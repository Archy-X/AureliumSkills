package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.support.LuckPermsSupport;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardTable {

    private final AureliumSkills plugin;
    private final @NotNull List<@NotNull Stat> statsLeveled;
    private final @NotNull Map<Integer, @NotNull List<@NotNull Reward>> rewards;

    public RewardTable(AureliumSkills plugin) {
        this.plugin = plugin;
        this.rewards = new HashMap<>();
        this.statsLeveled = new ArrayList<>();
    }

    public @NotNull ImmutableList<@NotNull Reward> getRewards(int level) {
        return ImmutableList.copyOf(rewards.get(level));
    }

    public @NotNull Map<Integer, @NotNull List<@NotNull Reward>> getRewardsMap() {
        return rewards;
    }

    public void addReward(@NotNull Reward reward, int level) {
        List<@NotNull Reward> rewards = this.rewards.computeIfAbsent(level, k -> new ArrayList<>());
        rewards.add(reward);
        if (reward instanceof StatReward) {
            StatReward statReward = (StatReward) reward;
            if (!statsLeveled.contains(statReward.getStat())) {
                statsLeveled.add(statReward.getStat());
            }
        }
    }

    public @NotNull ImmutableList<@NotNull Stat> getStatsLeveled() {
        return ImmutableList.copyOf(statsLeveled);
    }

    /**
     * Searches rewards for all rewards of a certain type
     * @param type The class of the type of reward to search
     * @param <T> The reward type
     * @return A map of each level to a list of rewards of that type
     */
    public <T extends @NotNull Reward> @NotNull Map<Integer, ImmutableList<T>> searchRewards(@NotNull Class<T> type) {
        Map<Integer, ImmutableList<T>> rewardMap = new HashMap<>();
        for (Map.Entry<Integer, @NotNull List<@NotNull Reward>> entry : rewards.entrySet()) {
            List<T> rewardList = new ArrayList<>();
            for (@NotNull Reward reward : entry.getValue()) {
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
    public <T extends @NotNull Reward> @NotNull ImmutableList<T> searchRewards(@NotNull Class<T> type, int level) {
        ImmutableList<@NotNull Reward> levelRewards = getRewards(level);
        List<T> rewardList = new ArrayList<>();
        for (Reward reward : levelRewards) {
            if (type.isInstance(reward)) {
                rewardList.add(type.cast(reward));
            }
        }
        return ImmutableList.copyOf(rewardList);
    }

    public void applyStats(@NotNull PlayerData playerData, int level) {
        Map<Integer, ImmutableList<@NotNull StatReward>> statRewardMap = searchRewards(StatReward.class);
        for (int i = 2; i <= level; i++) {
            ImmutableList<@NotNull StatReward> statRewardList = statRewardMap.get(i);
            if (statRewardList != null) {
                for (StatReward statReward : statRewardList) {
                    playerData.addStatLevel(statReward.getStat(), statReward.getValue());
                }
            }
        }
    }

    public void applyPermissions(@NotNull Player player, int level) {
        Map<Integer, ImmutableList<@NotNull PermissionReward>> permissionRewardMap = searchRewards(PermissionReward.class);
        for (Map.Entry<Integer, ImmutableList<@NotNull PermissionReward>> entry : permissionRewardMap.entrySet()) {
            int entryLevel = entry.getKey();
            for (PermissionReward reward : entry.getValue()) {
                LuckPermsSupport luckPermsSupport = plugin.getLuckPermsSupport();
                if (plugin.isLuckPermsEnabled() && luckPermsSupport != null) {
                    // Add permission if unlocked
                    if (level >= entryLevel) {
                        luckPermsSupport.addPermission(player, reward.getPermission(), reward.getValue());
                    }
                    // Remove permission if not unlocked
                    else {
                        luckPermsSupport.removePermission(player, reward.getPermission(), reward.getValue());
                    }
                }
            }
        }
    }

}
