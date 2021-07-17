package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.util.math.NumberUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;

public abstract class ReadiedManaAbility extends ManaAbilityProvider {

    private final Action[] actions;
    protected final String[] materials;

    private final static int READY_DURATION = 80;

    public ReadiedManaAbility(AureliumSkills plugin, MAbility manaAbility, ManaAbilityMessage activateMessage, ManaAbilityMessage stopMessage, String[] materials, Action[] actions) {
        super(plugin, manaAbility, activateMessage, stopMessage);
        this.materials = materials;
        this.actions = actions;
    }

    // Gets whether the clicked block should not ready the ability, override to implement
    protected boolean isExcludedBlock(Block block) {
        return false;
    }

    protected boolean isActivated(Player player) {
        return manager.isActivated(player.getUniqueId(), mAbility);
    }

    /**
     * Gets whether the ability is ready but not activated
     */
    protected boolean isReady(Player player) {
        return manager.isReady(player.getUniqueId(), mAbility) && !isActivated(player);
    }

    protected boolean isHoldingMaterial(Player player) {
        return materialMatches(player.getInventory().getItemInMainHand().getType().toString());
    }

    protected boolean materialMatches(String checked) {
        for (String material : materials) {
            if (checked.contains(material)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onReady(PlayerInteractEvent event) {
        if (!OptionL.isEnabled(skill)) return;
        if (!plugin.getAbilityManager().isEnabled(mAbility)) return;
        // Check action is valid
        boolean valid = false;
        for (Action action : actions) {
            if (event.getAction() == action) {
                valid = true;
                break;
            }
        }
        if (!valid) return;
        // Check block exclusions
        Block block = event.getClickedBlock();
        if (block != null) {
            if (isExcludedBlock(block)) return;
        }
        // Match sure material matches
        Player player = event.getPlayer();
        if (!isHoldingMaterial(player)) {
            return;
        }
        if (!isAllowReady(player, event)) {
            return;
        }
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        Locale locale = playerData.getLocale();
        if (playerData.getManaAbilityLevel(mAbility) <= 0) {
            return;
        }
        // Check if already activated
        if (manager.isActivated(player.getUniqueId(), mAbility)) {
            return;
        }
        // Checks if already ready
        if (manager.isReady(player.getUniqueId(), mAbility)) {
            return;
        }
        if (manager.getPlayerCooldown(player.getUniqueId(), mAbility) == 0) { // Ready
            manager.setReady(player.getUniqueId(), mAbility, true);
            plugin.getAbilityManager().sendMessage(player, ChatColor.GRAY + Lang.getMessage(ManaAbilityMessage.valueOf(mAbility.name() + "_RAISE"), locale));
            scheduleUnready(player, locale);
        } else { // Cannot ready, send cooldown error
            if (manager.getErrorTimer(player.getUniqueId(), mAbility) == 0) {
                plugin.getAbilityManager().sendMessage(player, Lang.getMessage(ManaAbilityMessage.NOT_READY, locale).replace("{cooldown}",
                        NumberUtil.format0((double) plugin.getManaAbilityManager().getPlayerCooldown(player.getUniqueId(), mAbility) / 20)));
                manager.setErrorTimer(player.getUniqueId(), mAbility, 2);
            }
        }
    }

    private void scheduleUnready(Player player, Locale locale) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!manager.isActivated(player.getUniqueId(), mAbility)) {
                    if (manager.isReady(player.getUniqueId(), mAbility)) {
                        manager.setReady(player.getUniqueId(), mAbility, false);
                        plugin.getAbilityManager().sendMessage(player, Lang.getMessage(ManaAbilityMessage.valueOf(mAbility.name() + "_LOWER"), locale));
                    }
                }
            }
        }.runTaskLater(plugin, READY_DURATION);
    }

    private boolean isAllowReady(Player player, PlayerInteractEvent event) {
        // Check if requires sneak
        if (manager.getOptionAsBooleanElseFalse(mAbility, "require_sneak")) {
            if (!player.isSneaking()) return false;
        }
        // Check if the offhand item is being placed
        if (isBlockPlace(event, player, mAbility)) {
            return false;
        }
        // Check disabled worlds
        if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
            return false;
        }
        // Check permission
        return player.hasPermission("aureliumskills." + skill.toString().toLowerCase(Locale.ENGLISH));
    }

    private boolean isBlockPlace(PlayerInteractEvent event, Player player, MAbility mAbility) {
        if (plugin.getManaAbilityManager().getOptionAsBooleanElseTrue(mAbility, "check_offhand")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (player.isSneaking() && plugin.getManaAbilityManager().getOptionAsBooleanElseTrue(mAbility, "sneak_offhand_bypass")) {
                    return false;
                }
                ItemStack item = player.getInventory().getItemInOffHand();
                if (item.getType() == Material.AIR) return false;
                return item.getType().isBlock();
            }
        }
        return false;
    }

}
