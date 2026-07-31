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
import dev.matthiesen.cobble_poke_bank.common.utility.*;
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
        var messagesConfig = CobblePokeBankCommon.INSTANCE.getMessagesConfig();
        Pokemon pokemon = findPokemonInPc();
        if (pokemon == null) {
            player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.depositMessages.pokemonMissing), false);
            return;
        }
        autoStripHeldItemIfNeeded(pokemon, TransferDirection.DEPOSIT);
        String transferValidationMessage = validatePokemonForTransfer(pokemon, TransferDirection.DEPOSIT);
        if (transferValidationMessage != null) {
            player.displayClientMessage(ChatHelper.buildChatMessage(transferValidationMessage), false);
            return;
        }

        JsonObject jsonObject = new PokeUtil(pokemon).toJson(player.level().registryAccess());
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
                player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.depositMessages.failedToSave), false);
                return;
            }

            if (!saved) {
                if (maxSlots > 0) {
                    player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.depositMessages.bankFull), false);
                } else {
                    player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.depositMessages.failedToSave), false);
                }
                return;
            }

            Pokemon latestPokemon = findPokemonInPc();
            if (latestPokemon == null || !Cobblemon.INSTANCE.getStorage().getPC(player).remove(latestPokemon)) {
                DatabaseServices.ASYNC_POKE_BANK.deleteBankEntry(userUUID, pokemonUUIDString);
                player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.depositMessages.pcRemovalFailed), false);
                return;
            }

            player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.depositMessages.pokemonDeposited), false);
            UIManager.openUIForcefully(player, new UserPCScreen(player).getPage());
        }));
    }

    private void withdrawAsync() {
        var messagesConfig = CobblePokeBankCommon.INSTANCE.getMessagesConfig();
        if (bankEntry == null) {
            player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.withdrawMessages.pokemonMissing), false);
            return;
        }

        Pokemon pokemon;
        try {
            pokemon = PokeUtil.fromJson(bankEntry.pokemon_json_data(), player.level().registryAccess()).getPokemon();
        } catch (Exception exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to deserialize bank Pokemon entry", exception);
            player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.withdrawMessages.failedToReadData), false);
            return;
        }

        autoStripHeldItemIfNeeded(pokemon, TransferDirection.WITHDRAW);
        String transferValidationMessage = validatePokemonForTransfer(pokemon, TransferDirection.WITHDRAW);
        if (transferValidationMessage != null) {
            player.displayClientMessage(ChatHelper.buildChatMessage(transferValidationMessage), false);
            return;
        }

        String userUUID = player.getUUID().toString();
        String pokemonUUIDString = bankEntry.pokemon_uuid().toString();

        DatabaseServices.ASYNC_POKE_BANK.deleteBankEntry(userUUID, pokemonUUIDString)
                .whenComplete((deleted, throwable) -> runOnServerThread(() -> {
                    if (throwable != null) {
                        CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to delete Pokemon from bank asynchronously", throwable);
                        player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.withdrawMessages.failedToRemoveFromBank), false);
                        return;
                    }

                    if (!deleted) {
                        player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.withdrawMessages.pokemonMissing), false);
                        return;
                    }

                    if (!Cobblemon.INSTANCE.getStorage().getPC(player).add(pokemon)) {
                        DatabaseServices.ASYNC_POKE_BANK.insertOrUpdateBankEntry(
                                userUUID,
                                pokemonUUIDString,
                                bankEntry.pokemon_json_data()
                        );
                        player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.withdrawMessages.pcFull), false);
                        return;
                    }

                    player.displayClientMessage(ChatHelper.buildChatMessage(messagesConfig.withdrawMessages.pokemonDeposited), false);
                    BankMenuNavigator.openBankMenuAsync(player);
                }));
    }

    private ItemStack resolvePreviewItem() {
        if (direction == TransferDirection.DEPOSIT) {
            Pokemon pokemon = findPokemonInPc();
            return pokemon != null ? new PokeUtil(pokemon).toItem() : MenuUtilities.getInvalidEntryItem();
        }

        if (bankEntry == null) {
            return MenuUtilities.getInvalidEntryItem();
        }

        try {
            Pokemon pokemon = PokeUtil.fromJson(bankEntry.pokemon_json_data(), player.level().registryAccess()).getPokemon();
            return new PokeUtil(pokemon).toItem();
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

    private String directionToLocation(TransferDirection direction) {
        var messagesConfig = CobblePokeBankCommon.INSTANCE.getMessagesConfig();
        return switch (direction) {
            case DEPOSIT -> messagesConfig.locationLabels.deposit;
            case WITHDRAW -> messagesConfig.locationLabels.withdraw;
        };
    }

    /**
     * If autoStrip is enabled and the Pokemon's held item would fail a held-item restriction,
     * strips the item, returns it to the player's inventory (drops if full), and notifies the player.
     */
    private void autoStripHeldItemIfNeeded(Pokemon pokemon, TransferDirection direction) {
        MainConfig.Bank bankConfig = CobblePokeBankCommon.INSTANCE.getConfig().bank;
        if (!bankConfig.heldItemRestrictions.autoStrip) return;

        ItemStack heldItem = pokemon.heldItem();
        if (heldItem.isEmpty()) return;

        boolean shouldStrip = bankConfig.noHeldItems
                || (bankConfig.heldItemRestrictions.officialTaggedOnly && !heldItem.is(ModTags.COBBLEMON_HELD_ITEMS))
                || MainConfig.parseHeldItemBlacklist(bankConfig.heldItemRestrictions.blacklist).contains(heldItem.getItem());

        if (!shouldStrip) return;

        ItemStack stripped = pokemon.removeHeldItem();
        if (!stripped.isEmpty()) {
            if (direction == TransferDirection.DEPOSIT) {
                if (!player.addItem(stripped)) {
                    player.drop(stripped, false);
                }
            }
            var messagesConfig = CobblePokeBankCommon.INSTANCE.getMessagesConfig();
            String location = directionToLocation(direction);
            player.displayClientMessage(
                    ChatHelper.buildChatMessage(messagesConfig.validationMessages.autoStrippedHeldItem.replace("%s", location)),
                    false
            );
        }
    }

    private String validatePokemonForTransfer(Pokemon pokemon, TransferDirection direction) {
        MainConfig.Bank bankConfig = CobblePokeBankCommon.INSTANCE.getConfig().bank;
        var messagesConfig = CobblePokeBankCommon.INSTANCE.getMessagesConfig();
        ItemStack heldItem = pokemon.heldItem();
        String location = directionToLocation(direction);

        if (!heldItem.isEmpty()) {
            if (bankConfig.noHeldItems) {
                return messagesConfig.validationMessages.noHeldItems.replace("%s", location);
            }

            if (bankConfig.heldItemRestrictions.officialTaggedOnly && !heldItem.is(ModTags.COBBLEMON_HELD_ITEMS)) {
                return messagesConfig.validationMessages.officialHeldItemsOnly.replace("%s", location);
            }

            List<Item> blacklistedItems = MainConfig.parseHeldItemBlacklist(bankConfig.heldItemRestrictions.blacklist);
            if (blacklistedItems.contains(heldItem.getItem())) {
                return messagesConfig.validationMessages.blacklistedHeldItem.replace("%s", location);
            }
        }

        if (bankConfig.noLegendaries && pokemon.isLegendary()) {
            return messagesConfig.validationMessages.noLegendaries.replace("%s", location);
        }

        if (bankConfig.noMythicals && pokemon.isMythical()) {
            return messagesConfig.validationMessages.noMythicals.replace("%s", location);
        }

        if (bankConfig.noUltraBeasts && pokemon.isUltraBeast()) {
            return messagesConfig.validationMessages.noUltraBeasts.replace("%s", location);
        }

        if (bankConfig.noFainted && pokemon.isFainted()) {
            return messagesConfig.validationMessages.noFainted.replace("%s", location);
        }

        return null;
    }
}
