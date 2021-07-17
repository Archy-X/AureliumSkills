package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.awt.*;

public class Absorption extends ReadiedManaAbility {

    public Absorption(AureliumSkills plugin) {
        super(plugin, MAbility.ABSORPTION, ManaAbilityMessage.ABSORPTION_START, ManaAbilityMessage.ABSORPTION_END,
                new String[] {"SHIELD"}, new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK});
    }

    @Override
    public void onActivate(Player player, PlayerData playerData) {
        playerData.getAbilityData(MAbility.ABSORPTION).setData("activated", true); // Register as absorption activated
        // Play sound
        if (XMaterial.isNewVersion()) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }
    }

    @Override
    public void onStop(Player player, PlayerData playerData) {
        playerData.getAbilityData(MAbility.ABSORPTION).setData("activated", false);
    }

    public void handleAbsorption(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
        if (playerData.getAbilityData(MAbility.ABSORPTION).getBoolean("activated") && isActivated(player)) {
            handleAbsorbedHit(event, player, playerData);
        } else if (isReady(player)) {
            // Activate ability if ready
            if (isActivated(player)) {
                return;
            }
            if (hasEnoughMana(player)) {
                activate(player);
                handleAbsorbedHit(event, player, playerData);
            }
        }
    }

    private void handleAbsorbedHit(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
        // Decrease mana and cancel event
        double mana = playerData.getMana() - event.getDamage() * 2;
        if (mana > 0) {
            playerData.setMana(mana);
            event.setCancelled(true);
            // Particle effects and sound
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_HURT, 1f, 1f);
            if (plugin.getManaAbilityManager().getOptionAsBooleanElseTrue(MAbility.ABSORPTION, "enable_particles")) {
                XParticle.circle(1, 1, 1, 20, 0, ParticleDisplay.colored(player.getLocation().add(0, 1, 0), Color.MAGENTA, 1));
            }
        }
    }

}
