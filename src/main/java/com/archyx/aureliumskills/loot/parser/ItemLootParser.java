package com.archyx.aureliumskills.loot.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.loot.builder.ItemLootBuilder;
import com.archyx.aureliumskills.util.item.MaterialUtil;
import com.archyx.aureliumskills.util.misc.Validate;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.cryptomorin.xseries.XEnchantment;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;

public class ItemLootParser extends LootParser {

    public ItemLootParser(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public Loot parse(Map<?, ?> map) {
        ItemStack item = parseItem(map);
        Validate.notNull(item, "Failed to parse item");

        int[] amount = parseAmount(map);

        return new ItemLootBuilder(plugin).item(item)
                .minAmount(amount[0])
                .maxAmount(amount[1])
                .message(parseMessage(map))
                .weight(parseWeight(map))
                .sources(parseSources(map))
                .xp(parseXp(map)).build();
    }

    @SuppressWarnings("deprecation")
    private ItemStack parseItem(Map<?, ?> map) {
        if (map.containsKey("key")) { // Item key
            return parseItemKey(map);
        } else { // Regular item
            String materialString = getString(map, "material");
            ItemStack item;
            if (!materialString.contains(":")) { // No legacy data
                String materialName = materialString.toUpperCase(Locale.ROOT);
                Material material = MaterialUtil.parse(materialName);
                if (material == null) {
                    throw new IllegalArgumentException("Unknown material " + materialString);
                }
                item = new ItemStack(material);
            } else { // With legacy data
                String[] splitMaterial = materialString.split(":");
                if (splitMaterial.length == 2) {
                    String materialName = splitMaterial[0].toUpperCase(Locale.ROOT);
                    Material material = MaterialUtil.parse(materialName);
                    if (material == null) {
                        throw new IllegalArgumentException("Unknown material " + materialName);
                    }
                    short data = NumberUtils.toShort(splitMaterial[1]);
                    item = new ItemStack(material, 1, data);
                } else {
                    throw new IllegalArgumentException("Material with data value can only have one :");
                }
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            // Display name and lore
            if (map.containsKey("display_name")) {
                String displayName = TextUtil.replaceNonEscaped(getString(map, "display_name"), "&", "§");
                meta.setDisplayName(displayName);
                item.setItemMeta(meta);
            }
            if (map.containsKey("lore")) {
                List<String> lore = getStringList(map, "lore");
                List<String> formattedLore = new ArrayList<>();
                for (String line : lore) {
                    formattedLore.add(TextUtil.replaceNonEscaped(line, "&", "§"));
                }
                meta.setLore(formattedLore);
                item.setItemMeta(meta);
            }
            // Enchantments
            if (map.containsKey("enchantments")) {
                List<String> enchantmentStrings = getStringList(map, "enchantments");
                for (String enchantmentEntry : enchantmentStrings) {
                    String[] splitEntry = enchantmentEntry.split(" ");
                    String enchantmentName = splitEntry[0];
                    int level = 1;
                    if (splitEntry.length > 1) {
                        level = NumberUtils.toInt(splitEntry[1], 1);
                    }
                    Optional<XEnchantment> xEnchantment = XEnchantment.matchXEnchantment(enchantmentName.toUpperCase(Locale.ROOT));
                    if (xEnchantment.isPresent()) {
                        Enchantment enchantment = xEnchantment.get().parseEnchantment();
                        if (enchantment != null) {
                            if (item.getType() == Material.ENCHANTED_BOOK && meta instanceof EnchantmentStorageMeta) {
                                EnchantmentStorageMeta esm = (EnchantmentStorageMeta) meta;
                                esm.addStoredEnchant(enchantment, level, true);
                                item.setItemMeta(esm);
                            } else {
                                meta.addEnchant(enchantment, level, true);
                                item.setItemMeta(meta);
                            }
                        } else {
                            throw new IllegalArgumentException("Invalid enchantment name " + enchantmentName);
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid enchantment name " + enchantmentName);
                    }
                }
            }
            // Potions
            if (map.containsKey("potion_data")) {
                Map<?, ?> potionDataMap = getMap(map, "potion_data");
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                PotionType potionType = PotionType.valueOf(getString(potionDataMap, "type").toUpperCase(Locale.ROOT));
                boolean extended = false;
                if (potionDataMap.containsKey("extended")) {
                    extended = getBoolean(potionDataMap, "extended");
                }
                boolean upgraded = false;
                if (potionDataMap.containsKey("upgraded")) {
                    upgraded = getBoolean(potionDataMap, "upgraded");
                }
                PotionData potionData = new PotionData(potionType, extended, upgraded);
                potionMeta.setBasePotionData(potionData);
                item.setItemMeta(potionMeta);
            }
            // Custom potion effects
            if (map.containsKey("custom_effects")) {
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                for (Map<?, ?> effectMap : getMapList(map, "custom_effects")) {
                    String effectName = getString(effectMap, "type");
                    PotionEffectType type = PotionEffectType.getByName(effectName);
                    if (type != null) {
                        int duration = getInt(effectMap, "duration");
                        int amplifier = getInt(effectMap, "amplifier");
                        potionMeta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
                        potionMeta.setColor(type.getColor());
                    } else {
                        throw new IllegalArgumentException("Invalid potion effect type " + effectName);
                    }
                }
                item.setItemMeta(potionMeta);
            }
            // Glowing w/o enchantments visible
            if (getBooleanOrDefault(map, "glow", false)) {
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
            // TODO Item flags
            // Custom NBT
            if (map.containsKey("nbt")) {
                Map<?, ?> nbtMap = getMap(map, "nbt");
                item = parseNBT(item, nbtMap);
            }
            return item;
        }
    }

    private ItemStack parseItemKey(Map<?, ?> map) {
        String itemKey = getString(map, "key");
        ItemStack item = plugin.getItemRegistry().getItem(itemKey);
        if (item != null) {
            return item;
        } else {
            throw new IllegalArgumentException("Item with key " + itemKey + " not found in item registry");
        }
    }

    private ItemStack parseNBT(ItemStack item, Map<?, ?> map) {
        NBTItem nbtItem = new NBTItem(item);
        applyMapToNBT(nbtItem, map);
        return nbtItem.getItem();
    }

    private void applyMapToNBT(NBTItem item, Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof String) {
                if (value instanceof Map<?, ?>) { // Recursively apply sub maps
                    applyMapToNBT(item, (Map<?, ?>) value);
                } else {
                    item.setObject((String) key, value);
                }
            }
        }
    }

    protected int[] parseAmount(Map<?, ?> map) {
        if (map.containsKey("amount")) {
            Object object = getElement(map, "amount");
            if (object instanceof Integer) {
                Integer amount = (Integer) object;
                return new int[] {amount, amount};
            } else if (object instanceof String) {
                String amountString = (String) object;
                String[] splitString = amountString.split("-");
                int minAmount = NumberUtils.toInt(splitString[0]);
                int maxAmount = minAmount;
                if (splitString.length > 1) {
                    maxAmount = NumberUtils.toInt(splitString[1], minAmount);
                }
                return new int[] {minAmount, maxAmount};
            }
        }
        return new int[] {1, 1};
    }

}
