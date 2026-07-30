package dev.matthiesen.cobble_poke_bank.common;

import dev.matthiesen.cobble_poke_bank.common.command.PokeBankCommand;
import dev.matthiesen.cobble_poke_bank.common.config.MessagesConfig;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankDatabaseConfig;
import dev.matthiesen.cobble_poke_bank.common.config.MainConfig;
import dev.matthiesen.cobble_poke_bank.common.database.service.DatabaseServices;
import dev.matthiesen.cobble_poke_bank.common.registry.PermissionRegistry;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.database.config.DatabaseConfig;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.core.database.CoreDatabase;
import dev.matthiesen.matthiesen_core.common.utility.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public final class CobblePokeBankCommon extends AbstractCommonMod {
    public static final String MOD_ID = "cobble_poke_bank";
    public static final String MOD_NAME = "Cobble Poke Bank";
    public static @Token final String METRICS_TOKEN = "e2cc0b9381f499678f05477461507d81";
    public static final CobblePokeBankCommon INSTANCE = new CobblePokeBankCommon();

    private static final ConfigManager<PokeBankDatabaseConfig> DATABASE_CONFIG_MANAGER =
            INSTANCE.createConfigManager(PokeBankDatabaseConfig.class, "database");
    private static final ConfigManager<MainConfig> CONFIG_MANAGER =
            INSTANCE.createConfigManager(MainConfig.class, "config");
    private static final ConfigManager<MessagesConfig> MESSAGES_CONFIG =
            INSTANCE.createConfigManager(MessagesConfig.class, "messages");

    private CoreDatabase database;
    private boolean databaseAvailable = false;

    public CobblePokeBankCommon() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    public void initialize() {
        super.initialize();
        loadConfigs(false);

        PermissionRegistry.init();
        getCommandsRegistryManager().registerCommand(PokeBankCommand.CMD);

        PlatformEvents.SERVER_STARTING.subscribe(event -> prepareDatabase());
        PlatformEvents.SERVER_RELOAD.subscribe(event -> loadConfigs(true));
        PlatformEvents.SERVER_STOPPING.subscribe(event -> shutdownDatabase());

        createInfoLog("Initialized");
    }

    public void loadConfigs(boolean reload) {
        DATABASE_CONFIG_MANAGER.loadConfig();
        CONFIG_MANAGER.loadConfig();
        MESSAGES_CONFIG.loadConfig();
        if (reload) {
            createInfoLog("Reloaded configs");
        } else {
            createInfoLog("Loaded configs");
        }
    }

    public void shutdownDatabase() {
        if (database != null) {
            database.close();
            databaseAvailable = false;
            createInfoLog("Database connection closed");
        } else {
            createInfoLog("No database connection to close");
        }
    }

    public CoreDatabase getDatabase() {
        return database;
    }

    public boolean isDatabaseAvailable() {
        return databaseAvailable;
    }

    public void prepareDatabase() {
        long start = System.currentTimeMillis();
        createInfoLog("Preparing database...");
        try {
            DatabaseConfig config = PokeBankDatabaseConfig.toDatabaseConfig(DATABASE_CONFIG_MANAGER.getConfig());
            database = new CoreDatabase(this, config);
            boolean connected = database.createConnection();
            if (!connected) {
                createErrorLog("Failed to connect to database");
                return;
            }
        } catch (Exception e) {
            createErrorLog("Failed to connect to database", e);
            return;
        }

        DatabaseServices.POKE_BANK.createTable();
        DatabaseServices.POKE_BANK.createIndexes();

        long end = System.currentTimeMillis();
        createInfoLog("Database prepared in " + (end - start) + "ms");
        databaseAvailable = true;
    }

    public MainConfig getConfig() {
        return CONFIG_MANAGER.getConfig();
    }

    public PokeBankDatabaseConfig getDatabaseConfig() {
        return DATABASE_CONFIG_MANAGER.getConfig();
    }

    public PermissionRegistry.Permissions getPermissions() {
        return PermissionRegistry.getPermissions();
    }

    public boolean checkPermission(CommandSourceStack source, Permission permission) {
        return PermissionRegistry.checkPermission(source, permission);
    }

    public MessagesConfig getMessagesConfig() {
        return MESSAGES_CONFIG.getConfig();
    }
}
