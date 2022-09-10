package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.ManaAbilityRefreshEvent;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.configuration.OptionValue;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skill;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ManaAbilityManager implements Listener {

    private final @NotNull Map<UUID, Map<MAbility, Integer>> cooldowns;
    private final @NotNull Map<UUID, Map<MAbility, Boolean>> ready;
    private final @NotNull Map<UUID, Map<MAbility, Boolean>> activated;
    private final @NotNull Map<UUID, Map<MAbility, Integer>> errorTimer;

    private final @NotNull Map<MAbility, ManaAbilityProvider> providers;

    private final @NotNull AureliumSkills plugin;

    public ManaAbilityManager(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        cooldowns = new HashMap<>();
        ready = new HashMap<>();
        activated = new HashMap<>();
        errorTimer = new HashMap<>();
        providers = new HashMap<>();
    }

    public void init() {
        registerProviders();
        startTimer();
    }

    private void registerProviders() {
        for (MAbility mAbility : MAbility.values()) {
            Class<? extends ManaAbilityProvider> providerClass = mAbility.getProvider();
            if (providerClass != null) {
                try {
                    Constructor<? extends ManaAbilityProvider> constructor = providerClass.getConstructor(AureliumSkills.class);
                    ManaAbilityProvider provider = constructor.newInstance(plugin);
                    register(provider);
                    providers.put(mAbility, provider);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    plugin.getLogger().warning("Failed to register mana ability provider for " + mAbility.toString().toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    private void register(@NotNull ManaAbilityProvider provider) {
        Bukkit.getPluginManager().registerEvents(provider, plugin);
    }

    public @Nullable ManaAbilityProvider getProvider(@NotNull MAbility mAbility) {
        return providers.get(mAbility);
    }

    public void setActivated(@NotNull Player player, @NotNull MAbility mAbility, boolean isActivated) {
        Map<MAbility, Boolean> map = activated.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        map.put(mAbility, isActivated);
    }

    //Sets cooldown
    public void setPlayerCooldown(@NotNull UUID id, @NotNull MAbility ability, int cooldown) {
        Map<MAbility, Integer> abilityCooldowns = cooldowns.get(id);
        if (abilityCooldowns != null) {
            abilityCooldowns.put(ability, cooldown);
        } else {
            abilityCooldowns = new HashMap<>();
            abilityCooldowns.put(ability, cooldown);
            cooldowns.put(id, abilityCooldowns);
        }
    }

    public void setPlayerCooldown(@NotNull Player player, @NotNull MAbility mAbility) {
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            double cooldown = getCooldown(mAbility, playerData);
            if (cooldown != 0) {
                setPlayerCooldown(player.getUniqueId(), mAbility, (int) (cooldown * 20));
            }
        }
    }

    //Gets cooldown
    public int getPlayerCooldown(@NotNull UUID id, MAbility ability) {
        Map<MAbility, Integer> abilityCooldowns = cooldowns.get(id);
        if (abilityCooldowns == null) {
            cooldowns.put(id, new HashMap<>());
            return 0;
        }
        Integer cooldown = abilityCooldowns.get(ability);
        if (cooldown != null) {
            return cooldown;
        } else {
            abilityCooldowns.put(ability, 0);
            return 0;
        }
    }

    //Gets if ability is ready
    public boolean isReady(@NotNull UUID id, @NotNull MAbility ability) {
        Map<MAbility, Boolean> readyMap = ready.get(id);
        if (readyMap == null) {
            ready.put(id, new HashMap<>());
            return false;
        }
        Boolean readyValue = readyMap.get(ability);
        if (readyValue != null) {
            return readyValue;
        } else {
            readyMap.put(ability, false);
            return false;
        }
    }

    //Gets the error timer
    public int getErrorTimer(@NotNull UUID id, @NotNull MAbility ability) {
        Map<MAbility, Integer> errorTimers = errorTimer.get(id);
        if (errorTimers == null) {
            errorTimer.put(id, new HashMap<>());
            return 0;
        }
        Integer timer = errorTimers.get(ability);
        if (timer != null) {
            return timer;
        } else {
            errorTimers.put(ability, 2);
            return 0;
        }
    }

    //Sets error timer
    public void setErrorTimer(@NotNull UUID id, @NotNull MAbility ability, int time) {
        Map<MAbility, Integer> errorTimers = errorTimer.computeIfAbsent(id, k -> new HashMap<>());
        errorTimers.put(ability, time);
    }

    //Gets if ability is ready
    public boolean isActivated(@NotNull UUID id, @NotNull MAbility ability) {
        Map<MAbility, Boolean> activatedMap = activated.get(id);
        if (activatedMap == null) {
            activated.put(id, new HashMap<>());
            return false;
        }
        Boolean activatedValue = activatedMap.get(ability);
        if (activatedValue != null) {
            return activatedValue;
        } else {
            activatedMap.put(ability, false);
            return false;
        }
    }

    //Sets ability ready status
    public void setReady(@NotNull UUID id, @NotNull MAbility ability, boolean isReady) {
        Map<MAbility, Boolean> readyMap = ready.computeIfAbsent(id, k -> new HashMap<>());
        readyMap.put(ability, isReady);
    }

    private void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : cooldowns.keySet()) {
                    Map<MAbility, Integer> abilityCooldowns = cooldowns.get(id);
                    if (abilityCooldowns != null) {
                        for (MAbility ab : abilityCooldowns.keySet()) {
                            Integer cooldown = abilityCooldowns.get(ab);
                            if (cooldown == null)
                                throw new IllegalStateException("Invalid ability cooldown index key: " + ab);
                            if (cooldown >= 2) {
                                abilityCooldowns.put(ab, cooldown - 2);
                            } else if (cooldown == 1) {
                                abilityCooldowns.put(ab, 0);
                            }
                            if (cooldown == 2 || cooldown == 1) {
                                @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(id);
                                if (playerData != null) {
                                    ManaAbilityRefreshEvent event = new ManaAbilityRefreshEvent(playerData.getPlayer(), ab);
                                    Bukkit.getPluginManager().callEvent(event);
                                }
                            }
                        }
                    } else {
                        cooldowns.put(id, new HashMap<>());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : errorTimer.keySet()) {
                    Map<MAbility, Integer> errorTimers = errorTimer.get(id);
                    if (errorTimers != null) {
                        for (MAbility ab : errorTimers.keySet()) {
                            Integer timer = errorTimers.get(ab);
                            if (timer == null)
                                throw new IllegalStateException("Invalid ability timer index key: " + timer);
                            if (timer > 0) {
                                errorTimers.put(ab, timer - 1);
                            }
                        }
                    } else {
                        errorTimer.put(id, new HashMap<>());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (!cooldowns.containsKey(id)) {
            cooldowns.put(id, new HashMap<>());
        }
        if (!ready.containsKey(id)) {
            ready.put(id, new HashMap<>());
        }
        if (!activated.containsKey(id)) {
            activated.put(id, new HashMap<>());
        }
        if (!errorTimer.containsKey(id)) {
            errorTimer.put(id, new HashMap<>());
        }
    }

    @EventHandler
    public void onLeave(@NotNull PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        // Remove cooldown map from memory if player has no cooldowns
        Map<MAbility, Integer> abilityCooldowns = cooldowns.get(id);
        if (abilityCooldowns != null) {
            boolean noCooldowns = true;
            for (Integer cooldown : abilityCooldowns.values()) {
                if (cooldown != 0) {
                    noCooldowns = false;
                    break;
                }
            }
            if (noCooldowns) {
                cooldowns.remove(id);
            }
        }
        // Remove activated map if player has no active mana abilities
        Map<MAbility, Boolean> activatedMap = activated.get(id);
        if (activatedMap != null) {
            boolean noneActivated = true;
            for (Boolean activated : activatedMap.values()) {
                if (activated) {
                    noneActivated = false;
                    break;
                }
            }
            if (noneActivated) {
                activated.remove(id);
            }
        }
        ready.remove(id);
        errorTimer.remove(id);
    }

    public double getValue(@NotNull MAbility mAbility, int level) {
        return getBaseValue(mAbility) + (getValuePerLevel(mAbility) * (level - 1));
    }

    public double getValue(@NotNull MAbility mAbility, @NotNull PlayerData playerData) {
        return getValue(mAbility, playerData.getManaAbilityLevel(mAbility));
    }

    public double getDisplayValue(@NotNull MAbility mAbility, int level) {
        if (mAbility == MAbility.SHARP_HOOK && getOptionAsBooleanElseTrue(mAbility, "display_damage_with_scaling")) {
            return getValue(mAbility, level) * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
        } else {
            return getValue(mAbility, level);
        }
    }

    public double getBaseValue(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getBaseValue();
        }
        return mAbility.getDefaultBaseValue();
    }

    public double getValuePerLevel(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getValuePerLevel();
        }
        return mAbility.getDefaultValuePerLevel();
    }

    public double getCooldown(@NotNull MAbility mAbility, int level) {
        double cooldown = getBaseCooldown(mAbility) + (getCooldownPerLevel(mAbility) * (level - 1));
        return cooldown > 0 ? cooldown : 0;
    }

    public double getCooldown(@NotNull MAbility mAbility, @NotNull PlayerData playerData) {
        double cooldown = getBaseCooldown(mAbility) + (getCooldownPerLevel(mAbility) * (playerData.getManaAbilityLevel(mAbility) - 1));
        return cooldown > 0 ? cooldown : 0;
    }

    public double getBaseCooldown(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getBaseCooldown();
        }
        return mAbility.getDefaultBaseCooldown();
    }

    public double getCooldownPerLevel(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getCooldownPerLevel();
        }
        return mAbility.getDefaultCooldownPerLevel();
    }

    public double getManaCost(@NotNull MAbility mAbility, @NotNull PlayerData playerData) {
        return getBaseManaCost(mAbility) + (getManaCostPerLevel(mAbility) * (playerData.getManaAbilityLevel(mAbility) - 1));
    }

    public double getManaCost(@NotNull MAbility mAbility, int level) {
        return getBaseManaCost(mAbility) + (getManaCostPerLevel(mAbility) * (level - 1));
    }

    public double getBaseManaCost(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getBaseManaCost();
        }
        return mAbility.getDefaultBaseManaCost();
    }

    public double getManaCostPerLevel(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getManaCostPerLevel();
        }
        return mAbility.getDefaultManaCostPerLevel();
    }

    public int getUnlock(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getUnlock();
        }
        return 7;
    }

    public int getLevelUp(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getLevelUp();
        }
        return 7;
    }

    public int getMaxLevel(@NotNull MAbility mAbility) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getMaxLevel();
        }
        return 0;
    }

    /**
     * Gets the mana ability unlocked or leveled up at a certain level
     * @param skill The skill
     * @param level The skill level
     * @return The mana ability unlocked or leveled up, or null
     */
    public @Nullable MAbility getManaAbility(@NotNull Skill skill, int level) {
        MAbility mAbility = skill.getManaAbility();
        if (mAbility != null) {
            if (level >= getUnlock(mAbility) && (level - getUnlock(mAbility)) % getLevelUp(mAbility) == 0) {
                return mAbility;
            }
        }
        return null;
    }

    public @Nullable OptionValue getOption(@NotNull MAbility mAbility, @NotNull String key) {
        ManaAbilityOption option = plugin.getAbilityManager().getAbilityOption(mAbility);
        if (option != null) {
            return option.getOption(key);
        } else {
            return mAbility.getDefaultOptions().get(key);
        }
    }

    public boolean getOptionAsBooleanElseTrue(@NotNull MAbility mAbility, @NotNull String key) {
        OptionValue value = getOption(mAbility, key);
        if (value != null) {
            return value.asBoolean();
        }
        return true;
    }

    public boolean getOptionAsBooleanElseFalse(@NotNull MAbility mAbility, @NotNull String key) {
        OptionValue value = getOption(mAbility, key);
        if (value != null) {
            return value.asBoolean();
        }
        return false;
    }

    public int getOptionAsInt(@NotNull MAbility mAbility, @NotNull String key, int defaultValue) {
        OptionValue value = getOption(mAbility, key);
        if (value != null) {
            return value.asInt();
        }
        return defaultValue;
    }

    public double getOptionAsDouble(@NotNull MAbility mAbility, @NotNull String key) {
        OptionValue value = getOption(mAbility, key);
        if (value != null) {
            return value.asDouble();
        }
        value = mAbility.getDefaultOptions().get(key);
        if (value == null)
            throw new IllegalStateException("Invalid option index key: " + key);
        return value.asDouble();
    }

    public @Nullable Set<@NotNull String> getOptionKeys(@NotNull MAbility mAbility) {
        if (!mAbility.getDefaultOptions().isEmpty()) {
            return mAbility.getDefaultOptions().keySet();
        }
        return null;
    }

}
