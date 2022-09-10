package com.archyx.aureliumskills.skills.endurance;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.CustomRegenEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class EnduranceAbilities extends AbilityProvider implements Listener {

    private final Random r = new Random();

    public EnduranceAbilities(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.ENDURANCE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void antiHunger(@NotNull FoodLevelChangeEvent event) {
        if (blockDisabled(Ability.ANTI_HUNGER)) return;
        if (!event.isCancelled()) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (blockAbility(player)) return;
                // Checks if food level would be decreased
                if (player.getFoodLevel() > event.getFoodLevel()) {
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) return;
                    double chance = getValue(Ability.ANTI_HUNGER, playerData) / 100;
                    if (r.nextDouble() < chance) {
                        event.setFoodLevel(player.getFoodLevel());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void goldenHealAndRecovery(@NotNull EntityRegainHealthEvent event) {
        if (OptionL.isEnabled(Skills.ENDURANCE)) {
            if (!event.isCancelled()) {
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (blockAbility(player)) return;
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) return;
                    // Golden Heal
                    if (event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.MAGIC_REGEN)) {
                        if (isEnabled(Ability.GOLDEN_HEAL)) {
                            //Applies modifier
                            double modifier = getValue(Ability.GOLDEN_HEAL, playerData) / 100;
                            event.setAmount(event.getAmount() * (1 + modifier));
                        }
                    }
                    // Recovery
                    else if (event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
                        if (isEnabled(Ability.RECOVERY)) {
                            // Gets health
                            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (attribute != null) {
                                double currentHealth = player.getHealth();
                                double maxHealth = attribute.getValue();
                                //Checks if health is less than half of max
                                if (currentHealth < (maxHealth / 2)) {
                                    //Applies modifier
                                    double modifier = getValue(Ability.RECOVERY, playerData) / 100;
                                    event.setAmount(event.getAmount() * (1 + modifier));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void recoveryCustom(@NotNull CustomRegenEvent event) {
        if (!event.isCancelled()) {
            if (isEnabled(Ability.RECOVERY)) {
                Player player = event.getPlayer();
                @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData == null) return;
                // Gets health
                AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (attribute != null) {
                    double currentHealth = player.getHealth();
                    double maxHealth = attribute.getValue();
                    // Checks if health is less than half of max
                    if (currentHealth < (maxHealth / 2)) {
                        // Applies modifier
                        double modifier = getValue(Ability.RECOVERY, playerData) / 100;
                        event.setAmount(event.getAmount() * (1 + modifier));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void mealSteal(@NotNull EntityDamageByEntityEvent event) {
        if (blockDisabled(Ability.MEAL_STEAL)) return;
        if (!event.isCancelled()) {
            // Checks if entity and damager are players
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Player enemy = (Player) event.getEntity();
                if (blockAbility(player)) return;
                @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData == null) return;
                // Calculates chance
                double chance = getValue(Ability.MEAL_STEAL, playerData) / 100;
                if (r.nextDouble() < chance) {
                    // Removes food from enemy
                    if (enemy.getFoodLevel() >= 1) {
                        enemy.setFoodLevel(enemy.getFoodLevel() - 1);
                    }
                    // Adds food level to player
                    if (player.getFoodLevel() < 20) {
                        player.setFoodLevel(player.getFoodLevel() + 1);
                    }
                    // Adds saturation if food is full
                    else if (player.getSaturation() < 20f) {
                        player.setSaturation(player.getSaturation() + 1f);
                    }
                }
            }
        }
    }

}
