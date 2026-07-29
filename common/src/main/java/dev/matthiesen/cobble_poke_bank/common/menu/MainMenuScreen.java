package dev.matthiesen.cobble_poke_bank.common.menu;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import dev.matthiesen.cobble_poke_bank.common.utility.MenuUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class MainMenuScreen {
    private final ServerPlayer player;

    public MainMenuScreen(ServerPlayer player) {
        this.player = player;
    }

    public Page getPage() {
        Button frame = GooeyButton.builder()
                .display(MenuUtilities.getFrameItem())
                .build();

        Button openPc = GooeyButton.builder()
                .display(MenuUtilities.getPcMenuItem())
                .onClick(action -> UIManager.openUIForcefully(player, new UserPCScreen(player).getPage()))
                .build();

        Button openBank = GooeyButton.builder()
                .display(MenuUtilities.getBankMenuItem())
                .onClick(action -> BankMenuNavigator.openBankMenuAsync(player))
                .build();

        Button info = GooeyButton.builder()
                .display(MenuUtilities.getInfoItem("Move Pokemon between PC and Bank"))
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .fill(frame)
                .set(11, openPc)
                .set(13, info)
                .set(15, openBank)
                .build();

        return GooeyPage.builder()
                .title(Component.literal(player.getName().getString() + "'s Poke Bank"))
                .template(template)
                .build();
    }
}
