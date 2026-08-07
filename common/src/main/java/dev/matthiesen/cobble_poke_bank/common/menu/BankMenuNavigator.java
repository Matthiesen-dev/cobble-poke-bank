package dev.matthiesen.cobble_poke_bank.common.menu;

import ca.landonjw.gooeylibs2.api.UIManager;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.cobble_poke_bank.common.database.service.DatabaseServices;
import dev.matthiesen.cobble_poke_bank.common.utility.ChatHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class BankMenuNavigator {
    private BankMenuNavigator() {}

    public static void openBankMenuAsync(ServerPlayer player) {
        player.displayClientMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageDatabaseLoadingData.get()), false);
        DatabaseServices.ASYNC_POKE_BANK.getUserBank(player.getUUID().toString())
                .whenComplete((entries, throwable) -> runOnServerThread(player, () -> {
                    if (throwable != null) {
                        CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to load user bank asynchronously", throwable);
                        player.displayClientMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageDatabaseFailedToLoadData.get()), false);
                        return;
                    }
                    UIManager.openUIForcefully(player, new UserBankScreen(player, entries).getPage());
                }));
    }

    private static void runOnServerThread(ServerPlayer player, Runnable task) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        server.execute(task);
    }
}
