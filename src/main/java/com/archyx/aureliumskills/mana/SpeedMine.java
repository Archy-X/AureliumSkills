package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.mining.MiningSource;
import com.archyx.aureliumskills.source.SourceTag;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedMine extends ReadiedManaAbility {

    public SpeedMine(AureliumSkills plugin) {
        super(plugin, MAbility.SPEED_MINE, ManaAbilityMessage.SPEED_MINE_START, ManaAbilityMessage.SPEED_MINE_END,
                new String[] {"PICKAXE"}, new Action[] {Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR});
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivate(Player player, PlayerData playerData) {
        int amplifier = manager.getOptionAsInt(mAbility, "haste_level", 10) - 1;
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (manager.getValue(MAbility.SPEED_MINE, playerData) * 20),
                amplifier, false, false), true);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    @Override
    public void onStop(Player player, PlayerData playerData) {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void activationListener(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (OptionL.getBoolean(Option.CHECK_BLOCK_REPLACE) && plugin.getRegionManager().isPlacedBlock(block)) {
            return;
        }
        Player player = event.getPlayer();
        MiningSource source = MiningSource.getSource(block);
        if (source == null) return;
        if (hasTag(source, SourceTag.SPEED_MINE_APPLICABLE)) {
            if (isReady(player) && isHoldingMaterial(player) && hasEnoughMana(player)) {
                activate(player);
            }
        }
    }

}
