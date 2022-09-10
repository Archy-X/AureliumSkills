package com.archyx.aureliumskills.skills.archery;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArcheryLeveler extends SkillLeveler implements Listener {

	public ArcheryLeveler(@NotNull AureliumSkills plugin) {
		super(plugin, Ability.ARCHER);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(@NotNull EntityDeathEvent event) {
		if (OptionL.isEnabled(Skills.ARCHERY)) {
			if (OptionL.getBoolean(Option.ARCHERY_DAMAGE_BASED)) return;
			LivingEntity e = event.getEntity();
			if (e.getKiller() != null) {
				if (e.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					@Nullable EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e.getLastDamageCause();
					if (ee != null && ee.getDamager() instanceof Projectile) {
						EntityType type = e.getType();
						@Nullable Player p = e.getKiller();
						Skill skill = Skills.ARCHERY;
						if (ee.getDamager() instanceof ThrownPotion) {
							if (OptionL.getBoolean(Option.ALCHEMY_GIVE_XP_ON_POTION_COMBAT)) { // Reward alchemy if potion used
								skill = Skills.ALCHEMY;
							} else {
								return;
							}
						}
						if (p == null)
							return;
						if (blockXpGainLocation(e.getLocation(), p)) return;
						if (blockXpGainPlayer(p)) return;
						if (e.equals(p)) return;
						double spawnerMultiplier = OptionL.getDouble(Option.ARCHERY_SPAWNER_MULTIPLIER);
						try {
							if (e.hasMetadata("aureliumskills_spawner_mob")) {
								plugin.getLeveler().addXp(p, skill, spawnerMultiplier * getXp(p, ArcherySource.valueOf(type.toString())));
							} else {
								plugin.getLeveler().addXp(p, skill, getXp(p, ArcherySource.valueOf(type.toString())));
							}
						} catch (IllegalArgumentException exception) {
							if (type.toString().equals("PIG_ZOMBIE")) {
								if (e.hasMetadata("aureliumskills_spawner_mob")) {
									plugin.getLeveler().addXp(p, skill, spawnerMultiplier * getXp(p, ArcherySource.ZOMBIFIED_PIGLIN));
								} else {
									plugin.getLeveler().addXp(p, skill, getXp(p, ArcherySource.ZOMBIFIED_PIGLIN));
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(@NotNull EntityDamageByEntityEvent event) {
		// Damage based listener
		if (OptionL.isEnabled(Skills.ARCHERY)) {
			if (event.isCancelled()) return;
			if (!OptionL.getBoolean(Option.ARCHERY_DAMAGE_BASED)) return;
			if (event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				Skill skill = Skills.ARCHERY;
				if (projectile instanceof ThrownPotion) {
					if (OptionL.getBoolean(Option.ALCHEMY_GIVE_XP_ON_POTION_COMBAT)) { // Reward alchemy if potion used
						skill = Skills.ALCHEMY;
					} else {
						return;
					}
				}
				if (projectile.getShooter() instanceof Player) {
					@Nullable Player player = (Player) projectile.getShooter();
					if (player != null && event.getEntity() instanceof LivingEntity) {
						LivingEntity entity = (LivingEntity) event.getEntity();
						if (blockXpGainLocation(entity.getLocation(), player)) return;
						EntityType type = entity.getType();
						if (blockXpGainPlayer(player)) return;
						if (entity.equals(player)) return;
						double health = entity.getHealth();
						double damage = Math.min(health, event.getFinalDamage());
						// Apply spawner multiplier
						if (entity.hasMetadata("aureliumskills_spawner_mob")) {
							double spawnerMultiplier = OptionL.getDouble(Option.ARCHERY_SPAWNER_MULTIPLIER);
							damage *= spawnerMultiplier;
						}
						try {
							plugin.getLeveler().addXp(player, skill, damage * getXp(player, ArcherySource.valueOf(type.toString())));
						} catch (IllegalArgumentException e) {
							if (type.toString().equals("PIG_ZOMBIE")) {
								plugin.getLeveler().addXp(player, skill, damage * getXp(player, ArcherySource.ZOMBIFIED_PIGLIN));
							}
						}
					}
				}
			}
		}
	}

}
