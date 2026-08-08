package dev.matthiesen.cobble_poke_bank.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DatabaseStartupConfig {

    public ModConfigSpec.BooleanValue useMySQL;

    public ModConfigSpec.ConfigValue<String> mysqlHost;
    public ModConfigSpec.IntValue mysqlPort;
    public ModConfigSpec.ConfigValue<String> mysqlDatabase;
    public ModConfigSpec.ConfigValue<String> mysqlUsername;
    public ModConfigSpec.ConfigValue<String> mysqlPassword;
    public ModConfigSpec.IntValue mysqlTimeout;

    public DatabaseStartupConfig(ModConfigSpec.Builder builder) {
        builder.comment("Database Configuration")
                .translation("cobble_poke_bank.configuration.database")
                .push("database");

        useMySQL = builder.comment(
                        "If true, the mod will use a MySQL database for storage.",
                        "If false, the mod will use a local SQLite database for storage.",
                        "Default: false"
                )
                .translation("cobble_poke_bank.configuration.database.useMySQL")
                .define("useMySQL", false);

        builder.comment("MySQL Configuration")
                .translation("cobble_poke_bank.configuration.database.mysql")
                .push("mysql");
        mysqlHost = builder.comment(
                        "The hostname or IP address of the MySQL server.",
                        "Default: localhost"
                )
                .translation("cobble_poke_bank.configuration.database.mysql.host")
                .define("host", "localhost");
        mysqlPort = builder.comment(
                        "The port number of the MySQL server.",
                        "Default: 3306"
                )
                .translation("cobble_poke_bank.configuration.database.mysql.port")
                .defineInRange("port", 3306, 1, 65535);
        mysqlDatabase = builder.comment(
                        "The name of the MySQL database to use.",
                        "Default: cobble_poke_bank"
                )
                .translation("cobble_poke_bank.configuration.database.mysql.database")
                .define("database", "cobble_poke_bank");
        mysqlUsername = builder.comment(
                        "The username to use when connecting to the MySQL server.",
                        "Default: root"
                )
                .translation("cobble_poke_bank.configuration.database.mysql.username")
                .define("username", "root");
        mysqlPassword = builder.comment(
                        "The password to use when connecting to the MySQL server.",
                        "Default: password"
                )
                .translation("cobble_poke_bank.configuration.database.mysql.password")
                .define("password", "password");
        mysqlTimeout = builder.comment(
                        "The timeout in milliseconds for connecting to the MySQL server.",
                        "Default: 5000"
                )
                .translation("cobble_poke_bank.configuration.database.mysql.timeout")
                .defineInRange("timeout", 5000, 1, Integer.MAX_VALUE);

        builder.pop(); // Closes "database.mysql"
        builder.pop(); // Closes "database"
    }
}
