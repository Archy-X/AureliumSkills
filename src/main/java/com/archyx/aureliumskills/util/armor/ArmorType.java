package com.archyx.aureliumskills.util.armor;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * @author Arnah
 * @since Jul 30, 2015
 */
public enum ArmorType{

    HELMET(5), CHESTPLATE(6), LEGGINGS(7), BOOTS(8);

    private final int slot;

    ArmorType(int slot){
        this.slot = slot;
    }

    /**
     * Attempts to match the ArmorType for the specified ItemStack.
     *
     * @param itemStack The ItemStack to parse the type of.
     * @return The parsed ArmorType, or null if not found.
     */
    public static ArmorType matchType(final ItemStack itemStack){
        if(ArmorListener.isAirOrNull(itemStack)) return null;
        assert (null != itemStack);
        String type = itemStack.getType().name();
        if(type.endsWith("_HELMET") || type.endsWith("_SKULL") || type.endsWith("_HEAD") || type.endsWith("SKULL_ITEM")) return HELMET;
        else if(type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
        else if(type.endsWith("_LEGGINGS")) return LEGGINGS;
        else if(type.endsWith("_BOOTS")) return BOOTS;
        else return null;
    }

    public int getSlot(){
        return slot;
    }

    public EquipmentSlot getEquipmentSlot() {
        switch (this) {
            case HELMET:
                return EquipmentSlot.HEAD;
            case CHESTPLATE:
                return EquipmentSlot.CHEST;
            case LEGGINGS:
                return EquipmentSlot.LEGS;
            case BOOTS:
            default:
                return EquipmentSlot.FEET;
        }
    }

}
