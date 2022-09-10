package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.CustomRegenEvent;
import com.archyx.aureliumskills.api.event.RegenReason;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.NotNull;

public class Regeneration implements Listener {

	private final @NotNull AureliumSkills plugin;
	
	public Regeneration(@NotNull AureliumSkills plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRegen(@NotNull EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player) {
			if (event.getRegainReason().equals(RegainReason.SATIATED)) {
				Player player = (Player) event.getEntity();
				//Check for disabled world
				if (!plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
					if (!OptionL.getBoolean(Option.REGENERATION_CUSTOM_REGEN_MECHANICS)) {
						PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
						if (playerData == null) return;
						if (player.getSaturation() > 0) {
							event.setAmount(event.getAmount() + (playerData.getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_SATURATED_MODIFIER)));
						} else if (player.getFoodLevel() == 20) {
							event.setAmount(event.getAmount() + (playerData.getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_HUNGER_FULL_MODIFIER)));
						} else if (player.getFoodLevel() >= 14) {
							event.setAmount(event.getAmount() + (playerData.getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_HUNGER_ALMOST_FULL_MODIFIER)));
						}
					} else {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	public void startRegen() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			if (OptionL.getBoolean(Option.REGENERATION_CUSTOM_REGEN_MECHANICS)) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
					if (playerData != null) {
						//Check for disabled world
						if (!plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
							if (!player.isDead()) {
								AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
								if (attribute != null) {
									if (player.getHealth() < attribute.getValue()) {
										if (player.getFoodLevel() >= 14 && player.getFoodLevel() < 20) {
											double amountGained = Math.min(OptionL.getDouble(Option.REGENERATION_BASE_REGEN) + playerData.getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_HUNGER_ALMOST_FULL_MODIFIER)
													, attribute.getValue() - player.getHealth());
											CustomRegenEvent event = new CustomRegenEvent(player, amountGained, RegenReason.HUNGER_FULL);
											Bukkit.getPluginManager().callEvent(event);
											if (!event.isCancelled()) {
												player.setHealth(player.getHealth() + amountGained);
												if (player.getFoodLevel() - 1 >= 0) {
													player.setFoodLevel(player.getFoodLevel() - 1);
												}
											}
										} else if (player.getFoodLevel() == 20 && player.getSaturation() == 0) {
											double amountGained = Math.min(OptionL.getDouble(Option.REGENERATION_BASE_REGEN) + playerData.getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_HUNGER_FULL_MODIFIER)
													, attribute.getValue() - player.getHealth());
											CustomRegenEvent event = new CustomRegenEvent(player, amountGained, RegenReason.HUNGER_ALMOST_FULL);
											Bukkit.getPluginManager().callEvent(event);
											if (!event.isCancelled()) {
												player.setHealth(player.getHealth() + amountGained);
												if (player.getFoodLevel() - 1 >= 0) {
													player.setFoodLevel(player.getFoodLevel() - 1);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}, 0L, OptionL.getInt(Option.REGENERATION_HUNGER_DELAY));
	}
	
	public void startSaturationRegen() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			if (OptionL.getBoolean(Option.REGENERATION_CUSTOM_REGEN_MECHANICS)) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
					if (playerData != null) {
						//Check for disabled world
						if (!plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
							if (!player.isDead()) {
								if (player.getSaturation() > 0 && player.getFoodLevel() >= 20) {
									AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
									if (attribute != null) {
										double amountGained = Math.min(OptionL.getDouble(Option.REGENERATION_BASE_REGEN) + playerData.getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_SATURATED_MODIFIER)
												, attribute.getValue() - player.getHealth());
										CustomRegenEvent event = new CustomRegenEvent(player, amountGained, RegenReason.SATURATED);
										Bukkit.getPluginManager().callEvent(event);
										if (!event.isCancelled()) {
											player.setHealth(player.getHealth() + amountGained);
										}
									}
								}
							}
						}
					}
				}
			}
		}, 0L, OptionL.getInt(Option.REGENERATION_SATURATED_DELAY));
	}
	
}
