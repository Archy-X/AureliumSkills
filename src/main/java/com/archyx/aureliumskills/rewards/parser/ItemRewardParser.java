package com.archyx.aureliumskills.rewards.parser;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.RewardMessage;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.rewards.builder.ItemRewardBuilder;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ItemRewardParser extends RewardParser {

    public ItemRewardParser(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Reward parse(@NotNull Map<?, ?> map) {
        String key = getString(map, "key");
        ItemRewardBuilder builder = new ItemRewardBuilder(plugin).itemKey(key);

        int amount = 1;
        if (map.containsKey("amount")) {
            int definedAmount = getInt(map, "amount");
            builder.amount(definedAmount);
            amount = definedAmount;
        } else {
            ItemStack item = plugin.getItemRegistry().getItem(key);
            if (item != null) {
                amount = item.getAmount();
            }
        }

        if (map.containsKey("menu_message")) {
            builder.menuMessage(getString(map, "menu_message"));
        } else {
            // Use default menu message
            ItemStack item = plugin.getItemRegistry().getItem(key);
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    RewardMessage messageKey;
                    if (amount == 1) {
                        messageKey = RewardMessage.ITEM_DEFAULT_MENU_MESSAGE;
                    } else {
                        messageKey = RewardMessage.ITEM_DEFAULT_MENU_MESSAGE_MULTIPLE;
                    }
                    if (meta.hasDisplayName() || meta.hasLocalizedName()) {
                        builder.menuMessage(TextUtil.replace(Lang.getMessage(messageKey, Lang.getDefaultLanguage()),
                                "{display_name}", meta.hasDisplayName() ? meta.getDisplayName() : meta.getLocalizedName(),
                                "{amount}", String.valueOf(amount),
                                "{key}", key));
                    }
                }
            }
        }

        if (map.containsKey("chat_message")) {
            builder.chatMessage(getString(map, "chat_message"));
        } else {
            // Use default chat message
            ItemStack item = plugin.getItemRegistry().getItem(key);
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    RewardMessage messageKey;
                    if (amount == 1) {
                        messageKey = RewardMessage.ITEM_DEFAULT_CHAT_MESSAGE;
                    } else {
                        messageKey = RewardMessage.ITEM_DEFAULT_CHAT_MESSAGE_MULTIPLE;
                    }
                    if (meta.hasDisplayName() || meta.hasLocalizedName()) {
                        builder.chatMessage(TextUtil.replace(Lang.getMessage(messageKey, Lang.getDefaultLanguage()),
                                "{display_name}", meta.hasDisplayName() ? meta.getDisplayName() : meta.getLocalizedName(),
                                "{amount}", String.valueOf(amount),
                                "{key}", key));
                    }
                }
            }
        }

        if (map.containsKey("message")) {
            String message = getString(map, "message");
            builder.chatMessage(message).menuMessage(message);
        }

        return builder.build();
    }
}
