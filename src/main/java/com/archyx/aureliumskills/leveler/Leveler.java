package com.archyx.aureliumskills.leveler;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.api.event.SkillLevelUpEvent;
import com.archyx.aureliumskills.api.event.XpGainEvent;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.LevelerMessage;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.rewards.CommandReward;
import com.archyx.aureliumskills.rewards.MoneyReward;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.StatLeveler;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class Leveler {
	
	private final AureliumSkills plugin;
	private final XpRequirements xpRequirements;
	private final StatLeveler statLeveler;
	private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

	public Leveler(AureliumSkills plugin) {
		this.plugin = plugin;
		this.xpRequirements = new XpRequirements(plugin);
		this.statLeveler = new StatLeveler(plugin);
	}
	
	public void loadLevelRequirements() {
		xpRequirements.loadXpRequirements();
	}

	public double getMultiplier(Player player, Skill skill) {
		double multiplier = 1.0;
		if (skill != null && !OptionL.getBoolean(Option.valueOf(skill + "_CHECK_MULTIPLIER_PERMISSIONS"))) { // Disable check option
			return multiplier;
		}
		// Add permission multipliers
		for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
			String permission = info.getPermission().toLowerCase(Locale.ROOT);
			if (permission.startsWith("aureliumskills.multiplier.")) {
				permission = TextUtil.replace(permission, "aureliumskills.multiplier.", "");
				if (pattern.matcher(permission).matches()) { // Parse all skills multiplier
					multiplier += Double.parseDouble(permission) / 100;
				} else if (skill != null) { // Skill specific multiplier
					String skillName = skill.toString().toLowerCase(Locale.ROOT);
					if (permission.startsWith(skillName)) {
						permission = TextUtil.replace(permission, skillName + ".", "");
						if (pattern.matcher(permission).matches()) {
							multiplier += Double.parseDouble(permission) / 100;
						}
					}
				}
			}
		}
		// Add multiplier modifiers
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData != null) {
			multiplier += playerData.getTotalMultiplier(skill) / 100;
		}
		return multiplier;
	}

	public double getMultiplier(Player player) {
		return getMultiplier(player, null);
	}

	//Method for adding xp with a defined amount
	public void addXp(Player player, Skill skill, double amount) {
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		//Checks if player has a skill profile for safety
		if (playerData != null) {
			//Checks if xp amount is not zero
			if (amount != 0) {
				//Gets xp amount
				double xpAmount = amount * getMultiplier(player, skill);
				//Calls event
				XpGainEvent event = new XpGainEvent(player, skill, xpAmount);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					//Adds xp
					playerData.addSkillXp(skill, event.getAmount());
					//Check if player leveled up
					checkLevelUp(player, skill);
					//Sends action bar message
					plugin.getActionBar().sendXpActionBar(player, skill, event.getAmount());
					// Sends boss bar if enabled
					sendBossBar(player, skill, playerData);
				}
			}
		}
	}

	private void sendBossBar(Player player, Skill skill, PlayerData playerData) {
		if (OptionL.getBoolean(Option.BOSS_BAR_ENABLED)) {
			// Check whether boss bar should update
			plugin.getBossBar().incrementAction(player, skill);
			int currentAction = plugin.getBossBar().getCurrentAction(player, skill);
			if (currentAction != -1 && currentAction % OptionL.getInt(Option.BOSS_BAR_UPDATE_EVERY) == 0) {
				int level = playerData.getSkillLevel(skill);
				boolean notMaxed = xpRequirements.getListSize(skill) > playerData.getSkillLevel(skill) - 1 && level < OptionL.getMaxLevel(skill);
				if (notMaxed) {
					plugin.getBossBar().sendBossBar(player, skill, playerData.getSkillXp(skill), xpRequirements.getXpRequired(skill, level + 1), level, false);
				} else {
					plugin.getBossBar().sendBossBar(player, skill, 1, 1, level, true);
				}
			}
		}
	}

	//Method for setting xp with a defined amount
	public void setXp(Player player, Skill skill, double amount) {
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		//Checks if player has a skill profile for safety
		if (playerData != null) {
			double originalAmount = playerData.getSkillXp(skill);
			//Sets Xp
			playerData.setSkillXp(skill, amount);
			//Check if player leveled up
			checkLevelUp(player, skill);
			//Sends action bar message
			plugin.getActionBar().sendXpActionBar(player, skill, amount - originalAmount);
			// Sends boss bar if enabled
			sendBossBar(player, skill, playerData);
		}
	}
	
	public void updateStats(Player player) {
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData == null) return;
		for (Stat stat : plugin.getStatRegistry().getStats()) {
			playerData.setStatLevel(stat, 0);
		}
		for (Skill skill : plugin.getSkillRegistry().getSkills()) {
			plugin.getRewardManager().getRewardTable(skill).applyStats(playerData, playerData.getSkillLevel(skill));
		}
		// Reloads modifiers
		for (String key : playerData.getStatModifiers().keySet()) {
			StatModifier modifier = playerData.getStatModifiers().get(key);
			if (modifier == null)
				throw new IllegalStateException("Invalid stat modifier index key: " + key);
			playerData.addStatLevel(modifier.getStat(), modifier.getValue());
		}
		statLeveler.reloadStat(player, Stats.HEALTH);
		statLeveler.reloadStat(player, Stats.WISDOM);
	}

	public void updatePermissions(Player player) {
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData == null) return;
		for (Skill skill : plugin.getSkillRegistry().getSkills()) {
			plugin.getRewardManager().getRewardTable(skill).applyPermissions(player, playerData.getSkillLevel(skill));
		}
	}

	public void applyLevelUpCommands(Player player, Skill skill, int oldLevel, int newLevel) {
		if (newLevel > oldLevel) {
			for (int i = oldLevel + 1; i <= newLevel; i++) {
				for (CommandReward reward : plugin.getRewardManager().getRewardTable(skill).searchRewards(CommandReward.class, i)) {
					reward.giveReward(player, skill, i);
				}
			}
		}
	}

	public void applyRevertCommands(Player player, Skill skill, int oldLevel, int newLevel) {
		if (newLevel < oldLevel) {
			for (int i = oldLevel; i > newLevel; i--) {
				for (CommandReward reward : plugin.getRewardManager().getRewardTable(skill).searchRewards(CommandReward.class, i)) {
					reward.executeRevert(player, skill, i);
				}
			}
		}
	}

	public void checkLevelUp(Player player, Skill skill) {
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData == null) return;
		int currentLevel = playerData.getSkillLevel(skill);
		double currentXp = playerData.getSkillXp(skill);
		if (currentLevel < OptionL.getMaxLevel(skill)) { //Check max level options
			if (xpRequirements.getListSize(skill) > currentLevel - 1) {
				if (currentXp >= xpRequirements.getXpRequired(skill, currentLevel + 1)) {
					levelUpSkill(playerData, skill);
				}
			}
		}
	}

	private void levelUpSkill(PlayerData playerData, Skill skill) {
		Player player = playerData.getPlayer();
		Locale locale = playerData.getLocale();

		double currentXp = playerData.getSkillXp(skill);
		int level = playerData.getSkillLevel(skill) + 1;

		playerData.setSkillXp(skill, currentXp - xpRequirements.getXpRequired(skill, level));
		playerData.setSkillLevel(skill, level);
		// Give custom rewards
		List<Reward> rewards = plugin.getRewardManager().getRewardTable(skill).getRewards(level);
		for (Reward reward : rewards) {
			reward.giveReward(player, skill, level);
		}
		// Adds money rewards if enabled
		if (plugin.isVaultEnabled()) {
			if (OptionL.getBoolean(Option.SKILL_MONEY_REWARDS_ENABLED)) {
				Economy economy = plugin.getEconomy();
				double base = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_BASE);
				double multiplier = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_MULTIPLIER);
				economy.depositPlayer(player, base + (multiplier * level * level));
			}
		}
		// Reload items and armor to check for newly met requirements
		plugin.getModifierManager().reloadPlayer(player);
		// Calls event
		SkillLevelUpEvent event = new SkillLevelUpEvent(player, skill, level);
		Bukkit.getPluginManager().callEvent(event);
		// Sends messages
		if (OptionL.getBoolean(Option.LEVELER_TITLE_ENABLED)) {
			sendTitle(player, locale, skill, level);
		}
		if (OptionL.getBoolean(Option.LEVELER_SOUND_ENABLED)) {
			playSound(player);
		}
		player.sendMessage(getLevelUpMessage(player, playerData, skill, level, locale, rewards));
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> checkLevelUp(player, skill), OptionL.getInt(Option.LEVELER_DOUBLE_CHECK_DELAY));
	}

	private void sendTitle(Player player, Locale locale, Skill skill, int level) {
		player.sendTitle(TextUtil.replace(Lang.getMessage(LevelerMessage.TITLE, locale),
						"{skill}", skill.getDisplayName(locale),
						"{old}", RomanNumber.toRoman(level - 1),
						"{new}", RomanNumber.toRoman(level)),
				TextUtil.replace(Lang.getMessage(LevelerMessage.SUBTITLE, locale),
						"{old}", RomanNumber.toRoman(level - 1),
						"{new}", RomanNumber.toRoman(level),
						"{skill}", skill.getDisplayName(locale))
				, OptionL.getInt(Option.LEVELER_TITLE_FADE_IN), OptionL.getInt(Option.LEVELER_TITLE_STAY), OptionL.getInt(Option.LEVELER_TITLE_FADE_OUT));
	}

	private void playSound(Player player) {
		try {
			player.playSound(player.getLocation(), Sound.valueOf(OptionL.getString(Option.LEVELER_SOUND_TYPE))
					, SoundCategory.valueOf(OptionL.getString(Option.LEVELER_SOUND_CATEGORY))
					, (float) OptionL.getDouble(Option.LEVELER_SOUND_VOLUME), (float) OptionL.getDouble(Option.LEVELER_SOUND_PITCH));
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[AureliumSkills] Error playing level up sound (Check config) Played the default sound instead");
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1f, 0.5f);
		}
	}

	private String getLevelUpMessage(Player player, PlayerData playerData, Skill skill, int newLevel, Locale locale, List<Reward> rewards) {
		String message = TextUtil.replace(Lang.getMessage(LevelerMessage.LEVEL_UP, locale)
				,"{skill}", skill.getDisplayName(locale)
				,"{old}", RomanNumber.toRoman(newLevel - 1)
				,"{new}", RomanNumber.toRoman(newLevel));
		if (plugin.isPlaceholderAPIEnabled()) {
			message = PlaceholderAPI.setPlaceholders(player, message);
		}
		StringBuilder rewardMessage = new StringBuilder();
		for (Reward reward : rewards) {
			rewardMessage.append(reward.getChatMessage(player, locale, skill, newLevel));
		}
		// Ability unlocks and level ups
		StringBuilder abilityUnlockMessage = new StringBuilder();
		StringBuilder abilityLevelUpMessage = new StringBuilder();
		for (Ability ability : plugin.getAbilityManager().getAbilities(skill, newLevel)) {
			if (plugin.getAbilityManager().isEnabled(ability)) {
				if (plugin.getAbilityManager().getUnlock(ability) == newLevel) {
					abilityUnlockMessage.append(TextUtil.replace(Lang.getMessage(LevelerMessage.ABILITY_UNLOCK, locale),"{ability}", ability.getDisplayName(locale)));
				} else {
					abilityLevelUpMessage.append(TextUtil.replace(Lang.getMessage(LevelerMessage.ABILITY_LEVEL_UP, locale)
							,"{ability}", ability.getDisplayName(locale)
							,"{level}", RomanNumber.toRoman(playerData.getAbilityLevel(ability))));
				}
			}
		}
		message = TextUtil.replace(message, "{stat_level}", rewardMessage.toString(),
				"{ability_unlock}", abilityUnlockMessage.toString(),
				"{ability_level_up}", abilityLevelUpMessage.toString());
		// Mana ability unlocks and level ups
		StringBuilder manaAbilityUnlockMessage = new StringBuilder();
		StringBuilder manaAbilityLevelUpMessage = new StringBuilder();
		MAbility mAbility = plugin.getManaAbilityManager().getManaAbility(skill, newLevel);
		if (mAbility != null) {
			if (plugin.getAbilityManager().isEnabled(mAbility)) {
				if (plugin.getManaAbilityManager().getUnlock(mAbility) == newLevel) {
					manaAbilityUnlockMessage.append(TextUtil.replace(Lang.getMessage(LevelerMessage.MANA_ABILITY_UNLOCK, locale), "{mana_ability}", mAbility.getDisplayName(locale)));
				} else {
					manaAbilityLevelUpMessage.append(TextUtil.replace(Lang.getMessage(LevelerMessage.MANA_ABILITY_LEVEL_UP, locale)
							, "{mana_ability}", mAbility.getDisplayName(locale)
							, "{level}", RomanNumber.toRoman(playerData.getManaAbilityLevel(mAbility))));
				}
			}
		}
		message = TextUtil.replace(message, "{mana_ability_unlock}", manaAbilityUnlockMessage.toString(), "{mana_ability_level_up}", manaAbilityLevelUpMessage.toString());
		// Build money rewards
		StringBuilder moneyRewardMessage = new StringBuilder();
		double totalMoney = 0;
		// Legacy system
		if (plugin.isVaultEnabled()) {
			if (OptionL.getBoolean(Option.SKILL_MONEY_REWARDS_ENABLED)) {
				double base = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_BASE);
				double multiplier = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_MULTIPLIER);
				totalMoney += base + (multiplier * newLevel * newLevel);
			}
		}
		// New rewards
		for (MoneyReward reward : plugin.getRewardManager().getRewardTable(skill).searchRewards(MoneyReward.class, newLevel)) {
			totalMoney += reward.getAmount();
		}
		if (totalMoney > 0) {
			NumberFormat nf = new DecimalFormat("#.##");
			moneyRewardMessage.append(TextUtil.replace(Lang.getMessage(LevelerMessage.MONEY_REWARD, locale),
					"{amount}", nf.format(totalMoney)));
		}
		message = TextUtil.replace(message, "{money_reward}", moneyRewardMessage.toString());
		return message.replaceAll("(\\u005C\\u006E)|(\\n)", "\n");
	}

	public XpRequirements getXpRequirements() {
		return xpRequirements;
	}

}