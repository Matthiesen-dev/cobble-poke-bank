package dev.matthiesen.cobble_poke_bank.common.menu;

import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.cobble_poke_bank.common.utility.MenuUtilities;
import dev.matthiesen.matthiesen_core.common.api.server_menu.ServerUIManager;
import dev.matthiesen.matthiesen_core.common.api.server_menu.button.*;
import dev.matthiesen.matthiesen_core.common.api.server_menu.page.*;
import dev.matthiesen.matthiesen_core.common.api.server_menu.template.types.ChestTemplate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class MainMenuScreen {
    private final ServerPlayer player;

    public MainMenuScreen(ServerPlayer player) {
        this.player = player;
    }

    public Page getPage() {
        var config = PokeBankConfig.SERVER_CONFIG;

        Button frame = ServerButton.builder()
                .display(MenuUtilities.getFrameItem())
                .build();

        Button openPc = ServerButton.builder()
                .display(MenuUtilities.getPcMenuItem())
                .onClick(action -> ServerUIManager.openUIForcefully(player, new UserPCScreen(player).getPage()))
                .build();

        Button openBank = ServerButton.builder()
                .display(MenuUtilities.getBankMenuItem())
                .onClick(action -> BankMenuNavigator.openBankMenuAsync(player))
                .build();

        Button info = ServerButton.builder()
                .display(MenuUtilities.getInfoItem(config.guiText_mainMenuInfo.get()))
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .fill(frame)
                .set(11, openPc)
                .set(13, info)
                .set(15, openBank)
                .build();

        String titleText = config.guiText_mainMenuTitle.get()
                .replace("{player}", player.getName().getString());
        return ServerPage.builder()
                .title(Component.literal(titleText))
                .template(template)
                .build();
    }
}
