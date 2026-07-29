package dev.matthiesen.cobble_poke_bank.common;

import dev.matthiesen.cobble_poke_bank.common.command.PokeBankCommand;
import dev.matthiesen.cobble_poke_bank.common.config.DatabaseConfig;
import dev.matthiesen.cobble_poke_bank.common.config.MainConfig;
import dev.matthiesen.cobble_poke_bank.common.database.Database;
import dev.matthiesen.cobble_poke_bank.common.database.service.DatabaseServices;
import dev.matthiesen.cobble_poke_bank.common.registry.PermissionRegistry;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.utility.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public final class CobblePokeBankCommon extends AbstractCommonMod {
    public static final String MOD_ID = "cobble_poke_bank";
    public static final String MOD_NAME = "Cobble Poke Bank";
    public static @Token final String METRICS_TOKEN = "e2cc0b9381f499678f05477461507d81";
    public static final CobblePokeBankCommon INSTANCE = new CobblePokeBankCommon();

    public static final ConfigManager<DatabaseConfig> DATABASE_CONFIG_MANAGER =
            INSTANCE.createConfigManager(DatabaseConfig.class, "database");
    public static final ConfigManager<MainConfig> CONFIG_MANAGER =
            INSTANCE.createConfigManager(MainConfig.class, "config");

    private Database database;

    public CobblePokeBankCommon() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    public void initialize() {
        super.initialize();
        DATABASE_CONFIG_MANAGER.loadConfig();
        CONFIG_MANAGER.loadConfig();

        boolean databaseReady = prepareDatabase();
        if (!databaseReady) return;

        PermissionRegistry.init();
        getCommandsRegistryManager().registerCommand(PokeBankCommand.CMD);

        PlatformEvents.SERVER_RELOAD.subscribe(event -> {
            DATABASE_CONFIG_MANAGER.loadConfig();
            CONFIG_MANAGER.loadConfig();
            createInfoLog("Reloaded configs");
        });

        createInfoLog("Initialized");
    }

    public Database getDatabase() {
        return database;
    }

    public boolean prepareDatabase() {
        long start = System.currentTimeMillis();
        try {
            database = new Database(DATABASE_CONFIG_MANAGER.getConfig());
            boolean connected = database.createConnection();
            if (!connected) {
                createErrorLog("Failed to connect to database");
                return false;
            }
        } catch (Exception e) {
            createErrorLog("Failed to connect to database", e);
            return false;
        }

        DatabaseServices.POKE_BANK.createTable();
        DatabaseServices.POKE_BANK.createIndexes();

        long end = System.currentTimeMillis();
        createInfoLog("Database prepared in " + (end - start) + "ms");
        return true;
    }

    public MainConfig getConfig() {
        return CONFIG_MANAGER.getConfig();
    }

    public PermissionRegistry.Permissions getPermissions() {
        return PermissionRegistry.getPermissions();
    }

    public boolean checkPermission(CommandSourceStack source, Permission permission) {
        return PermissionRegistry.checkPermission(source, permission);
    }
}
