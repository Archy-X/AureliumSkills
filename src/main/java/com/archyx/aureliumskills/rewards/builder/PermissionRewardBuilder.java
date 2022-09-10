package com.archyx.aureliumskills.rewards.builder;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.rewards.PermissionReward;
import com.archyx.aureliumskills.rewards.Reward;

import java.util.Objects;

public class PermissionRewardBuilder extends MessagedRewardBuilder {

    private String permission;
    private boolean value;

    public PermissionRewardBuilder(AureliumSkills plugin) {
        super(plugin);
        this.value = true;
    }

    public PermissionRewardBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }
    
    public PermissionRewardBuilder value(boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public Reward build() {
        Objects.requireNonNull(permission, "You must specify a permission");
        return new PermissionReward(plugin, menuMessage, chatMessage, permission, value);
    }
    

}
