package dev.matthiesen.cobble_poke_bank.common.menu;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobble_poke_bank.common.database.repository.PokemonBankRepository;
import dev.matthiesen.cobble_poke_bank.common.utility.MenuUtilities;
import dev.matthiesen.cobble_poke_bank.common.utility.PokemonUtility;
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
        return Component.literal(getPlayer().getName().getString() + "'s Bank");
    }

    private Button buildBankEntryButton(PokemonBankRepository.PokemonBankEntry entry) {
        Pokemon pokemon;
        try {
            pokemon = PokemonUtility.pokemonFromJson(entry.pokemon_json_data(), getPlayer().level().registryAccess());
        } catch (Exception exception) {
            return GooeyButton.builder()
                    .display(MenuUtilities.getInvalidEntryItem())
                    .onClick(action -> action.getPlayer().displayClientMessage(
                            Component.literal("[CobblePokeBank] Invalid Pokemon entry. Check server logs."),
                            false
                    ))
                    .build();
        }

        return GooeyButton.builder()
                .display(PokemonUtility.pokemonToItem(pokemon))
                .onClick(action -> UIManager.openUIForcefully(
                        getPlayer(),
                        ConfirmationScreen.withdraw(getPlayer(), entry).getPage()
                ))
                .build();
    }
}
