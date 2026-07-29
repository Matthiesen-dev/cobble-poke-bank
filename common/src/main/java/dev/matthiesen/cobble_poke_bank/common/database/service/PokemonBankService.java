package dev.matthiesen.cobble_poke_bank.common.database.service;

import com.google.gson.JsonObject;
import dev.matthiesen.cobble_poke_bank.common.database.Database;
import dev.matthiesen.cobble_poke_bank.common.database.repository.PokemonBankRepository;

import java.util.Map;

public final class PokemonBankService {
    private final PokemonBankRepository repository;

    public PokemonBankService(Database database) {
        this.repository = new PokemonBankRepository(database);
    }

    public void createTable() {
        repository.createTable();
    }

    public void createIndexes() {
        repository.createIndexes();
    }

    public void insertOrUpdateBankEntry(String user_uuid, String pokemon_uuid, JsonObject pokemon_json_data) {
        repository.insertOrUpdateBankEntry(user_uuid, pokemon_uuid, pokemon_json_data);
    }

    public void deleteBankEntry(String user_uuid, String pokemon_uuid) {
        repository.deleteBankEntry(user_uuid, pokemon_uuid);
    }

    public Map<Integer, PokemonBankRepository.PokemonBankEntry> getUserBank(String user_uuid) {
        return repository.getUserBank(user_uuid);
    }
}
