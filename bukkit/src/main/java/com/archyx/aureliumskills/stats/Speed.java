package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class Speed {

    private final AureliumSkills plugin;

    public Speed(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public void reload(Player player) {
        setSpeed(player);
    }

    public void setSpeed(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute != null) {
            if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
                player.setWalkSpeed(0.2f);
            }
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                double speed = playerData.getStatLevel(Stats.SPEED) / 100;
                player.setWalkSpeed((float) (0.2 * (1 + speed)));
            }
        }
    }

}
