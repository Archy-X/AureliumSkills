package com.archyx.aureliumskills.util.item;

import com.archyx.aureliumskills.modifier.ModifierType;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ItemUtils {

	public static boolean isArmor(Material material) {
		String materialName = material.name().toLowerCase(Locale.ENGLISH);
		return materialName.contains("helmet") || materialName.contains("chestplate") || materialName.contains("leggings") || materialName.contains("boots") || materialName.equals("elytra");
	}

	public static boolean isWeapon(Material material) {
		String materialName = material.name().toLowerCase(Locale.ENGLISH);
		return materialName.contains("sword") || materialName.equals("bow") || materialName.equals("trident") || materialName.equals("crossbow");
	}

	public static boolean isTool(Material material) {
		String materialName = material.name().toLowerCase(Locale.ENGLISH);
		return materialName.contains("pickaxe") || materialName.contains("axe") || materialName.contains("hoe") || materialName.contains("shovel") || materialName.contains("spade")
				|| materialName.equals("shears") || materialName.equals("fishing_rod") || materialName.equals("flint_and_steel") || materialName.equals("shield")
				|| materialName.contains("on_a_stick");
	}

	public static boolean isAxe(Material material) {
		String materialName = material.name().toLowerCase(Locale.ENGLISH);
		return materialName.contains("_axe");
	}

	public static boolean isPickaxe(Material material) {
		return material.name().toLowerCase(Locale.ENGLISH).contains("pickaxe");
	}

	public static boolean isDurable(Material material) {
		return isArmor(material) || isWeapon(material) || isTool(material);
	}

	public static List<String> formatLore(List<String> input) {
		List<String> lore = new ArrayList<>();
		for (String entry : input) {
			lore.addAll(Arrays.asList(entry.split("(\\u005C\\u006E)|(\\n)")));
		}
		return lore;
	}

	public static NBTCompound getCompound(NBTCompound root, String name) {
		NBTCompound compound = root.getCompound(name);
		if (compound == null) {
			compound = root.addCompound(name);
		}
		return compound;
	}

	public static NBTCompound getRootCompound(NBTItem item) {
		NBTCompound compound = item.getCompound("AureliumSkills");
		if (compound == null) {
			compound = item.addCompound("AureliumSkills");
		}
		return compound;
	}

	public static NBTCompound getModifiersCompound(NBTItem item) {
		return getCompound(getRootCompound(item), "Modifiers");
	}

	public static NBTCompound getModifiersTypeCompound(NBTItem item, ModifierType type) {
		return getCompound(getModifiersCompound(item), TextUtil.capitalize(type.name().toLowerCase(Locale.ROOT)));
	}

	public static NBTCompound getRequirementsCompound(NBTItem item) {
		return getCompound(getRootCompound(item), "Requirements");
	}

	public static NBTCompound getRequirementsTypeCompound(NBTItem item, ModifierType type) {
		return getCompound(getRequirementsCompound(item), TextUtil.capitalize(type.name().toLowerCase(Locale.ROOT)));
	}

	public static NBTCompound getMultipliersCompound(NBTItem item) {
		return getCompound(getRootCompound(item), "Multipliers");
	}

	public static NBTCompound getMultipliersTypeCompound(NBTItem item, ModifierType type) {
		return getCompound(getMultipliersCompound(item), TextUtil.capitalize(type.name().toLowerCase(Locale.ROOT)));
	}

	public static void removeParentCompounds(NBTCompound compound) {
		if (compound.getKeys().size() == 0) {
			NBTCompound parent = compound.getParent();
			parent.removeKey(compound.getName());
			if (parent.getKeys().size() == 0) {
				parent.getParent().removeKey(parent.getName());
				if (parent.getParent().getKeys().size() == 0) {
					parent.getParent().getParent().removeKey(parent.getParent().getName());
				}
			}
		}
	}

	public static boolean isInventoryFull(Player player) {
		for (ItemStack item : player.getInventory().getStorageContents()) {
			if (item == null || item.getType() == Material.AIR) {
				return false;
			}
		}
		return true;
	}

	public static ItemStack addItemToInventory(Player player, ItemStack item) {
		PlayerInventory inventory = player.getInventory();
		int amountRemaining = item.getAmount();
		for (int slot = 0; slot < 36; slot++) {
			ItemStack slotItem = inventory.getItem(slot);
			if (amountRemaining > 0) {
				if (slotItem == null || slotItem.getType() == Material.AIR) {
					if (amountRemaining > item.getMaxStackSize() && item.getMaxStackSize() != -1) {
						ItemStack maxStackItem = item.clone();
						maxStackItem.setAmount(item.getMaxStackSize());
						inventory.setItem(slot, maxStackItem);
						amountRemaining -= item.getMaxStackSize();
					} else {
						ItemStack addedItem = item.clone();
						addedItem.setAmount(amountRemaining);
						inventory.setItem(slot, addedItem);
						amountRemaining = 0;
					}
				} else if (slotItem.isSimilar(item)) {
					int amountAdded = Math.min(amountRemaining, slotItem.getMaxStackSize() - slotItem.getAmount());
					slotItem.setAmount(slotItem.getAmount() + amountAdded);
					amountRemaining -= amountAdded;
				}
			}
		}
		if (amountRemaining > 0) {
			ItemStack leftoverItem = item.clone();
			leftoverItem.setAmount(amountRemaining);
			return leftoverItem;
		}
		return null;
	}

	public static boolean canAddItemToInventory(Player player, ItemStack item) {
		PlayerInventory inventory = player.getInventory();
		int amountRemaining = item.getAmount();
		for (int slot = 0; slot < 36; slot++) {
			ItemStack slotItem = inventory.getItem(slot);
			if (amountRemaining > 0) {
				if (slotItem == null || slotItem.getType() == Material.AIR) {
					if (amountRemaining > item.getMaxStackSize() && item.getMaxStackSize() != -1) {
						amountRemaining -= item.getMaxStackSize();
					} else {
						return true;
					}
				} else if (slotItem.isSimilar(item)) {
					int amountCanAdd = Math.min(amountRemaining, slotItem.getMaxStackSize() - slotItem.getAmount());
					amountRemaining -= amountCanAdd;
				}
			}
		}
		return amountRemaining <= 0;
	}

	public static ItemStack parseItem(String name) {
		Material material = Material.getMaterial(name);
		if (material != null) {
			return new ItemStack(material);
		}
		Optional<XMaterial> materialOptional = XMaterial.matchXMaterial(name);
		return materialOptional.map(XMaterial::parseItem).orElse(null);
	}

} 
