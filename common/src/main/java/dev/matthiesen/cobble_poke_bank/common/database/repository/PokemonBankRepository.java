package dev.matthiesen.cobble_poke_bank.common.database.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.database.Database;
import dev.matthiesen.cobble_poke_bank.common.database.dialect.MySQLDialect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PokemonBankRepository implements IRepository {

    private final Database database;

    public PokemonBankRepository(Database database) {
        this.database = database;
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS pokemon_bank (" +
                "id " + database.getDialect().getDataType("integer") + " PRIMARY KEY " + (database.getDialect() instanceof MySQLDialect ? " AUTO_INCREMENT" : "") + "," +
                "user_uuid" + database.getDialect().getDataType("varchar") + "(36) NOT NULL," +
                "pokemon_uuid" + database.getDialect().getDataType("varchar") + "(36) NOT NULL," +
                "pokemon_json_data" + database.getDialect().getDataType("text") + " NOT NULL" +
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
            sql = "ALTER TABLE pokemon_bank ADD INDEX idx_user_uuid (user_uuid);";
        } else {
            sql = "CREATE INDEX IF NOT EXISTS idx_user_uuid ON pokemon_bank(user_uuid);";
        }
        database.execute(sql, false);
    }

    public void insertOrUpdateBankEntry(String user_uuid, String pokemon_uuid, JsonObject pokemon_json_data) {
        String query = "INSERT INTO pokemon_bank(user_uuid, pokemon_uuid, pokemon_json_data) VALUES (?, ?, ?) " +
                database.getDialect().getOnConflictDoNothing("pokemon_uuid");

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, user_uuid);
                preparedStatement.setString(2, pokemon_uuid);
                preparedStatement.setString(3, pokemon_json_data.toString());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to insert or update bank entry in database", e);
            }
        });
    }

    public void deleteBankEntry(String user_uuid, String pokemon_uuid) {
        String query = "DELETE FROM pokemon_bank WHERE user_uuid = ? AND pokemon_uuid = ?";

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, user_uuid);
                preparedStatement.setString(2, pokemon_uuid);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to delete bank entry from database", e);
            }
        });
    }

    public Map<Integer, PokemonBankEntry> getUserBank(String user_uuid) {
        Map<Integer, PokemonBankEntry> bankEntries = new HashMap<>();
        String query = "SELECT pokemon_uuid, pokemon_json_data FROM pokemon_bank WHERE user_uuid = ?";

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

    public record PokemonBankEntry(UUID pokemon_uuid, JsonObject pokemon_json_data) {}
}
