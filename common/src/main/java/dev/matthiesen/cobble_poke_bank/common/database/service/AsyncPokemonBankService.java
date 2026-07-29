package dev.matthiesen.cobble_poke_bank.common.database.service;

import com.google.gson.JsonObject;
import dev.matthiesen.cobble_poke_bank.common.database.repository.PokemonBankRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class AsyncPokemonBankService {
    private final PokemonBankService syncService;
    private final ExecutorService executor;

    public AsyncPokemonBankService(PokemonBankService syncService) {
        this.syncService = syncService;
        int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        this.executor = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
            private int counter = 0;

            @Override
            public synchronized Thread newThread(@NotNull Runnable runnable) {
                Thread thread = new Thread(runnable, "cobble-poke-bank-db-" + counter++);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public CompletableFuture<Map<Integer, PokemonBankRepository.PokemonBankEntry>> getUserBank(String userUUID) {
        return CompletableFuture.supplyAsync(() -> syncService.getUserBank(userUUID), executor);
    }

    public CompletableFuture<Integer> getUserBankSize(String userUUID) {
        return CompletableFuture.supplyAsync(() -> syncService.getUserBankSize(userUUID), executor);
    }

    public CompletableFuture<Boolean> insertOrUpdateBankEntry(String userUUID, String pokemonUUID, JsonObject pokemonJsonData) {
        return CompletableFuture.supplyAsync(
                () -> syncService.insertOrUpdateBankEntry(userUUID, pokemonUUID, pokemonJsonData),
                executor
        );
    }

    public CompletableFuture<Boolean> deleteBankEntry(String userUUID, String pokemonUUID) {
        return CompletableFuture.supplyAsync(() -> syncService.deleteBankEntry(userUUID, pokemonUUID), executor);
    }
}
