package com.archyx.aureliumskills.util.armor;

import com.archyx.aureliumskills.util.armor.ArmorEquipEvent.EquipMethod;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Arnah
 * @since Jul 30, 2015
 */
public class ArmorListener implements Listener{

    private final List<String> blockedMaterials;

    private static final String[] defBlocked = new String[] {
            "CHEST", "TRAPPED_CHEST", "ENDER_CHEST",
            "FURNACE", "WORKBENCH", "CRAFTING_TABLE",
            "ANVIL", "ENCHANTING_TABLE", "ENCHANTMENT_TABLE",
            "*SHULKER_BOX", "*BED", "BED_BLOCK",
            "NOTE_BLOCK", "BREWING_STAND", "HOPPER",
            "DISPENSER", "DROPPER", "*BUTTON",
            "REPEATER", "?DIODE", "LEVER",
            "?COMPARATOR", "*DOOR!?IRON_DOOR", "*FENCE_GATE",
            "?DAYLIGHT_DETECTOR", "BEACON", "?COMMAND",
            "CARTOGRAPHY_TABLE", "LECTERN", "GRINDSTONE",
            "SMITHING_TABLE", "STONECUTTER", "BLAST_FURNACE",
            "BELL", "SMOKER", "BARREL", "LOOM",
            "CHIPPED_ANVIL", "DAMAGED_ANVIL", "FLOWER_POT",
            "*SIGN"
    };

    public ArmorListener(List<String> blockedMaterials){
        this.blockedMaterials = new LinkedList<>();
        this.blockedMaterials.addAll(Arrays.asList(defBlocked));
        this.blockedMaterials.addAll(blockedMaterials);
    }
    //Event Priority is highest because other plugins might cancel the events before we check.

