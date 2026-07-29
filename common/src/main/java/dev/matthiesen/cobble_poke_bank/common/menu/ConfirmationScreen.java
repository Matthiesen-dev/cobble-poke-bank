package dev.matthiesen.cobble_poke_bank.common.menu;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.config.MainConfig;
import dev.matthiesen.cobble_poke_bank.common.database.repository.PokemonBankRepository;
import dev.matthiesen.cobble_poke_bank.common.database.service.DatabaseServices;
import dev.matthiesen.cobble_poke_bank.common.utility.MenuUtilities;
import dev.matthiesen.cobble_poke_bank.common.utility.ModTags;
import dev.matthiesen.cobble_poke_bank.common.utility.PokemonUtility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ConfirmationScreen {
    private final ServerPlayer player;
    private final TransferDirection direction;
    private final UUID pokemonUUID;
    private final PokemonBankRepository.PokemonBankEntry bankEntry;

    public enum TransferDirection {
        DEPOSIT,
        WITHDRAW
    }

    private ConfirmationScreen(
            ServerPlayer player,
            TransferDirection direction,
            UUID pokemonUUID,
            PokemonBankRepository.PokemonBankEntry bankEntry
    ) {
        this.player = player;
        this.direction = direction;
        this.pokemonUUID = pokemonUUID;
        this.bankEntry = bankEntry;
    }

    public static ConfirmationScreen deposit(ServerPlayer player, UUID pokemonUUID) {
        return new ConfirmationScreen(player, TransferDirection.DEPOSIT, pokemonUUID, null);
    }

    public static ConfirmationScreen withdraw(ServerPlayer player, PokemonBankRepository.PokemonBankEntry bankEntry) {
        return new ConfirmationScreen(player, TransferDirection.WITHDRAW, bankEntry.pokemon_uuid(), bankEntry);
    }

    public Page getPage() {
        Button frame = GooeyButton.builder()
                .display(MenuUtilities.getFrameItem())
                .build();

        Button confirm = GooeyButton.builder()
                .display(MenuUtilities.getConfirmItem())
                .onClick(action -> handleConfirm())
                .build();

        Button cancel = GooeyButton.builder()
                .display(MenuUtilities.getCancelItem())
                .onClick(action -> openBackPage())
                .build();

        Button info = GooeyButton.builder()
                .display(MenuUtilities.getInfoItem(direction == TransferDirection.DEPOSIT ?
                        "Move Pokemon to bank?" : "Move Pokemon to PC?"))
                .build();

        Button preview = GooeyButton.builder()
                .display(resolvePreviewItem())
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .fill(frame)
                .set(11, cancel)
                .set(13, preview)
                .set(15, confirm)
                .set(4, info)
                .build();

        return GooeyPage.builder()
                .title(Component.literal("Confirm Transfer"))
                .template(template)
                .build();
    }

    private void handleConfirm() {
        if (direction == TransferDirection.DEPOSIT) {
            depositAsync();
        } else {
            withdrawAsync();
        }
    }

    private void depositAsync() {
        Pokemon pokemon = findPokemonInPc();
        if (pokemon == null) {
            player.displayClientMessage(Component.literal("[CobblePokeBank] Pokemon is no longer in your PC."), false);
            return;
        }
        String heldItemValidationMessage = validateHeldItemForTransfer(pokemon);
        if (heldItemValidationMessage != null) {
            player.displayClientMessage(Component.literal(heldItemValidationMessage), false);
            return;
        }

        JsonObject jsonObject = PokemonUtility.pokemonToJson(pokemon, player.level().registryAccess());
        String userUUID = player.getUUID().toString();
        String pokemonUUIDString = pokemon.getUuid().toString();
        int maxSlots = CobblePokeBankCommon.INSTANCE.getConfig().bank.maxSlots;

        CompletableFuture<Boolean> saveFuture = DatabaseServices.ASYNC_POKE_BANK.getUserBankSize(userUUID)
                .thenCompose(currentBankSize -> {
                    if (maxSlots > 0 && currentBankSize >= maxSlots) {
                        return CompletableFuture.completedFuture(false);
                    }
                    return DatabaseServices.ASYNC_POKE_BANK.insertOrUpdateBankEntry(userUUID, pokemonUUIDString, jsonObject);
                });

        saveFuture.whenComplete((saved, throwable) -> runOnServerThread(() -> {
            if (throwable != null) {
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to store Pokemon in bank asynchronously", throwable);
                player.displayClientMessage(Component.literal("[CobblePokeBank] Failed to save Pokemon to bank."), false);
                return;
            }

            if (!saved) {
                if (maxSlots > 0) {
                    player.displayClientMessage(Component.literal("[CobblePokeBank] Your bank is full."), false);
                } else {
                    player.displayClientMessage(Component.literal("[CobblePokeBank] Failed to save Pokemon to bank."), false);
                }
                return;
            }

            Pokemon latestPokemon = findPokemonInPc();
            if (latestPokemon == null || !Cobblemon.INSTANCE.getStorage().getPC(player).remove(latestPokemon)) {
                DatabaseServices.ASYNC_POKE_BANK.deleteBankEntry(userUUID, pokemonUUIDString);
                player.displayClientMessage(Component.literal("[CobblePokeBank] Failed to remove Pokemon from PC."), false);
                return;
            }

            player.displayClientMessage(Component.literal("[CobblePokeBank] Pokemon deposited into your bank."), false);
            UIManager.openUIForcefully(player, new UserPCScreen(player).getPage());
        }));
    }

    private void withdrawAsync() {
        if (bankEntry == null) {
            player.displayClientMessage(Component.literal("[CobblePokeBank] Pokemon is no longer in your bank."), false);
            return;
        }

        Pokemon pokemon;
        try {
            pokemon = PokemonUtility.pokemonFromJson(bankEntry.pokemon_json_data(), player.level().registryAccess());
        } catch (Exception exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to deserialize bank Pokemon entry", exception);
            player.displayClientMessage(Component.literal("[CobblePokeBank] Failed to read Pokemon data."), false);
            return;
        }

        String heldItemValidationMessage = validateHeldItemForTransfer(pokemon);
        if (heldItemValidationMessage != null) {
            player.displayClientMessage(Component.literal(heldItemValidationMessage), false);
            return;
        }

        String userUUID = player.getUUID().toString();
        String pokemonUUIDString = bankEntry.pokemon_uuid().toString();

        DatabaseServices.ASYNC_POKE_BANK.deleteBankEntry(userUUID, pokemonUUIDString)
                .whenComplete((deleted, throwable) -> runOnServerThread(() -> {
                    if (throwable != null) {
                        CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to delete Pokemon from bank asynchronously", throwable);
                        player.displayClientMessage(Component.literal("[CobblePokeBank] Failed to remove Pokemon from bank."), false);
                        return;
                    }

                    if (!deleted) {
                        player.displayClientMessage(Component.literal("[CobblePokeBank] Pokemon is no longer in your bank."), false);
                        return;
                    }

                    if (!Cobblemon.INSTANCE.getStorage().getPC(player).add(pokemon)) {
                        DatabaseServices.ASYNC_POKE_BANK.insertOrUpdateBankEntry(
                                userUUID,
                                pokemonUUIDString,
                                bankEntry.pokemon_json_data()
                        );
                        player.displayClientMessage(Component.literal("[CobblePokeBank] Your PC is full."), false);
                        return;
                    }

                    player.displayClientMessage(Component.literal("[CobblePokeBank] Pokemon moved into your PC."), false);
                    BankMenuNavigator.openBankMenuAsync(player);
                }));
    }

    private ItemStack resolvePreviewItem() {
        if (direction == TransferDirection.DEPOSIT) {
            Pokemon pokemon = findPokemonInPc();
            return pokemon != null ? PokemonUtility.pokemonToItem(pokemon) : MenuUtilities.getInvalidEntryItem();
        }

        if (bankEntry == null) {
            return MenuUtilities.getInvalidEntryItem();
        }

        try {
            Pokemon pokemon = PokemonUtility.pokemonFromJson(bankEntry.pokemon_json_data(), player.level().registryAccess());
            return PokemonUtility.pokemonToItem(pokemon);
        } catch (Exception exception) {
            return MenuUtilities.getInvalidEntryItem();
        }
    }

    private Pokemon findPokemonInPc() {
        for (Pokemon pokemon : Cobblemon.INSTANCE.getStorage().getPC(player)) {
            if (pokemon != null && pokemon.getUuid().equals(pokemonUUID)) {
                return pokemon;
            }
        }
        return null;
    }

    private void openBackPage() {
        if (direction == TransferDirection.DEPOSIT) {
            UIManager.openUIForcefully(player, new UserPCScreen(player).getPage());
            return;
        }
        BankMenuNavigator.openBankMenuAsync(player);
    }

    private void runOnServerThread(Runnable task) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        server.execute(task);
    }

    private String validateHeldItemForTransfer(Pokemon pokemon) {
        MainConfig.Bank bankConfig = CobblePokeBankCommon.INSTANCE.getConfig().bank;
        ItemStack heldItem = pokemon.heldItem();
        if (heldItem.isEmpty()) {
            return null;
        }

        if (!bankConfig.allowHeldItems) {
            return "[CobblePokeBank] Pokemon with held items are not allowed in the bank.";
        }

        if (bankConfig.restrictHeldItemsToOfficialOnly && !heldItem.is(ModTags.COBBLEMON_HELD_ITEMS)) {
            return "[CobblePokeBank] Only official held items are allowed in the bank.";
        }

        List<Item> blacklistedItems = MainConfig.parseHeldItemBlacklist(bankConfig.heldItemBlacklist);
        if (blacklistedItems.contains(heldItem.getItem())) {
            return "[CobblePokeBank] This held item is blacklisted in the bank.";
        }

        return null;
    }
}
