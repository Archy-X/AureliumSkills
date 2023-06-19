package com.archyx.aureliumskills.skills.sorcery;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SorceryLeveler extends SkillLeveler implements Listener {

    public SorceryLeveler(AureliumSkills plugin) {
        super(plugin, Ability.SORCERER);
    }

    public void level(Player player, double manaUsed) {
        plugin.getLeveler().addXp(player, Skills.SORCERY, manaUsed * getAbilityXp(player, SorcerySource.MANA_ABILITY_USE));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!OptionL.isEnabled(Skills.SORCERY)) {
            return;
        }
        // Check cancelled
        if (OptionL.getBoolean(Option.SORCERY_CHECK_CANCELLED) && event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        // Check block replace
        if (OptionL.getBoolean(Option.CHECK_BLOCK_REPLACE) && plugin.getRegionManager().isPlacedBlock(block)) {
            return;
        }

        Player player = event.getPlayer();
        if (blockXpGainLocation(block.getLocation(), player, Skills.SORCERY)) return;
        if (blockXpGainPlayer(player)) return;

        // Search through sources until a match is found for the block broken
        for (SorcerySource source : SorcerySource.values()) {
            // Add XP to player if matched
            if (!source.isMatch(block)) continue;
            // Check silk touch
            plugin.getLeveler().addXp(player, Skills.SORCERY, getAbilityXp(player, source));
            break; // Stop searching if matched
        }
    }

}
