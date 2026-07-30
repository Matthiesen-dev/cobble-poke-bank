package dev.matthiesen.cobble_poke_bank.common.menu;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobble_poke_bank.common.utility.PokeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class UserPCScreen extends AbstractUserScreen {

    public UserPCScreen(ServerPlayer player) {
        super(player);
    }

    @Override
    protected List<Button> getPokemonButtons() {
        List<Button> buttons = new ArrayList<>();
        var pc = Cobblemon.INSTANCE.getStorage().getPC(getPlayer());
        for (Pokemon pokemon : pc) {
            if (pokemon == null) continue;
            Button button = GooeyButton.builder()
                    .display(new PokeUtil(pokemon).toItem())
                    .onClick(action -> UIManager.openUIForcefully(
                            getPlayer(),
                            ConfirmationScreen.deposit(getPlayer(), pokemon.getUuid()).getPage()
                    ))
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    @Override
    protected Component getPageTitle() {
        return Component.literal(getPlayer().getName().getString() + "'s PC");
    }
}
