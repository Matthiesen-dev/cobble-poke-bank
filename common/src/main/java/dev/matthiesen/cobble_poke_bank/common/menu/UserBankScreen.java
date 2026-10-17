package dev.matthiesen.cobble_poke_bank.common.menu;

import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.cobble_poke_bank.common.database.repository.PokemonBankRepository;
import dev.matthiesen.cobble_poke_bank.common.utility.*;
import dev.matthiesen.matthiesen_core.common.api.server_menu.ServerUIManager;
import dev.matthiesen.matthiesen_core.common.api.server_menu.button.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class UserBankScreen extends AbstractUserScreen {
    private final Map<Integer, PokemonBankRepository.PokemonBankEntry> entries;

    public UserBankScreen(ServerPlayer player, Map<Integer, PokemonBankRepository.PokemonBankEntry> entries) {
        super(player);
        this.entries = entries;
    }

    @Override
    protected List<Button> getPokemonButtons() {
        List<Button> buttons = new ArrayList<>();

        entries.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(entry -> buttons.add(buildBankEntryButton(entry.getValue())));
        return buttons;
    }

    @Override
    protected Component getPageTitle() {
        String titleTemplate = PokeBankConfig.SERVER_CONFIG.guiText_userBankScreenTitle.get();
        String playerName = getPlayer().getName().getString();
        String title = titleTemplate.replace("{player}", playerName);
        return Component.literal(title);
    }

    private Button buildBankEntryButton(PokemonBankRepository.PokemonBankEntry entry) {
        Pokemon pokemon;
        try {
            pokemon = PokeUtil.fromJson(entry.pokemon_json_data(), getPlayer().level().registryAccess()).getPokemon();
        } catch (Exception exception) {
            return ServerButton.builder()
                    .display(MenuUtilities.getInvalidEntryItem())
                    .onClick(action -> action.getPlayer().displayClientMessage(
                            ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageDatabaseInvalidPokemonData.get()),
                            false
                    ))
                    .build();
        }

        return ServerButton.builder()
                .display(new PokeUtil(pokemon).toItem())
                .onClick(action -> ServerUIManager.openUIForcefully(
                        getPlayer(),
                        ConfirmationScreen.withdraw(getPlayer(), entry).getPage()
                ))
                .build();
    }
}
