package com.archyx.aureliumskills.loot.handler;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.support.WorldGuardFlags;
import com.archyx.aureliumskills.support.WorldGuardSupport;
import com.archyx.lootmanager.loot.Loot;
import com.archyx.lootmanager.loot.LootPool;
import com.archyx.lootmanager.loot.LootTable;
import com.archyx.lootmanager.loot.type.CommandLoot;
import com.archyx.lootmanager.loot.type.ItemLoot;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public abstract class BlockLootHandler extends LootHandler implements Listener {

    private final Random random = new Random();

    public BlockLootHandler(@NotNull AureliumSkills plugin, @NotNull Skill skill, @NotNull Ability ability) {
        super(plugin, skill, ability);
    }

    public abstract @Nullable Source getSource(@NotNull Block block);

    public abstract double getChance(@NotNull LootPool pool, @NotNull PlayerData playerData);

    public abstract @NotNull LootDropCause getCause(@NotNull LootPool pool);

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(@NotNull BlockBreakEvent event) {
        if (!OptionL.isEnabled(skill)) return;
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        if (getSource(block) == null) return;

        Player player = event.getPlayer();
        if (blockAbility(player)) return;

        if (player.getGameMode() != GameMode.SURVIVAL) { // Only drop loot in survival mode
            return;
        }

        WorldGuardSupport worldGuardSupport = plugin.getWorldGuardSupport();
        if (plugin.isWorldGuardEnabled() && worldGuardSupport != null) {
            if (worldGuardSupport.blockedByFlag(block.getLocation(), player, WorldGuardFlags.FlagKey.CUSTOM_LOOT)) {
                return;
            }
        }

        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;

        Source originalSource = getSource(block);

        LootTable table = plugin.getLootTableManager().getLootTable(skill);
        if (table == null) return;
        for (LootPool pool : table.getPools()) {
            // Calculate chance for pool
            double chance = getChance(pool, playerData);
            LootDropCause cause = getCause(pool);

            // Select pool and give loot
            if (originalSource != null && selectBlockLoot(pool, player, chance, originalSource, event, cause)) {
                break;
            }
        }
    }

    private boolean selectBlockLoot(@NotNull LootPool pool, @NotNull Player player, double chance, @NotNull Source originalSource, @NotNull BlockBreakEvent event, @NotNull LootDropCause cause) {
        if (random.nextDouble() < chance) { // Pool is selected
            Loot selectedLoot = selectLoot(pool, originalSource);
            // Give loot
            if (selectedLoot != null) {
                if (selectedLoot instanceof ItemLoot) {
                    ItemLoot itemLoot = (ItemLoot) selectedLoot;
                    giveBlockItemLoot(player, itemLoot, event, originalSource, cause);
                } else if (selectedLoot instanceof CommandLoot) {
                    CommandLoot commandLoot = (CommandLoot) selectedLoot;
                    giveCommandLoot(player, commandLoot, originalSource);
                }
                // Override vanilla loot if enabled
                if (pool.overridesVanillaLoot()) {
                    event.setDropItems(false);
                }
                return true;
            }
        }
        return false;
    }

}
