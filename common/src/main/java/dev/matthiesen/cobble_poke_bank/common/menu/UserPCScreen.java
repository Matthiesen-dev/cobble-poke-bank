package dev.matthiesen.cobble_poke_bank.common.menu;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.cobble_poke_bank.common.utility.PokeUtil;
import dev.matthiesen.matthiesen_core.common.api.server_menu.ServerUIManager;
import dev.matthiesen.matthiesen_core.common.api.server_menu.button.*;
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
            Button button = ServerButton.builder()
                    .display(new PokeUtil(pokemon).toItem())
                    .onClick(action -> ServerUIManager.openUIForcefully(
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
        String titleTemplate = PokeBankConfig.SERVER_CONFIG.guiText_userPCScreenTitle.get();
        String playerName = getPlayer().getName().getString();
        String title = titleTemplate.replace("{player}", playerName);
        return Component.literal(title);
    }
}
