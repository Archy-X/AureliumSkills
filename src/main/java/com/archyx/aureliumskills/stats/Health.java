package com.archyx.aureliumskills.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.skills.agility.AgilityAbilities;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Health implements Listener {

	private final AureliumSkills plugin;
	private final AgilityAbilities agilityAbilities;
	private final Map<UUID, Double> worldChangeHealth = new HashMap<>();
	private final Map<Integer, Double> hearts = new HashMap<>();
	private static final double threshold = 0.1;

	public Health(AureliumSkills plugin) {
		this.plugin = plugin;
		this.agilityAbilities = new AgilityAbilities(plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		applyScaling(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLoad(PlayerDataLoadEvent event) {
		setHealth(event.getPlayerData().getPlayer());
	}

	public void reload(Player player) {
		if (player != null) {
			setHealth(player);
			agilityAbilities.removeFleeting(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void worldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		if (plugin.getWorldManager().isInDisabledWorld(player.getLocation()) && !plugin.getWorldManager().isDisabledWorld(event.getFrom())) {
			worldChangeHealth.put(player.getUniqueId(), player.getHealth());
		}
		if (OptionL.getInt(Option.HEALTH_UPDATE_DELAY) > 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					setHealth(player);
					if (plugin.getWorldManager().isDisabledWorld(event.getFrom()) && !plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
						if (worldChangeHealth.containsKey(player.getUniqueId())) {
							Double health = worldChangeHealth.get(player.getUniqueId());
							if (health == null)
								throw new IllegalStateException("Invalid player index key for: " + player.getName());
							player.setHealth(health);
							worldChangeHealth.remove(player.getUniqueId());
						}
					}
				}
			}.runTaskLater(plugin, OptionL.getInt(Option.HEALTH_UPDATE_DELAY));
		} else {
			setHealth(player);
			if (plugin.getWorldManager().isDisabledWorld(event.getFrom()) && !plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
				if (worldChangeHealth.containsKey(player.getUniqueId())) {
					Double health = worldChangeHealth.get(player.getUniqueId());
					if (health == null)
						throw new IllegalStateException("Invalid player index key for: " + player.getName());
					player.setHealth(health);
					worldChangeHealth.remove(player.getUniqueId());
				}
			}
		}
	}

	private void setHealth(Player player) {
		// Calculates the amount of health to add
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData == null) return;
		double modifier = (playerData.getStatLevel(Stats.HEALTH)) * OptionL.getDouble(Option.HEALTH_MODIFIER);
		AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (attribute == null) return;
		double originalMaxHealth = attribute.getValue();
		boolean hasChange = true;
		// Removes existing modifiers of the same name and check for change
		for (AttributeModifier am : attribute.getModifiers()) {
			if (am.getName().equals("skillsHealth")) {
				// Check for any changes, if not, return
				if (Math.abs(originalMaxHealth - (originalMaxHealth - am.getAmount() + modifier)) <= threshold) {
					hasChange = false;
				}
				// Removes if has change
				if (hasChange) {
					attribute.removeModifier(am);
				}
			}
		}
		// Disable health if in disable world
		if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
			player.setHealthScaled(false);
			for (AttributeModifier am : attribute.getModifiers()) {
				if (am.getName().equals("skillsHealth")) {
					attribute.removeModifier(am);
				}
			}
			return;
		}
		// Force base health if enabled
		if (OptionL.getBoolean(Option.HEALTH_FORCE_BASE_HEALTH)) {
			attribute.setBaseValue(20.0);
		}
		// Return if no change
		if (hasChange) {
			// Applies modifier
			attribute.addModifier(new AttributeModifier("skillsHealth", modifier, Operation.ADD_NUMBER));
			// Sets health to max if over max
			if (player.getHealth() > attribute.getValue()) {
				player.setHealth(attribute.getValue());
			}
			if (OptionL.getBoolean(Option.HEALTH_KEEP_FULL_ON_INCREASE) && attribute.getValue() > originalMaxHealth) {
				// Heals player to full health if had full health before modifier
				if (player.getHealth() >= originalMaxHealth) {
					player.setHealth(attribute.getValue());
				}
			}
		}
		applyScaling(player);
	}

	private void applyScaling(Player player) {
		AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (attribute == null) return;
		if (OptionL.getBoolean(Option.HEALTH_HEALTH_SCALING)) {
			double health = attribute.getValue();
			player.setHealthScaled(true);
			int scaledHearts = 0;
			for (Integer heartNum : hearts.keySet()) {
				Double healthNum = hearts.get(heartNum);
				if (healthNum == null)
					throw new IllegalStateException("Invalid heart index key [" + heartNum + "] for player: " + player.getName());
				if (health >= healthNum) {
					if (heartNum > scaledHearts) {
						scaledHearts = heartNum;
					}
				}
			}
			if (scaledHearts == 0) {
				scaledHearts = 10;
			}
			player.setHealthScale(scaledHearts * 2);
		} else {
			player.setHealthScaled(false);
		}
	}

	public void loadHearts(FileConfiguration config) {
		// Load default hearts
		this.hearts.clear();
		this.hearts.put(10, 0.0);
		this.hearts.put(11, 24.0);
		this.hearts.put(12, 29.0);
		this.hearts.put(13, 37.0);
		this.hearts.put(14, 50.0);
		this.hearts.put(15, 71.0);
		this.hearts.put(16, 105.0);
		this.hearts.put(17, 160.0);
		this.hearts.put(18, 249.0);
		this.hearts.put(19, 393.0);
		this.hearts.put(20, 626.0);
		// Load hearts from config
		ConfigurationSection heartsSection = config.getConfigurationSection("health.hearts");
		if (heartsSection != null) {
			try {
				for (String key : heartsSection.getKeys(false)) {
					int heartsNum = Integer.parseInt(key);
					double healthNum = heartsSection.getDouble(key, -1.0);
					if (healthNum != -1.0) {
						this.hearts.put(heartsNum, healthNum);
					}
				}
			} catch (Exception e) {
				Bukkit.getLogger().warning("[AureliumSkills] There was an error loading health.hearts data! Check to make sure the keys are only integers and the values are only numbers.");
			}
		}
	}

}
