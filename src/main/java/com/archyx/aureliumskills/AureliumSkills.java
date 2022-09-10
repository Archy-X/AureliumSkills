package com.archyx.aureliumskills;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.archyx.aureliumskills.ability.AbilityManager;
import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.commands.ManaCommand;
import com.archyx.aureliumskills.commands.SkillCommands;
import com.archyx.aureliumskills.commands.SkillsCommand;
import com.archyx.aureliumskills.commands.StatsCommand;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerManager;
import com.archyx.aureliumskills.data.backup.BackupProvider;
import com.archyx.aureliumskills.data.backup.LegacyFileBackup;
import com.archyx.aureliumskills.data.backup.MysqlBackup;
import com.archyx.aureliumskills.data.backup.YamlBackup;
import com.archyx.aureliumskills.data.converter.LegacyFileToYamlConverter;
import com.archyx.aureliumskills.data.converter.LegacyMysqlToMysqlConverter;
import com.archyx.aureliumskills.data.storage.MySqlStorageProvider;
import com.archyx.aureliumskills.data.storage.StorageProvider;
import com.archyx.aureliumskills.data.storage.YamlStorageProvider;
import com.archyx.aureliumskills.item.ItemRegistry;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.leaderboard.LeaderboardManager;
import com.archyx.aureliumskills.leveler.Leveler;
import com.archyx.aureliumskills.listeners.DamageListener;
import com.archyx.aureliumskills.listeners.PlayerJoinQuit;
import com.archyx.aureliumskills.loot.LootTableManager;
import com.archyx.aureliumskills.mana.ManaAbilityManager;
import com.archyx.aureliumskills.mana.ManaManager;
import com.archyx.aureliumskills.menus.MenuFileManager;
import com.archyx.aureliumskills.menus.MenuRegistrar;
import com.archyx.aureliumskills.menus.sources.SorterItem;
import com.archyx.aureliumskills.modifier.ArmorModifierListener;
import com.archyx.aureliumskills.modifier.ItemListener;
import com.archyx.aureliumskills.modifier.ModifierManager;
import com.archyx.aureliumskills.region.RegionBlockListener;
import com.archyx.aureliumskills.region.RegionListener;
import com.archyx.aureliumskills.region.RegionManager;
import com.archyx.aureliumskills.requirement.RequirementListener;
import com.archyx.aureliumskills.requirement.RequirementManager;
import com.archyx.aureliumskills.rewards.RewardManager;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.SkillRegistry;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.skills.agility.AgilityAbilities;
import com.archyx.aureliumskills.skills.agility.AgilityLeveler;
import com.archyx.aureliumskills.skills.alchemy.AlchemyAbilities;
import com.archyx.aureliumskills.skills.alchemy.AlchemyLeveler;
import com.archyx.aureliumskills.skills.archery.ArcheryAbilities;
import com.archyx.aureliumskills.skills.archery.ArcheryLeveler;
import com.archyx.aureliumskills.skills.defense.DefenseAbilities;
import com.archyx.aureliumskills.skills.defense.DefenseLeveler;
import com.archyx.aureliumskills.skills.enchanting.EnchantingAbilities;
import com.archyx.aureliumskills.skills.enchanting.EnchantingLeveler;
import com.archyx.aureliumskills.skills.endurance.EnduranceAbilities;
import com.archyx.aureliumskills.skills.endurance.EnduranceLeveler;
import com.archyx.aureliumskills.skills.excavation.ExcavationLeveler;
import com.archyx.aureliumskills.skills.excavation.ExcavationLootHandler;
import com.archyx.aureliumskills.skills.farming.FarmingAbilities;
import com.archyx.aureliumskills.skills.farming.FarmingHarvestLeveler;
import com.archyx.aureliumskills.skills.farming.FarmingInteractLeveler;
import com.archyx.aureliumskills.skills.farming.FarmingLeveler;
import com.archyx.aureliumskills.skills.fighting.FightingAbilities;
import com.archyx.aureliumskills.skills.fighting.FightingLeveler;
import com.archyx.aureliumskills.skills.fishing.FishingAbilities;
import com.archyx.aureliumskills.skills.fishing.FishingLeveler;
import com.archyx.aureliumskills.skills.fishing.FishingLootHandler;
import com.archyx.aureliumskills.skills.foraging.ForagingAbilities;
import com.archyx.aureliumskills.skills.foraging.ForagingLeveler;
import com.archyx.aureliumskills.skills.foraging.ForagingLootHandler;
import com.archyx.aureliumskills.skills.forging.ForgingAbilities;
import com.archyx.aureliumskills.skills.forging.ForgingLeveler;
import com.archyx.aureliumskills.skills.healing.HealingAbilities;
import com.archyx.aureliumskills.skills.healing.HealingLeveler;
import com.archyx.aureliumskills.skills.mining.MiningAbilities;
import com.archyx.aureliumskills.skills.mining.MiningLeveler;
import com.archyx.aureliumskills.skills.mining.MiningLootHandler;
import com.archyx.aureliumskills.skills.sorcery.SorceryLeveler;
import com.archyx.aureliumskills.source.SourceManager;
import com.archyx.aureliumskills.source.SourceRegistry;
import com.archyx.aureliumskills.stats.*;
import com.archyx.aureliumskills.support.*;
import com.archyx.aureliumskills.ui.ActionBar;
import com.archyx.aureliumskills.ui.ActionBarCompatHandler;
import com.archyx.aureliumskills.ui.SkillBossBar;
import com.archyx.aureliumskills.util.armor.ArmorListener;
import com.archyx.aureliumskills.util.version.ReleaseData;
import com.archyx.aureliumskills.util.version.UpdateChecker;
import com.archyx.aureliumskills.util.version.VersionUtils;
import com.archyx.aureliumskills.util.world.WorldManager;
import com.archyx.slate.Slate;
import com.archyx.slate.menu.MenuManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import fr.minuskube.inv.InventoryManager;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AureliumSkills extends JavaPlugin {

	private PlayerManager playerManager;
	private StorageProvider storageProvider;
	private BackupProvider backupProvider;
	private LootTableManager lootTableManager;
	private InventoryManager inventoryManager;
	private AbilityManager abilityManager;
	private WorldGuardSupport worldGuardSupport;
	private WorldGuardFlags worldGuardFlags;
	private WorldManager worldManager;
	private ManaManager manaManager;
	private ManaAbilityManager manaAbilityManager;
	private RewardManager rewardManager;
	private boolean holographicDisplaysEnabled;
	private boolean worldGuardEnabled;
	private boolean placeholderAPIEnabled;
	private boolean vaultEnabled;
	private boolean protocolLibEnabled;
	private boolean townyEnabled;
	private TownySupport townySupport;
	private boolean luckPermsEnabled;
	private boolean slimefunEnabled;
	private boolean nbtAPIEnabled;
	private Economy economy;
	private OptionL optionLoader;
	private PaperCommandManager commandManager;
	private ActionBar actionBar;
	private SkillBossBar bossBar;
	private SourceManager sourceManager;
	private SorceryLeveler sorceryLeveler;
	private RegionBlockListener regionBlockListener;
	private RequirementManager requirementManager;
	private ModifierManager modifierManager;
	private Lang lang;
	private Leveler leveler;
	private Health health;
	private LeaderboardManager leaderboardManager;
	private RegionManager regionManager;
	private StatRegistry statRegistry;
	private SkillRegistry skillRegistry;
	private LuckPermsSupport luckPermsSupport;
	private SourceRegistry sourceRegistry;
	private ItemRegistry itemRegistry;
	private ProtocolLibSupport protocolLibSupport;
	private Slate slate;
	private MenuFileManager menuFileManager;
	private ForgingLeveler forgingLeveler;

	@Override
	public void onEnable() {
		// Registries
		statRegistry = new StatRegistry();
		registerStats();
		skillRegistry = new SkillRegistry();
		registerSkills();
		sourceRegistry = new SourceRegistry();
		itemRegistry = new ItemRegistry(this);
		inventoryManager = new InventoryManager(this);
		inventoryManager.init();
		AureliumAPI.setPlugin(this);
		// Checks for world guard
		if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
			if (WorldGuardPlugin.inst().getDescription().getVersion().contains("7.0")) {
				worldGuardEnabled = true;
				worldGuardSupport = new WorldGuardSupport(this);
				worldGuardSupport.loadRegions();
			}
		}
		else {
			worldGuardEnabled = false;
		}
		// Checks for PlaceholderAPI
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderSupport(this).register();
			placeholderAPIEnabled = true;
			getLogger().info("PlaceholderAPI Support Enabled!");
		}
		else {
			placeholderAPIEnabled = false;
		}
		// Checks for Vault
		if (setupEconomy()) {
			vaultEnabled = true;
			getLogger().info("Vault Support Enabled!");
		}
		else {
			vaultEnabled = false;
		}
		// Check for protocol lib
		protocolLibEnabled = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
		// Check towny
		townyEnabled = Bukkit.getPluginManager().isPluginEnabled("Towny");
		townySupport = new TownySupport(this);
		// Check for LuckPerms
		luckPermsEnabled = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
		if (luckPermsEnabled) {
			luckPermsSupport = new LuckPermsSupport();
		}
		// Check for Slimefun
		slimefunEnabled = Bukkit.getPluginManager().isPluginEnabled("Slimefun");
		if (slimefunEnabled) {
			getServer().getPluginManager().registerEvents(new SlimefunSupport(this), this);
			getLogger().info("Slimefun Support Enabled!");
		}
		// Load health
		Health health = new Health(this);
		this.health = health;
		getServer().getPluginManager().registerEvents(health, this);
		// Load config
		loadConfig();
		this.requirementManager = new RequirementManager(this);
		optionLoader = new OptionL(this);
		optionLoader.loadOptions();
		requirementManager.load();
		this.modifierManager = new ModifierManager(this);
		// Load sources
		sourceManager = new SourceManager(this);
		sourceManager.loadSources();
		// Load boss bar
		bossBar = new SkillBossBar(this);
		bossBar.loadOptions();
		// Checks for holographic displays
		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			holographicDisplaysEnabled = true;
			getServer().getPluginManager().registerEvents(new HologramSupport(this), this);
			getLogger().info("HolographicDisplays Support Enabled!");
		}
		else {
			holographicDisplaysEnabled = false;
		}
		commandManager = new PaperCommandManager(this);
		// Load items
		itemRegistry.loadFromFile();
		// Load languages
		lang = new Lang(this);
		getServer().getPluginManager().registerEvents(getLang(), this);
		lang.init();
		lang.loadEmbeddedMessages(getCommandManager());
		lang.loadLanguages(getCommandManager());
		// Load rewards
		rewardManager = new RewardManager(this);
		rewardManager.loadRewards();
		// Registers Commands
		registerCommands();
		// Region manager
		this.regionManager = new RegionManager(this);
		// Registers events
		registerEvents();
		// Load ability manager
		manaAbilityManager = new ManaAbilityManager(this);
		getServer().getPluginManager().registerEvents(getManaAbilityManager(), this);
		manaAbilityManager.init();
		// Load ability options
		abilityManager = new AbilityManager(this);
		abilityManager.loadOptions();
		// Load menus
		slate = new Slate(this);
		registerAndLoadMenus();
		// Load stats
		Regeneration regeneration = new Regeneration(this);
		getServer().getPluginManager().registerEvents(regeneration, this);
		regeneration.startRegen();
		regeneration.startSaturationRegen();
		// Load Action Bar
		if (protocolLibEnabled) {
			protocolLibSupport = new ProtocolLibSupport();
			new ActionBarCompatHandler(this).registerListeners();
		}
		actionBar.startUpdateActionBar();
		// Initialize storage
		this.playerManager = new PlayerManager(this);
		this.leaderboardManager = new LeaderboardManager();
		// Set proper storage provider
		if (OptionL.getBoolean(Option.MYSQL_ENABLED)) {
			MySqlStorageProvider mySqlStorageProvider = new MySqlStorageProvider(this);
			mySqlStorageProvider.init();

			MysqlBackup mysqlBackup = new MysqlBackup(this, mySqlStorageProvider);
			if (!mySqlStorageProvider.localeColumnExists()) {
				mysqlBackup.saveBackup(Bukkit.getConsoleSender(), false);
			}

			new LegacyMysqlToMysqlConverter(this, mySqlStorageProvider).convert();
			setStorageProvider(mySqlStorageProvider);
			this.backupProvider = mysqlBackup;
		} else {
			// Try to backup and convert legacy files
			new LegacyFileBackup(this).saveBackup(Bukkit.getConsoleSender(), false);
			new LegacyFileToYamlConverter(this).convert();
			setStorageProvider(new YamlStorageProvider(this));
			this.backupProvider = new YamlBackup(this);
		}
		// Initialize leaderboards
		new BukkitRunnable() {
			@Override
			public void run() {
				if (leaderboardManager.isNotSorting()) {
					storageProvider.updateLeaderboards();
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 12000);
		// Load leveler
		leveler = new Leveler(this);
		leveler.loadLevelRequirements();
		// Load loot tables
		lootTableManager = new LootTableManager(this);
		// Load world manager
		worldManager = new WorldManager(this);
		worldManager.loadWorlds();
		// B-stats
		int pluginId = 8629;
		new Metrics(this, pluginId);
		getLogger().info("Aurelium Skills has been enabled");
		if (System.currentTimeMillis() > ReleaseData.RELEASE_TIME + 21600000L) {
			checkUpdates();
		}
		MinecraftVersion.disableUpdateCheck();
		// Check if NBT API is supported for the version
		if (MinecraftVersion.getVersion() == MinecraftVersion.UNKNOWN) {
			getLogger().warning("NBT API is not yet supported for your Minecraft version, item modifier, requirement, and some other functionality is disabled!");
			nbtAPIEnabled = false;
		} else {
			nbtAPIEnabled = true;
		}
	}
	
	@Override
	public void onDisable() {
		for (PlayerData playerData : getPlayerManager().getPlayerDataMap().values()) {
			storageProvider.save(playerData.getPlayer(), false);
		}
		getPlayerManager().getPlayerDataMap().clear();
		File file = new File(this.getDataFolder(), "config.yml");
		if (file.exists()) {
			// Reloads config
			reloadConfig();
			// Save config
			saveConfig();
		}
		regionManager.saveAllRegions(false, true);
		regionManager.clearRegionMap();
		backupAutomatically();
		itemRegistry.saveToFile();
		// Remove fleeting
		AgilityAbilities agilityAbilities = new AgilityAbilities(this);
		for (Player player : Bukkit.getOnlinePlayers()) {
			agilityAbilities.removeFleetingQuit(player);
		}
	}

	private void backupAutomatically() {
		// Automatic backups
		if (OptionL.getBoolean(Option.AUTOMATIC_BACKUPS_ENABLED)) {
			File metaFile = new File(this.getDataFolder(), "/backups/meta.yml");
			FileConfiguration metaConfig = YamlConfiguration.loadConfiguration(metaFile);
			long lastBackup = metaConfig.getLong("last_automatic_backup", 0);
			// Save backup if past minimum interval
			if (lastBackup + (long) (OptionL.getDouble(Option.AUTOMATIC_BACKUPS_MINIMUM_INTERVAL_HOURS) * 3600000) <= System.currentTimeMillis()) {
				if (backupProvider != null) {
					backupProvider.saveBackup(getServer().getConsoleSender(), false);
					// Update meta file
					metaConfig.set("last_automatic_backup", System.currentTimeMillis());
					try {
						metaConfig.save(metaFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onLoad() {
		// Register WorldGuard flags
		if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
			if (WorldGuardPlugin.inst().getDescription().getVersion().contains("7.0")) {
				worldGuardFlags = new WorldGuardFlags();
				worldGuardFlags.register();
			}
		}
	}

	public void checkUpdates() {
		// Check for updates
		if (!OptionL.getBoolean(Option.CHECK_FOR_UPDATES)) return;
		new UpdateChecker(this, 81069).getVersion(version -> {
			if (!this.getDescription().getVersion().contains("Pre-Release") && !this.getDescription().getVersion().contains("Build")) {
				if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
					getLogger().info("New update available! You are on version " + this.getDescription().getVersion() + ", latest version is " +
							version);
					getLogger().info("Download it on Spigot:");
					getLogger().info("https://spigotmc.org/resources/81069");
				}
			}
			else {
				getLogger().info("You are on an in development version of the plugin, plugin may be buggy or unstable!");
				getLogger().info("Report any bugs to the support discord server or submit an issue here: https://github.com/Archy-X/AureliumSkills/issues");
			}
		});
	}

	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		try {
			InputStream is = getResource("config.yml");
			if (is != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(is));
				ConfigurationSection config = defConfig.getConfigurationSection("");
				if (config != null) {
					for (String key : config.getKeys(true)) {
						if (!getConfig().contains(key)) {
							getConfig().set(key, defConfig.get(key));
						}
					}
				}
				saveConfig();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerCommands() {
		getCommandManager().enableUnstableAPI("help");
		getCommandManager().usePerIssuerLocale(true, false);
		getCommandManager().getCommandContexts().registerContext(Stat.class, c -> {
			String input = c.popFirstArg();
			if (input == null)
				throw new IndexOutOfBoundsException();
			Stat stat = statRegistry.getStat(input);
			if (stat != null) {
				return stat;
			} else {
				throw new InvalidCommandArgument("Stat " + input + " not found!");
			}
		});
		getCommandManager().getCommandContexts().registerContext(Skill.class, c -> {
			@Nullable String input = c.popFirstArg();
			@Nullable Skill skill = null;
			
			if (input != null)
				skill = skillRegistry.getSkill(input);
			
			if (skill != null) {
				return skill;
			} else {
				throw new InvalidCommandArgument("Skill " + input + " not found!");
			}
		});
		getCommandManager().getCommandCompletions().registerAsyncCompletion("skills", c -> {
			List<@NotNull String> values = new ArrayList<>();
			for (Skill skill : skillRegistry.getSkills()) {
				if (OptionL.isEnabled(skill)) {
					values.add(skill.toString().toLowerCase(Locale.ENGLISH));
				}
			}
			return values;
		});
		getCommandManager().getCommandCompletions().registerAsyncCompletion("skills_global", c -> {
			List<@NotNull String> values = new ArrayList<>();
			values.add("global");
			for (Skill skill : skillRegistry.getSkills()) {
				if (OptionL.isEnabled(skill)) {
					values.add(skill.toString().toLowerCase(Locale.ENGLISH));
				}
			}
			return values;
		});
		getCommandManager().getCommandCompletions().registerAsyncCompletion("skillTop", c -> {
			List<@NotNull String> values = new ArrayList<>();
			for (Skill skill : skillRegistry.getSkills()) {
				if (OptionL.isEnabled(skill)) {
					values.add(skill.toString().toLowerCase(Locale.ENGLISH));
				}
			}
			values.add("average");
			return values;
		});
		getCommandManager().getCommandCompletions().registerAsyncCompletion("stats", c -> {
			List<@NotNull String> values = new ArrayList<>();
			for (Stat stat : statRegistry.getStats()) {
				values.add(stat.toString().toLowerCase(Locale.ENGLISH));
			}
			return values;
		});
		getCommandManager().getCommandCompletions().registerAsyncCompletion("lang", c -> Lang.getDefinedLanguagesSet());
		getCommandManager().getCommandCompletions().registerAsyncCompletion("modifiers", c -> {
			@Nullable Player player = c.getPlayer();
			if (player != null) {
				@Nullable PlayerData playerData = getPlayerManager().getPlayerData(player);
				if (playerData != null) {
					return playerData.getStatModifiers().keySet();
				}
			}
			return null;
		});
		getCommandManager().getCommandCompletions().registerAsyncCompletion("item_keys", c -> itemRegistry.getKeys());
		getCommandManager().getCommandCompletions().registerAsyncCompletion("sort_types", c -> {
			SorterItem.SortType[] sortTypes = SorterItem.SortType.values();
			List<@NotNull String> typeNames = new ArrayList<>();
			for (SorterItem.SortType sortType : sortTypes) {
				typeNames.add(sortType.toString().toLowerCase(Locale.ROOT));
			}
			return typeNames;
		});
		getCommandManager().registerCommand(new SkillsCommand(this));
		getCommandManager().registerCommand(new StatsCommand(this));
		getCommandManager().registerCommand(new ManaCommand(this));
		if (OptionL.getBoolean(Option.ENABLE_SKILL_COMMANDS)) {
			if (OptionL.isEnabled(Skills.FARMING)) { getCommandManager().registerCommand(new SkillCommands.FarmingCommand(this)); }
			if (OptionL.isEnabled(Skills.FORAGING)) { getCommandManager().registerCommand(new SkillCommands.ForagingCommand(this)); }
			if (OptionL.isEnabled(Skills.MINING)) { getCommandManager().registerCommand(new SkillCommands.MiningCommand(this)); }
			if (OptionL.isEnabled(Skills.FISHING)) { getCommandManager().registerCommand(new SkillCommands.FishingCommand(this)); }
			if (OptionL.isEnabled(Skills.EXCAVATION)) { getCommandManager().registerCommand(new SkillCommands.ExcavationCommand(this)); }
			if (OptionL.isEnabled(Skills.ARCHERY)) { getCommandManager().registerCommand(new SkillCommands.ArcheryCommand(this)); }
			if (OptionL.isEnabled(Skills.DEFENSE)) { getCommandManager().registerCommand(new SkillCommands.DefenseCommand(this)); }
			if (OptionL.isEnabled(Skills.FIGHTING)) { getCommandManager().registerCommand(new SkillCommands.FightingCommand(this)); }
			if (OptionL.isEnabled(Skills.ENDURANCE)) { getCommandManager().registerCommand(new SkillCommands.EnduranceCommand(this)); }
			if (OptionL.isEnabled(Skills.AGILITY)) { getCommandManager().registerCommand(new SkillCommands.AgilityCommand(this)); }
			if (OptionL.isEnabled(Skills.ALCHEMY)) { getCommandManager().registerCommand(new SkillCommands.AlchemyCommand(this)); }
			if (OptionL.isEnabled(Skills.ENCHANTING)) { getCommandManager().registerCommand(new SkillCommands.EnchantingCommand(this)); }
			if (OptionL.isEnabled(Skills.SORCERY)) { getCommandManager().registerCommand(new SkillCommands.SorceryCommand(this)); }
			if (OptionL.isEnabled(Skills.HEALING)) { getCommandManager().registerCommand(new SkillCommands.HealingCommand(this)); }
			if (OptionL.isEnabled(Skills.FORGING)) { getCommandManager().registerCommand(new SkillCommands.ForgingCommand(this)); }
		}
	}

	public void registerEvents() {
		// Registers Events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerJoinQuit(this), this);
		regionBlockListener = new RegionBlockListener(this);
		pm.registerEvents(regionBlockListener, this);
		pm.registerEvents(new FarmingLeveler(this), this);
		if (VersionUtils.isAtLeastVersion(16)) {
			pm.registerEvents(new FarmingHarvestLeveler(this), this);
		} else {
			pm.registerEvents(new FarmingInteractLeveler(this), this);
		}
		pm.registerEvents(new ForagingLeveler(this), this);
		pm.registerEvents(new MiningLeveler(this), this);
		pm.registerEvents(new ExcavationLeveler(this), this);
		pm.registerEvents(new FishingLeveler(this), this);
		pm.registerEvents(new FightingLeveler(this), this);
		pm.registerEvents(new ArcheryLeveler(this), this);
		pm.registerEvents(new DefenseLeveler(this), this);
		EnduranceLeveler enduranceLeveler = new EnduranceLeveler(this);
		enduranceLeveler.startTracking();
		pm.registerEvents(enduranceLeveler, this);
		pm.registerEvents(new AgilityLeveler(this), this);
		pm.registerEvents(new AlchemyLeveler(this), this);
		pm.registerEvents(new EnchantingLeveler(this), this);
		sorceryLeveler = new SorceryLeveler(this);
		pm.registerEvents(sorceryLeveler, this);
		pm.registerEvents(new HealingLeveler(this), this);
		forgingLeveler = new ForgingLeveler(this);
		pm.registerEvents(forgingLeveler, this);
		pm.registerEvents(new Luck(this), this);
		pm.registerEvents(new Wisdom(this), this);
		pm.registerEvents(new FarmingAbilities(this), this);
		pm.registerEvents(new ForagingAbilities(this), this);
		pm.registerEvents(new MiningAbilities(this), this);
		pm.registerEvents(new FishingAbilities(this), this);
		pm.registerEvents(new ArcheryAbilities(this), this);
		DefenseAbilities defenseAbilities = new DefenseAbilities(this);
		pm.registerEvents(defenseAbilities, this);
		FightingAbilities fightingAbilities = new FightingAbilities(this);
		pm.registerEvents(fightingAbilities, this);
		pm.registerEvents(new FightingAbilities(this), this);
		pm.registerEvents(new EnduranceAbilities(this), this);
		pm.registerEvents(new AgilityAbilities(this), this);
		pm.registerEvents(new AlchemyAbilities(this), this);
		pm.registerEvents(new EnchantingAbilities(this), this);
		pm.registerEvents(new HealingAbilities(this), this);
		pm.registerEvents(new ForgingAbilities(this), this);
		pm.registerEvents(new DamageListener(this, defenseAbilities, fightingAbilities), this);
		// Load mana manager
		manaManager = new ManaManager(this);
		getServer().getPluginManager().registerEvents(getManaManager(), this);
		manaManager.startRegen();
		ItemListener itemListener = new ItemListener(this);
		pm.registerEvents(itemListener, this);
		itemListener.scheduleTask();
		pm.registerEvents(new ArmorListener(OptionL.getList(Option.MODIFIER_ARMOR_EQUIP_BLOCKED_MATERIALS)), this);
		pm.registerEvents(new ArmorModifierListener(this), this);
		pm.registerEvents(new RequirementListener(this), this);
		this.actionBar = new ActionBar(this);
		pm.registerEvents(actionBar, this);
		pm.registerEvents(new RegionListener(this), this);
		pm.registerEvents(new FishingLootHandler(this), this);
		pm.registerEvents(new ExcavationLootHandler(this), this);
		pm.registerEvents(new MiningLootHandler(this), this);
		pm.registerEvents(new ForagingLootHandler(this), this);
	}

	private boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<@NotNull Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return true;
	}

	private void registerStats() {
		statRegistry.register("strength", Stats.STRENGTH);
		statRegistry.register("health", Stats.HEALTH);
		statRegistry.register("regeneration", Stats.REGENERATION);
		statRegistry.register("luck", Stats.LUCK);
		statRegistry.register("wisdom", Stats.WISDOM);
		statRegistry.register("toughness", Stats.TOUGHNESS);
	}

	private void registerSkills() {
		skillRegistry.register("farming", Skills.FARMING);
		skillRegistry.register("foraging", Skills.FORAGING);
		skillRegistry.register("mining", Skills.MINING);
		skillRegistry.register("fishing", Skills.FISHING);
		skillRegistry.register("excavation", Skills.EXCAVATION);
		skillRegistry.register("archery", Skills.ARCHERY);
		skillRegistry.register("defense", Skills.DEFENSE);
		skillRegistry.register("fighting", Skills.FIGHTING);
		skillRegistry.register("endurance", Skills.ENDURANCE);
		skillRegistry.register("agility", Skills.AGILITY);
		skillRegistry.register("alchemy", Skills.ALCHEMY);
		skillRegistry.register("enchanting", Skills.ENCHANTING);
		skillRegistry.register("sorcery", Skills.SORCERY);
		skillRegistry.register("healing", Skills.HEALING);
		skillRegistry.register("forging", Skills.FORGING);
	}

	private void registerAndLoadMenus() {
		new MenuRegistrar(this).register();
		menuFileManager = new MenuFileManager(this);
		menuFileManager.generateDefaultFiles();
		menuFileManager.loadMenus();
	}

	public @NotNull RewardManager getRewardManager() {
		Objects.requireNonNull(rewardManager);
		return rewardManager;
	}

	public @NotNull PlayerManager getPlayerManager() {
		Objects.requireNonNull(playerManager);
		return playerManager;
	}

	public @NotNull Economy getEconomy() {
		Objects.requireNonNull(economy);
		return economy;
	}

	public @NotNull LootTableManager getLootTableManager() {
		Objects.requireNonNull(lootTableManager);
		return lootTableManager;
	}

	public @NotNull InventoryManager getInventoryManager() {
		Objects.requireNonNull(inventoryManager);
		return inventoryManager;
	}

	public @NotNull AbilityManager getAbilityManager() {
		Objects.requireNonNull(abilityManager);
		return abilityManager;
	}

	public @Nullable WorldGuardSupport getWorldGuardSupport() {
		return worldGuardSupport;
	}

	public @NotNull WorldManager getWorldManager() {
		Objects.requireNonNull(worldManager);
		return worldManager;
	}

	public @NotNull ManaManager getManaManager() {
		Objects.requireNonNull(manaManager);
		return manaManager;
	}

	public @NotNull ManaAbilityManager getManaAbilityManager() {
		Objects.requireNonNull(manaAbilityManager);
		return manaAbilityManager;
	}

	public @NotNull PaperCommandManager getCommandManager() {
		Objects.requireNonNull(commandManager);
		return commandManager;
	}

	public static @NotNull String getPrefix(@Nullable Locale locale) {
		return Lang.getMessage(CommandMessage.PREFIX, locale);
	}

	public @NotNull ActionBar getActionBar() {
		Objects.requireNonNull(actionBar);
		return actionBar;
	}

	public @NotNull SkillBossBar getBossBar() {
		Objects.requireNonNull(bossBar);
		return bossBar;
	}

	public @NotNull SourceManager getSourceManager() {
		Objects.requireNonNull(sourceManager);
		return sourceManager;
	}

	public @NotNull SorceryLeveler getSorceryLeveler() {
		Objects.requireNonNull(sorceryLeveler);
		return sorceryLeveler;
	}

	public @NotNull RegionBlockListener getCheckBlockReplace() {
		Objects.requireNonNull(regionBlockListener);
		return regionBlockListener;
	}

	public @NotNull RequirementManager getRequirementManager() {
		Objects.requireNonNull(requirementManager);
		return requirementManager;
	}

	public @NotNull OptionL getOptionLoader() {
		Objects.requireNonNull(optionLoader);
		return optionLoader;
	}

	public @NotNull ModifierManager getModifierManager() {
		Objects.requireNonNull(modifierManager);
		return modifierManager;
	}

	public @NotNull Lang getLang() {
		Objects.requireNonNull(lang);
		return lang;
	}

	public @NotNull Leveler getLeveler() {
		Objects.requireNonNull(leveler);
		return leveler;
	}

	public boolean isHolographicDisplaysEnabled() {
		Objects.requireNonNull(holographicDisplaysEnabled);
		return holographicDisplaysEnabled;
	}

	public boolean isWorldGuardEnabled() {
		Objects.requireNonNull(worldGuardEnabled);
		return worldGuardEnabled;
	}

	public void setWorldGuardEnabled(boolean worldGuardEnabled) {
		this.worldGuardEnabled = worldGuardEnabled;
	}

	public boolean isPlaceholderAPIEnabled() {
		return placeholderAPIEnabled;
	}

	public boolean isVaultEnabled() {
		return vaultEnabled;
	}

	public boolean isProtocolLibEnabled() {
		return protocolLibEnabled;
	}

	public boolean isLuckPermsEnabled() {
		return luckPermsEnabled;
	}

	public boolean isSlimefunEnabled() {
		return slimefunEnabled;
	}

	public @NotNull Health getHealth() {
		Objects.requireNonNull(health);
		return health;
	}

	public @NotNull StorageProvider getStorageProvider() {
		Objects.requireNonNull(storageProvider);
		return storageProvider;
	}

	public void setStorageProvider(StorageProvider storageProvider) {
		this.storageProvider = storageProvider;
	}

	public @NotNull BackupProvider getBackupProvider() {
		Objects.requireNonNull(backupProvider);
		return backupProvider;
	}

	public @NotNull LeaderboardManager getLeaderboardManager() {
		Objects.requireNonNull(leaderboardManager);
		return leaderboardManager;
	}

	public boolean isTownyEnabled() {
		return townyEnabled;
	}

	public @Nullable TownySupport getTownySupport() {
		return townySupport;
	}

	public @NotNull RegionManager getRegionManager() {
		Objects.requireNonNull(regionManager);
		return regionManager;
	}

	public @NotNull StatRegistry getStatRegistry() {
		Objects.requireNonNull(statRegistry);
		return statRegistry;
	}

	public @NotNull SkillRegistry getSkillRegistry() {
		Objects.requireNonNull(skillRegistry);
		return skillRegistry;
	}

	public @Nullable LuckPermsSupport getLuckPermsSupport() {
		return luckPermsSupport;
	}

	public @NotNull SourceRegistry getSourceRegistry() {
		Objects.requireNonNull(sourceRegistry);
		return sourceRegistry;
	}

	public @NotNull ItemRegistry getItemRegistry() {
		Objects.requireNonNull(itemRegistry);
		return itemRegistry;
	}

	public @Nullable WorldGuardFlags getWorldGuardFlags() {
		return worldGuardFlags;
	}

	public @Nullable ProtocolLibSupport getProtocolLibSupport() {
		return protocolLibSupport;
	}

	public boolean isNBTAPIDisabled() {
		return !nbtAPIEnabled;
	}

	public @NotNull Slate getSlate() {
		Objects.requireNonNull(slate);
		return slate;
	}

	public @NotNull MenuManager getMenuManager() {
		return slate.getMenuManager();
	}

	public @NotNull MenuFileManager getMenuFileManager() {
		Objects.requireNonNull(menuFileManager);
		return menuFileManager;
	}

	public @NotNull ForgingLeveler getForgingLeveler() {
		Objects.requireNonNull(forgingLeveler);
		return forgingLeveler;
	}
}
