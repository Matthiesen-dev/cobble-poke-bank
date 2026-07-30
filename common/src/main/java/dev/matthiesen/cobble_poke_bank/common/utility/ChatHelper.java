package dev.matthiesen.cobble_poke_bank.common.utility;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import net.minecraft.network.chat.Component;

public final class ChatHelper {
    private ChatHelper() {}

    public static String getChatPrefix() {
        return CobblePokeBankCommon.INSTANCE.getMessagesConfig().prefix;
    }

    public static Component getChatPrefixComponent() {
        return Component.literal(getChatPrefix());
    }

    public static Component buildChatMessage(String message) {
        return Component.empty().append(getChatPrefixComponent()).append(Component.literal(message));
    }
}
