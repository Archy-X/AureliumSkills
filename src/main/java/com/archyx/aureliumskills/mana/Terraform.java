package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.TerraformBlockBreakEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.skills.excavation.ExcavationSource;
import com.archyx.aureliumskills.source.SourceTag;
import com.archyx.aureliumskills.support.TownySupport;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.LinkedList;

public class Terraform extends ReadiedManaAbility {

    public Terraform(AureliumSkills plugin) {
        super(plugin, MAbility.TERRAFORM, ManaAbilityMessage.TERRAFORM_START, ManaAbilityMessage.TERRAFORM_END,
                new String[]{"SHOVEL", "SPADE"}, new Action[]{Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR});
    }

    @Override
    public void onActivate(Player player, PlayerData playerData) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    @Override
    public void onStop(Player player, PlayerData playerData) {

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!OptionL.isEnabled(Skills.EXCAVATION)) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (blockAbility(player)) return;
        if (!plugin.getAbilityManager().isEnabled(MAbility.TERRAFORM)) return;
        if (block.hasMetadata("block-ignore")) { // Compatibility fix
            return;
        }
        if (!block.hasMetadata("AureliumSkills-Terraform") && event.getClass() == BlockBreakEvent.class) {
            applyTerraform(player, block);
        }
    }

    private void applyTerraform(Player player, Block block) {
        // Check if block is applicable to ability
        ExcavationSource source = ExcavationSource.getSource(block);
        if (source == null) return;
        if (!hasTag(source, SourceTag.TERRAFORM_APPLICABLE)) return;
        // Apply if activated
        if (isActivated(player)) {
            terraformBreak(player, block);
            return;
        }
        //Checks if speed mine is ready
        if (isReady(player) && isHoldingMaterial(player) && hasEnoughMana(player)) {
            activate(player);
            terraformBreak(player, block);
        }
    }

    private void terraformBreak(Player player, Block block) {
        Material material = block.getType();
        BlockFace[] faces = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        LinkedList<Block> toCheck = new LinkedList<>();
        toCheck.add(block);
        int count = 0;
        Block nextBlock;
        while ((nextBlock = toCheck.poll()) != null && count < 61) {
            if (nextBlock.getType() == material) {
                nextBlock.setMetadata("AureliumSkills-Terraform", new FixedMetadataValue(plugin, true));
                breakBlock(player, nextBlock);
                for (BlockFace face : faces) {
                    toCheck.add(nextBlock.getRelative(face));
                }
                count++;
            }
        }
    }

    private void breakBlock(Player player, Block block) {
        TownySupport townySupport = plugin.getTownySupport();
        if (plugin.isTownyEnabled() && townySupport != null) {
            if (!townySupport.canBreak(player, block)) {
                block.removeMetadata("AureliumSkills-Terraform", plugin);
                return;
            }
        }
        TerraformBlockBreakEvent event = new TerraformBlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            block.breakNaturally(player.getInventory().getItemInMainHand());
        }
        block.removeMetadata("AureliumSkills-Terraform", plugin);
    }

}
