package com.archyx.aureliumskills.ability;

import com.archyx.aureliumskills.configuration.OptionValue;
import com.archyx.aureliumskills.lang.AbilityMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public enum Ability implements AbstractAbility {
	
	BOUNTIFUL_HARVEST(() -> Skills.FARMING, 10.0, 5.0),
	FARMER(() -> Skills.FARMING, 10.0, 10.0),
	SCYTHE_MASTER(() -> Skills.FARMING, 3.0, 2.0),
	GENETICIST(() -> Skills.FARMING, 1.0, 2.0),
	TRIPLE_HARVEST(() -> Skills.FARMING, 5.0, 3.0),
	LUMBERJACK(() -> Skills.FORAGING, 10.0, 5.0),
	FORAGER(() -> Skills.FORAGING, 10.0, 10.0),
	AXE_MASTER(() -> Skills.FORAGING, 4.0, 3.0),
	VALOR(() -> Skills.FORAGING, 1.0, 1.0),
	SHREDDER(() -> Skills.FORAGING, 3.0, 3.0),
	LUCKY_MINER(() -> Skills.MINING, 3.0, 3.0),
	MINER(() -> Skills.MINING, 10.0, 10.0),
	PICK_MASTER(() -> Skills.MINING, 3.0, 2.0),
	STAMINA(() -> Skills.MINING, 1.0, 1.0),
	HARDENED_ARMOR(() -> Skills.MINING, 3.0, 3.0),
	LUCKY_CATCH(() -> Skills.FISHING, 10.0, 5.0),
	FISHER(() -> Skills.FISHING, 10.0, 10.0),
	TREASURE_HUNTER(() -> Skills.FISHING, 1.0, 1.0),
	GRAPPLER(() -> Skills.FISHING, 50.0, 100.0),
	EPIC_CATCH(() -> Skills.FISHING, 0.4, 0.4),
	METAL_DETECTOR(() -> Skills.EXCAVATION, 0.5, 0.4),
	EXCAVATOR(() -> Skills.EXCAVATION, 10.0, 10.0),
	SPADE_MASTER(() -> Skills.EXCAVATION, 3.0, 2.0),
	BIGGER_SCOOP(() -> Skills.EXCAVATION, 3.0, 3.0),
	LUCKY_SPADES(() -> Skills.EXCAVATION, 0.1, 0.05),
	CRIT_CHANCE(() -> Skills.ARCHERY, 5.0, 5.0),
	ARCHER(() -> Skills.ARCHERY, 10.0, 10.0),
	BOW_MASTER(() -> Skills.ARCHERY, 3.0, 2.0),
	PIERCING(() -> Skills.ARCHERY, 3.0, 3.0),
	STUN(() -> Skills.ARCHERY, 2.0, 1.0),
	SHIELDING(() -> Skills.DEFENSE, 2.0, 3.0),
	DEFENDER(() -> Skills.DEFENSE, 10.0, 10.0),
	MOB_MASTER(() -> Skills.DEFENSE, 2.0, 3.0),
	IMMUNITY(() -> Skills.DEFENSE, 0.5, 0.4),
	NO_DEBUFF(() -> Skills.DEFENSE, 5.0, 5.0),
	CRIT_DAMAGE(() -> Skills.FIGHTING, 5.0, 4.0),
	FIGHTER(() -> Skills.FIGHTING, 10.0, 10.0),
	SWORD_MASTER(() -> Skills.FIGHTING, 3.0, 2.0),
	FIRST_STRIKE(() -> Skills.FIGHTING, 20.0, 10.0, new String[] {"enable_message", "cooldown_ticks"}, new Object[] {true}),
	BLEED(() -> Skills.FIGHTING, 3.0, 4.0, 0.5, 0.5,
			new String[] {"enable_enemy_message", "enable_self_message", "enable_stop_message", "base_ticks", "added_ticks", "max_ticks", "tick_period", "show_particles"},
			new Object[] {true, true, true, 3, 2, 11, 40, true}),
	ANTI_HUNGER(() -> Skills.ENDURANCE, 5.0, 5.0),
	RUNNER(() -> Skills.ENDURANCE, 10.0, 10.0),
	GOLDEN_HEAL(() -> Skills.ENDURANCE, 5.0, 6.0),
	RECOVERY(() -> Skills.ENDURANCE, 10, 10),
	MEAL_STEAL(() -> Skills.ENDURANCE, 1.0, 2.0),
	LIGHT_FALL(() -> Skills.AGILITY, 2.0, 1.5),
	JUMPER(() -> Skills.AGILITY, 10.0, 10.0),
	SUGAR_RUSH(() -> Skills.AGILITY, 5.0, 7.0),
	FLEETING(() -> Skills.AGILITY, 5.0, 4.0, new String[] {"health_percent_required"}, new Object[] {20.0}),
	THUNDER_FALL(() -> Skills.AGILITY, 3.0, 2.0, 10, 5),
	ALCHEMIST(() -> Skills.ALCHEMY, 3.0, 4.0, new String[] {"add_item_lore"}, new Object[] {true}),
	BREWER(() -> Skills.ALCHEMY, 10.0, 10.0),
	SPLASHER(() -> Skills.ALCHEMY, 0.5, 0.25),
	LINGERING(() -> Skills.ALCHEMY, 5.0, 4.0, 3.0, 2.0),
	WISE_EFFECT(() -> Skills.ALCHEMY, 1.0, 1.0),
	XP_CONVERT(() -> Skills.ENCHANTING, 20.0, -0.75),
	ENCHANTER(() -> Skills.ENCHANTING, 10.0, 10.0),
	XP_WARRIOR(() -> Skills.ENCHANTING, 5.0, 4.0),
	ENCHANTED_STRENGTH(() -> Skills.ENCHANTING, 0.5, 0.5),
	LUCKY_TABLE(() -> Skills.ENCHANTING, 5.0, 5.0),
	SORCERER(() -> Skills.SORCERY, 10.0, 10.0),
	LIFE_ESSENCE(() -> Skills.HEALING, 10.0, 7.0),
	HEALER(() -> Skills.HEALING, 10.0, 10.0),
	LIFE_STEAL(() -> Skills.HEALING, 2.5, 1.0),
	GOLDEN_HEART(() -> Skills.HEALING, 5.0, 3.0),
	REVIVAL(() -> Skills.HEALING, 5.0, 4.0, 7.0, 6.0, new String[] {"enable_message"}, new Object[] {true}),
	DISENCHANTER(() -> Skills.FORGING, 10.0, 7.0),
	FORGER(() -> Skills.FORGING, 10.0, 10.0),
	REPAIRING(() -> Skills.FORGING, 5.0, 4.0),
	ANVIL_MASTER(() -> Skills.FORGING, 43.0, 3.0),
	SKILL_MENDER(() -> Skills.FORGING, 2.0, 1.0);
	
	private final double baseValue;
	private final double valuePerLevel;
	private boolean hasTwoValues;
	private double baseValue2;
	private double valuePerLevel2;
	private final Supplier<Skill> skill;
	private Map<String, OptionValue> options;
	
	Ability(Supplier<Skill> skill, double baseValue, double valuePerLevel) {
		this.baseValue = baseValue;
		this.valuePerLevel = valuePerLevel;
		this.skill = skill;
		this.options = new HashMap<>();
	}

	Ability(Supplier<Skill> skill, double baseValue, double valuePerLevel, String[] optionKeys, Object[] optionValues) {
		this(skill, baseValue, valuePerLevel);
		this.options = new HashMap<>();
		for (int i = 0; i < optionKeys.length; i++) {
			if (i < optionValues.length) {
				options.put(optionKeys[i], new OptionValue(optionValues[i]));
			}
		}
	}

	Ability(Supplier<Skill> skill, double baseValue1, double valuePerLevel1, double baseValue2, double valuePerLevel2) {
		this(skill, baseValue1, valuePerLevel1);
		this.hasTwoValues = true;
		this.baseValue2 = baseValue2;
		this.valuePerLevel2 = valuePerLevel2;
	}

	Ability(Supplier<Skill> skill, double baseValue1, double valuePerLevel1, double baseValue2, double valuePerLevel2, String[] optionKeys, Object[] optionValues) {
		this(skill, baseValue1, valuePerLevel1, baseValue2, valuePerLevel2);
		this.options = new HashMap<>();
		for (int i = 0; i < optionKeys.length; i++) {
			if (i < optionValues.length) {
				options.put(optionKeys[i], new OptionValue(optionValues[i]));
			}
		}
	}

	public String getInfo(Locale locale) {
		return Lang.getMessage(AbilityMessage.valueOf(this.name() + "_INFO"), locale);
	}

	@Override
	public Skill getSkill() {
		return skill.get();
	}

	public boolean hasTwoValues() {
		return hasTwoValues;
	}

	@Override
	public double getDefaultBaseValue() {
		return baseValue;
	}

	@Override
	public double getDefaultValuePerLevel() {
		return valuePerLevel;
	}

	public double getDefaultBaseValue2() {
		return baseValue2;
	}

	public double getDefaultValuePerLevel2() {
		return valuePerLevel2;
	}

	public String getDisplayName(Locale locale) {
		return Lang.getMessage(AbilityMessage.valueOf(this.name() + "_NAME"), locale);
	}
	
	public String getDescription(Locale locale) {
		return Lang.getMessage(AbilityMessage.valueOf(this.name() + "_DESC"), locale);
	}

	public Map<String, OptionValue> getDefaultOptions() {
		return options;
	}

}
