package dev.matthiesen.cobble_poke_bank.common.database.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.matthiesen_core.common.api.database.repository.IRepository;
import dev.matthiesen.matthiesen_core.common.core.database.CoreDatabase;
import dev.matthiesen.matthiesen_core.common.core.database.dialect.MySQLDialect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PokemonBankRepository implements IRepository {

    private final CoreDatabase database;

    public PokemonBankRepository(CoreDatabase database) {
        this.database = database;
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS pokemon_bank (" +
                "id " + database.getDialect().getDataType("integer") + " PRIMARY KEY " + (database.getDialect() instanceof MySQLDialect ? " AUTO_INCREMENT" : "") + "," +
                "user_uuid " + database.getDialect().getDataType("varchar") + "(36) NOT NULL," +
                "pokemon_uuid " + database.getDialect().getDataType("varchar") + "(36) NOT NULL UNIQUE," +
                "pokemon_json_data " + database.getDialect().getDataType("text") + " NOT NULL" +
                ")";
        if (database.getDialect() instanceof MySQLDialect) {
            sql += " ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;";
        } else {
            sql += ";";
        }
        database.createTable(sql);
    }

    public void createIndexes() {
        String sql;
        if (database.getDialect() instanceof MySQLDialect) {
            sql = "ALTER TABLE pokemon_bank ADD INDEX idx_user_uuid (user_uuid), ADD UNIQUE INDEX idx_pokemon_uuid (pokemon_uuid);";
        } else {
            sql = "CREATE INDEX IF NOT EXISTS idx_user_uuid ON pokemon_bank(user_uuid);";
        }
        database.execute(sql, false);
    }

    public boolean insertOrUpdateBankEntry(String user_uuid, String pokemon_uuid, JsonObject pokemon_json_data) {
        String query = "INSERT INTO pokemon_bank(user_uuid, pokemon_uuid, pokemon_json_data) VALUES (?, ?, ?) " +
                database.getDialect().getOnConflictDoNothing("pokemon_uuid");
        AtomicBoolean success = new AtomicBoolean(true);

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, user_uuid);
                preparedStatement.setString(2, pokemon_uuid);
                preparedStatement.setString(3, pokemon_json_data.toString());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                success.set(false);
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to insert or update bank entry in database", e);
            }
        });
        database.queue.execute();
        return success.get();
    }

    public boolean deleteBankEntry(String user_uuid, String pokemon_uuid) {
        String query = "DELETE FROM pokemon_bank WHERE user_uuid = ? AND pokemon_uuid = ?";
        AtomicBoolean success = new AtomicBoolean(true);

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, user_uuid);
                preparedStatement.setString(2, pokemon_uuid);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                success.set(false);
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to delete bank entry from database", e);
            }
        });
        database.queue.execute();
        return success.get();
    }

    public Map<Integer, PokemonBankEntry> getUserBank(String user_uuid) {
        Map<Integer, PokemonBankEntry> bankEntries = new HashMap<>();
        String query = "SELECT pokemon_uuid, pokemon_json_data FROM pokemon_bank WHERE user_uuid = ? ORDER BY id ASC";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, user_uuid);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                String pokemon_uuid = resultSet.getString(1);
                String pokemon_json_data = resultSet.getString(2);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(pokemon_json_data, JsonObject.class);
                bankEntries.put(bankEntries.size(), new PokemonBankEntry(UUID.fromString(pokemon_uuid), jsonObject));
            }

        } catch (SQLException exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to get user bank from database", exception);
        }
        return bankEntries;
    }

    public int getUserBankSize(String user_uuid) {
        String query = "SELECT COUNT(*) FROM pokemon_bank WHERE user_uuid = ?";
        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, user_uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to get user bank size from database", exception);
        }
        return 0;
    }

    public record PokemonBankEntry(UUID pokemon_uuid, JsonObject pokemon_json_data) {}
}
