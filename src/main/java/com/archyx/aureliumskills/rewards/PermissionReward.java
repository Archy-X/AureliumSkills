package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.support.LuckPermsSupport;
import org.bukkit.entity.Player;

public class PermissionReward extends MessagedReward {

    private final String permission;
    private final boolean value;

    public PermissionReward(AureliumSkills plugin, String menuMessage, String chatMessage, String permission, boolean value) {
        super(plugin, menuMessage, chatMessage);
        this.permission = permission;
        this.value = value;
    }

    @Override
    public void giveReward(Player player, Skill skill, int level) {
        LuckPermsSupport luckPermsSupport = plugin.getLuckPermsSupport();
        if (plugin.isLuckPermsEnabled() && luckPermsSupport != null) {
            luckPermsSupport.addPermission(player, permission, value);
        }
    }

    public String getPermission() {
        return permission;
    }

    public boolean getValue() {
        return value;
    }

}
