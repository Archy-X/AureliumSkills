package com.archyx.aureliumskills.skills.defense;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefenseLeveler extends SkillLeveler implements Listener {

	public DefenseLeveler(@NotNull AureliumSkills plugin) {
		super(plugin, Ability.DEFENDER);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	@SuppressWarnings("deprecation")
	public void onDamage(@NotNull EntityDamageByEntityEvent event) {
		if (OptionL.isEnabled(Skills.DEFENSE)) {
			//Checks cancelled
			if (OptionL.getBoolean(Option.DEFENSE_CHECK_CANCELLED)) {
				if (event.isCancelled()) {
					return;
				}
			}
			if (event.getEntity() instanceof Player) {
				if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
					Player player = (Player) event.getEntity();
					if (blockXpGain(player)) return;
					if (!OptionL.getBoolean(Option.DEFENSE_ALLOW_SHIELD_BLOCKING)) { // Check for shield blocking
						if (player.isBlocking()) {
							return;
						}
					}
					Skill skill = Skills.DEFENSE;
					double originalDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
					if (event.getFinalDamage() < player.getHealth()) {
						//Player Damage
						if (event.getDamager() instanceof Player) {
							if (event.getDamager().equals(player)) return;
							if (originalDamage * getXp(DefenseSource.PLAYER_DAMAGE) <= OptionL.getDouble(Option.DEFENSE_MAX)) {
								if (originalDamage * getXp(DefenseSource.PLAYER_DAMAGE) >= OptionL.getDouble(Option.DEFENSE_MIN)) {
									plugin.getLeveler().addXp(player, skill, originalDamage * getXp(player, DefenseSource.PLAYER_DAMAGE));
								}
							} else {
								plugin.getLeveler().addXp(player, skill, getXp(player, OptionL.getDouble(Option.DEFENSE_MAX)));
							}
						}
						//Mob damage
						else {
							// Make sure player didn't cause own damage
							if (event.getDamager() instanceof Projectile) {
								Projectile projectile = (Projectile) event.getDamager();
								@Nullable ProjectileSource shooter = projectile.getShooter();
								if (shooter != null && projectile.getShooter() instanceof Player) {
									if (shooter.equals(player)) return;
								}
							}
							if (originalDamage * getXp(DefenseSource.MOB_DAMAGE) <= OptionL.getDouble(Option.DEFENSE_MAX)) {
								if (originalDamage * getXp(DefenseSource.MOB_DAMAGE) >= OptionL.getDouble(Option.DEFENSE_MIN)) {
									plugin.getLeveler().addXp(player, skill, originalDamage * getXp(player, DefenseSource.MOB_DAMAGE));
								}
							} else {
								plugin.getLeveler().addXp(player, skill, getXp(player, OptionL.getDouble(Option.DEFENSE_MAX)));
							}
						}
					}
				}
			}
		}
	}
}
