package com.archyx.aureliumskills.data.storage;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.AbstractAbility;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.AbilityData;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.leaderboard.LeaderboardManager;
import com.archyx.aureliumskills.leaderboard.SkillValue;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.misc.KeyIntPair;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class MySqlStorageProvider extends StorageProvider {

    private Connection connection;
    private final String host, database, username, password;
    private final int port;
    private final boolean ssl;

    public MySqlStorageProvider(AureliumSkills plugin) {
        super(plugin);
        this.host = OptionL.getString(Option.MYSQL_HOST);
        this.database = OptionL.getString(Option.MYSQL_DATABASE);
        this.username = OptionL.getString(Option.MYSQL_USERNAME);
        this.password = OptionL.getString(Option.MYSQL_PASSWORD);
        this.port = OptionL.getInt(Option.MYSQL_PORT);
        this.ssl = OptionL.getBoolean(Option.MYSQL_SSL);
    }

    public void init() {
        try {
            openConnection();
            createTable();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to connect to MySQL database, see error below:");
            e.printStackTrace();
        }
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database + "?useSSL=" + ssl + "&autoReconnect=true", this.username, this.password);
            plugin.getLogger().info("Connected to MySQL database");
        }
    }

    @Override
    public void load(Player player) {
        try {
            String query = "SELECT * FROM SkillData WHERE ID=?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        PlayerData playerData = new PlayerData(player, plugin);
                        // Load skill data
                        for (Skill skill : Skills.values()) {
                            int level = result.getInt(skill.name().toUpperCase(Locale.ROOT) + "_LEVEL");
                            double xp = result.getDouble(skill.name().toUpperCase(Locale.ROOT) + "_XP");
                            playerData.setSkillLevel(skill, level);
                            playerData.setSkillXp(skill, xp);
                            // Add stat levels
                            plugin.getRewardManager().getRewardTable(skill).applyStats(playerData, level);
                        }
                        // Load stat modifiers
                        String statModifiers = result.getString("STAT_MODIFIERS");
                        if (statModifiers != null) {
                            JsonArray jsonModifiers = new Gson().fromJson(statModifiers, JsonArray.class);
                            assert (null != jsonModifiers);
                            for (JsonElement modifierElement : jsonModifiers.getAsJsonArray()) {
                                JsonObject modifierObject = modifierElement.getAsJsonObject();
                                String name = modifierObject.get("name").getAsString();
                                String statName = modifierObject.get("stat").getAsString();
                                double value = modifierObject.get("value").getAsDouble();
                                if (name != null && statName != null) {
                                    Stat stat = plugin.getStatRegistry().getStat(statName);
                                    if (stat != null) {
                                        StatModifier modifier = new StatModifier(name, stat, value);
                                        playerData.addStatModifier(modifier);
                                    }
                                }
                            }
                        }
                        playerData.setMana(result.getDouble("mana"));
                        // Load locale
                        String locale = result.getString("locale");
                        if (locale != null) {
                            playerData.setLocale(new Locale(locale));
                        }
                        // Load ability data
                        String abilityData = result.getString("ABILITY_DATA");
                        if (abilityData != null) {
                            JsonObject jsonAbilityData = new Gson().fromJson(abilityData, JsonObject.class);
                            assert (null != jsonAbilityData);
                            for (Map.Entry<String, JsonElement> abilityEntry : jsonAbilityData.entrySet()) {
                                String abilityName = abilityEntry.getKey();
                                AbstractAbility ability = AbstractAbility.valueOf(abilityName.toUpperCase(Locale.ROOT));
                                if (ability != null) {
                                    AbilityData data = playerData.getAbilityData(ability);
                                    JsonObject dataObject = abilityEntry.getValue().getAsJsonObject();
                                    for (Map.Entry<String, JsonElement> dataEntry : dataObject.entrySet()) {
                                        String key = Objects.requireNonNull(dataEntry.getKey());
                                        JsonElement element = dataEntry.getValue();
                                        if (element.isJsonPrimitive()) {
                                            JsonPrimitive primitive = Objects.requireNonNull(dataEntry.getValue().getAsJsonPrimitive());
                                            Object value = Objects.requireNonNull(parsePrimitive(primitive));
                                            data.setData(key, value);
                                        }
                                    }
                                }
                            }
                        }
                        // Load unclaimed items
                        String unclaimedItemsString = result.getString("UNCLAIMED_ITEMS");
                        if (unclaimedItemsString != null) {
                            List<KeyIntPair> unclaimedItems = new ArrayList<>();
                            String[] splitString = unclaimedItemsString.split(",");
                            for (String entry : splitString) {
                                String[] splitEntry = entry.split(" ");
                                String itemKey = splitEntry[0];
                                int amount = 1;
                                if (splitEntry.length >= 2) {
                                    amount = NumberUtil.toInt(splitEntry[1], 1);
                                }
                                unclaimedItems.add(new KeyIntPair(itemKey, amount));
                            }
                            playerData.setUnclaimedItems(unclaimedItems);
                            playerData.clearInvalidItems();
                        }
                        playerManager.addPlayerData(playerData);
                        plugin.getLeveler().updatePermissions(player);
                        PlayerDataLoadEvent event = new PlayerDataLoadEvent(playerData);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.getPluginManager().callEvent(event);
                            }
                        }.runTask(plugin);
                    } else {
                        createNewPlayer(player);
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("There was an error loading player data for player " + player.getName() + " with UUID " + player.getUniqueId() + ", see below for details.");
            e.printStackTrace();
            PlayerData playerData = createNewPlayer(player);
            playerData.setShouldSave(false);
            sendErrorMessageToPlayer(player, e);
        }
    }

    private void createTable() throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, "SkillData", null);
        if (!tables.next()) {
            try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
                statement.execute("CREATE TABLE SkillData (" +
                        "ID varchar(40), " +
                        "AGILITY_LEVEL int, AGILITY_XP double, " +
                        "ALCHEMY_LEVEL int, ALCHEMY_XP double, " +
                        "ARCHERY_LEVEL int, ARCHERY_XP double, " +
                        "DEFENSE_LEVEL int, DEFENSE_XP double, " +
                        "ENCHANTING_LEVEL int, ENCHANTING_XP double, " +
                        "ENDURANCE_LEVEL int, ENDURANCE_XP double, " +
                        "EXCAVATION_LEVEL int, EXCAVATION_XP double, " +
                        "FARMING_LEVEL int, FARMING_XP double, " +
                        "FIGHTING_LEVEL int, FIGHTING_XP double, " +
                        "FISHING_LEVEL int, FISHING_XP double, " +
                        "FORAGING_LEVEL int, FORAGING_XP double, " +
                        "FORGING_LEVEL int, FORGING_XP double, " +
                        "HEALING_LEVEL int, HEALING_XP double, " +
                        "MINING_LEVEL int, MINING_XP double, " +
                        "SORCERY_LEVEL int, SORCERY_XP double, " +
                        "LOCALE varchar(10), " +
                        "STAT_MODIFIERS varchar(4096), " +
                        "MANA double, " +
                        "ABILITY_DATA varchar(4096), " +
                        "UNCLAIMED_ITEMS varchar(4096), " +
                        "CONSTRAINT PKEY PRIMARY KEY (ID))");
            }
        }
    }

    @Override
    public void save(Player player, boolean removeFromMemory) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData == null) return;
        if (playerData.shouldNotSave()) return;
        try {
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO SkillData (ID, ");
            for (Skill skill : Skills.getOrderedValues()) {
                sqlBuilder.append(skill.toString()).append("_LEVEL, ");
                sqlBuilder.append(skill).append("_XP, ");
            }
            sqlBuilder.append("LOCALE, STAT_MODIFIERS, MANA, ABILITY_DATA, UNCLAIMED_ITEMS) VALUES(?, ");
            for (int i = 0; i < Skills.getOrderedValues().size(); i++) {
                sqlBuilder.append("?, ?, ");
            }
            sqlBuilder.append("?, ?, ?, ?, ?) ");
            sqlBuilder.append("ON DUPLICATE KEY UPDATE ");
            for (Skill skill : Skills.getOrderedValues()) {
                sqlBuilder.append(skill.toString()).append("_LEVEL=?, ");
                sqlBuilder.append(skill).append("_XP=?, ");
            }
            sqlBuilder.append("LOCALE=?, ");
            sqlBuilder.append("STAT_MODIFIERS=?, ");
            sqlBuilder.append("MANA=?, ");
            sqlBuilder.append("ABILITY_DATA=?, ");
            sqlBuilder.append("UNCLAIMED_ITEMS=?");
            // Enter values into prepared statement
            try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
                statement.setString(1, player.getUniqueId().toString());
                int index = 2;
                for (int i = 0; i < 2; i++) {
                    for (Skill skill : Skills.getOrderedValues()) {
                        statement.setInt(index++, playerData.getSkillLevel(skill));
                        statement.setDouble(index++, playerData.getSkillXp(skill));
                    }
                    Locale locale = playerData.getLocale();
                    assert (null != locale);
                    statement.setString(index++, locale.toString());
                    // Build stat modifiers json
                    StringBuilder modifiersJson = new StringBuilder();
                    if (playerData.getStatModifiers().size() > 0) {
                        modifiersJson.append("[");
                        for (StatModifier statModifier : playerData.getStatModifiers().values()) {
                            modifiersJson.append("{\"name\":\"").append(statModifier.getName())
                                    .append("\",\"stat\":\"").append(statModifier.getStat().toString().toLowerCase(Locale.ROOT))
                                    .append("\",\"value\":").append(statModifier.getValue()).append("},");
                        }
                        modifiersJson.deleteCharAt(modifiersJson.length() - 1);
                        modifiersJson.append("]");
                    }
                    // Add stat modifiers to prepared statement
                    if (!modifiersJson.toString().equals("")) {
                        statement.setString(index++, modifiersJson.toString());
                    } else {
                        statement.setNull(index++, Types.VARCHAR);
                    }
                    statement.setDouble(index++, playerData.getMana()); // Set mana
                    // Build ability json
                    StringBuilder abilityJson = new StringBuilder();
                    if (playerData.getAbilityDataMap().size() > 0) {
                        abilityJson.append("{");
                        for (AbilityData abilityData : playerData.getAbilityDataMap().values()) {
                            String abilityName = abilityData.getAbility().toString().toLowerCase(Locale.ROOT);
                            if (abilityData.getDataMap().size() > 0) {
                                abilityJson.append("\"").append(abilityName).append("\"").append(":{");
                                for (Map.Entry<String, Object> dataEntry : abilityData.getDataMap().entrySet()) {
                                    String value = String.valueOf(dataEntry.getValue());
                                    if (dataEntry.getValue() instanceof String) {
                                        value = "\"" + dataEntry.getValue() + "\"";
                                    }
                                    abilityJson.append("\"").append(dataEntry.getKey()).append("\":").append(value).append(",");
                                }
                                abilityJson.deleteCharAt(abilityJson.length() - 1);
                                abilityJson.append("},");
                            }
                        }
                        if (abilityJson.length() > 1) {
                            abilityJson.deleteCharAt(abilityJson.length() - 1);
                        }
                        abilityJson.append("}");
                    }
                    // Add ability data to prepared statement
                    if (!abilityJson.toString().equals("")) {
                        statement.setString(index++, abilityJson.toString());
                    } else {
                        statement.setNull(index++, Types.VARCHAR);
                    }
                    // Unclaimed items
                    StringBuilder unclaimedItemsStringBuilder = new StringBuilder();
                    List<KeyIntPair> unclaimedItems = playerData.getUnclaimedItems();
                    for (KeyIntPair unclaimedItem : unclaimedItems) {
                        unclaimedItemsStringBuilder.append(unclaimedItem.getKey()).append(" ").append(unclaimedItem.getValue()).append(",");
                    }
                    if (unclaimedItemsStringBuilder.length() > 0) {
                        unclaimedItemsStringBuilder.deleteCharAt(unclaimedItemsStringBuilder.length() - 1);
                    }
                    if (!unclaimedItemsStringBuilder.toString().equals("")) {
                        statement.setString(index++, unclaimedItemsStringBuilder.toString());
                    } else {
                        statement.setNull(index++, Types.VARCHAR);
                    }
                }
                statement.executeUpdate();
            }
            if (removeFromMemory) {
                playerManager.removePlayerData(player.getUniqueId());
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("There was an error saving player data for player " + player.getName() + " with UUID " + player.getUniqueId() + ", see below for details.");
            e.printStackTrace();
        }
    }

    @Override
    public void loadBackup(FileConfiguration config, CommandSender sender) {
        ConfigurationSection playerDataSection = config.getConfigurationSection("player_data");
        Locale locale = plugin.getLang().getLocale(sender);
        if (playerDataSection != null) {
            try {
                for (String stringId : playerDataSection.getKeys(false)) {
                    UUID id = UUID.fromString(stringId);
                    // Load levels and xp from backup
                    Map<Skill, Integer> levels = getLevelsFromBackup(playerDataSection, stringId);
                    Map<Skill, Double> xpLevels = getXpLevelsFromBackup(playerDataSection, stringId);
                    PlayerData playerData = playerManager.getPlayerData(id);
                    if (playerData != null) {
                        applyData(playerData, levels, xpLevels);
                    } else {
                        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO SkillData (ID, ");
                        for (Skill skill : Skills.getOrderedValues()) {
                            sqlBuilder.append(skill.toString()).append("_LEVEL, ");
                            sqlBuilder.append(skill).append("_XP, ");
                        }
                        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
                        sqlBuilder.append(") VALUES(?, ");
                        for (int i = 0; i < Skills.getOrderedValues().size(); i++) {
                            sqlBuilder.append("?, ?, ");
                        }
                        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
                        sqlBuilder.append(") ");
                        sqlBuilder.append("ON DUPLICATE KEY UPDATE ");
                        for (Skill skill : Skills.getOrderedValues()) {
                            sqlBuilder.append(skill.toString()).append("_LEVEL=?, ");
                            sqlBuilder.append(skill).append("_XP=?, ");
                        }
                        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());
                        // Add values to prepared statement
                        try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
                            statement.setString(1, id.toString());
                            int index = 2;
                            for (int i = 0; i < 2; i++) {
                                for (Skill skill : Skills.getOrderedValues()) {
                                    Integer level = levels.get(skill);
                                    if (level == null)
                                        throw new IllegalStateException("Invalid level for skill index key: " + skill.name());
                                    statement.setInt(index++, level);
                                    Double xpLevel = xpLevels.get(skill);
                                    if (xpLevel == null)
                                        throw new IllegalStateException("Invalid experience level for skill index key: " + skill.name());
                                    statement.setDouble(index++, xpLevel);
                                }
                            }
                            statement.executeUpdate();
                            sender.sendMessage(AureliumSkills.getPrefix(locale) + Lang.getMessage(CommandMessage.BACKUP_LOAD_LOADED, locale));
                        }
                    }
                }
            } catch (Exception e) {
                String message = Lang.getMessage(CommandMessage.BACKUP_LOAD_ERROR, locale);
                String m = e.getMessage();
                if (m != null)
                    message = TextUtil.replace(m, "{error}", m);
                sender.sendMessage(AureliumSkills.getPrefix(locale) + message);
            }
        }
    }

    private Object parsePrimitive(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        } else if (primitive.isString()) {
            return primitive.getAsString();
        } else if (primitive.isNumber()) {
            if (primitive.getAsDouble() % 1 != 0) {
                return primitive.getAsDouble();
            } else {
                return primitive.getAsInt();
            }
        }
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean localeColumnExists() {
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            try (ResultSet columns = dbm.getColumns(null, null, "SkillData", "LOCALE")) {
                if (columns.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateLeaderboards() {
        LeaderboardManager manager = plugin.getLeaderboardManager();
        manager.setSorting(true);
        // Initialize lists
        Map<Skill, List<SkillValue>> leaderboards = new HashMap<>();
        for (Skill skill : Skills.values()) {
            leaderboards.put(skill, new ArrayList<>());
        }
        List<SkillValue> powerLeaderboard = new ArrayList<>();
        List<SkillValue> averageLeaderboard = new ArrayList<>();
        // Add players already in memory
        Set<UUID> loadedFromMemory = addLoadedPlayersToLeaderboards(leaderboards, powerLeaderboard, averageLeaderboard);

        try {
            try (Statement statement = connection.createStatement()) {
                String query = "SELECT * FROM SkillData;";
                try (ResultSet result = statement.executeQuery(query)) {
                    while (result.next()) {
                        try {
                            UUID id = UUID.fromString(result.getString("ID"));
                            if (!loadedFromMemory.contains(id)) {
                                int powerLevel = 0;
                                double powerXp = 0;
                                int numEnabled = 0;

                                for (Skill skill : Skills.values()) {
                                    // Load from database
                                    int level = result.getInt(skill.toString().toUpperCase(Locale.ROOT) + "_LEVEL");
                                    if (level == 0) {
                                        level = 1;
                                    }
                                    double xp = result.getDouble(skill.toString().toUpperCase(Locale.ROOT) + "_XP");
                                    // Add to lists
                                    SkillValue skillLevel = new SkillValue(id, level, xp);
                                    leaderboards.get(skill).add(skillLevel);

                                    if (OptionL.isEnabled(skill)) {
                                        powerLevel += level;
                                        powerXp += xp;
                                        numEnabled++;
                                    }
                                }
                                // Add power and average
                                SkillValue powerValue = new SkillValue(id, powerLevel, powerXp);
                                powerLeaderboard.add(powerValue);
                                double averageLevel = (double) powerLevel / numEnabled;
                                SkillValue averageValue = new SkillValue(id, 0, averageLevel);
                                averageLeaderboard.add(averageValue);
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("[AureliumSkills] Error reading player with uuid " + result.getString("ID") + " from the database!");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error while updating leaderboards:");
            e.printStackTrace();
        }
        sortLeaderboards(leaderboards, powerLeaderboard, averageLeaderboard);
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        String query = "DELETE FROM SkillData WHERE ID=?;";
        try {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IOException("Failed to delete player data from database");
        }
    }


}
