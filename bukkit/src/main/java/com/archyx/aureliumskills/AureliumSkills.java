package com.archyx.aureliumskills;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.archyx.aureliumskills.ability.AbilityManager;
import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.commands.*;
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
import com.archyx.aureliumskills.listeners.PlayerDeath;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
	private boolean decentHologramsEnabled;
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
	private final int resourceId = 81069;

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
			getServer().getPluginManager().registerEvents(new SlimeFunSupport(this), this);
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
		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays") && OptionL.getBoolean(Option.HOLOGRAPHIC_DISPLAYS_ENABLED)) {
			decentHologramsEnabled = false;
			holographicDisplaysEnabled = true;
			getLogger().info("HolographicDisplays Support Enabled!");
		}
		else if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms") && OptionL.getBoolean(Option.DECENT_HOLOGRAMS_ENABLED)) {
			holographicDisplaysEnabled = false;
			decentHologramsEnabled = true;
			getLogger().info("DecentHolograms Support Enabled!");
		}
		else {
			holographicDisplaysEnabled = false;
			decentHologramsEnabled = false;
		}
		if (holographicDisplaysEnabled || decentHologramsEnabled) {
			getServer().getPluginManager().registerEvents(new HologramSupport(this), this);
		}
		commandManager = new PaperCommandManager(this);
		// Load	items
		itemRegistry.loadFromFile();
		// Load languages
		lang = new Lang(this);
		getServer().getPluginManager().registerEvents(lang, this);
		lang.init();
		lang.loadEmbeddedMessages(commandManager);
		lang.loadLanguages(commandManager);
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
		getServer().getPluginManager().registerEvents(manaAbilityManager, this);
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
		checkUpdates();
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
		for (PlayerData playerData : playerManager.getPlayerDataMap().values()) {
			storageProvider.save(playerData.getPlayer(), false);
		}
		playerManager.getPlayerDataMap().clear();
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
		UpdateChecker updateChecker = new UpdateChecker(this, 81069);
		updateChecker.getVersion(version -> {
			if (updateChecker.isOutdated(this.getDescription().getVersion(), version)) {
				getLogger().info("New update available! You are on version " + this.getDescription().getVersion() + ", latest version is " +
						version);
				getLogger().info("Download it on Spigot:");
				getLogger().info("https://spigotmc.org/resources/" + resourceId);
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
		commandManager.enableUnstableAPI("help");
		commandManager.usePerIssuerLocale(true, false);
		commandManager.getCommandContexts().registerContext(Stat.class, c -> {
			String input = c.popFirstArg();
			Stat stat = statRegistry.getStat(input);
			if (stat != null) {
				return stat;
			} else {
				throw new InvalidCommandArgument("Stat " + input + " not found!");
			}
		});
		commandManager.getCommandContexts().registerContext(Skill.class, c -> {
			String input = c.popFirstArg();
			Skill skill = skillRegistry.getSkill(input);
			if (skill != null) {
				return skill;
			} else {
				throw new InvalidCommandArgument("Skill " + input + " not found!");
			}
		});
		commandManager.getCommandContexts().registerContext(UUID.class, c -> {
			String input = c.popFirstArg();
			try {
				return UUID.fromString(input);
			} catch (IllegalArgumentException e) {
				throw new InvalidCommandArgument(input + "is not a valid UUID!");
			}
		});
		commandManager.getCommandCompletions().registerAsyncCompletion("skills", c -> {
			List<String> values = new ArrayList<>();
			for (Skill skill : skillRegistry.getSkills()) {
				if (OptionL.isEnabled(skill)) {
					values.add(skill.toString().toLowerCase(Locale.ENGLISH));
				}
			}
			return values;
		});
		commandManager.getCommandCompletions().registerAsyncCompletion("skills_global", c -> {
			List<String> values = new ArrayList<>();
			values.add("global");
			for (Skill skill : skillRegistry.getSkills()) {
				if (OptionL.isEnabled(skill)) {
					values.add(skill.toString().toLowerCase(Locale.ENGLISH));
				}
			}
			return values;
		});
		commandManager.getCommandCompletions().registerAsyncCompletion("skillTop", c -> {
			List<String> values = new ArrayList<>();
			for (Skill skill : skillRegistry.getSkills()) {
				if (OptionL.isEnabled(skill)) {
					values.add(skill.toString().toLowerCase(Locale.ENGLISH));
				}
			}
			values.add("average");
			return values;
		});
		commandManager.getCommandCompletions().registerAsyncCompletion("stats", c -> {
			List<String> values = new ArrayList<>();
			for (Stat stat : statRegistry.getStats()) {
				values.add(stat.toString().toLowerCase(Locale.ENGLISH));
			}
			return values;
		});
		commandManager.getCommandCompletions().registerAsyncCompletion("lang", c -> Lang.getDefinedLanguagesSet());
		commandManager.getCommandCompletions().registerAsyncCompletion("modifiers", c -> {
			Player player = c.getPlayer();
			PlayerData playerData = getPlayerManager().getPlayerData(player);
			if (playerData != null) {
				return playerData.getStatModifiers().keySet();
			}
			return null;
		});
		commandManager.getCommandCompletions().registerAsyncCompletion("item_keys", c -> itemRegistry.getKeys());
		commandManager.getCommandCompletions().registerAsyncCompletion("sort_types", c -> {
			SorterItem.SortType[] sortTypes = SorterItem.SortType.values();
			List<String> typeNames = new ArrayList<>();
			for (SorterItem.SortType sortType : sortTypes) {
				typeNames.add(sortType.toString().toLowerCase(Locale.ROOT));
			}
			return typeNames;
		});
		commandManager.getCommandReplacements().addReplacement("skills_alias", "skills|sk|skill");
		commandManager.registerCommand(new SkillsRootCommand(this));
		commandManager.registerCommand(new ArmorCommand(this));
		commandManager.registerCommand(new BackupCommand(this));
		commandManager.registerCommand(new ItemCommand(this));
		commandManager.registerCommand(new ModifierCommand(this));
		commandManager.registerCommand(new ProfileCommand(this));
		commandManager.registerCommand(new SkillCommand(this));
		commandManager.registerCommand(new XpCommand(this));
		commandManager.registerCommand(new StatsCommand(this));
		commandManager.registerCommand(new ManaCommand(this));
		if (OptionL.getBoolean(Option.ENABLE_SKILL_COMMANDS)) {
			if (OptionL.isEnabled(Skills.FARMING)) { commandManager.registerCommand(new SkillCommands.FarmingCommand(this)); }
			if (OptionL.isEnabled(Skills.FORAGING)) { commandManager.registerCommand(new SkillCommands.ForagingCommand(this)); }
			if (OptionL.isEnabled(Skills.MINING)) { commandManager.registerCommand(new SkillCommands.MiningCommand(this)); }
			if (OptionL.isEnabled(Skills.FISHING)) { commandManager.registerCommand(new SkillCommands.FishingCommand(this)); }
			if (OptionL.isEnabled(Skills.EXCAVATION)) { commandManager.registerCommand(new SkillCommands.ExcavationCommand(this)); }
			if (OptionL.isEnabled(Skills.ARCHERY)) { commandManager.registerCommand(new SkillCommands.ArcheryCommand(this)); }
			if (OptionL.isEnabled(Skills.DEFENSE)) { commandManager.registerCommand(new SkillCommands.DefenseCommand(this)); }
			if (OptionL.isEnabled(Skills.FIGHTING)) { commandManager.registerCommand(new SkillCommands.FightingCommand(this)); }
			if (OptionL.isEnabled(Skills.ENDURANCE)) { commandManager.registerCommand(new SkillCommands.EnduranceCommand(this)); }
			if (OptionL.isEnabled(Skills.AGILITY)) { commandManager.registerCommand(new SkillCommands.AgilityCommand(this)); }
			if (OptionL.isEnabled(Skills.ALCHEMY)) { commandManager.registerCommand(new SkillCommands.AlchemyCommand(this)); }
			if (OptionL.isEnabled(Skills.ENCHANTING)) { commandManager.registerCommand(new SkillCommands.EnchantingCommand(this)); }
			if (OptionL.isEnabled(Skills.SORCERY)) { commandManager.registerCommand(new SkillCommands.SorceryCommand(this)); }
			if (OptionL.isEnabled(Skills.HEALING)) { commandManager.registerCommand(new SkillCommands.HealingCommand(this)); }
			if (OptionL.isEnabled(Skills.FORGING)) { commandManager.registerCommand(new SkillCommands.ForgingCommand(this)); }
		}
	}

	public void registerEvents() {
		// Registers Events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerJoinQuit(this), this);
		pm.registerEvents(new PlayerDeath(this), this);
		regionBlockListener = new RegionBlockListener(this);
		pm.registerEvents(regionBlockListener, this);
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
		getServer().getPluginManager().registerEvents(manaManager, this);
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
		pm.registerEvents(bossBar, this);
	}

	private boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return true;
	}

	private void registerStats() {
		statRegistry.register("strength", Stats.STRENGTH);
		statRegistry.registerProvider(Stats.STRENGTH, new Strength());
		statRegistry.register("health", Stats.HEALTH);
		statRegistry.register("regeneration", Stats.REGENERATION);
		statRegistry.register("luck", Stats.LUCK);
		statRegistry.register("wisdom", Stats.WISDOM);
		statRegistry.register("toughness", Stats.TOUGHNESS);
		statRegistry.register("crit_chance", Stats.CRIT_CHANCE);
		statRegistry.register("crit_damage", Stats.CRIT_DAMAGE);
		statRegistry.register("speed", Stats.SPEED);
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

	public RewardManager getRewardManager() {
		return rewardManager;
	}

	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	public Economy getEconomy() {
		return economy;
	}

	public LootTableManager getLootTableManager() {
		return lootTableManager;
	}

	public InventoryManager getInventoryManager() {
		return inventoryManager;
	}

	public AbilityManager getAbilityManager() {
		return abilityManager;
	}

	public WorldGuardSupport getWorldGuardSupport() {
		return worldGuardSupport;
	}

	public WorldManager getWorldManager() {
		return worldManager;
	}

	public ManaManager getManaManager() {
		return manaManager;
	}

	public ManaAbilityManager getManaAbilityManager() {
		return manaAbilityManager;
	}

	public PaperCommandManager getCommandManager() {
		return commandManager;
	}

	public static String getPrefix(Locale locale) {
		return Lang.getMessage(CommandMessage.PREFIX, locale);
	}

	public ActionBar getActionBar() {
		return actionBar;
	}

	public SkillBossBar getBossBar() {
		return bossBar;
	}

	public SourceManager getSourceManager() {
		return sourceManager;
	}

	public SorceryLeveler getSorceryLeveler() {
		return sorceryLeveler;
	}

	public RegionBlockListener getCheckBlockReplace() {
		return regionBlockListener;
	}

	public RequirementManager getRequirementManager() {
		return requirementManager;
	}

	public OptionL getOptionLoader() {
		return optionLoader;
	}

	public ModifierManager getModifierManager() {
		return modifierManager;
	}

	public Lang getLang() {
		return lang;
	}

	public Leveler getLeveler() {
		return leveler;
	}

	public boolean isHolographicDisplaysEnabled() {
		return holographicDisplaysEnabled;
	}

	public boolean isDecentHologramsEnabled() {
		return decentHologramsEnabled;
	}

	public boolean isWorldGuardEnabled() {
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

	public Health getHealth() {
		return health;
	}

	public StorageProvider getStorageProvider() {
		return storageProvider;
	}

	public void setStorageProvider(StorageProvider storageProvider) {
		this.storageProvider = storageProvider;
	}

	public BackupProvider getBackupProvider() {
		return backupProvider;
	}

	public LeaderboardManager getLeaderboardManager() {
		return leaderboardManager;
	}

	public boolean isTownyEnabled() {
		return townyEnabled;
	}

	public TownySupport getTownySupport() {
		return townySupport;
	}

	public RegionManager getRegionManager() {
		return regionManager;
	}

	public StatRegistry getStatRegistry() {
		return statRegistry;
	}

	public SkillRegistry getSkillRegistry() {
		return skillRegistry;
	}

	public LuckPermsSupport getLuckPermsSupport() {
		return luckPermsSupport;
	}

	public SourceRegistry getSourceRegistry() {
		return sourceRegistry;
	}

	public ItemRegistry getItemRegistry() {
		return itemRegistry;
	}

	@Nullable
	public WorldGuardFlags getWorldGuardFlags() {
		return worldGuardFlags;
	}

	public ProtocolLibSupport getProtocolLibSupport() {
		return protocolLibSupport;
	}

	public boolean isNBTAPIDisabled() {
		return !nbtAPIEnabled;
	}

	public Slate getSlate() {
		return slate;
	}

	public MenuManager getMenuManager() {
		return slate.getMenuManager();
	}

	public MenuFileManager getMenuFileManager() {
		return menuFileManager;
	}

	public ForgingLeveler getForgingLeveler() {
		return forgingLeveler;
	}

	public int getResourceId() {
		return resourceId;
	}
}