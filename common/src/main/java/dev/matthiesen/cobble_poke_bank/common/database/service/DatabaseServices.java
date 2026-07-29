package dev.matthiesen.cobble_poke_bank.common.database.service;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;

public interface DatabaseServices {
    PokemonBankService POKE_BANK = new PokemonBankService(CobblePokeBankCommon.INSTANCE.getDatabase());
    AsyncPokemonBankService ASYNC_POKE_BANK = new AsyncPokemonBankService(POKE_BANK);
}
