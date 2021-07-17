package com.archyx.aureliumskills.skills.farming;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skills;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FarmingAbilities extends AbilityProvider implements Listener {

	private static final Random r = new Random();

	public FarmingAbilities(AureliumSkills plugin) {
		super(plugin, Skills.FARMING);
	}

	public void bountifulHarvest(Player player, Block block) {
		if (OptionL.isEnabled(Skills.FARMING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.BOUNTIFUL_HARVEST)) {
				if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
					if (playerData == null) return;
					if (playerData.getAbilityLevel(Ability.BOUNTIFUL_HARVEST) > 0) {
						if (r.nextDouble() < (getValue(Ability.BOUNTIFUL_HARVEST, playerData)) / 100) {
							for (ItemStack item : block.getDrops()) {
								checkMelonSilkTouch(player, block, item);
								PlayerLootDropEvent event = new PlayerLootDropEvent(player, item.clone(), block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.BOUNTIFUL_HARVEST);
								Bukkit.getPluginManager().callEvent(event);
								if (!event.isCancelled()) {
									block.getWorld().dropItem(event.getLocation(), event.getItemStack());
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void tripleHarvest(Player player, Block block) {
		if (OptionL.isEnabled(Skills.FARMING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.TRIPLE_HARVEST)) {
				if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
					if (playerData == null) return;
					if (playerData.getAbilityLevel(Ability.TRIPLE_HARVEST) > 0) {
						if (r.nextDouble() < (getValue(Ability.TRIPLE_HARVEST, playerData)) / 100) {
							for (ItemStack item : block.getDrops()) {
								checkMelonSilkTouch(player, block, item);
								ItemStack droppedItem = item.clone();
								droppedItem.setAmount(2);
								PlayerLootDropEvent event = new PlayerLootDropEvent(player, droppedItem, block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.TRIPLE_HARVEST);
								Bukkit.getPluginManager().callEvent(event);
								if (!event.isCancelled()) {
									block.getWorld().dropItem(event.getLocation(), event.getItemStack());
								}
							}
						}
					}
				}
			}
		}
	}

	private void checkMelonSilkTouch(Player player, Block block, ItemStack item) {
		if (block.getType() == XMaterial.MELON.parseMaterial()) {
			if (player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
				Material melon = XMaterial.MELON.parseMaterial();
				if (melon != null) {
					item.setType(melon);
					item.setAmount(1);
				}
			}
		}
	}

	@EventHandler
	public void geneticist(PlayerItemConsumeEvent event) {
		if (blockDisabled(Ability.GENETICIST)) return;
		Player player = event.getPlayer();
		if (blockAbility(player)) return;
		Material mat = event.getItem().getType();
		if (mat.equals(Material.BREAD) || mat.equals(Material.APPLE) || mat.equals(Material.GOLDEN_APPLE) || mat.equals(XMaterial.POTATO.parseMaterial())
				|| mat.equals(Material.BAKED_POTATO) || mat.equals(XMaterial.CARROT.parseMaterial()) || mat.equals(Material.GOLDEN_CARROT) || mat.equals(Material.MELON)
				|| mat.equals(Material.PUMPKIN_PIE) || mat.equals(Material.BEETROOT) || mat.equals(Material.BEETROOT_SOUP) || mat.equals(XMaterial.MUSHROOM_STEW.parseMaterial())
				|| mat.equals(Material.POISONOUS_POTATO)) {
			PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
			if (playerData == null) return;
			float amount = (float) getValue(Ability.GENETICIST, playerData) / 10;
			player.setSaturation(player.getSaturation() + amount);
		}
	}

	public void scytheMaster(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
		if (blockDisabled(Ability.SCYTHE_MASTER)) return;
			//Check permission
			if (!player.hasPermission("aureliumskills.farming")) {
				return;
			}
			if (playerData.getAbilityLevel(Ability.SCYTHE_MASTER) > 0) {
				event.setDamage(event.getDamage() * (1 + (getValue(Ability.SCYTHE_MASTER, playerData) / 100)));
			}
	}
}
