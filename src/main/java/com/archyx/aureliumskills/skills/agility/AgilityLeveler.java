package com.archyx.aureliumskills.skills.agility;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.skills.Skills;
import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class AgilityLeveler extends SkillLeveler implements Listener {
	
	private final Set<UUID> prevPlayersOnGround = Sets.newHashSet();
	
	public AgilityLeveler(@NotNull AureliumSkills plugin) {
		super(plugin, Ability.JUMPER);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	@SuppressWarnings("deprecation")
	public void onFall(@NotNull EntityDamageEvent event) {
		if (OptionL.isEnabled(Skills.AGILITY)) {
			//Check cancelled
			if (OptionL.getBoolean(Option.AGILITY_CHECK_CANCELLED)) {
				if (event.isCancelled()) {
					return;
				}
			}
			if (event.getCause().equals(DamageCause.FALL)) {
				if (event.getEntity() instanceof Player) {
					Player player = (Player) event.getEntity();
					if (blockXpGain(player)) return;
					if (event.getFinalDamage() < player.getHealth()) {
						plugin.getLeveler().addXp(player, Skills.AGILITY, getXp(player, event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE) * getXp(AgilitySource.FALL_DAMAGE)));
					}
				}
			}
		}
	}
	
    @EventHandler
	@SuppressWarnings("deprecation")
    public void onMove(@NotNull PlayerMoveEvent e) {
    	if (OptionL.isEnabled(Skills.AGILITY)) {
    		//Check cancelled
    		if (OptionL.getBoolean(Option.AGILITY_CHECK_CANCELLED)) {
    			if (e.isCancelled()) {
    				return;
				}
			}
			Player player = e.getPlayer();
	        if (player.getVelocity().getY() > 0) {
	            double jumpVelocity = 0.42F;
	            if (player.hasPotionEffect(PotionEffectType.JUMP)) {
					PotionEffect effect = player.getPotionEffect(PotionEffectType.JUMP);
					if (effect != null) {
						jumpVelocity += ((float) (effect.getAmplifier() + 1) * 0.1F);
					}
	            }
	            if (e.getPlayer().getLocation().getBlock().getType() != Material.LADDER && prevPlayersOnGround.contains(player.getUniqueId())) {
	                if (!player.isOnGround() && Double.compare(player.getVelocity().getY(), jumpVelocity) == 0) {
	                	if (player.hasMetadata("skillsJumps")) {
	                		player.setMetadata("skillsJumps", new FixedMetadataValue(plugin, player.getMetadata("skillsJumps").get(0).asInt() + 1));
	                		if (player.getMetadata("skillsJumps").get(0).asInt() >= 100) {
								if (blockXpGain(player)) return;
								plugin.getLeveler().addXp(player, Skills.AGILITY, getXp(player, AgilitySource.JUMP_PER_100));
	                			player.removeMetadata("skillsJumps", plugin);
	                		}
	                	}
	                	else {
	                		player.setMetadata("skillsJumps", new FixedMetadataValue(plugin, 1));
	                	}
	                }
	            }
	        }
	        if (player.isOnGround()) {
	            prevPlayersOnGround.add(player.getUniqueId());
	        } else {
	            prevPlayersOnGround.remove(player.getUniqueId());
	        }
    	}
    }
	
}
