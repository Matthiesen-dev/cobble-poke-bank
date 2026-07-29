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
import dev.matthiesen.cobble_poke_bank.common.utility.MenuUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * AbstractUserScreen is an abstract class that provides a framework for creating user interface screens for players in the
 * CobblePokeBank mod. It defines common functionality for displaying a paginated menu of buttons representing a player's
 * Pokemon collection, as well as navigation controls to move between pages and return to the main menu.
 */
public abstract class AbstractUserScreen {
    private final ServerPlayer player;

    /**
     * Constructs an AbstractUserScreen for the specified player.
     * @param player The ServerPlayer object representing the player viewing this screen.
     */
    public AbstractUserScreen(ServerPlayer player) {
        this.player = player;
    }

    /**
     * Retrieves the ServerPlayer associated with this screen.
     * @return The ServerPlayer object representing the player viewing this screen.
     */
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * Generates a list of buttons representing the user's Pokemon collection.
     * @return A list of Button objects representing the user's Pokemon collection.
     */
    protected abstract List<Button> getPokemonButtons();

    /**
     * Generates the title for the paginated menu page.
     * @return A Component representing the title of the menu page.
     */
    protected abstract Component getPageTitle();

    /**
     * Generates a paginated menu page for the user's Pokemon collection.
     * @return A Page object representing the user's Pokemon collection menu.
     */
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
        page.setTitle(getPageTitle());
        return page;
    }
}
