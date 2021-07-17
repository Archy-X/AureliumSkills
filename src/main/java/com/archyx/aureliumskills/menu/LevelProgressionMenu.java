package com.archyx.aureliumskills.menu;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.MenuInitializeEvent;
import com.archyx.aureliumskills.api.event.MenuOpenEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menu.items.*;
import com.archyx.aureliumskills.menu.templates.InProgressTemplate;
import com.archyx.aureliumskills.menu.templates.LockedTemplate;
import com.archyx.aureliumskills.menu.templates.TemplateType;
import com.archyx.aureliumskills.menu.templates.UnlockedTemplate;
import com.archyx.aureliumskills.skills.Skill;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LevelProgressionMenu implements InventoryProvider {

	private final PlayerData playerData;
	private final Skill skill;
	private final Locale locale;
	private final List<Integer> track;
	private final MenuOption options;
	private final AureliumSkills plugin;
	private final int pages;


	public LevelProgressionMenu(PlayerData playerData, Locale locale, Skill skill, MenuOption menuOption, AureliumSkills plugin) {
		this.playerData = playerData;
		this.locale = locale;
		this.skill = skill;
		this.options = menuOption;
		this.plugin = plugin;
		pages = (OptionL.getMaxLevel(skill) - 2) / 24 + 1;
		this.track = new ArrayList<>();
		for (int i = 0; i < pages; i++) {
			track.add(i*36); track.add(9 + i*36); track.add(18 + i*36); track.add(27 + i*36); track.add(28 + i*36);
			track.add(29 + i*36); track.add(20 + i*36); track.add(11 + i*36); track.add(2 + i*36); track.add(3 + i*36);
			track.add(4 + i*36); track.add(13 + i*36); track.add(22 + i*36); track.add(31 + i*36); track.add(32 + i*36);
			track.add(33 + i*36); track.add(24 + i*36); track.add(15 + i*36); track.add(6 + i*36); track.add(7 + i*36);
			track.add(8 + i*36); track.add(17 + i*36); track.add(26 + i*36); track.add(35 + i*36);
		}
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		int currentLevel = playerData.getSkillLevel(skill);

		// Fill item
		if (options.isFillEnabled()) {
			contents.fill(ClickableItem.empty(options.getFillItem()));
		}

		SkillItem skillItem = (SkillItem) options.getItem(ItemType.SKILL);
		contents.set(skillItem.getPos(), ClickableItem.empty(skillItem.getItem(skill, playerData, player, locale)));

		BackItem backItem = (BackItem) options.getItem(ItemType.BACK);
		contents.set(backItem.getPos(), ClickableItem.of(backItem.getItem(player, locale), e -> {
			SmartInventory inventory = SkillsMenu.getInventory(player, plugin);
			if (inventory == null) return;
			MenuOpenEvent event = new MenuOpenEvent(player, MenuType.SKILLS);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			inventory.open(player);
		}));

		CloseItem closeItem = (CloseItem) options.getItem(ItemType.CLOSE);
		contents.set(closeItem.getPos(), ClickableItem.of(closeItem.getItem(player, locale), e -> player.closeInventory()));

		RankItem rankItem = (RankItem) options.getItem(ItemType.RANK);
		contents.set(rankItem.getPos(), ClickableItem.empty(rankItem.getItem(skill, player, locale)));

		Pagination pagination = contents.pagination();
		ClickableItem[] items = new ClickableItem[36 * pages];

		if (options.isFillEnabled() && options.getFillItem() != null) {
			for (int i = 0; i < items.length; i++) {
				items[i] = ClickableItem.empty(options.getFillItem());
			}
		}

		UnlockedTemplate unlocked = (UnlockedTemplate) options.getTemplate(TemplateType.UNLOCKED);
		InProgressTemplate inProgress = (InProgressTemplate) options.getTemplate(TemplateType.IN_PROGRESS);
		LockedTemplate locked = (LockedTemplate) options.getTemplate(TemplateType.LOCKED);

		for (int i = pagination.getPage() * 24; i < pagination.getPage() * 24 + 24; i++) {
			if (i + 2 <= OptionL.getMaxLevel(skill)) {
				if (i + 2 <= currentLevel) {
					items[track.get(i)] = ClickableItem.empty(unlocked.getItem(skill, i + 2, player, locale));
				} else if (i + 2 == currentLevel + 1) {
					items[track.get(i)] = ClickableItem.empty(inProgress.getItem(skill, playerData, i + 2, player, locale));
				} else {
					items[track.get(i)] = ClickableItem.empty(locked.getItem(skill, i + 2, player, locale));
				}
			}
			else {
				if (options.isFillEnabled() && options.getFillItem() != null) {
					items[track.get(i)] = ClickableItem.empty(options.getFillItem());
				}
				else {
					items[track.get(i)] = ClickableItem.empty(new ItemStack(Material.AIR));
				}
			}
		}
		
		pagination.setItems(items);
		pagination.setItemsPerPage(36);

		int a = 0;
		for (int i = 9; i < 45; i++) {
			int row = i/9;
			int column = i%9;
			contents.set(row, column, pagination.getPageItems()[a]);
			a++;
		}

		NextPageItem nextPageItem = (NextPageItem) options.getItem(ItemType.NEXT_PAGE);
		if (pagination.getPage() + 1 < pages) {
			contents.set(nextPageItem.getPos(), ClickableItem.of(nextPageItem.getItem(player, locale), e -> {
				int page = pagination.next().getPage();
				SmartInventory inventory = getInventory(playerData, skill, page, plugin);
				inventory.open(player, page);
			}));
		}

		PreviousPageItem previousPageItem = (PreviousPageItem) options.getItem(ItemType.PREVIOUS_PAGE);
		if (pagination.getPage() - 1 >= 0) {
			contents.set(previousPageItem.getPos(), ClickableItem.of(previousPageItem.getItem(player, locale), e -> {
				int previous = pagination.previous().getPage();
				SmartInventory inventory = getInventory(playerData, skill, previous, plugin);
				inventory.open(player, previous);
			}));
		}

		// Call API event
		MenuInitializeEvent event = new MenuInitializeEvent(player, MenuType.LEVEL_PROGRESSION, contents);
		Bukkit.getPluginManager().callEvent(event);
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		
		
	}

	public static SmartInventory getInventory(PlayerData playerData, Skill skill, int page, AureliumSkills plugin) {
		Locale locale = playerData.getLocale();
		MenuOption menuOption = plugin.getMenuLoader().getMenu(MenuType.LEVEL_PROGRESSION);
		return SmartInventory.builder()
				.provider(new LevelProgressionMenu(playerData, locale, skill, menuOption, plugin))
				.size(menuOption.getRows(), 9)
				.title(Lang.getMessage(MenuMessage.LEVEL_PROGRESSION_MENU_TITLE, locale).replace("{skill}", skill.getDisplayName(locale)).replace("{page}", String.valueOf(page + 1)))
				.manager(plugin.getInventoryManager())
				.build();
	}
	
}
