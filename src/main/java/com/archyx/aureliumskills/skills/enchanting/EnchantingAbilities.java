package com.archyx.aureliumskills.skills.enchanting;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.XpGainEvent;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stats;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

public class EnchantingAbilities extends AbilityProvider implements Listener {

    private final Random random = new Random();

    public EnchantingAbilities(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.ENCHANTING);
        enchantedStrength();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void xpConvert(@NotNull XpGainEvent event) {
        if (event.isCancelled()) return;
        if (blockDisabled(Ability.XP_CONVERT)) return;
        Player player = event.getPlayer();
        if (blockAbility(player)) return;
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            if (playerData.getAbilityLevel(Ability.XP_CONVERT) > 0 && event.getAmount() > 0) {
                double totalXp = playerData.getAbilityData(Ability.XP_CONVERT).getDouble("xp") + event.getAmount();
                double value =  getValue(Ability.XP_CONVERT, playerData);
                if (value > 0) {
                    int added = (int) (totalXp / value);
                    double remainder = totalXp - added * value;
                    player.giveExp(added);
                    playerData.getAbilityData(Ability.XP_CONVERT).setData("xp", remainder);
                }
            }
        }
    }

    @EventHandler
    public void xpWarrior(@NotNull EntityDeathEvent event) {
        if (blockDisabled(Ability.XP_WARRIOR)) return;
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() != null) {
            @Nullable Player player = entity.getKiller();
            if (player == null)
                return;
            if (blockAbility(player))
                return;
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                if (playerData.getAbilityLevel(Ability.XP_WARRIOR) > 0 && event.getDroppedExp() > 0) {
                    if (random.nextDouble() < getValue(Ability.XP_WARRIOR, playerData) / 100) {
                        event.setDroppedExp(event.getDroppedExp() * 2);
                    }
                }
            }
        }
    }

    private void enchantedStrength() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (blockDisabled(Ability.ENCHANTED_STRENGTH)) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData != null) {
                        if (playerData.getAbilityLevel(Ability.ENCHANTED_STRENGTH) > 0) {
                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (item.getEnchantments().size() > 0) {
                                if (!blockAbility(player)) {
                                    // Apply modifier
                                    double strengthPerType = getValue(Ability.ENCHANTED_STRENGTH, playerData);
                                    StatModifier modifier = new StatModifier("AbilityModifier-EnchantedStrength", Stats.STRENGTH, strengthPerType * item.getEnchantments().size());
                                    playerData.addStatModifier(modifier, false);
                                }
                            } else {
                                playerData.removeStatModifier("AbilityModifier-EnchantedStrength");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void luckyTable(@NotNull EnchantItemEvent event) {
        if (event.isCancelled()) return;
        if (blockDisabled(Ability.LUCKY_TABLE)) return;
        Player player = event.getEnchanter();
        if (blockAbility(player)) return;
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        if (playerData.getAbilityLevel(Ability.LUCKY_TABLE) > 0) {
            for (Map.Entry<Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
                if (entry.getKey().getMaxLevel() > entry.getValue()) {
                    if (random.nextDouble() < getValue(Ability.LUCKY_TABLE, playerData) / 100) {
                        entry.setValue(entry.getValue() + 1);
                    }
                }
            }
        }
    }

}
