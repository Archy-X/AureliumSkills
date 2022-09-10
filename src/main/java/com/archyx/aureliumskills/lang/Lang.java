package com.archyx.aureliumskills.lang;

import co.aikar.commands.MessageKeys;
import co.aikar.commands.MinecraftMessageKeys;
import co.aikar.commands.PaperCommandManager;
import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.util.text.TextUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang implements Listener {

	private final String[] embeddedLanguages = new String[] {"en", "id", "es", "fr", "zh-TW", "tr", "pl", "pt-BR", "zh-CN", "de", "lt", "ru", "it", "ko", "cs"};
	private static final Map<Locale, Map<MessageKey, String>> messages = new HashMap<>();
	private static Map<Locale, String> definedLanguages;
	private static Locale defaultLanguage;
	private final AureliumSkills plugin;
	private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

	public Lang(AureliumSkills plugin) {
		this.plugin = plugin;
	}

	public void init() {
		moveFilesToFolder();
		loadLanguageFiles();
	}

	public void loadLanguageFiles() {
		for (String language : embeddedLanguages) {
			if (!new File(plugin.getDataFolder() + "/messages", "messages_" + language + ".yml").exists()) {
				plugin.saveResource("messages/messages_" + language + ".yml", false);
			}
		}
	}

	public void loadEmbeddedMessages(PaperCommandManager commandManager) {
        Objects.requireNonNull(commandManager);
		// Loads default file from embedded resource
		InputStream inputStream = plugin.getResource("messages/messages_en.yml");
		if (inputStream != null) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			Locale locale = new Locale("en");
			// Load messages
			loadMessages(config, locale, commandManager);
		}
	}

	public void loadLanguages(PaperCommandManager commandManager) {
        Objects.requireNonNull(commandManager);
		Bukkit.getLogger().info("[AureliumSkills] Loading languages...");
		long startTime = System.currentTimeMillis();
		FileConfiguration pluginConfig = plugin.getConfig();
		// Load languages list and default, add to default if not present
		List<String> languages = new ArrayList<>(pluginConfig.getStringList("languages"));
		String defaultLanguageString = pluginConfig.getString("default-language");
		if (defaultLanguageString == null) {
			defaultLanguageString = "en";
		}
		if (!languages.contains(defaultLanguageString)) {
			languages.add(defaultLanguageString);
		}
		// Sets default language
		setDefaultLanguage(new Locale(defaultLanguageString));
		definedLanguages = new HashMap<>();
		// Load languages
		int languagesLoaded = 0;
		for (String language : languages) {
		    if (language == null)
		        throw new IllegalStateException("Null value returned from configuration key: languages");
			// Load file
			try {
				Locale locale = new Locale(language);
				File file = new File(plugin.getDataFolder() + "/messages", "messages_" + language + ".yml");
				// Load and update file
				FileConfiguration config = updateFile(file, YamlConfiguration.loadConfiguration(file), language);
				if (config.contains("file_version")) {
					// Load messages
					loadMessages(config, locale, commandManager);
					languagesLoaded++;
					definedLanguages.put(locale, language);
				}
				else {
					Bukkit.getLogger().warning("[AureliumSkills] Could not load file messages_" + language + ".yml! Does this file exist and does it contain a file_version?");
					if (language.equals(defaultLanguageString)) {
						Bukkit.getLogger().warning("[AureliumSkills] The default-language could not be loaded, setting the default language to en!");
						setDefaultLanguage(Locale.ENGLISH);
					}
				}
			} catch (Exception e) {
				Bukkit.getLogger().warning("[AureliumSkills] Error loading messages file messages_" + language + ".yml");
				e.printStackTrace();
			}
		}
		long endTime = System.currentTimeMillis();
		Bukkit.getLogger().info("[AureliumSkills] Loaded " + languagesLoaded + " languages in " + (endTime - startTime) + "ms");
	}

	private void loadMessages(FileConfiguration config, Locale locale, PaperCommandManager commandManager) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(locale);
        Objects.requireNonNull(commandManager);
		// Load units
		Map<UnitMessage, String> units = new HashMap<>();
		for (UnitMessage key : UnitMessage.values()) {
			String message = config.getString(key.getPath());
			if (message != null) {
				units.put(key, applyColor(message));
			}
		}
		// Load message keys
		Map<MessageKey, String> messages = Lang.messages.get(locale);
		if (messages == null) {
			messages = new HashMap<>();
		}
		for (String path : config.getKeys(true)) {
			if (!config.isConfigurationSection(path)) {
				MessageKey key = null;
				// Try to find message key
				for (MessageKey messageKey : MessageKey.values()) {
					String keyPath = messageKey.getPath();
					assert (null != keyPath);
					if (keyPath.equals(path)) {
						key = messageKey;
					}
				}
				// Create custom key if not found
				if (key == null && !path.startsWith("acf")) {
					key = new CustomMessageKey(path);
				}
				if (key != null) {
					String message = config.getString(path);
					if (message != null) {
						messages.put(key, applyColor(TextUtil.replace(message
								, "{mana_unit}", units.get(UnitMessage.MANA)
								, "{hp_unit}", units.get(UnitMessage.HP)
								, "{xp_unit}", units.get(UnitMessage.XP))));
					}
				}
			}
		}
		// Check that each message key has a value
		for (MessageKey key : MessageKey.values()) {
			String path = key.getPath();
			assert (null != path);
			if (path.isEmpty())
			    continue;
			String message = config.getString(path);
			if (message == null && locale.equals(Locale.ENGLISH)) {
				plugin.getLogger().warning("[" + locale.toLanguageTag() + "] Message with path " + key.getPath() + " not found!");
			}
		}
		for (ACFCoreMessage message : ACFCoreMessage.values()) {
			String path = message.getPath();
			String configMessage = config.getString(path);
			if (configMessage != null) {
				commandManager.getLocales().addMessage(locale, MessageKeys.valueOf(message.name()), applyColor(configMessage));
			}
		}
		for (ACFMinecraftMessage message : ACFMinecraftMessage.values()) {
			String path = message.getPath();
			String configMessage = config.getString(path);
			if (configMessage != null) {
				commandManager.getLocales().addMessage(locale, MinecraftMessageKeys.valueOf(message.name()), applyColor(configMessage));
			}
		}
		Lang.messages.put(locale, messages);
	}

	private void moveFilesToFolder() {
		File file = new File(plugin.getDataFolder(), "messages_en.yml");
		if (file.exists()) {
			try {
				File directory = new File(plugin.getDataFolder() + "/messages");
				if (directory.mkdir()) {
					File[] pluginFiles = plugin.getDataFolder().listFiles();
					if (pluginFiles != null) {
						int filesMoved = 0;
						for (File pluginFile : pluginFiles) {
							if (pluginFile.getName().contains("messages") && pluginFile.isFile()) {
								if (pluginFile.renameTo(new File(plugin.getDataFolder() + "/messages/" + pluginFile.getName()))) {
									filesMoved++;
								} else {
									Bukkit.getLogger().warning("[AureliumSkills] Failed to move file " + pluginFile.getName() + " to messages folder!");
								}
							}
						}
						Bukkit.getLogger().warning("[AureliumSkills] Moved " + filesMoved + " files to messages folder. " +
								"If you are seeing this message, an update moved message files to a separate messages folder. " +
								"From now on use the messages folder for editing and adding messages. " +
								"If there are still message files in the root directory, you can delete them.");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Bukkit.getLogger().severe("[AureliumSkills] Error moving messages files to messages folder!");
			}
		}
	}

	private FileConfiguration updateFile(File file, FileConfiguration config, String language) {
		if (config.contains("file_version")) {
			InputStream stream = plugin.getResource("messages/messages_" + language + ".yml");
			if (stream != null) {
				int currentVersion = config.getInt("file_version");
				FileConfiguration imbConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
				int imbVersion = imbConfig.getInt("file_version");
				//If versions do not match
				if (currentVersion != imbVersion) {
					try {
						ConfigurationSection configSection = imbConfig.getConfigurationSection("");
						int keysAdded = 0;
						if (configSection != null) {
							for (String key : configSection.getKeys(true)) {
								if (!configSection.isConfigurationSection(key)) {
									if (!config.contains(key)) {
										config.set(key, imbConfig.get(key));
										keysAdded++;
									}
								}
							}
							// Messages to override
							for (MessageUpdates update : MessageUpdates.values()) {
								if (currentVersion < update.getVersion() && imbVersion >= update.getVersion()) {
									ConfigurationSection section = imbConfig.getConfigurationSection(update.getPath());
									if (section != null) {
										for (String key : section.getKeys(false)) {
											config.set(section.getCurrentPath() + "." + key, section.getString(key));
										}
										if (update.getMessage() != null) {
											Bukkit.getLogger().warning("[AureliumSkills] messages_" + language + ".yml was changed: " + update.getMessage());
										}
									} else {
										Object value = imbConfig.get(update.getPath());
										if (value != null) {
											config.set(update.getPath(), value);
											if (update.getMessage() != null) {
												Bukkit.getLogger().warning("[AureliumSkills] messages_" + language + ".yml was changed: " + update.getMessage());
											}
										}
									}
								}
							}
						}
						config.set("file_version", imbVersion);
						config.save(file);
						Bukkit.getLogger().info("[AureliumSkills] messages_" + language + ".yml was updated to a new file version, " + keysAdded + " new keys were added.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return YamlConfiguration.loadConfiguration(file);
	}

	public static String getMessage(MessageKey key, Locale locale) {
        Objects.requireNonNull(key);
		Map<MessageKey, String> localeMap;
		// Set default locale if locale not present
		if (!messages.containsKey(locale)) {
			locale = getDefaultLanguage();
		}
		// Try the requested locale
		localeMap = messages.get(locale);
		// Try falling back to the default configured language
		if (localeMap == null) {
			localeMap = Lang.messages.get(getDefaultLanguage());
		}
		// Try falling back to English
		if (localeMap == null) {
			localeMap = Lang.messages.get(Locale.ENGLISH);
		}
		// No valid locale
		if (localeMap == null) {
			throw new IllegalStateException("Failed to acquire a locale translation map");
		}
		String message = localeMap.get(key);
		if (message == null)
			throw new IllegalStateException("Missing translation for index key: " + key + " with locale: " + locale);
		return message;
	}

	public static boolean hasLocale(Locale locale) {
		return messages.containsKey(locale);
	}

	public static Map<Locale, String> getDefinedLanguages() {
		return definedLanguages;
	}

	public static Set<String> getDefinedLanguagesSet() {
		Set<String> languages = new HashSet<>();
		for (Map.Entry<Locale, String> entry : definedLanguages.entrySet()) {
			languages.add(entry.getValue());
		}
		return languages;
	}

	private static void setDefaultLanguage(Locale language) {
        Objects.requireNonNull(language);
		defaultLanguage = language;
	}

	public static Locale getDefaultLanguage() {
		return defaultLanguage;
	}

	public Locale getLocale(Player player) {
        Objects.requireNonNull(player);
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
		if (playerData != null) {
			return playerData.getLocale();
		} else {
			return getDefaultLanguage();
		}
	}

	public Locale getLocale(CommandSender sender) {
		if (sender instanceof Player) {
			PlayerData playerData = plugin.getPlayerManager().getPlayerData((Player) sender);
			if (playerData != null) {
				return playerData.getLocale();
			}
		}
		return getDefaultLanguage();
	}

	@EventHandler
	public void onJoin(PlayerDataLoadEvent event) {
		Player player = event.getPlayerData().getPlayer();
		PlayerData playerData = event.getPlayerData();
		if (playerData.getLocale() == null) {
			if (OptionL.getBoolean(Option.TRY_DETECT_CLIENT_LANGUAGE)) {
				try {
					Locale locale = new Locale(player.getLocale().split("_")[0].toLowerCase(Locale.ENGLISH));
					if (messages.containsKey(locale)) {
						playerData.setLocale(locale);
					} else {
						playerData.setLocale(getDefaultLanguage());
					}
				} catch (Exception e) {
					playerData.setLocale(getDefaultLanguage());
				}
			}
			// Otherwise set to default
			else {
				playerData.setLocale(getDefaultLanguage());
			}
		}
	}

	private String applyColor(String message) {
        Objects.requireNonNull(message);
		Matcher matcher = hexPattern.matcher(message);
		StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
		while (matcher.find()) {
			String group = matcher.group(1);
			char COLOR_CHAR = ChatColor.COLOR_CHAR;
			matcher.appendReplacement(buffer, COLOR_CHAR + "x"
					+ COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
					+ COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
					+ COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
			);
		}
		message = matcher.appendTail(buffer).toString();
		return TextUtil.replace(message, "&", "§");
	}

}
