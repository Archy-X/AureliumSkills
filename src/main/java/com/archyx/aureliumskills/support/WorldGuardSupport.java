package com.archyx.aureliumskills.support;

import com.archyx.aureliumskills.AureliumSkills;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class WorldGuardSupport {

    private RegionContainer container;
    private final AureliumSkills plugin;
    private List<String> blockedRegions;
    private List<String> blockedCheckBlockReplaceRegions;

    public WorldGuardSupport(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public void loadRegions() {
        try {
            container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            FileConfiguration config = plugin.getConfig();
            blockedRegions = new LinkedList<>();
            blockedRegions.addAll(config.getStringList("blocked-regions"));
            blockedCheckBlockReplaceRegions = new LinkedList<>();
            blockedCheckBlockReplaceRegions.addAll(config.getStringList("blocked-check-block-replace-regions"));
            Bukkit.getLogger().info("[AureliumSkills] WorldGuard Support Enabled!");
        }
        catch (Exception e) {
            plugin.setWorldGuardEnabled(false);
            Bukkit.getLogger().warning("[AureliumSkills] WorldGuard support failed to load, disabling World Guard support!");
        }
    }

    public boolean isInBlockedRegion(@NotNull Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            return false;
        }
        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        for (ProtectedRegion region : set) {
            if (blockedRegions.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean blockedByFlag(@NotNull Location location, Player player, WorldGuardFlags.FlagKey flagKey) {
        WorldGuardFlags worldGuardFlags = plugin.getWorldGuardFlags();
        if (worldGuardFlags == null) return false;

        StateFlag flag = worldGuardFlags.getStateFlag(flagKey);
        if (flag == null) return false;

        World world = location.getWorld();
        if (world == null) return false;

        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            return false;
        }
        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        StateFlag.State state = set.queryState(localPlayer, flag);
        return state == StateFlag.State.DENY;
    }

    public boolean isInBlockedCheckRegion(@NotNull Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            return false;
        }
        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        for (ProtectedRegion region : set) {
            if (blockedCheckBlockReplaceRegions.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }

}
