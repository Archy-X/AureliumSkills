package com.archyx.aureliumskills.menu;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.MenuInitializeEvent;
import com.archyx.aureliumskills.api.event.MenuOpenEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menu.items.CloseItem;
import com.archyx.aureliumskills.menu.items.ItemType;
import com.archyx.aureliumskills.menu.items.YourSkillsItem;
import com.archyx.aureliumskills.menu.templates.SkillTemplate;
import com.archyx.aureliumskills.menu.templates.TemplateType;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SkillsMenu implements InventoryProvider{

	private final PlayerData playerData;
	private final Locale locale;
	private final MenuOption options;
	private final AureliumSkills plugin;

	public SkillsMenu(PlayerData playerData, Locale locale, MenuOption menuOption, AureliumSkills plugin) {
		this.playerData = playerData;
		this.locale = locale;
		this.options = menuOption;
		this.plugin = plugin;
	}

	public void init(Player player, InventoryContents contents) {
		// Fill item
		if (options.isFillEnabled()) {
			contents.fill(ClickableItem.empty(options.getFillItem()));
		}
		// Close item
		CloseItem closeItem = (CloseItem) options.getItem(ItemType.CLOSE);
		contents.set(closeItem.getPos(), ClickableItem.of(closeItem.getItem(player, locale), e -> player.closeInventory()));
		// Your skills item
		YourSkillsItem yourSkillsItem = (YourSkillsItem) options.getItem(ItemType.YOUR_SKILLS);
		contents.set(yourSkillsItem.getPos(), ClickableItem.empty(yourSkillsItem.getItem(player, locale)));
		// Skill items
		SkillTemplate skillTemplate = (SkillTemplate) options.getTemplate(TemplateType.SKILL);
		if (OptionL.isEnabled(Skills.FARMING)) {
			contents.set(skillTemplate.getPosition(Skills.FARMING), ClickableItem.of(skillTemplate.getItem(Skills.FARMING, playerData, player, locale), e -> open(player, playerData, Skills.FARMING)));
		}
		if (OptionL.isEnabled(Skills.FORAGING)) {
			contents.set(skillTemplate.getPosition(Skills.FORAGING), ClickableItem.of(skillTemplate.getItem(Skills.FORAGING, playerData, player, locale), e -> open(player, playerData, Skills.FORAGING)));
		}
		if (OptionL.isEnabled(Skills.MINING)) {
			contents.set(skillTemplate.getPosition(Skills.MINING), ClickableItem.of(skillTemplate.getItem(Skills.MINING, playerData, player, locale), e -> open(player, playerData, Skills.MINING)));
		}
		if (OptionL.isEnabled(Skills.FISHING)) {
			contents.set(skillTemplate.getPosition(Skills.FISHING), ClickableItem.of(skillTemplate.getItem(Skills.FISHING, playerData, player, locale), e -> open(player, playerData, Skills.FISHING)));
		}
		if (OptionL.isEnabled(Skills.EXCAVATION)) {
			contents.set(skillTemplate.getPosition(Skills.EXCAVATION), ClickableItem.of(skillTemplate.getItem(Skills.EXCAVATION, playerData, player, locale), e -> open(player, playerData, Skills.EXCAVATION)));
		}
		//Combat Skills
		if (OptionL.isEnabled(Skills.ARCHERY)) {
			contents.set(skillTemplate.getPosition(Skills.ARCHERY), ClickableItem.of(skillTemplate.getItem(Skills.ARCHERY, playerData, player, locale), e -> open(player, playerData, Skills.ARCHERY)));
		}
		if (OptionL.isEnabled(Skills.DEFENSE)) {
			contents.set(skillTemplate.getPosition(Skills.DEFENSE), ClickableItem.of(skillTemplate.getItem(Skills.DEFENSE, playerData, player, locale), e -> open(player, playerData, Skills.DEFENSE)));
		}
		if (OptionL.isEnabled(Skills.FIGHTING)) {
			contents.set(skillTemplate.getPosition(Skills.FIGHTING), ClickableItem.of(skillTemplate.getItem(Skills.FIGHTING, playerData, player, locale), e -> open(player, playerData, Skills.FIGHTING)));
		}
		if (OptionL.isEnabled(Skills.ENDURANCE)) {
			contents.set(skillTemplate.getPosition(Skills.ENDURANCE), ClickableItem.of(skillTemplate.getItem(Skills.ENDURANCE, playerData, player, locale), e -> open(player, playerData, Skills.ENDURANCE)));
		}
		if (OptionL.isEnabled(Skills.AGILITY)) {
			contents.set(skillTemplate.getPosition(Skills.AGILITY), ClickableItem.of(skillTemplate.getItem(Skills.AGILITY, playerData, player, locale), e -> open(player, playerData, Skills.AGILITY)));
		}
		//Magic Skills
		if (OptionL.isEnabled(Skills.ALCHEMY)) {
			contents.set(skillTemplate.getPosition(Skills.ALCHEMY), ClickableItem.of(skillTemplate.getItem(Skills.ALCHEMY, playerData, player, locale), e -> open(player, playerData, Skills.ALCHEMY)));
		}
		if (OptionL.isEnabled(Skills.ENCHANTING)) {
			contents.set(skillTemplate.getPosition(Skills.ENCHANTING), ClickableItem.of(skillTemplate.getItem(Skills.ENCHANTING, playerData, player, locale), e -> open(player, playerData, Skills.ENCHANTING)));
		}
		if (OptionL.isEnabled(Skills.SORCERY)) {
			contents.set(skillTemplate.getPosition(Skills.SORCERY), ClickableItem.of(skillTemplate.getItem(Skills.SORCERY, playerData, player, locale), e -> open(player, playerData, Skills.SORCERY)));
		}
		if (OptionL.isEnabled(Skills.HEALING)) {
			contents.set(skillTemplate.getPosition(Skills.HEALING), ClickableItem.of(skillTemplate.getItem(Skills.HEALING, playerData, player, locale), e -> open(player, playerData, Skills.HEALING)));
		}
		if (OptionL.isEnabled(Skills.FORGING)) {
			contents.set(skillTemplate.getPosition(Skills.FORGING), ClickableItem.of(skillTemplate.getItem(Skills.FORGING, playerData, player, locale), e -> open(player, playerData, Skills.FORGING)));
		}
		// Call API event
		MenuInitializeEvent event = new MenuInitializeEvent(player, MenuType.SKILLS, contents);
		Bukkit.getPluginManager().callEvent(event);
	}

	public void update(Player player, InventoryContents contents) {

	}

	public static int getPage(Skill skill, PlayerData playerData) {
		int page = (playerData.getSkillLevel(skill) - 2) / 24;
		int maxLevelPage = (OptionL.getMaxLevel(skill) - 2) / 24;
		if (page > maxLevelPage) {
			page = maxLevelPage;
		}
		return page;
	}

	private void open(Player player, PlayerData playerData, Skill skill) {
		if (player.hasPermission("aureliumskills." + skill.name().toLowerCase(Locale.ENGLISH))) {
			int page = getPage(skill, playerData);
			SmartInventory inventory  = LevelProgressionMenu.getInventory(playerData, skill, page, plugin);
			MenuOpenEvent event = new MenuOpenEvent(player, MenuType.LEVEL_PROGRESSION);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			inventory.open(player, page);
		}
	}

	@Nullable
	public static SmartInventory getInventory(Player player, AureliumSkills plugin) {
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData == null) {
			return null;
		}
		Locale locale = playerData.getLocale();
		MenuOption menuOption = plugin.getMenuLoader().getMenu(MenuType.SKILLS);
		return SmartInventory.builder()
				.provider(new SkillsMenu(playerData, locale, menuOption, plugin))
				.size(menuOption.getRows(), 9)
				.title(Lang.getMessage(MenuMessage.SKILLS_MENU_TITLE, locale))
				.manager(plugin.getInventoryManager())
				.build();
	}

}
