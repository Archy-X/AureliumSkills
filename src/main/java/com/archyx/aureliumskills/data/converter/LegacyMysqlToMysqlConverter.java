package com.archyx.aureliumskills.data.converter;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.storage.MySqlStorageProvider;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class LegacyMysqlToMysqlConverter extends DataConverter {

    private final MySqlStorageProvider storageProvider;

    public LegacyMysqlToMysqlConverter(@NotNull AureliumSkills plugin, MySqlStorageProvider storageProvider) {
        super(plugin);
        this.storageProvider = storageProvider;
    }

    @Override
    public void convert() {
        Connection connection = storageProvider.getConnection();
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "SkillData", null);
            if (tables.next()) {
                ResultSet localeColumn = dbm.getColumns(null, null, "SkillData", "LOCALE");
                if (!localeColumn.next()) {
                    try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
                        statement.execute("ALTER TABLE SkillData ADD COLUMN LOCALE varchar(10), " +
                                "ADD COLUMN STAT_MODIFIERS varchar(4096), " +
                                "ADD COLUMN MANA double, " +
                                "ADD COLUMN ABILITY_DATA varchar(4096), " +
                                "DROP COLUMN NAME;");
                        Bukkit.getLogger().info("[AureliumSkills] Successfully converted old MySQL format to new format");
                    }
                }
                ResultSet unclaimedItemsColumn = dbm.getColumns(null, null, "SkillData", "UNCLAIMED_ITEMS");
                if (!unclaimedItemsColumn.next()) {
                    try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
                        statement.execute("ALTER TABLE SkillData ADD COLUMN UNCLAIMED_ITEMS varchar(4096);");
                        plugin.getLogger().info("Successfully added UNCLAIMED_ITEMS column to database");
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[AureliumSkills] Error converting legacy MySQL table to new format, see error below for details:");
            e.printStackTrace();
        }
    }
}
