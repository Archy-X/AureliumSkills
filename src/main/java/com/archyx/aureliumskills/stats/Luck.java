package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.support.WorldGuardFlags;
import com.archyx.aureliumskills.support.WorldGuardSupport;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class Luck implements Listener {

	private final Random r = new Random();
	private final AureliumSkills plugin;

	public Luck(AureliumSkills plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(@NotNull PlayerDataLoadEvent event) {
		setLuck(event.getPlayerData().getPlayer());
	}
	
	public void reload(@Nullable Player player) {
		if (player != null) {
			setLuck(player);
		}
	}

	@EventHandler
	public void worldChange(@NotNull PlayerChangedWorldEvent event) {
		setLuck(event.getPlayer());
	}

	private void setLuck(@NotNull Player player) {
		AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_LUCK);
		if (attribute != null) {
			boolean hasModifier = false;
			//Removes existing modifiers of the same name
			for (AttributeModifier am : attribute.getModifiers()) {
				if (am.getName().equals("AureliumSkills-Luck")) {
					attribute.removeModifier(am);
					hasModifier = true;
				}
			}
			if (!hasModifier) {
				attribute.setBaseValue(0.0);
			}
			if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
				return;
			}
			PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
			if (playerData != null) {
				double luck = playerData.getStatLevel(Stats.LUCK) * OptionL.getDouble(Option.LUCK_MODIFIER);
				attribute.addModifier(new AttributeModifier("AureliumSkills-Luck", luck, AttributeModifier.Operation.ADD_NUMBER));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	@SuppressWarnings("deprecation")
	public void onBlockBreak(@NotNull BlockBreakEvent event) {
		if (OptionL.getBoolean(Option.LUCK_DOUBLE_DROP_ENABLED) && !event.isCancelled()) {
			Player player = event.getPlayer();
			Block block = event.getBlock();
			//Checks if in blocked or disabled world
			if (plugin.getWorldManager().isInBlockedWorld(block.getLocation())) {
				return;
			}
			//Checks if in blocked region
            WorldGuardSupport worldGuardSupport = plugin.getWorldGuardSupport();
            if (plugin.isWorldGuardEnabled() && worldGuardSupport != null) {
				if (worldGuardSupport.isInBlockedRegion(block.getLocation())) {
					return;
				}
				// Check if blocked by flags
				else if (worldGuardSupport.blockedByFlag(block.getLocation(), player, WorldGuardFlags.FlagKey.XP_GAIN)) {
					return;
				}
			}
			if (!event.isDropItems()) {
				return;
			}
			if (player.getGameMode().equals(GameMode.SURVIVAL)) {
				if (plugin.getRegionManager().isPlacedBlock(block)) return;
				PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
				if (playerData == null) return;
				Material mat = block.getType();
				if (mat.equals(Material.STONE) || mat.equals(Material.COBBLESTONE) || mat.equals(Material.SAND) || mat.equals(Material.GRAVEL)
						|| mat.equals(Material.DIRT) || mat.equals(XMaterial.GRASS_BLOCK.parseMaterial()) || mat.equals(XMaterial.ANDESITE.parseMaterial())
						|| mat.equals(XMaterial.DIORITE.parseMaterial()) || mat.equals(XMaterial.GRANITE.parseMaterial())) {
					//Calculate chance
					double chance = playerData.getStatLevel(Stats.LUCK) * OptionL.getDouble(Option.LUCK_DOUBLE_DROP_MODIFIER);
					if (chance * 100 > OptionL.getDouble(Option.LUCK_DOUBLE_DROP_PERCENT_MAX)) {
						chance = OptionL.getDouble(Option.LUCK_DOUBLE_DROP_PERCENT_MAX);
					}
					if (r.nextDouble() < chance) {
						ItemStack tool = player.getInventory().getItemInMainHand();
						for (ItemStack item : block.getDrops(tool)) {
							//If silk touch
							if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
								if (mat.equals(Material.STONE)) {
									if (!XMaterial.isNewVersion()) {
										if (block.getData() == 0) {
											PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, new ItemStack(Material.STONE), block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.LUCK_DOUBLE_DROP);
											Bukkit.getPluginManager().callEvent(dropEvent);
											if (!event.isCancelled()) {
												block.getWorld().dropItem(dropEvent.getLocation(), dropEvent.getItemStack());
											}
										}
									}
									else {
										PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, new ItemStack(Material.STONE), block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.LUCK_DOUBLE_DROP);
										Bukkit.getPluginManager().callEvent(dropEvent);
										if (!event.isCancelled()) {
											block.getWorld().dropItem(dropEvent.getLocation(), dropEvent.getItemStack());
										}
									}
								}
								else {
									@Nullable Material grassBlock = XMaterial.GRASS_BLOCK.parseMaterial();
									if (grassBlock == null)
										return;
									if (mat.equals(grassBlock)) {
										PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, new ItemStack(grassBlock), block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.LUCK_DOUBLE_DROP);
										Bukkit.getPluginManager().callEvent(dropEvent);
										if (!event.isCancelled()) {
											block.getWorld().dropItem(dropEvent.getLocation(), dropEvent.getItemStack());
										}
									}
								}
							}
							//Drop regular item if not silk touch
							else {
								PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, item.clone(), block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.LUCK_DOUBLE_DROP);
								Bukkit.getPluginManager().callEvent(dropEvent);
								if (!event.isCancelled()) {
									block.getWorld().dropItem(dropEvent.getLocation(), dropEvent.getItemStack());
								}
							}
						}
					}
				}
			}
		}
	}
	
}
