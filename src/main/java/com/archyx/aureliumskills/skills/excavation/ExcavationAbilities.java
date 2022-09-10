package com.archyx.aureliumskills.skills.excavation;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.version.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ExcavationAbilities extends AbilityProvider implements Listener {

	private final Random r = new Random();

	public ExcavationAbilities(@NotNull AureliumSkills plugin) {
		super(plugin, Skills.EXCAVATION);
	}

	public void spadeMaster(@NotNull EntityDamageByEntityEvent event, @NotNull Player player, @NotNull PlayerData playerData) {
		if (OptionL.isEnabled(Skills.EXCAVATION)) {
			if (plugin.getAbilityManager().isEnabled(Ability.SPADE_MASTER)) {
				//Check permission
				if (!player.hasPermission("aureliumskills.excavation")) {
					return;
				}
				if (playerData.getAbilityLevel(Ability.SPADE_MASTER) > 0) {
					event.setDamage(event.getDamage() * (1 + (getValue(Ability.SPADE_MASTER, playerData) / 100)));
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void biggerScoop(@NotNull ExcavationSource source, @NotNull Block block, @NotNull Player player) {
		if (!plugin.getAbilityManager().isEnabled(Ability.BIGGER_SCOOP)) return;
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData == null) return;
		if (player.getGameMode() != GameMode.SURVIVAL) return;
		if (r.nextDouble() < (getValue(Ability.BIGGER_SCOOP, playerData) / 100)) {
			ItemStack tool = player.getInventory().getItemInMainHand();
			Material mat =  block.getType();
			for (ItemStack item : block.getDrops(tool)) {
				// If silk touch
				if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
					PlayerLootDropEvent event;
					Location loc = block.getLocation().add(0.5, 0.5, 0.5);
					if (source.getLegacyData() == -1) {
						event = new PlayerLootDropEvent(player, new ItemStack(mat, 2), loc, LootDropCause.BIGGER_SCOOP);
					} else {
						if (VersionUtils.isAtLeastVersion(13)) {
							event = new PlayerLootDropEvent(player, new ItemStack(mat, 2), loc, LootDropCause.BIGGER_SCOOP);
						} else {
							event = new PlayerLootDropEvent(player, new ItemStack(mat, 2, source.getLegacyData()), loc, LootDropCause.BIGGER_SCOOP);
						}
					}
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						block.getWorld().dropItem(event.getLocation(), event.getItemStack());
					}
				}
				// Drop regular item if not silk touch
				else {
					ItemStack drop = item.clone();
					drop.setAmount(2);
					PlayerLootDropEvent event = new PlayerLootDropEvent(player, drop, block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.BIGGER_SCOOP);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						block.getWorld().dropItem(event.getLocation(), event.getItemStack());
					}
				}
			}
		}
	}
}
