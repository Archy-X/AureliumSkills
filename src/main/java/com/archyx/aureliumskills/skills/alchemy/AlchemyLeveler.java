package com.archyx.aureliumskills.skills.alchemy;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.leveler.Leveler;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.region.BlockPosition;
import com.archyx.aureliumskills.skills.Skills;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlchemyLeveler extends SkillLeveler implements Listener {

	private final @NotNull Map<BlockPosition, BrewingStandData> brewingStands;

	public AlchemyLeveler(@NotNull AureliumSkills plugin) {
		super(plugin, Ability.BREWER);
		this.brewingStands = new HashMap<>();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBrew(@NotNull BrewEvent event) {
		if (!OptionL.isEnabled(Skills.ALCHEMY)) return;
		// Check cancelled
		if (OptionL.getBoolean(Option.ALCHEMY_CHECK_CANCELLED)) {
			if (event.isCancelled()) {
				return;
			}
		}
		if (OptionL.getBoolean(Option.ALCHEMY_GIVE_XP_ON_TAKEOUT)) {
			checkBrewedSlots(event);
		} else {
			if (event.getBlock().hasMetadata("skillsBrewingStandOwner")) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(event.getBlock().getMetadata("skillsBrewingStandOwner").get(0).asString()));
				if (offlinePlayer.isOnline()) {
					if (event.getContents().getIngredient() != null) {
						Player player = offlinePlayer.getPlayer();
						if (player != null) {
							if (blockXpGainLocation(event.getBlock().getLocation(), player)) return;
							if (blockXpGainPlayer(player)) return;
							@Nullable ItemStack item = event.getContents().getIngredient();
							if (item != null)
								addAlchemyXp(player, item.getType());
						}
					}
				}
			}
		}
	}

	private void addAlchemyXp(@NotNull Player player, @NotNull Material mat) {
		Leveler leveler = plugin.getLeveler();
		if (mat.equals(Material.REDSTONE)) {
			leveler.addXp(player, Skills.ALCHEMY, getXp(player, AlchemySource.EXTENDED));
		} else if (mat.equals(Material.GLOWSTONE_DUST)) {
			leveler.addXp(player, Skills.ALCHEMY, getXp(player, AlchemySource.UPGRADED));
		} else if (mat.equals(XMaterial.NETHER_WART.parseMaterial())) {
			leveler.addXp(player, Skills.ALCHEMY, getXp(player, AlchemySource.AWKWARD));
		} else if (mat.equals(XMaterial.GUNPOWDER.parseMaterial())) {
			leveler.addXp(player, Skills.ALCHEMY, getXp(player, AlchemySource.SPLASH));
		} else if (mat.equals(XMaterial.DRAGON_BREATH.parseMaterial())) {
			leveler.addXp(player, Skills.ALCHEMY, getXp(player, AlchemySource.LINGERING));
		} else {
			leveler.addXp(player, Skills.ALCHEMY, getXp(player, AlchemySource.REGULAR));
		}
	}

	private void checkBrewedSlots(@NotNull BrewEvent event) {
		BrewerInventory before = event.getContents();
		ItemStack ingredient = before.getIngredient();
		if (ingredient == null) return;
		ItemStack clonedIngredient = ingredient.clone();
		ItemStack[] beforeItems = Arrays.copyOf(before.getContents(), 3); // Items in result slots before
		new BukkitRunnable() {
			@Override
			public void run() {
				BlockState blockState = event.getBlock().getState();
				if (blockState instanceof BrewingStand) {
					BrewingStand brewingStand = (BrewingStand) blockState;
					BrewerInventory after = brewingStand.getInventory();
					ItemStack[] afterItems = Arrays.copyOf(after.getContents(), 3); // Items in result slots after
					BrewingStandData standData = new BrewingStandData(clonedIngredient);
					// Set the items that changed as brewed
					for (int i = 0; i < 3; i++) {
						ItemStack beforeItem = beforeItems[i];
						ItemStack afterItem = afterItems[i];
						if (beforeItem != null && beforeItem.getType() != Material.AIR && afterItem != null && afterItem.getType() != Material.AIR) {
							if (!beforeItem.equals(afterItem)) {
								standData.setSlotBrewed(i, true);
							}
						}
					}
					brewingStands.put(BlockPosition.fromBlock(event.getBlock()), standData); // Register the stand data
				}
			}
		}.runTaskLater(plugin, 1);
	}
	
	@EventHandler
	public void onBlockPlace(@NotNull BlockPlaceEvent event) {
		if (event.getBlock().getType().equals(Material.BREWING_STAND)) {
			if (OptionL.isEnabled(Skills.ALCHEMY)) {
				event.getBlock().setMetadata("skillsBrewingStandOwner", new FixedMetadataValue(plugin, event.getPlayer().getUniqueId()));
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(@NotNull BlockBreakEvent event) {
		if (event.getBlock().getType().equals(Material.BREWING_STAND)) {
			if (OptionL.isEnabled(Skills.ALCHEMY)) {
				if (event.getBlock().hasMetadata("skillsBrewingStandOwner")) {
					event.getBlock().removeMetadata("skillsBrewingStandOwner", plugin);
				}
			}
			brewingStands.remove(BlockPosition.fromBlock(event.getBlock()));
		}
	}
	
	@EventHandler
	public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
		if (event.getInventory().getType().equals(InventoryType.BREWING)) {
			if (OptionL.isEnabled(Skills.ALCHEMY)) {
				if (event.getInventory().getHolder() != null) {
					if (event.getInventory().getLocation() != null) {
						@Nullable Location location = event.getInventory().getLocation();
						if (location == null)
							return;
						Block block = location.getBlock();
						if (!block.hasMetadata("skillsBrewingStandOwner")) {
							block.setMetadata("skillsBrewingStandOwner", new FixedMetadataValue(plugin, event.getPlayer().getUniqueId()));
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onTakePotionOut(@NotNull InventoryClickEvent event) {
		if (!OptionL.isEnabled(Skills.ALCHEMY)) return;
		if (!OptionL.getBoolean(Option.ALCHEMY_GIVE_XP_ON_TAKEOUT)) return;
		// Check cancelled
		if (OptionL.getBoolean(Option.ALCHEMY_CHECK_CANCELLED)) {
			if (event.isCancelled()) {
				return;
			}
		}
		Inventory inventory = event.getClickedInventory();
		if (inventory == null) return;
		if (inventory.getType() != InventoryType.BREWING && !(inventory instanceof BrewerInventory)) return;

		int slot = event.getSlot();
		if (slot > 2) return; // Slots 0-2 are result slots

		InventoryAction action = event.getAction();
		// Filter out other actions
		if (action != InventoryAction.PICKUP_ALL && action != InventoryAction.PICKUP_HALF && action != InventoryAction.PICKUP_SOME
				&& action != InventoryAction.PICKUP_ONE && action != InventoryAction.MOVE_TO_OTHER_INVENTORY && action != InventoryAction.HOTBAR_SWAP
				&& action != InventoryAction.HOTBAR_MOVE_AND_READD) {
			return;
		}
		ItemStack item = event.getCurrentItem();
		if (item == null) return;
		// Get the brewing stand data
		Location location = inventory.getLocation();
		if (location == null) return;
		BrewingStandData standData = brewingStands.get(BlockPosition.fromBlock(location.getBlock()));
		if (standData == null) return;

		if (!(event.getWhoClicked() instanceof Player)) return;
		Player player = (Player) event.getWhoClicked();
		if (blockXpGainLocation(location, player)) return;
		if (blockXpGainPlayer(player)) return;

		if (!standData.isSlotBrewed(slot)) return; // Check that the slot was brewed

		ItemStack ingredient = standData.getIngredient();
		if (ingredient == null) return;
		if (ingredient.getType() == Material.AIR) return;
		addAlchemyXp(player, ingredient.getType()); // Add XP
		standData.setSlotBrewed(slot, false); // Set data to false
	}
}
