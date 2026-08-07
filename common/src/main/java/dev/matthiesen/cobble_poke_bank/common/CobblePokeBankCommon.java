package dev.matthiesen.cobble_poke_bank.common;

import dev.matthiesen.cobble_poke_bank.common.command.PokeBankCommand;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.cobble_poke_bank.common.database.service.DatabaseServices;
import dev.matthiesen.cobble_poke_bank.common.registry.PermissionRegistry;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.database.config.DatabaseConfig;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.api.events.server.ServerEvent;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.api.platform.loader.ModConfigType;
import dev.matthiesen.matthiesen_core.common.core.database.CoreDatabase;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public final class CobblePokeBankCommon extends AbstractCommonMod {
    public static final String MOD_ID = "cobble_poke_bank";
    public static final String MOD_NAME = "Cobble Poke Bank";
    public static @Token final String METRICS_TOKEN = "e2cc0b9381f499678f05477461507d81";
    public static final CobblePokeBankCommon INSTANCE = new CobblePokeBankCommon();

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
        registerModConfig(MOD_ID, ModConfigType.STARTUP, PokeBankConfig.PERMISSIONS_START_SPEC, "cobble_poke_bank/permissions.toml");
        registerModConfig(MOD_ID, ModConfigType.STARTUP, PokeBankConfig.DATABASE_SPEC, "cobble_poke_bank/database.toml");
        registerModConfig(MOD_ID, ModConfigType.SERVER, PokeBankConfig.SERVER_SPEC, "cobble_poke_bank/server.toml");

        PermissionRegistry.init();
        getCommandsRegistryManager().registerCommand(PokeBankCommand.CMD);

        PlatformEvents.SERVER_STARTING.subscribe(this::startupDatabase);
        PlatformEvents.SERVER_RELOAD.subscribe(this::reloadSystem);
        PlatformEvents.SERVER_STOPPING.subscribe(this::shutdownDatabase);

        createInfoLog("Initialized");
    }

    public void startupDatabase(ServerEvent.Starting event) {
        prepareDatabase(true);
    }

    public void reloadSystem(ServerEvent.Reload event) {
        var dbConfig = PokeBankConfig.getDatabaseConfig();

        createInfoLog("Reloading database connection...");

        if (dbConfig.useMySQL) {
            try {
                prepareDatabase(false);
                if (databaseAvailable) {
                    createInfoLog("Database connection reloaded successfully");
                } else {
                    createErrorLog("Failed to reload database connection, please check your database configuration");
                }
            } catch (Exception e) {
                createErrorLog("Failed to reload database connection", e);
            }
        } else {
            createInfoLog("Database connection reload skipped, using SQLite");
        }
    }

    public void shutdownDatabase(ServerEvent.Stopping event) {
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

    public void prepareDatabase(boolean initial) {
        long start = System.currentTimeMillis();
        createInfoLog("Preparing database...");
        try {
            DatabaseConfig config = PokeBankConfig.getDatabaseConfig();
            database = new CoreDatabase(this, config);
            boolean connected = initial ? database.createConnection() : database.reConnect(config);
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

    public PermissionRegistry.Permissions getPermissions() {
        return PermissionRegistry.getPermissions();
    }

    public boolean checkPermission(CommandSourceStack source, Permission permission) {
        return PermissionRegistry.checkPermission(source, permission);
    }
}
