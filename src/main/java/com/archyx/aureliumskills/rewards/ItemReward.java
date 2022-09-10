package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.LevelerMessage;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.archyx.aureliumskills.util.misc.KeyIntPair;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ItemReward extends MessagedReward {

    private final @NotNull String itemKey;
    private final int amount; // Amount of -1 means no amount was specified and should use amount of registered item

    public ItemReward(@NotNull AureliumSkills plugin, @NotNull String menuMessage, @NotNull String chatMessage, @NotNull String itemKey, int amount) {
        super(plugin, menuMessage, chatMessage);
        this.itemKey = itemKey;
        this.amount = amount;
    }

    @Override
    public void giveReward(@NotNull Player player, @NotNull Skill skill, int level) {
        ItemStack item = plugin.getItemRegistry().getItem(itemKey);
        // Send warning if item not found
        if (item == null) {
            plugin.getLogger().warning("Could not find a registered item with key " + itemKey + " when granting " +
                    "item reward (" + TextUtil.capitalize(skill.toString().toLowerCase(Locale.ROOT)) + " " + level + ")");
            return;
        }
        if (amount != -1) {
            item.setAmount(amount);
        }
        ItemStack leftoverItem = ItemUtils.addItemToInventory(player, item); // Attempt item give
        // Handle items that could not fit in the inventory
        if (leftoverItem != null) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            // Add unclaimed item key and amount to player data
            playerData.getUnclaimedItems().add(new KeyIntPair(itemKey, leftoverItem.getAmount()));
            // Notify player
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    player.sendMessage(AureliumSkills.getPrefix(playerData.getLocale()) + Lang.getMessage(LevelerMessage.UNCLAIMED_ITEM, playerData.getLocale())), 1);
        }
    }

}
