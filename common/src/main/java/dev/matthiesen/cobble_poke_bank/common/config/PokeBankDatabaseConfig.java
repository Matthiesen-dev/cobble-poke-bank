package dev.matthiesen.cobble_poke_bank.common.config;

import com.google.gson.annotations.SerializedName;
import dev.matthiesen.matthiesen_core.common.api.database.config.DatabaseConfig;

public final class PokeBankDatabaseConfig {

    /**
     * If true, the mod will use a MySQL database instead of SQLite. This requires a MySQL server to be running and accessible.
     */
    @SerializedName("useMySQL")
    public boolean useMySQL = false;

    /**
     * Config options when using MySQL. These options will be ignored if useMySQL is false.
     */
    @SerializedName("mySQLConfig")
    public MySQLConfig mySQLConfig = new MySQLConfig();

    public static class MySQLConfig {
        @SerializedName("host")
        public String host = "localhost";

        @SerializedName("port")
        public int port = 3306;

        @SerializedName("database")
        public String database = "cobble_poke_bank";

        @SerializedName("username")
        public String username = "root";

        @SerializedName("password")
        public String password = "password";

        @SerializedName("timeout")
        public int timeout = 5000;
    }

    public static DatabaseConfig toDatabaseConfig(PokeBankDatabaseConfig config) {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.useMySQL = config.useMySQL;
        databaseConfig.mySQLConfig.host = config.mySQLConfig.host;
        databaseConfig.mySQLConfig.port = config.mySQLConfig.port;
        databaseConfig.mySQLConfig.database = config.mySQLConfig.database;
        databaseConfig.mySQLConfig.username = config.mySQLConfig.username;
        databaseConfig.mySQLConfig.password = config.mySQLConfig.password;
        databaseConfig.mySQLConfig.timeout = config.mySQLConfig.timeout;
        databaseConfig.sqLiteConfig.fileName = "cobble_poke_bank.db";
        return databaseConfig;
    }
}
