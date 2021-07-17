package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.Skills;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class LightningBlade extends ReadiedManaAbility {

    public LightningBlade(AureliumSkills plugin) {
        super(plugin, MAbility.LIGHTNING_BLADE, ManaAbilityMessage.LIGHTNING_BLADE_START, ManaAbilityMessage.LIGHTNING_BLADE_END,
                new String[] {"SWORD"}, new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK});

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void activationListener(EntityDamageByEntityEvent event) {
        if (!OptionL.isEnabled(Skills.FIGHTING)) return;
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (blockAbility(player)) return;
        //If player used sword
        if (!isHoldingMaterial(player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        // Checks if already activated
        if (isActivated(player)) {
            return;
        }
        // Checks if ready
        if (isReady(player)) {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            if (hasEnoughMana(player)) {
                activate(player);
            }
        }
    }

    @Override
    protected int getDuration(PlayerData playerData) {
        double baseDuration = manager.getOptionAsDouble(mAbility, "base_duration");
        double durationPerLevel = manager.getOptionAsDouble(mAbility, "duration_per_level");
        double durationSeconds = baseDuration + (durationPerLevel * (playerData.getManaAbilityLevel(mAbility) - 1));
        return (int) Math.round(durationSeconds * 20);
    }

    @Override
    public void onActivate(Player player, PlayerData playerData) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute == null) return;

        // Remove existing modifier if exists
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifier.getName().equals("AureliumSkills-LightningBlade")) {
                attribute.removeModifier(modifier);
            }
        }
        // Increase attack speed attribute
        double currentValue = attribute.getValue();
        double addedValue = plugin.getManaAbilityManager().getValue(MAbility.LIGHTNING_BLADE, playerData) / 100 * currentValue;
        attribute.addModifier(new AttributeModifier("AureliumSkills-LightningBlade", addedValue, AttributeModifier.Operation.ADD_NUMBER));
        // Play sound and send message
        if (XMaterial.isNewVersion()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.5f, 1);
        } else {
            player.playSound(player.getLocation(), "entity.illusion_illager.prepare_mirror", 0.5f, 1);
        }
    }

    @Override
    public void onStop(Player player, PlayerData playerData) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute == null) return;
        // Remove modifier if exists
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifier.getName().equals("AureliumSkills-LightningBlade")) {
                attribute.removeModifier(modifier);
            }
        }
    }

    @EventHandler
    public void lightningBladeJoin(PlayerJoinEvent event) {
        // Only remove if not activated
        Player player = event.getPlayer();
        if (plugin.getManaAbilityManager().isActivated(player.getUniqueId(), MAbility.LIGHTNING_BLADE)) {
            return;
        }
        // Remove attack speed attribute modifier
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute == null) return;
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifier.getName().equals("AureliumSkills-LightningBlade")) {
                attribute.removeModifier(modifier);
            }
        }
    }

}
