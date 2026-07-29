package dev.matthiesen.cobble_poke_bank.common.database;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.config.DatabaseConfig;
import dev.matthiesen.cobble_poke_bank.common.database.dialect.IDatabaseDialect;
import dev.matthiesen.cobble_poke_bank.common.database.dialect.MySQLDialect;
import dev.matthiesen.cobble_poke_bank.common.database.dialect.SQLiteDialect;
import dev.matthiesen.cobble_poke_bank.common.database.queue.IQueue;
import dev.matthiesen.cobble_poke_bank.common.database.queue.Queue;
import dev.matthiesen.cobble_poke_bank.common.database.queue.SqlTask;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public final class Database {

    @Nullable
    private Connection connection;
    public final IQueue queue;
    public final IQueue batchQueue;
    private IDatabaseDialect dialect;
    private final Object lock = new Object();
    private final DatabaseConfig config;

    public Database(DatabaseConfig config) {
        this.config = config;
        queue = new Queue(this, false);
        batchQueue = new Queue(this, true);
    }

    public boolean createConnection() {
        boolean connected;
        if (config.useMySQL) {
            connected = createMySqlConnection();
            dialect = new MySQLDialect();
        } else {
            connected = createSqliteConnection();
            dialect = new SQLiteDialect();
        }
        if (connection != null) {
            CobblePokeBankCommon.INSTANCE.createInfoLog("Database connection established");
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to set auto commit to false", e);
                return false;
            }
        }
        return connected && connection != null;
    }

    public boolean createMySqlConnection() {
        String host = config.mySQLConfig.host;
        int port = config.mySQLConfig.port;
        String database = config.mySQLConfig.database;
        String user = config.mySQLConfig.username;
        String password = config.mySQLConfig.password;
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?allowReconnect=true&autoReconnect=true&connectTimeout=" + config.mySQLConfig.timeout;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("MySQL JDBC Driver not found", e);
            return false;
        }
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to create MySQL connection", e);
            return false;
        }
        return connection != null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean createSqliteConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("SQLite JDBC Driver not found", e);
            return false;
        }

        Path gameDirectory = CobblePokeBankCommon.INSTANCE.getCommonUtils().getGameDirectory();
        Path configDirectory = gameDirectory.resolve("config/" + CobblePokeBankCommon.MOD_ID);
        if (!configDirectory.toFile().exists()) {
            configDirectory.toFile().mkdirs();
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + configDirectory.resolve("cobble_poke_bank.db"));
        } catch (SQLException e) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to create SQLite connection", e);
            return false;
        }
        return connection != null;
    }

    public void createTable(String sql) {
        execute(sql, true);
    }

    public void execute(String sql, boolean logError) {
        if (connection == null) return;

        synchronized (lock) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                connection.commit();
            } catch (SQLException e) {
                if (logError) {
                    CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to execute statement", e);
                }
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to rollback", ex);
                }
            }
        }
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        if (connection != null) {
            return connection.prepareStatement(query);
        } else {
            throw new SQLException("Connection is null");
        }
    }

    public void executeQueue(List<Object> items, boolean isBatch) {
        if (connection == null) return;

        synchronized (lock) {
            try {
                for (Object item : items) {
                    if (item instanceof PreparedStatement preparedStatement) {
                        if (preparedStatement.isClosed()) {
                            continue;
                        }
                        try (preparedStatement) {
                            if (isBatch) {
                                preparedStatement.executeBatch();
                            } else {
                                preparedStatement.executeUpdate();
                            }
                        }
                    } else if (item instanceof SqlTask task) {
                        task.execute(connection);
                    }
                }
                if (!items.isEmpty()) {
                    if (connection != null && !connection.isClosed()) {
                        connection.commit();
                    }
                }
            } catch (SQLException e) {
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to execute database queue", e);
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.rollback();
                    }
                } catch (SQLException ex) {
                    CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to rollback transaction", ex);
                }
            }
        }
    }

    public IDatabaseDialect getDialect() {
        return dialect;
    }
}
