package com.archyx.aureliumskills.modifier;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.requirement.Requirements;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.StatLeveler;
import com.archyx.aureliumskills.util.armor.ArmorEquipEvent;
import com.archyx.aureliumskills.util.armor.ArmorType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArmorModifierListener implements Listener {

    private final AureliumSkills plugin;
    private final Modifiers modifiers;
    private final Requirements requirements;
    private final Multipliers multipliers;
    private final StatLeveler statLeveler;
    private final Map<UUID, Map<ArmorType, ItemStack>> storedArmor;

    public ArmorModifierListener(AureliumSkills plugin) {
        this.plugin = plugin;
        this.modifiers = new Modifiers(plugin);
        this.requirements = new Requirements(plugin);
        this.multipliers = new Multipliers(plugin);
        this.statLeveler = new StatLeveler(plugin);
        this.storedArmor = new HashMap<>();
        if (OptionL.getBoolean(Option.MODIFIER_ARMOR_TIMER_ENABLED)) {
            startTimer();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerDataLoadEvent event) {
        Player player = event.getPlayerData().getPlayer();
        PlayerData playerData = event.getPlayerData();
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null) {
                if (OptionL.getBoolean(Option.MODIFIER_ARMOR_TIMER_ENABLED)) {
                    storedArmor.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(ArmorType.matchType(armor), armor.clone());
                }
                if (!armor.getType().equals(Material.AIR)) {
                    if (requirements.meetsRequirements(ModifierType.ARMOR, armor, player)) {
                        for (StatModifier modifier : modifiers.getModifiers(ModifierType.ARMOR, armor)) {
                            playerData.addStatModifier(modifier, false);
                        }
                        for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ARMOR, armor)) {
                            playerData.addMultiplier(multiplier);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEquip(ArmorEquipEvent event) {
        if (OptionL.getBoolean(Option.MODIFIER_ARMOR_TIMER_ENABLED)) return; // Don't use if timer is enabled
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            // Equip
            ItemStack armorPiece = event.getNewArmorPiece();
            if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                if (requirements.meetsRequirements(ModifierType.ARMOR, armorPiece, player)) {
                    for (StatModifier modifier : modifiers.getModifiers(ModifierType.ARMOR, armorPiece)) {
                        playerData.addStatModifier(modifier);
                    }
                    for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ARMOR, armorPiece)) {
                        playerData.addMultiplier(multiplier);
                    }
                }
            }
            // Un-equip
            armorPiece = event.getOldArmorPiece();
            if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                for (StatModifier modifier : modifiers.getModifiers(ModifierType.ARMOR, armorPiece)) {
                    playerData.removeStatModifier(modifier.getName());
                }
                for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ARMOR, armorPiece)) {
                    playerData.removeMultiplier(multiplier.getName());
                }
            }
        }
    }

    // Timer based detection
    private void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    // Get the stored armor player has or create if it doesn't exist
                    Map<ArmorType, ItemStack> playerStoredArmor = storedArmor.computeIfAbsent(uuid, k -> new HashMap<>());
                    for (ArmorType armorType : ArmorType.values()) { // Go through each armor slot
                        ItemStack stored = playerStoredArmor.get(armorType); // Get the stored item in the slot
                        ItemStack wearing = player.getInventory().getItem(armorType.getEquipmentSlot()); // Get the armor player is currently wearing

                        boolean remove = true;
                        if (stored == null) {
                            remove = false;
                        } else if (stored.equals(wearing)) { // Don't check if stored and wearing are the same item
                            continue;
                        }
                        assert (null != stored);

                        Set<Stat> statsToReload = new HashSet<>();
                        // Remove modifiers and multipliers that are on stored item from player
                        if (remove && stored.getType() != Material.AIR) {
                            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                            if (playerData == null) continue;

                            for (StatModifier modifier : modifiers.getModifiers(ModifierType.ARMOR, stored)) {
                                playerData.removeStatModifier(modifier.getName(), false);
                                statsToReload.add(modifier.getStat());
                            }
                            for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ARMOR, stored)) {
                                playerData.removeMultiplier(multiplier.getName());
                            }
                        }
                        // Add modifiers and multipliers that are on worn item to the player
                        if (wearing != null && wearing.getType() != Material.AIR) {
                            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                            if (playerData == null) continue;

                            if (requirements.meetsRequirements(ModifierType.ARMOR, wearing, player)) {
                                for (StatModifier modifier : modifiers.getModifiers(ModifierType.ARMOR, wearing)) {
                                    playerData.addStatModifier(modifier, false);
                                    statsToReload.add(modifier.getStat());
                                }
                                for (Multiplier multiplier : multipliers.getMultipliers(ModifierType.ARMOR, wearing)) {
                                    playerData.addMultiplier(multiplier);
                                }
                            }
                        }
                        for (Stat stat : statsToReload) {
                            statLeveler.reloadStat(player, stat);
                        }
                        // Set stored item to worn item
                        if (wearing != null) {
                            playerStoredArmor.put(armorType, wearing.clone());
                        } else {
                            playerStoredArmor.put(armorType, new ItemStack(Material.AIR));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, OptionL.getInt(Option.MODIFIER_ARMOR_TIMER_CHECK_PERIOD));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        storedArmor.remove(event.getPlayer().getUniqueId());
    }

}
