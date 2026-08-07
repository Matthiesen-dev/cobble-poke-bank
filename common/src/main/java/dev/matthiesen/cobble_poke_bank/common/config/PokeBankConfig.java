package dev.matthiesen.cobble_poke_bank.common.config;

import dev.matthiesen.matthiesen_core.common.api.database.config.DatabaseConfig;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public final class PokeBankConfig {
    public static final ServerConfig SERVER_CONFIG;
    public static final ModConfigSpec SERVER_SPEC;

    public static final DatabaseStartupConfig DATABASE_CONFIG;
    public static final ModConfigSpec DATABASE_SPEC;

    public static final PermissionsStartupConfig PERMISSIONS_START_CONFIG;
    public static final ModConfigSpec PERMISSIONS_START_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER_CONFIG = specPair.getLeft();

        Pair<DatabaseStartupConfig, ModConfigSpec> databaseSpecPair = new ModConfigSpec.Builder().configure(DatabaseStartupConfig::new);
        DATABASE_SPEC = databaseSpecPair.getRight();
        DATABASE_CONFIG = databaseSpecPair.getLeft();

        Pair<PermissionsStartupConfig, ModConfigSpec> permissionsSpecPair = new ModConfigSpec.Builder().configure(PermissionsStartupConfig::new);
        PERMISSIONS_START_SPEC = permissionsSpecPair.getRight();
        PERMISSIONS_START_CONFIG = permissionsSpecPair.getLeft();
    }

    private static final List<Item> heldItemBlacklistCache = new ArrayList<>();

    public static List<Item> getHeldItemBlacklist() {
        if  (heldItemBlacklistCache.isEmpty()) {
            for (String itemString : SERVER_CONFIG.heldItemBlacklist.get()) {
                Item item = ItemDecoder.stringToItem(itemString, Items.BARRIER);
                if (item != null) {
                    heldItemBlacklistCache.add(item);
                }
            }
        }
        return heldItemBlacklistCache;
    }

    public static DatabaseConfig getDatabaseConfig() {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.useMySQL = DATABASE_CONFIG.useMySQL.getAsBoolean();
        databaseConfig.mySQLConfig.host = DATABASE_CONFIG.mysqlHost.get();
        databaseConfig.mySQLConfig.port = DATABASE_CONFIG.mysqlPort.getAsInt();
        databaseConfig.mySQLConfig.database = DATABASE_CONFIG.mysqlDatabase.get();
        databaseConfig.mySQLConfig.username = DATABASE_CONFIG.mysqlUsername.get();
        databaseConfig.mySQLConfig.password = DATABASE_CONFIG.mysqlPassword.get();
        databaseConfig.mySQLConfig.timeout = DATABASE_CONFIG.mysqlTimeout.getAsInt();
        databaseConfig.sqLiteConfig.fileName = "cobble_poke_bank.db";
        return databaseConfig;
    }
}
