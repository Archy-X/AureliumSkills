package com.archyx.aureliumskills.item;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.archyx.aureliumskills.util.misc.KeyIntPair;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UnclaimedItemsMenu implements InventoryProvider {

    private final AureliumSkills plugin;
    private final PlayerData playerData;

    public UnclaimedItemsMenu(AureliumSkills plugin, PlayerData playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @Override
    public void init(@NotNull Player player, @NotNull InventoryContents contents) {
        for (int slot = 0; slot < 54; slot++) {
            int row = slot / 9;
            int column = slot % 9;
            if (playerData.getUnclaimedItems().size() <= slot) { // Empty slot
                contents.set(row, column, ClickableItem.empty(new ItemStack(Material.AIR)));
            } else { // Slot with item
                KeyIntPair keyIntPair = playerData.getUnclaimedItems().get(slot);

                String itemKey = keyIntPair.getKey();
                int amount = keyIntPair.getValue();
                ItemStack item = plugin.getItemRegistry().getItem(itemKey);
                if (item == null) {
                    plugin.getLogger().warning("Could not find a registered item with key " + itemKey + " when claiming unclaimed item rewards");
                    continue;
                }
                item.setAmount(amount);
                contents.set(row, column, ClickableItem.of(getDisplayItem(item), event -> {
                    // Give item on click
                    ItemStack leftoverItem = ItemUtils.addItemToInventory(player, item);
                    if (leftoverItem == null) { // All items were added
                        playerData.getUnclaimedItems().remove(keyIntPair);
                        if (playerData.getUnclaimedItems().size() > 0) {
                            init(player, contents);
                        } else {
                            player.closeInventory();
                        }
                    } else if (leftoverItem.getAmount() != item.getAmount()) { // Some items could not fit
                        keyIntPair.setValue(leftoverItem.getAmount());
                        init(player, contents);
                    } else { // All items could not fit
                        player.sendMessage(Lang.getMessage(MenuMessage.INVENTORY_FULL, playerData.getLocale()));
                        player.closeInventory();
                    }
                }));
            }
        }
    }

    public static @NotNull SmartInventory getInventory(@NotNull AureliumSkills plugin, @NotNull PlayerData playerData) {
        return SmartInventory.builder()
                .manager(plugin.getInventoryManager())
                .provider(new UnclaimedItemsMenu(plugin, playerData))
                .size(6, 9)
                .title(Lang.getMessage(MenuMessage.UNCLAIMED_ITEMS_TITLE, playerData.getLocale()))
                .build();
    }

    private @NotNull ItemStack getDisplayItem(@NotNull ItemStack baseItem) {
        ItemStack displayItem = baseItem.clone();
        ItemMeta meta = displayItem.getItemMeta();
        if (meta != null) {
            List<@NotNull String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            } else {
                lore.add(" ");
            }
            lore.add(Lang.getMessage(MenuMessage.CLICK_TO_CLAIM, playerData.getLocale()));
            meta.setLore(lore);
        }
        displayItem.setItemMeta(meta);
        return displayItem;
    }

}
