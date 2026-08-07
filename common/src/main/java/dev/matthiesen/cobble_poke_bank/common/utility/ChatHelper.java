package dev.matthiesen.cobble_poke_bank.common.utility;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.matthiesen_core.common.api.text_parsers.TextParser;
import net.minecraft.network.chat.Component;

public final class ChatHelper {
    private ChatHelper() {}

    public static String getChatPrefix() {
        return PokeBankConfig.SERVER_CONFIG.messagePrefix.get();
    }

    public static TextParser getTextParser() {
        var manager = CobblePokeBankCommon.INSTANCE.getTextParserManager();
        return manager.getTextParser(PokeBankConfig.SERVER_CONFIG.messageTextParser.get());
    }

    public static Component parseText(String text) {
        return getTextParser().parse(text);
    }

    public static Component buildChatMessage(String message) {
        return Component.empty().append(parseText(getChatPrefix())).append(parseText(message));
    }
}
