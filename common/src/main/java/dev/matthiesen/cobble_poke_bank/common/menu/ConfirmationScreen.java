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
import dev.matthiesen.cobble_poke_bank.common.database.repository.PokemonBankRepository;
import dev.matthiesen.cobble_poke_bank.common.database.service.DatabaseServices;
import dev.matthiesen.cobble_poke_bank.common.utility.MenuUtilities;
import dev.matthiesen.cobble_poke_bank.common.utility.PokemonUtility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;

public final class ConfirmationScreen {
    private final ServerPlayer player;
    private final TransferDirection direction;
    private final UUID pokemonUUID;

    public enum TransferDirection {
        DEPOSIT,
        WITHDRAW
    }

    public ConfirmationScreen(ServerPlayer player, TransferDirection direction, UUID pokemonUUID) {
        this.player = player;
        this.direction = direction;
        this.pokemonUUID = pokemonUUID;
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
                .onClick(action -> UIManager.openUIForcefully(player, getBackPage()))
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
        TransferResult result = direction == TransferDirection.DEPOSIT ? deposit() : withdraw();
        player.displayClientMessage(Component.literal(result.message), false);
        if (result.success) {
            UIManager.openUIForcefully(player, getBackPage());
        }
    }

    private TransferResult deposit() {
        int maxSlots = CobblePokeBankCommon.INSTANCE.getConfig().bank.maxSlots;
        int currentBankSize = DatabaseServices.POKE_BANK.getUserBank(player.getUUID().toString()).size();
        if (maxSlots > 0 && currentBankSize >= maxSlots) {
            return new TransferResult(false, "[CobblePokeBank] Your bank is full.");
        }

        Pokemon pokemon = findPokemonInPc();
        if (pokemon == null) {
            return new TransferResult(false, "[CobblePokeBank] Pokemon is no longer in your PC.");
        }

        JsonObject jsonObject = PokemonUtility.pokemonToJson(pokemon, player.level().registryAccess());
        boolean saved = DatabaseServices.POKE_BANK.insertOrUpdateBankEntry(
                player.getUUID().toString(),
                pokemon.getUuid().toString(),
                jsonObject
        );
        if (!saved) {
            return new TransferResult(false, "[CobblePokeBank] Failed to save Pokemon to bank.");
        }

        boolean removed = Cobblemon.INSTANCE.getStorage().getPC(player).remove(pokemon);
        if (!removed) {
            DatabaseServices.POKE_BANK.deleteBankEntry(player.getUUID().toString(), pokemon.getUuid().toString());
            return new TransferResult(false, "[CobblePokeBank] Failed to remove Pokemon from PC.");
        }

        return new TransferResult(true, "[CobblePokeBank] Pokemon deposited into your bank.");
    }

    private TransferResult withdraw() {
        PokemonBankRepository.PokemonBankEntry entry = findBankEntry();
        if (entry == null) {
            return new TransferResult(false, "[CobblePokeBank] Pokemon is no longer in your bank.");
        }

        Pokemon pokemon;
        try {
            pokemon = PokemonUtility.pokemonFromJson(entry.pokemon_json_data(), player.level().registryAccess());
        } catch (Exception exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to deserialize bank Pokemon entry", exception);
            return new TransferResult(false, "[CobblePokeBank] Failed to read Pokemon data.");
        }

        boolean added = Cobblemon.INSTANCE.getStorage().getPC(player).add(pokemon);
        if (!added) {
            return new TransferResult(false, "[CobblePokeBank] Your PC is full.");
        }

        boolean deleted = DatabaseServices.POKE_BANK.deleteBankEntry(
                player.getUUID().toString(),
                entry.pokemon_uuid().toString()
        );
        if (!deleted) {
            Cobblemon.INSTANCE.getStorage().getPC(player).remove(pokemon);
            return new TransferResult(false, "[CobblePokeBank] Failed to remove Pokemon from bank.");
        }

        return new TransferResult(true, "[CobblePokeBank] Pokemon moved into your PC.");
    }

    private ItemStack resolvePreviewItem() {
        if (direction == TransferDirection.DEPOSIT) {
            Pokemon pokemon = findPokemonInPc();
            return pokemon != null ? PokemonUtility.pokemonToItem(pokemon) : MenuUtilities.getInvalidEntryItem();
        }

        PokemonBankRepository.PokemonBankEntry entry = findBankEntry();
        if (entry == null) {
            return MenuUtilities.getInvalidEntryItem();
        }

        try {
            Pokemon pokemon = PokemonUtility.pokemonFromJson(entry.pokemon_json_data(), player.level().registryAccess());
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

    private PokemonBankRepository.PokemonBankEntry findBankEntry() {
        Map<Integer, PokemonBankRepository.PokemonBankEntry> userBank =
                DatabaseServices.POKE_BANK.getUserBank(player.getUUID().toString());
        for (PokemonBankRepository.PokemonBankEntry value : userBank.values()) {
            if (value.pokemon_uuid().equals(pokemonUUID)) {
                return value;
            }
        }
        return null;
    }

    private Page getBackPage() {
        return direction == TransferDirection.DEPOSIT ?
                new UserPCScreen(player).getPage() :
                new UserBankScreen(player).getPage();
    }

    private record TransferResult(boolean success, String message) {}
}
