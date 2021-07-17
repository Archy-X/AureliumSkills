package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.ManaAbilityActivateEvent;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.sorcery.SorceryLeveler;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class ManaAbilityProvider extends AbilityProvider implements Listener {

    protected final AureliumSkills plugin;
    protected final ManaAbilityManager manager;
    protected final MAbility mAbility;
    protected final Skill skill;
    protected final SorceryLeveler sorceryLeveler;
    protected final ManaAbilityMessage activateMessage;
    protected final ManaAbilityMessage stopMessage;

    public ManaAbilityProvider(AureliumSkills plugin, MAbility mAbility, ManaAbilityMessage activateMessage, @Nullable ManaAbilityMessage stopMessage) {
        super(plugin, mAbility.getSkill());
        this.plugin = plugin;
        this.manager = plugin.getManaAbilityManager();
        this.mAbility = mAbility;
        this.skill = mAbility.getSkill();
        this.sorceryLeveler = plugin.getSorceryLeveler();
        this.activateMessage = activateMessage;
        this.stopMessage = stopMessage;
    }

    public void activate(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;

        int duration = getDuration(playerData);
        ManaAbilityActivateEvent event = new ManaAbilityActivateEvent(player, mAbility, duration);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        manager.setActivated(player, mAbility, true);

        onActivate(player, playerData); // Mana ability specific behavior is run
        consumeMana(player, playerData);

        if (duration != 0) {
            //Schedules stop
            new BukkitRunnable() {
                @Override
                public void run() {
                    stop(player);
                    manager.setActivated(player, mAbility, false);
                    manager.setReady(player.getUniqueId(), mAbility, false);
                }
            }.runTaskLater(plugin, duration);
        } else {
            stop(player);
            manager.setActivated(player, mAbility, false);
            manager.setReady(player.getUniqueId(), mAbility, false);
        }
    }

    public abstract void onActivate(Player player, PlayerData playerData);

    public void stop(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        onStop(player, playerData); // Mana ability specific stop behavior is run
        manager.setPlayerCooldown(player, mAbility); // Apply cooldown
        // Send stop message if applicable
        if (stopMessage != null) {
            plugin.getAbilityManager().sendMessage(player, Lang.getMessage(stopMessage, plugin.getLang().getLocale(player)));
        }
    }

    public abstract void onStop(Player player, PlayerData playerData);

    protected int getDuration(PlayerData playerData) {
        return (int) Math.round(getValue(mAbility, playerData) * 20);
    }

    protected void consumeMana(Player player, PlayerData playerData) {
        double manaConsumed = manager.getManaCost(mAbility, playerData);
        playerData.setMana(playerData.getMana() - manaConsumed);
        sorceryLeveler.level(player, manaConsumed);
        plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(activateMessage, playerData.getLocale())
                ,"{mana}", NumberUtil.format0(manaConsumed)));
    }

    // Returns true if player has enough mana
    protected boolean hasEnoughMana(Player player) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return false;
        Locale locale = playerData.getLocale();
        if (playerData.getMana() >= plugin.getManaAbilityManager().getManaCost(mAbility, playerData)) {
            return true;
        }
        else {
            plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(ManaAbilityMessage.NOT_ENOUGH_MANA, locale)
                    ,"{mana}", NumberUtil.format0(plugin.getManaAbilityManager().getManaCost(mAbility, playerData))
                    , "{current_mana}", String.valueOf(Math.round(playerData.getMana()))
                    , "{max_mana}", String.valueOf(Math.round(playerData.getMaxMana()))));
            return false;
        }
    }

}
