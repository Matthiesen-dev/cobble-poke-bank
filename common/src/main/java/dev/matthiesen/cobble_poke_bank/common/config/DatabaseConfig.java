package dev.matthiesen.cobble_poke_bank.common.config;

import com.google.gson.annotations.SerializedName;

public final class DatabaseConfig {

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
}
