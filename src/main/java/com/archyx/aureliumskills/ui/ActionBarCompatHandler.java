package com.archyx.aureliumskills.ui;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.util.version.VersionUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionBarCompatHandler {

    private final @NotNull AureliumSkills plugin;
    private final ActionBar actionBar;
    private static final int PAUSE_TICKS = 50;

    public ActionBarCompatHandler(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.actionBar = plugin.getActionBar();
    }

    public void registerListeners() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        if (VersionUtils.isAtLeastVersion(17)) {
            registerNewListener(manager);
        } else {
            registerLegacyListener(manager);
        }
        if (VersionUtils.isAtLeastVersion(19)) {
            registerSystemChatListener(manager);
        }
        registerChatListener(manager);
    }

    private void registerNewListener(@NotNull ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.SET_ACTION_BAR_TEXT) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                if (packet.getMeta("AureliumSkills").isPresent()) return; // Ignore Aurelium Skills action bars
                actionBar.setPaused(player, PAUSE_TICKS);
            }
        });
    }

    private void registerSystemChatListener(@NotNull ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                if (packet.getMeta("AureliumSkills").isPresent()) return;
                StructureModifier<Integer> integers = packet.getIntegers();
                if (integers.size() == 1) {
                    if (integers.read(0) == EnumWrappers.ChatType.GAME_INFO.getId()) {
                        actionBar.setPaused(player, PAUSE_TICKS);
                    }
                } else if (packet.getBooleans().read(0)) {
                    actionBar.setPaused(player, PAUSE_TICKS);
                }
            }
        });
    }

    private void registerLegacyListener(@NotNull ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.TITLE) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                // Make sure the title packet is for the action bar
                if (packet.getEnumModifier(EnumWrappers.TitleAction.class, 0).read(0) != EnumWrappers.TitleAction.ACTIONBAR) {
                    return;
                }
                if (packet.getMeta("AureliumSkills").isPresent()) return; // Ignore Aurelium Skills action bars
                actionBar.setPaused(player, PAUSE_TICKS);
            }
        });
    }

    private void registerChatListener(@NotNull ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(@NotNull PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                // Make sure the chat packet is for the action bar
                if (packet.getChatTypes().read(0) != EnumWrappers.ChatType.GAME_INFO) {
                    return;
                }
                actionBar.setPaused(player, PAUSE_TICKS);
            }
        });
    }

}
