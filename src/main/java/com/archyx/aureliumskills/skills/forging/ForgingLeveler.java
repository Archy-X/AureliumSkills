package com.archyx.aureliumskills.skills.forging;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ForgingLeveler extends SkillLeveler implements Listener {

	public ForgingLeveler(@NotNull AureliumSkills plugin) {
		super(plugin, Ability.FORGER);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onForge(@NotNull InventoryClickEvent event) {
		if (OptionL.isEnabled(Skills.FORGING)) {
			//Check cancelled
			if (OptionL.getBoolean(Option.FORGING_CHECK_CANCELLED)) {
				if (event.isCancelled()) {
					return;
				}
			}
			Inventory inventory = event.getClickedInventory();
			if (inventory != null) {
				if (!(event.getWhoClicked() instanceof Player)) return;
				Player player = (Player) event.getWhoClicked();
				ClickType click = event.getClick();
				// Only allow right and left clicks if inventory full
				if (click != ClickType.LEFT && click != ClickType.RIGHT && ItemUtils.isInventoryFull(player)) return;
				if (event.getResult() != Event.Result.ALLOW) return; // Make sure the click was successful
				if (player.getItemOnCursor().getType() != Material.AIR) return; // Make sure cursor is empty
				InventoryAction action = event.getAction();
				// Only give if item was picked up
				if (action != InventoryAction.PICKUP_ALL && action != InventoryAction.MOVE_TO_OTHER_INVENTORY
						&& action != InventoryAction.PICKUP_HALF && action != InventoryAction.DROP_ALL_SLOT
						&& action != InventoryAction.DROP_ONE_SLOT && action != InventoryAction.HOTBAR_SWAP) {
					return;
				}
				if (inventory.getType().equals(InventoryType.ANVIL)) {
					if (event.getSlot() == 2) {
						ItemStack addedItem = inventory.getItem(1);
						ItemStack baseItem = inventory.getItem(0);
						@Nullable Location location = inventory.getLocation();
						if (location != null) {
							if (blockXpGainLocation(location, player)) return;
						} else {
							if (blockXpGainLocation(event.getWhoClicked().getLocation(), player)) return;
						}
						if (blockXpGainPlayer(player)) return;
						Skill s = Skills.FORGING;
						AnvilInventory anvil = (AnvilInventory) inventory;
						if (addedItem != null && baseItem != null) {
							if (addedItem.getType().equals(Material.ENCHANTED_BOOK)) {
								if (ItemUtils.isArmor(baseItem.getType())) {
									plugin.getLeveler().addXp(player, s, anvil.getRepairCost() * getXp(player, ForgingSource.COMBINE_ARMOR_PER_LEVEL));
								} else if (ItemUtils.isWeapon(baseItem.getType())) {
									plugin.getLeveler().addXp(player, s, anvil.getRepairCost() * getXp(player, ForgingSource.COMBINE_WEAPON_PER_LEVEL));
								} else if (baseItem.getType().equals(Material.ENCHANTED_BOOK)) {
									plugin.getLeveler().addXp(player, s, anvil.getRepairCost() * getXp(player, ForgingSource.COMBINE_BOOKS_PER_LEVEL));
								} else {
									plugin.getLeveler().addXp(player, s, anvil.getRepairCost() * getXp(player, ForgingSource.COMBINE_TOOL_PER_LEVEL));
								}
							}
						}

					}
				} else if (inventory.getType().toString().equals("GRINDSTONE")) {
					if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
					@Nullable Location location = inventory.getLocation();
					if (location != null) {
						if (blockXpGainLocation(location, player)) return;
					} else {
						if (blockXpGainLocation(event.getWhoClicked().getLocation(), player)) return;
					}
					if (blockXpGainPlayer(player)) return;
					// Calculate total level
					int totalLevel = 0;
					ItemStack topItem = inventory.getItem(0); // Get item in top slot
					totalLevel += getTotalLevel(topItem);
					ItemStack bottomItem = inventory.getItem(1); // Get item in bottom slot
					totalLevel += getTotalLevel(bottomItem);
					plugin.getLeveler().addXp(player, Skills.FORGING, totalLevel * getXp(player, ForgingSource.GRINDSTONE_PER_LEVEL));
				}
			}
		}
	}

	private int getTotalLevel(@Nullable ItemStack item) {
		int totalLevel = 0;
		if (item != null) {
			for (Map.Entry<@NotNull Enchantment, @NotNull Integer> entry : item.getEnchantments().entrySet()) {
				if (isDisenchantable(entry.getKey())) {
					totalLevel += entry.getValue();
				}
			}
			if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
				@Nullable EnchantmentStorageMeta esm = (EnchantmentStorageMeta) item.getItemMeta();
				if (esm != null)
					for (Map.Entry<@NotNull Enchantment, @NotNull Integer> entry : esm.getStoredEnchants().entrySet()) {
						if (isDisenchantable(entry.getKey())) {
							totalLevel += entry.getValue();
						}
					}
			}
		}
		return totalLevel;
	}

	public boolean isDisenchantable(@NotNull Enchantment enchant) {
		// Block vanilla curses
		if (enchant.equals(Enchantment.BINDING_CURSE) || enchant.equals(Enchantment.VANISHING_CURSE)) {
			return false;
		}
		// Check blocked list in config
		List<@NotNull String> blockedList = OptionL.getList(Option.FORGING_BLOCKED_GRINDSTONE_ENCHANTS);
		for (String blockedEnchantName : blockedList) {
			if (enchant.getKey().getKey().equalsIgnoreCase(blockedEnchantName)) {
				return false;
			}
		}
		return true;
	}

}