    @EventHandler(priority =  EventPriority.HIGHEST, ignoreCancelled = true)
    public final void inventoryClick(final InventoryClickEvent e){
        boolean shift = false, numberkey = false;
        if(e.isCancelled()) return;
        if(e.getAction() == InventoryAction.NOTHING) return;// Why does this get called if nothing happens??
        if(e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)){
            shift = true;
        }
        if(e.getClick().equals(ClickType.NUMBER_KEY)){
            numberkey = true;
        }
        if(e.getSlotType() != SlotType.ARMOR && e.getSlotType() != SlotType.QUICKBAR && e.getSlotType() != SlotType.CONTAINER) return;
        Inventory inventory = e.getClickedInventory();
        if (inventory == null)
            return;
        if(!inventory.getType().equals(InventoryType.PLAYER)) return;
        if (!e.getInventory().getType().equals(InventoryType.CRAFTING) && !e.getInventory().getType().equals(InventoryType.PLAYER)) return;
        if(!(e.getWhoClicked() instanceof Player)) return;
        ArmorType newArmorType = ArmorType.matchType(shift ? e.getCurrentItem() : e.getCursor());
        if(!shift && newArmorType != null && e.getRawSlot() != newArmorType.getSlot()){
            // Used for drag and drop checking to make sure you aren't trying to place a helmet in the boots slot.
            return;
        }
        if(shift){
            newArmorType = ArmorType.matchType(e.getCurrentItem());
            if(newArmorType != null){
                boolean equipping = true;
                if(e.getRawSlot() == newArmorType.getSlot()){
                    equipping = false;
                }
                if(newArmorType.equals(ArmorType.HELMET) && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getHelmet())) || newArmorType.equals(ArmorType.CHESTPLATE) && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getChestplate())) || newArmorType.equals(ArmorType.LEGGINGS) && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getLeggings())) || newArmorType.equals(ArmorType.BOOTS) && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getBoots()))){
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) e.getWhoClicked(), EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if(armorEquipEvent.isCancelled()){
                        e.setCancelled(true);
                    }
                }
            }
        }else{
            ItemStack newArmorPiece = e.getCursor();
            ItemStack oldArmorPiece = e.getCurrentItem();
            if(numberkey){
                if(inventory.getType().equals(InventoryType.PLAYER)){// Prevents shit in the 2by2 crafting
                    // e.getClickedInventory() == The players inventory
                    // e.getHotBarButton() == key people are pressing to equip or unequip the item to or from.
                    // e.getRawSlot() == The slot the item is going to.
                    // e.getSlot() == Armor slot, can't use e.getRawSlot() as that gives a hotbar slot ;-;
                    ItemStack hotbarItem = inventory.getItem(e.getHotbarButton());
                    if(!isAirOrNull(hotbarItem)){// Equipping
                        newArmorType = ArmorType.matchType(hotbarItem);
                        newArmorPiece = hotbarItem;
                        oldArmorPiece = inventory.getItem(e.getSlot());
                    }else{// Unequipping
                        newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
                    }
                }
            }else{
                if(isAirOrNull(e.getCursor()) && !isAirOrNull(e.getCurrentItem())){// unequip with no new item going into the slot.
                    newArmorType = ArmorType.matchType(e.getCurrentItem());
                }
                // e.getCurrentItem() == Unequip
                // e.getCursor() == Equip
                // newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
            }
            if(newArmorType != null && e.getRawSlot() == newArmorType.getSlot()){
                EquipMethod method = EquipMethod.PICK_DROP;
                if(e.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey) method = EquipMethod.HOTBAR_SWAP;
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) e.getWhoClicked(), method, newArmorType, oldArmorPiece, newArmorPiece);
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                if(armorEquipEvent.isCancelled()){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority =  EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent e){
        if(e.useItemInHand().equals(Result.DENY))return;
        //
        if(e.getAction() == Action.PHYSICAL) return;
        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
            Player player = e.getPlayer();
            if (e.getItem() != null) {
                ItemStack itemStack = e.getItem();
                if (itemStack == null)
                    return;
                Material mat = itemStack.getType();
                if (mat.name().endsWith("_HEAD") || mat.name().endsWith("_SKULL") || mat.name().endsWith("SKULL_ITEM")) {
                    return;
                }
            }
            if(!e.useInteractedBlock().equals(Result.DENY)){
                if(e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()){// Having both of these checks is useless, might as well do it though.
                    // Some blocks have actions when you right click them which stops the client from equipping the armor in hand.
                    Block clickedBlock = e.getClickedBlock();
                    if (clickedBlock == null)
                        return;
                    Material mat = clickedBlock.getType();
                    for (String s : blockedMaterials){
                        String[] split = s.split("!");
                        String checked = split[0];
                        List<String> excluded = new ArrayList<>();
                        if (split.length > 1) {
                            excluded.addAll(Arrays.asList(split[1].split("&")));
                        }
                        if (checked.startsWith("*")) {
                            //Check material
                            if (mat.name().endsWith(checked.replace("*", "").toUpperCase()) && !isExcluded(mat, excluded)) {
                                return;
                            }
                        }
                        else if (checked.startsWith("?")) {
                            if (mat.name().contains(s.replace("?", "").toUpperCase()) && !isExcluded(mat, excluded)) {
                                return;
                            }
                        }
                        else if (mat.name().equalsIgnoreCase(s) && !isExcluded(mat, excluded)) return;
                    }
                }
            }
            ArmorType newArmorType = ArmorType.matchType(e.getItem());
            if(newArmorType != null){
                if(newArmorType.equals(ArmorType.HELMET) && isAirOrNull(e.getPlayer().getInventory().getHelmet()) || newArmorType.equals(ArmorType.CHESTPLATE) && isAirOrNull(e.getPlayer().getInventory().getChestplate()) || newArmorType.equals(ArmorType.LEGGINGS) && isAirOrNull(e.getPlayer().getInventory().getLeggings()) || newArmorType.equals(ArmorType.BOOTS) && isAirOrNull(e.getPlayer().getInventory().getBoots())){
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(e.getPlayer(), EquipMethod.HOTBAR, ArmorType.matchType(e.getItem()), null, e.getItem());
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if(armorEquipEvent.isCancelled()){
                        e.setCancelled(true);
                        player.updateInventory();
                    }
                }
            }
        }
    }

    private boolean isExcluded(Material mat, List<String> excluded) {
        boolean isExcluded = false;
        //Check exclusions
        for (String exclusion : excluded) {
            if (exclusion.startsWith("*")) {
                if (mat.name().endsWith(exclusion.replace("*", "").toUpperCase())) {
                    isExcluded = true;
                    break;
                }
            }
            else if (exclusion.startsWith("?")) {
                if (mat.name().contains(exclusion.replace("?", "").toUpperCase())) {
                    isExcluded = true;
                    break;
                }
            }
        }
        return isExcluded;
    }

    @EventHandler(priority =  EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryDrag(InventoryDragEvent event){
        // getType() seems to always be even.
        // Old Cursor gives the item you are equipping
        // Raw slot is the ArmorType slot
        // Can't replace armor using this method making getCursor() useless.
        ArmorType type = ArmorType.matchType(event.getOldCursor());
        if(event.getRawSlots().isEmpty()) return;// Idk if this will ever happen
        if(type != null && type.getSlot() == event.getRawSlots().stream().findFirst().orElse(0)){
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) event.getWhoClicked(), EquipMethod.DRAG, type, null, event.getOldCursor());
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if(armorEquipEvent.isCancelled()){
                event.setResult(Result.DENY);
                event.setCancelled(true);
            }
        }
        // Debug
		/*System.out.println("Slots: " + event.getInventorySlots().toString());
		System.out.println("Raw Slots: " + event.getRawSlots().toString());
		if(event.getCursor() != null){
			System.out.println("Cursor: " + event.getCursor().getType().name());
		}
		if(event.getOldCursor() != null){
			System.out.println("OldCursor: " + event.getOldCursor().getType().name());
		}
		System.out.println("Type: " + event.getType().name());*/
    }

    @EventHandler
    public void itemBreakEvent(PlayerItemBreakEvent e){
        ArmorType type = ArmorType.matchType(e.getBrokenItem());
        if(type != null){
            Player p = e.getPlayer();
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(p, EquipMethod.BROKE, type, e.getBrokenItem(), null);
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if(armorEquipEvent.isCancelled()){
                ItemStack i = e.getBrokenItem().clone();
                i.setAmount(1);
                i.setDurability((short) (i.getDurability() - 1));
                if(type.equals(ArmorType.HELMET)){
                    p.getInventory().setHelmet(i);
                }else if(type.equals(ArmorType.CHESTPLATE)){
                    p.getInventory().setChestplate(i);
                }else if(type.equals(ArmorType.LEGGINGS)){
                    p.getInventory().setLeggings(i);
                }else if(type.equals(ArmorType.BOOTS)){
                    p.getInventory().setBoots(i);
                }
            }
        }
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent e){
        Player p = e.getEntity();
        if(e.getKeepInventory()) return;
        for(ItemStack i : p.getInventory().getArmorContents()){
            if(!isAirOrNull(i)){
                Bukkit.getServer().getPluginManager().callEvent(new ArmorEquipEvent(p, EquipMethod.DEATH, ArmorType.matchType(i), i, null));
                // No way to cancel a death event.
            }
        }
    }

    /**
     * A utility method to support versions that use null or air ItemStacks.
     */
    public static boolean isAirOrNull(ItemStack item){
        return item == null || item.getType().equals(Material.AIR);
    }
}
