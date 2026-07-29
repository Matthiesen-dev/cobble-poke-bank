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
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobble_poke_bank.common.utility.MenuUtilities;
import dev.matthiesen.cobble_poke_bank.common.utility.PokemonUtility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class UserPCScreen {
    private final ServerPlayer player;

    public UserPCScreen(ServerPlayer player) {
        this.player = player;
    }

    private List<Button> getPokemonButtons() {
        List<Button> buttons = new ArrayList<>();
        var pc = Cobblemon.INSTANCE.getStorage().getPC(player);
        for (Pokemon pokemon : pc) {
            if (pokemon == null) continue;
            Button button = GooeyButton.builder()
                    .display(PokemonUtility.pokemonToItem(pokemon))
                    .onClick(action -> UIManager.openUIForcefully(
                            player,
                            ConfirmationScreen.deposit(player, pokemon.getUuid()).getPage()
                    ))
                    .build();
            buttons.add(button);
        }
        return buttons;
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
        page.setTitle(Component.literal(player.getName().getString() + "'s PC"));
        return page;
    }
}
