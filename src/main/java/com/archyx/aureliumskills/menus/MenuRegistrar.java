package com.archyx.aureliumskills.menus;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.item.ItemRegistryMenuProvider;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.menus.abilities.*;
import com.archyx.aureliumskills.menus.common.*;
import com.archyx.aureliumskills.menus.contexts.*;
import com.archyx.aureliumskills.menus.leaderboard.LeaderboardMenu;
import com.archyx.aureliumskills.menus.leaderboard.LeaderboardPlayerItem;
import com.archyx.aureliumskills.menus.levelprogression.*;
import com.archyx.aureliumskills.menus.skills.ClickableSkillItem;
import com.archyx.aureliumskills.menus.skills.SkillsMenu;
import com.archyx.aureliumskills.menus.skills.StatsItem;
import com.archyx.aureliumskills.menus.skills.YourSkillsItem;
import com.archyx.aureliumskills.menus.sources.SorterItem;
import com.archyx.aureliumskills.menus.sources.SourceItem;
import com.archyx.aureliumskills.menus.sources.SourcesMenu;
import com.archyx.aureliumskills.menus.stats.SkullItem;
import com.archyx.aureliumskills.menus.stats.StatItem;
import com.archyx.aureliumskills.menus.stats.StatsMenu;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.slate.Slate;
import com.archyx.slate.context.ContextManager;
import com.archyx.slate.item.provider.ProviderManager;
import com.archyx.slate.menu.MenuManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MenuRegistrar {

    private final @NotNull AureliumSkills plugin;
    private final Slate slate;

    public MenuRegistrar(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.slate = plugin.getSlate();
    }

    public void register() {
        ContextManager contextManager = slate.getContextManager();
        // Register contexts
        contextManager.registerContext("Skill", Skill.class, new SkillContext(plugin));
        contextManager.registerContext("Stat", Stat.class, new StatContext(plugin));
        contextManager.registerContext("Source", Source.class, new SourceContext(plugin));
        contextManager.registerContext("SortType", SorterItem.SortType.class, new SortTypeContext());
        contextManager.registerContext("Ability", Ability.class, new AbilityContext());
        contextManager.registerContext("MAbility", MAbility.class, new ManaAbilityContext());

        MenuManager manager = plugin.getMenuManager();
        // Register menus
        manager.registerMenuProvider("skills", new SkillsMenu(plugin));
        manager.registerMenuProvider("stats", new StatsMenu(plugin));
        manager.registerMenuProvider("level_progression", new LevelProgressionMenu(plugin));
        manager.registerMenuProvider("leaderboard", new LeaderboardMenu(plugin));
        manager.registerMenuProvider("sources", new SourcesMenu(plugin));
        manager.registerMenuProvider("abilities", new AbilitiesMenu(plugin));
        // Register default options
        Map<String, Object> levelProgressionOptions = new HashMap<>();
        levelProgressionOptions.put("use_level_as_amount", false);
        levelProgressionOptions.put("over_max_stack_amount", 1);
        levelProgressionOptions.put("items_per_page", 24);
        manager.registerDefaultOptions("level_progression", levelProgressionOptions);
        Map<String, Object> leaderboardOptions = new HashMap<>();
        leaderboardOptions.put("auto_replace_heads_on_legacy", true);
        manager.registerDefaultOptions("leaderboard", leaderboardOptions);
        // Global items
        manager.registerSingleItem("back", new BackItem(plugin));
        manager.registerSingleItem("close", new CloseItem(plugin));
        manager.registerSingleItem("next_page", new NextPageItem(plugin));
        manager.registerSingleItem("previous_page", new PreviousPageItem(plugin));
        // Keyed item provider
        manager.getGlobalProviderManager().registerKeyedItemProvider(new ItemRegistryMenuProvider(plugin.getItemRegistry()));

        // Register menu specific items and templates
        ProviderManager skills = manager.getProviderManager("skills");
        skills.registerSingleItem("your_skills", new YourSkillsItem(plugin));
        skills.registerSingleItem("stats", new StatsItem(plugin));
        skills.registerTemplateItem("skill", new ClickableSkillItem(plugin));

        ProviderManager stats = manager.getProviderManager("stats");
        stats.registerSingleItem("skull", new SkullItem(plugin));
        stats.registerTemplateItem("stat", new StatItem(plugin));

        ProviderManager levelProgression = manager.getProviderManager("level_progression");
        levelProgression.registerSingleItem("next_page", new NextPageItem(plugin));
        levelProgression.registerSingleItem("previous_page", new PreviousPageItem(plugin));
        levelProgression.registerSingleItem("rank", new RankItem(plugin));
        levelProgression.registerSingleItem("sources", new SourcesItem(plugin));
        levelProgression.registerSingleItem("abilities", new AbilitiesItem(plugin));
        levelProgression.registerTemplateItem("skill", new StaticSkillItem(plugin));
        levelProgression.registerTemplateItem("unlocked", new UnlockedItem(plugin));
        levelProgression.registerTemplateItem("in_progress", new InProgressItem(plugin));
        levelProgression.registerTemplateItem("locked", new LockedItem(plugin));

        ProviderManager leaderboard = manager.getProviderManager("leaderboard");
        leaderboard.registerSingleItem("back", new BackToLevelProgressionItem(plugin));
        leaderboard.registerTemplateItem("leaderboard_player", new LeaderboardPlayerItem(plugin));

        ProviderManager sources = manager.getProviderManager("sources");
        sources.registerSingleItem("sorter", new SorterItem(plugin));
        sources.registerSingleItem("back", new BackToLevelProgressionItem(plugin));
        sources.registerTemplateItem("source", new SourceItem(plugin));

        ProviderManager abilities = manager.getProviderManager("abilities");
        abilities.registerSingleItem("back", new BackToLevelProgressionItem(plugin));
        abilities.registerTemplateItem("locked_ability", new LockedAbilityItem(plugin));
        abilities.registerTemplateItem("locked_mana_ability", new LockedManaAbilityItem(plugin));
        abilities.registerTemplateItem("unlocked_ability", new UnlockedAbilityItem(plugin));
        abilities.registerTemplateItem("unlocked_mana_ability", new UnlockedManaAbilityItem(plugin));
    }
}
