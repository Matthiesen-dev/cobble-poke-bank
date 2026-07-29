package dev.matthiesen.cobble_poke_bank.common.menu;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
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

public final class UserBankScreen {
    private final ServerPlayer player;
    private final Map<Integer, PokemonBankRepository.PokemonBankEntry> entries;

    public UserBankScreen(ServerPlayer player, Map<Integer, PokemonBankRepository.PokemonBankEntry> entries) {
        this.player = player;
        this.entries = entries;
    }

    private List<Button> getPokemonButtons() {
        List<Button> buttons = new ArrayList<>();

        entries.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(entry -> buttons.add(buildBankEntryButton(entry.getValue())));
        return buttons;
    }

    private Button buildBankEntryButton(PokemonBankRepository.PokemonBankEntry entry) {
        Pokemon pokemon;
        try {
            pokemon = PokemonUtility.pokemonFromJson(entry.pokemon_json_data(), player.level().registryAccess());
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
                        player,
                        ConfirmationScreen.withdraw(player, entry).getPage()
                ))
                .build();
    }

    public Page getPage() {
        PlaceholderButton placeholder = new PlaceholderButton();
        Button frame = GooeyButton.builder().display(MenuUtilities.getFrameItem()).build();

        Button back = GooeyButton.builder()
                .display(MenuUtilities.getBackItem())
                .onClick(action -> UIManager.openUIForcefully(player, new MainMenuScreen(player).getPage()))
                .build();

        LinkedPageButton previous = LinkedPageButton.builder()
                .display(MenuUtilities.getPrevItem())
                .linkType(LinkType.Previous)
                .build();
        LinkedPageButton next = LinkedPageButton.builder()
                .display(MenuUtilities.getNextItem())
                .linkType(LinkType.Next)
                .build();

        ChestTemplate template = ChestTemplate.builder(6)
                .rectangle(0, 0, 5, 9, placeholder)
                .set(45, previous)
                .set(49, back)
                .set(53, next)
                .fill(frame)
                .build();

        LinkedPage page = PaginationHelper.createPagesFromPlaceholders(template, getPokemonButtons(), null);
        page.setTitle(Component.literal(player.getName().getString() + "'s Bank"));
        return page;
    }
}
