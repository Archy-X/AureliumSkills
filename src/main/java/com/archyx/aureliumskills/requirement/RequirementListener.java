package com.archyx.aureliumskills.requirement;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MessageKey;
import com.archyx.aureliumskills.modifier.ModifierType;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.armor.ArmorEquipEvent;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public class RequirementListener implements Listener {

    private final @NotNull AureliumSkills plugin;
    private final RequirementManager manager;
    private final @NotNull Requirements requirements;

    public RequirementListener(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.manager = plugin.getRequirementManager();
        this.requirements = new Requirements(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEquip(@NotNull ArmorEquipEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack item = event.getNewArmorPiece();
        if (item != null) {
            if (item.getType() != Material.AIR) {
                if (!requirements.meetsRequirements(ModifierType.ARMOR, item, player)) {
                    @Nullable Locale locale = plugin.getLang().getLocale(player);
                    event.setCancelled(true);
                    Integer timer = manager.getErrorMessageTimer().get(player.getUniqueId());
                    if (timer != null) {
                        if (timer.equals(0)) {
                            sendMessage(CommandMessage.ARMOR_REQUIREMENT_EQUIP, CommandMessage.ARMOR_REQUIREMENT_ENTRY, ModifierType.ARMOR, player, locale, item);
                            manager.getErrorMessageTimer().put(player.getUniqueId(), 8);
                        }
                    }
                    else {
                        sendMessage(CommandMessage.ARMOR_REQUIREMENT_EQUIP, CommandMessage.ARMOR_REQUIREMENT_ENTRY, ModifierType.ARMOR, player, locale, item);
                        manager.getErrorMessageTimer().put(player.getUniqueId(), 8);
                    }
                }
            }
        }
    }

    private void sendMessage(@NotNull MessageKey baseMessage, @NotNull MessageKey entryMessage, @NotNull ModifierType modifierType, @NotNull Player player, @Nullable Locale locale, @NotNull ItemStack item) {
        // Build requirements message that shows skills and levels
        StringBuilder requirementsString = new StringBuilder();
        Map<Skill, Integer> requirementMap = requirements.getRequirements(modifierType, item);
        @Nullable String m;
        for (Map.Entry<Skill, Integer> entry : requirementMap.entrySet()) {
            m = TextUtil.replace(Lang.getMessage(entryMessage, locale),
                    "{skill}", entry.getKey().getDisplayName(locale), "{level}", RomanNumber.toRoman(entry.getValue()));
            requirementsString.append(m);
        }
        Map<Skill, Integer> globalRequirementMap = requirements.getGlobalRequirements(modifierType, item);
        for (Map.Entry<Skill, Integer> entry : globalRequirementMap.entrySet()) {
            m = TextUtil.replace(Lang.getMessage(entryMessage, locale),
                    "{skill}", entry.getKey().getDisplayName(locale), "{level}", RomanNumber.toRoman(entry.getValue()));
            requirementsString.append(m);
        }
        if (requirementsString.length() >= 2) {
            requirementsString.delete(requirementsString.length() - 2, requirementsString.length());
        }

        player.sendMessage(AureliumSkills.getPrefix(locale) + TextUtil.replace(Lang.getMessage(baseMessage, locale)
                , "{requirements}", requirementsString.toString()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (OptionL.getBoolean(Option.REQUIREMENT_ITEM_PREVENT_TOOL_USE)) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) return;
            checkItemRequirements(player, item, event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(@NotNull BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (OptionL.getBoolean(Option.REQUIREMENT_ITEM_PREVENT_BLOCK_PLACE)) {
            Player player = event.getPlayer();
            ItemStack item = event.getItemInHand();
            if (item.getType() == Material.AIR) return;
            checkItemRequirements(player, item, event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAttack(@NotNull EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (OptionL.getBoolean(Option.REQUIREMENT_ITEM_PREVENT_WEAPON_USE)) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) return;
                checkItemRequirements(player, item, event);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShoot(@NotNull EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        if (!OptionL.getBoolean(Option.REQUIREMENT_ITEM_PREVENT_WEAPON_USE)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getBow();
        if (item == null) return;
        if (item.getType() == Material.AIR) return;

        checkItemRequirements(player, item, event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (!OptionL.getBoolean(Option.REQUIREMENT_ITEM_PREVENT_INTERACT)) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getType() == Material.AIR) return;
        checkItemRequirements(event.getPlayer(), item, event);
    }

    private void checkItemRequirements(@NotNull Player player, @NotNull ItemStack item, @NotNull Cancellable event) {
        if (!requirements.meetsRequirements(ModifierType.ITEM, item, player)) {
            @Nullable Locale locale = plugin.getLang().getLocale(player);
            event.setCancelled(true);
            Integer timer = manager.getErrorMessageTimer().get(player.getUniqueId());
            if (timer != null) {
                if (timer.equals(0)) {
                    sendMessage(CommandMessage.ITEM_REQUIREMENT_USE, CommandMessage.ITEM_REQUIREMENT_ENTRY, ModifierType.ITEM, player, locale, item);
                    manager.getErrorMessageTimer().put(player.getUniqueId(), 8);
                }
            } else {
                sendMessage(CommandMessage.ITEM_REQUIREMENT_USE, CommandMessage.ITEM_REQUIREMENT_ENTRY, ModifierType.ITEM, player, locale, item);
                manager.getErrorMessageTimer().put(player.getUniqueId(), 8);
            }
        }
    }

}
