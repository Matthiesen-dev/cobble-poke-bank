package dev.matthiesen.cobble_poke_bank.common.database.service;

import com.google.gson.JsonObject;
import dev.matthiesen.cobble_poke_bank.common.database.repository.PokemonBankRepository;
import dev.matthiesen.matthiesen_core.common.core.database.CoreDatabase;

import java.util.Map;

public final class PokemonBankService {
    private final PokemonBankRepository repository;

    public PokemonBankService(CoreDatabase database) {
        this.repository = new PokemonBankRepository(database);
    }

    public void createTable() {
        repository.createTable();
    }

    public void createIndexes() {
        repository.createIndexes();
    }

    public boolean insertOrUpdateBankEntry(String user_uuid, String pokemon_uuid, JsonObject pokemon_json_data) {
        return repository.insertOrUpdateBankEntry(user_uuid, pokemon_uuid, pokemon_json_data);
    }

    public boolean deleteBankEntry(String user_uuid, String pokemon_uuid) {
        return repository.deleteBankEntry(user_uuid, pokemon_uuid);
    }

    public Map<Integer, PokemonBankRepository.PokemonBankEntry> getUserBank(String user_uuid) {
        return repository.getUserBank(user_uuid);
    }

    public int getUserBankSize(String user_uuid) {
        return repository.getUserBankSize(user_uuid);
    }
}
