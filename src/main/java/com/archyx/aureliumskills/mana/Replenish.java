package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.util.block.BlockUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Replenish extends ReadiedManaAbility {

    public Replenish(AureliumSkills plugin) {
        super(plugin, MAbility.REPLENISH, ManaAbilityMessage.REPLENISH_START, ManaAbilityMessage.REPLENISH_END, new String[] {"HOE"},
                new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK});
    }

    @Override
    public void onActivate(Player player, PlayerData playerData) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    @Override
    public void onStop(Player player, PlayerData playerData) {

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void activationListener(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (!BlockUtil.isReplenishable(event.getBlock().getType())) return;
        Player player = event.getPlayer();
        if (isActivated(player)) {
            onBreak(event);
        } else if (isReady(player) && isHoldingMaterial(player) && hasEnoughMana(player)) {
            activate(player);
            onBreak(event);
        }
    }

    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (BlockUtil.isFullyGrown(block)) {
            replantCrop(block);
        } else if (manager.getOptionAsBooleanElseTrue(mAbility, "prevent_unripe_break")) {
            event.setCancelled(true);
        }
    }

    private void replantCrop(Block block) {
        Material material = block.getType();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!BlockUtil.isNetherWart(material)) {
                    if (block.getRelative(BlockFace.DOWN).getType().equals(XMaterial.FARMLAND.parseMaterial())) {
                        block.setType(material);
                        attemptSpawnParticle(block);
                    }
                } else {
                    if (block.getRelative(BlockFace.DOWN).getType().equals(XMaterial.SOUL_SAND.parseMaterial())) {
                        block.setType(material);
                        attemptSpawnParticle(block);
                    }
                }
            }
        }.runTaskLater(plugin, manager.getOptionAsInt(MAbility.REPLENISH, "replant_delay", 4));
    }

    private void attemptSpawnParticle(Block block) {
        if (manager.getOptionAsBooleanElseTrue(mAbility, "show_particles")) {
            block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.2, 0.5), 8, 0.25, 0, 0.25);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean isExcludedBlock(Block block) {
        if (XMaterial.isNewVersion()) {
            return block.getType() == XMaterial.DIRT.parseMaterial()
                    || block.getType() == XMaterial.GRASS_BLOCK.parseMaterial()
                    || block.getType() == XMaterial.COARSE_DIRT.parseMaterial()
                    || block.getType() == XMaterial.DIRT_PATH.parseMaterial()
                    || block.getType() == XMaterial.FARMLAND.parseMaterial();
        } else {
            if (block.getType() == XMaterial.GRASS_BLOCK.parseMaterial()
                    || block.getType() == XMaterial.DIRT_PATH.parseMaterial()
                    || block.getType() == XMaterial.FARMLAND.parseMaterial()) {
                return true;
            } else if (block.getType() == Material.DIRT) {
                switch (block.getData()) {
                    case 0:
                    case 1:
                        return true;
                }
            }
        }
        return false;
    }
}
