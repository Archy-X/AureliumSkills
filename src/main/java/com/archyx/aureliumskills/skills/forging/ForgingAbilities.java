package com.archyx.aureliumskills.skills.forging;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.XpGainEvent;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.archyx.aureliumskills.util.mechanics.EnchantmentValue;
import com.archyx.aureliumskills.util.mechanics.GrindstoneEnchant;
import com.archyx.aureliumskills.util.version.VersionUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ForgingAbilities extends AbilityProvider implements Listener {

    private final Random random = new Random();

    public ForgingAbilities(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.FORGING);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void disenchanter(@NotNull InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (blockDisabled(Ability.DISENCHANTER)) return;
        if (!VersionUtils.isAtLeastVersion(14)) return; // This ability requires at least 1.14
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (blockAbility(player)) return;
            @Nullable Inventory inventory = event.getClickedInventory();
            if (inventory == null) return;
            ClickType click = event.getClick();
            // Only allow right and left clicks if inventory full
            if (click != ClickType.LEFT && click != ClickType.RIGHT && ItemUtils.isInventoryFull(player)) return;
            if (event.getResult() != Event.Result.ALLOW) return; // Make sure the click was successful
            if (player.getItemOnCursor().getType() != Material.AIR) return; // Make sure cursor is empty
            if (inventory.getType() == InventoryType.GRINDSTONE) {
                if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) return;
                    if (playerData.getAbilityLevel(Ability.DISENCHANTER) == 0) return;
                    Location location = inventory.getLocation();
                    if (location == null) return;
                    ItemStack first = inventory.getItem(0);
                    ItemStack second = inventory.getItem(1);
                    if (first != null && second != null) { // If two items, make sure items are the same type
                        if (first.getType() != second.getType()) {
                            return;
                        }
                    }
                    Set<EnchantmentValue> enchants = new HashSet<>();
                    // Add enchants to disenchant
                    checkEnchants(first, enchants);
                    checkEnchants(second, enchants);
                    if (enchants.size() == 0) return;
                    // Calculate the sum
                    try {
                        int sum = 0;
                        for (EnchantmentValue value : enchants) {
                            String enchantName = value.getEnchantment().getKey().getKey().toUpperCase(Locale.ENGLISH);
                            if (containsEnchant(enchantName)) {
                                sum += GrindstoneEnchant.valueOf(enchantName).getLevel(value.getLevel());
                            }
                        }
                        int average = (sum + (int) Math.ceil(((double) sum) / 2)) / 2; // Get the average experience that would drop
                        int added = (int) Math.round(average * (getValue(Ability.DISENCHANTER, playerData) / 100));
                        World world = location.getWorld();
                        if (world != null) {
                            world.spawn(location, ExperienceOrb.class).setExperience(added);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    private void checkEnchants(@Nullable ItemStack item, @NotNull Set<EnchantmentValue> enchants) {
        if (item != null) {
            for (Map.Entry<@NotNull Enchantment, @NotNull Integer> entry : item.getEnchantments().entrySet()) {
                if (plugin.getForgingLeveler().isDisenchantable(entry.getKey())) {
                    enchants.add(new EnchantmentValue(entry.getKey(), entry.getValue()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    @SuppressWarnings("deprecation")
    public void repairing(@NotNull PrepareAnvilEvent event) {
        if (blockDisabled(Ability.REPAIRING)) return;
        Player player = getHighestPlayer(event.getViewers());
        if (player == null) return;
        if (blockAbility(player)) return;
        AnvilInventory inventory = event.getInventory();
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        if (playerData.getAbilityLevel(Ability.REPAIRING) == 0) return;
        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);
        ItemStack result = event.getResult();
        if (first == null || second == null || result == null) return;
        // Check if the seconds slot is a raw material for repair
        XMaterial rawMaterial = getRawMaterial(first.getType());
        if (rawMaterial == null) return;
        if (second.getType() == rawMaterial.parseMaterial()) {
            if (XMaterial.isNewVersion()) {
                ItemMeta meta = first.getItemMeta();
                if (meta == null) return;
                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;
                    short max = first.getType().getMaxDurability();
                    // Calculate durability to add, vanilla by default adds 20% of the max durability
                    long addedLong = second.getAmount() * (Math.round(0.25 * max) + Math.round(max * 0.25 * (getValue(Ability.REPAIRING, playerData) / 100)));
                    short added;
                    if (addedLong > Short.MAX_VALUE) {
                        added = (short) damageable.getDamage();
                    } else if (addedLong < Short.MIN_VALUE) {
                        added = 0;
                    } else {
                        added = (short) addedLong;
                    }
                    damageable.setDamage(Math.max(damageable.getDamage() - added, 0));
                    result.setItemMeta(damageable);
                }
            } else {
                // For old versions
                short max = result.getType().getMaxDurability();
                long addedLong = second.getAmount() * (Math.round(0.25 * max) + Math.round(max * 0.25 * (getValue(Ability.REPAIRING, playerData) / 100)));
                short added;
                if (addedLong > Short.MAX_VALUE) {
                    added = first.getDurability();
                } else if (addedLong < Short.MIN_VALUE) {
                    added = 0;
                } else {
                    added = (short) addedLong;
                }
                result.setDurability((short) Math.max(first.getDurability() - added, 0));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void anvilMaster(@NotNull InventoryOpenEvent event) {
        if (event.isCancelled()) return;
        if (blockDisabled(Ability.ANVIL_MASTER)) return;
        if (!VersionUtils.isAtLeastVersion(13)) return;
        Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.ANVIL && inventory instanceof AnvilInventory) {
            AnvilInventory anvil = (AnvilInventory) inventory;
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                if (blockAbility(player)) return;
                @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData == null) return;
                if (playerData.getAbilityLevel(Ability.ANVIL_MASTER) > 0) {
                    int maxCost = (int) Math.round(getValue(Ability.ANVIL_MASTER, playerData));
                    anvil.setMaximumRepairCost(maxCost);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    @SuppressWarnings("deprecation")
    public void skillMender(@NotNull XpGainEvent event) {
        if (event.isCancelled()) return;
        if (blockDisabled(Ability.SKILL_MENDER)) return;
        Player player = event.getPlayer();
        if (blockAbility(player)) return;
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        if (playerData.getAbilityLevel(Ability.SKILL_MENDER) == 0) return;
        if (random.nextDouble() < getValue(Ability.SKILL_MENDER, playerData) / 100) {
            // Get all the items that have mending
            List<ItemStack> mendingItems = new ArrayList<>();
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null) {
                    if (item.containsEnchantment(Enchantment.MENDING) && hasDamage(item)) {
                        mendingItems.add(item);
                    }
                }
            }
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (mainHandItem.containsEnchantment(Enchantment.MENDING) && hasDamage(mainHandItem)) {
                mendingItems.add(mainHandItem);
            }
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (offHandItem.containsEnchantment(Enchantment.MENDING) && hasDamage(offHandItem)) {
                mendingItems.add(offHandItem);
            }
            if (mendingItems.size() == 0) return;
            ItemStack mendedItem = mendingItems.get(random.nextInt(mendingItems.size())); // Pick a random item
            int durabilityToRepair = (int) Math.round(event.getAmount() / 2); // One durability per 2 skill xp gained
            // Apply durability repair
            if (XMaterial.isNewVersion()) {
                ItemMeta meta = mendedItem.getItemMeta();
                if (meta == null) return;
                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;
                    damageable.setDamage(Math.max(damageable.getDamage() - durabilityToRepair, 0));
                    mendedItem.setItemMeta(damageable);
                }
            } else {
                mendedItem.setDurability((short) Math.max(mendedItem.getDurability() - durabilityToRepair, 0));
            }
        }
    }

    private @Nullable XMaterial getRawMaterial(@NotNull Material material) {
        String name = material.name();
        if (name.startsWith("DIAMOND_")) {
            if (!name.equals("DIAMOND_ORE") && !name.equals("DIAMOND_BLOCK") && !name.equals("DIAMOND_HORSE_ARMOR")) {
                return XMaterial.DIAMOND;
            }
        } else if (name.startsWith("GOLD_") || name.startsWith("GOLDEN_")) {
            if (!name.contains("APPLE") && !name.contains("CARROT") && !name.contains("HORSE_ARMOR")
                && !name.contains("GOLD") && !name.contains("INGOT") && !name.contains("NUGGET")
                && !name.contains("ORE") && !name.contains("BARDING")) {
                return XMaterial.GOLD_INGOT;
            }
        } else if (name.startsWith("IRON_")) {
            if (!name.contains("BARS") && !name.contains("DOOR") && !name.contains("BLOCK")
                && !name.contains("INGOT") && !name.contains("NUGGET") && !name.contains("ORE")
                && !name.contains("TRAPDOOR") && !name.contains("HORSE_ARMOR")) {
                return XMaterial.IRON_INGOT;
            }
        } else if (name.startsWith("LEATHER_")) {
            if (!name.contains("HORSE_ARMOR")) {
                return XMaterial.LEATHER;
            }
        } else if (name.startsWith("NETHERITE_")) {
            if (!name.contains("SCRAP") && !name.contains("INGOT") && !name.contains("BLOCK")) {
                return XMaterial.NETHERITE_INGOT;
            }
        } else if (name.equals("TURTLE_HELMET")) {
            return XMaterial.SCUTE;
        } else if (material == Material.ELYTRA) {
            return XMaterial.PHANTOM_MEMBRANE;
        } else if (name.startsWith("CHAINMAIL")) {
            return XMaterial.IRON_INGOT;
        }
        return null;
    }

    private @Nullable Player getHighestPlayer(@NotNull List<HumanEntity> viewers) {
        int highestLevel = 0;
        Player highestPlayer = null;
        for (HumanEntity entity : viewers) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData != null) {
                    int level = playerData.getSkillLevel(Skills.FORGING);
                    if (level > highestLevel) {
                        highestLevel = level;
                        highestPlayer = player;
                    }
                }
            }
        }
        return highestPlayer;
    }

    @SuppressWarnings("deprecation")
    private boolean hasDamage(@NotNull ItemStack item) {
        if (XMaterial.isNewVersion()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta instanceof Damageable) {
                    return ((Damageable) meta).hasDamage();
                }
            }
        } else {
            return item.getDurability() > 0;
        }
        return false;
    }

    private boolean containsEnchant(String enchantName) {
        for (GrindstoneEnchant grindstoneEnchant : GrindstoneEnchant.values()) {
            if (grindstoneEnchant.toString().equals(enchantName)) {
                return true;
            }
        }
        return false;
    }

}
