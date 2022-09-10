package com.archyx.aureliumskills.support;

import com.archyx.aureliumskills.util.text.TextUtil;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorldGuardFlags {

    private final @NotNull Map<FlagKey, StateFlag> stateFlags;

    public WorldGuardFlags() {
        this.stateFlags = new HashMap<>();
    }

    public @Nullable StateFlag getStateFlag(FlagKey flagKey) {
        return stateFlags.get(flagKey);
    }

    public void register() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        for (FlagKey flagKey : FlagKey.values()) {
            Object def = flagKey.getDefault();
            // State flags
            if (def instanceof Boolean) {
                String flagName = "aureliumskills-" + TextUtil.replace(flagKey.toString().toLowerCase(Locale.ROOT), "_", "-");
                try {
                    StateFlag stateFlag = new StateFlag(flagName, (boolean) def);
                    registry.register(stateFlag);
                    stateFlags.put(flagKey, stateFlag);
                } catch (FlagConflictException e) {
                    Flag<?> existing = registry.get(flagName);
                    if (existing instanceof StateFlag) {
                        stateFlags.put(flagKey, (StateFlag) existing);
                    } else {
                        Bukkit.getLogger().warning("Could not register flag " + flagName);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public enum FlagKey {

        XP_GAIN(true),
        CUSTOM_LOOT(true);

        private final Object def;

        FlagKey(Object def) {
            this.def = def;
        }

        public Object getDefault() {
            return def;
        }

    }


}
