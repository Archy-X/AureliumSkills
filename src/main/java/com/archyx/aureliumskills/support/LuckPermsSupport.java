package com.archyx.aureliumskills.support;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LuckPermsSupport {

    private final LuckPerms luckPerms;

    public LuckPermsSupport() {
        luckPerms = LuckPermsProvider.get();
    }

    public void addPermission(@NotNull Player player, @NotNull String permission, boolean value) {
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user ->
                user.data().add(Node.builder(permission).value(value).build()));
    }

    public void addPermission(@NotNull Player player, @NotNull String permission) {
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user ->
                user.data().add(Node.builder(permission).value(true).build()));
    }

    public void removePermission(@NotNull Player player, @NotNull String permission, boolean value) {
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user ->
                user.data().remove(Node.builder(permission).value(value).build()));
    }

}
