package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.skills.foraging.ForagingSource;
import com.archyx.aureliumskills.util.block.BlockFaceUtil;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Treecapitator extends ReadiedManaAbility {

    public Treecapitator(AureliumSkills plugin) {
        super(plugin, MAbility.TREECAPITATOR, ManaAbilityMessage.TREECAPITATOR_START, ManaAbilityMessage.TREECAPITATOR_END,
                new String[] {"_AXE"}, new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK});
    }

    @Override
    public void onActivate(Player player, PlayerData playerData) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    @Override
    public void onStop(Player player, PlayerData playerData) {

    }

    @Override
    protected boolean materialMatches(String checked) {
        // Don't ready world edit axe
        if (checked.equals("WOODEN_AXE") || checked.equals("WOOD_AXE")) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
                return false;
            }
        }

        for (String material : materials) {
            if (checked.contains(material)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        // Checks if block broken is log
        Block block = event.getBlock();
        if (plugin.getRegionManager().isPlacedBlock(block)) {
            return;
        }
        if (isTrunk(block) || block.getType().toString().contains("STRIPPED")) {
            Player player = event.getPlayer();
            if (isActivated(player)) {
                breakTree(player, block);
                return;
            }
            if (isReady(player) && isHoldingMaterial(player) && hasEnoughMana(player)) {
                if (hasEnoughMana(player)) {
                    activate(player);
                    breakTree(player, block);
                }
            }
        }
    }

    public void breakTree(Player player, Block block) {
        if (plugin.getManaAbilityManager().isActivated(player.getUniqueId(), MAbility.TREECAPITATOR)) {
            ForagingSource source = ForagingSource.getSource(block);
            if (source != null) {
                breakBlock(player, block, new TreecapitatorTree(block));
            }
        }
    }

    private void breakBlock(Player player, Block block, TreecapitatorTree tree) {
        if (tree.getBlocksBroken() > tree.getMaxBlocks()) {
            return;
        }
        for (Block rel : BlockFaceUtil.getSurroundingBlocks(block)) {
            boolean isTrunk = isTrunk(rel);
            boolean isLeaf = isLeaf(rel);
            if (!isTrunk && !isLeaf && !rel.getType().toString().equals("SHROOMLIGHT")) continue; // Check block is leaf or trunk
            // Make sure block was not placed
            if (plugin.getRegionManager().isPlacedBlock(rel)) {
                continue;
            }
            ForagingSource source = ForagingSource.getSource(rel);
            rel.breakNaturally();
            tree.incrementBlocksBroken();
            if (source != null) {
                plugin.getLeveler().addXp(player, Skills.FORAGING, getXp(player, source, Ability.FORAGER));
            }
            // Continue breaking blocks
            Block originalBlock = tree.getOriginalBlock();
            if (rel.getX() > originalBlock.getX() + 6 || rel.getZ() > originalBlock.getZ() + 6 || rel.getY() > originalBlock.getY() + 31) {
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    breakBlock(player, rel, tree);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    private boolean isTrunk(Block block) {
        ForagingSource source = ForagingSource.getSource(block);
        if (source != null) {
            return source.isTrunk();
        }
        return false;
    }

    private boolean isLeaf(Block block) {
        ForagingSource source = ForagingSource.getSource(block);
        if (source != null) {
            return source.isLeaf();
        }
        return false;
    }

    private static class TreecapitatorTree {

        private final Block originalBlock;
        private int blocksBroken;
        private int maxBlocks;

        public TreecapitatorTree(Block originalBlock) {
            this.originalBlock = originalBlock;
            setMaxBlocks();
        }

        public Block getOriginalBlock() {
            return originalBlock;
        }

        public int getBlocksBroken() {
            return blocksBroken;
        }

        public void incrementBlocksBroken() {
            blocksBroken++;
        }

        public int getMaxBlocks() {
            return maxBlocks;
        }

        private void setMaxBlocks() {
            ForagingSource source = ForagingSource.getSource(originalBlock);
            String matName = originalBlock.getType().toString();
            if (source == null && matName.contains("STRIPPED")) {
                String[] woodNames = new String[] {"OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK", "CRIMSON", "WARPED"};
                for (String woodName : woodNames) {
                    if (matName.contains(woodName)) {
                        if (woodName.equals("CRIMSON") || woodName.equals("WARPED")) {
                            source = ForagingSource.valueOf(woodName + "_STEM");
                        } else {
                            source = ForagingSource.valueOf(woodName + "_LOG");
                        }
                        break;
                    }
                }
            }
            if (source != null) {
                switch (source) {
                    case OAK_LOG:
                    case BIRCH_LOG:
                    case ACACIA_LOG:
                        maxBlocks = 100;
                        break;
                    case SPRUCE_LOG:
                    case CRIMSON_STEM:
                    case WARPED_STEM:
                        maxBlocks = 125;
                        break;
                    case JUNGLE_LOG:
                        maxBlocks = 200;
                        break;
                    case DARK_OAK_LOG:
                        maxBlocks = 150;
                        break;
                }
            } else {
                maxBlocks = 100;
            }
        }

    }

}
