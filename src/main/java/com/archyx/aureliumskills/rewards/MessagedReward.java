package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.CustomMessageKey;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.text.TextUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.Locale;

public abstract class MessagedReward extends Reward {

    protected final String menuMessage;
    protected final String chatMessage;

    public MessagedReward(AureliumSkills plugin, String menuMessage, String chatMessage) {
        super(plugin);
        this.menuMessage = menuMessage;
        this.chatMessage = chatMessage;
    }

    @Override
    public String getMenuMessage(Player player, Locale locale, Skill skill, int level) {
        return attemptAsMessageKey(menuMessage, player, locale, skill, level);
    }

    @Override
    public String getChatMessage(Player player, Locale locale, Skill skill, int level) {
        return attemptAsMessageKey(chatMessage, player, locale, skill, level);
    }

    /**
     * Attempts to use the input as a message key. If a matching translation for the key is found, it will return the translation.
     * Otherwise it will return the key.
     */
    private String attemptAsMessageKey(String potentialKey, Player player, Locale locale, Skill skill, int level) {
        CustomMessageKey key = new CustomMessageKey(potentialKey);
        String message = potentialKey;
        try {
            message = Lang.getMessage(key, locale);
        }
        catch (IllegalStateException ex) {
            // No custom message exists when using the message as a key
            plugin.getLogger().warning("Unknown custom message with path: " + key);
        }
        
        return replacePlaceholders(message, player, skill, level);
    }

    private String replacePlaceholders(String message, Player player, Skill skill, int level) {
        message = TextUtil.replace(message, "{player}", player.getName(),
                "{skill}", skill.toString().toLowerCase(Locale.ROOT),
                "{level}", String.valueOf(level));
        if (plugin.isPlaceholderAPIEnabled()) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        message = TextUtil.replaceNonEscaped(message, "&", "§");
        return message;
    }

}
