package dev.matthiesen.cobble_poke_bank.common.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.config.MainConfig;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankDatabaseConfig;
import dev.matthiesen.cobble_poke_bank.common.menu.MainMenuScreen;
import dev.matthiesen.cobble_poke_bank.common.utility.ChatHelper;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

public final class PokeBankCommand implements CoreCommand {
    public static final PokeBankCommand CMD = new PokeBankCommand();

    public static Predicate<CommandSourceStack> requirePredicate(Permission level) {
        return source -> CobblePokeBankCommon.INSTANCE.checkPermission(source, level);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        var permissions = CobblePokeBankCommon.INSTANCE.getPermissions();

        // /pokebank status blacklist
        var blacklistCMD = CommandBuilder.create("blacklist")
                .requires(requirePredicate(permissions.POKEBANK_STATUS_PERMISSION))
                .executes(this::statusBlacklist);

        // /pokebank status
        var statusCMD = CommandBuilder.create("status")
                .requires(requirePredicate(permissions.POKEBANK_STATUS_PERMISSION))
                .executes(this::status)
                .then(blacklistCMD);

        // /pokebank
        var pokeBankCMD = CommandBuilder.create("pokebank")
                .requires(requirePredicate(permissions.POKEBANK_PERMISSION))
                .executes(this::action)
                .then(statusCMD);

        dispatcher.register(pokeBankCMD.build());
    }

    private int action(CommandContext<CommandSourceStack> context) {
        var messagesConfig = CobblePokeBankCommon.INSTANCE.getMessagesConfig();
        if (!CobblePokeBankCommon.INSTANCE.isDatabaseAvailable()) {
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(messagesConfig.commandMessages.databaseUnavailable));
            return 0;
        }

        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to find executing player for pokebank command", exception);
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(messagesConfig.commandMessages.playerNotFound));
            return 0;
        }

        if (PlayerExtensionsKt.isInBattle(player)) {
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(messagesConfig.commandMessages.inBattle));
            return 0;
        }

        UIManager.openUIForcefully(player, new MainMenuScreen(player).getPage());
        return 1;
    }

    private int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MainConfig.Bank bankConfig = CobblePokeBankCommon.INSTANCE.getConfig().bank;
        PokeBankDatabaseConfig databaseConfig = CobblePokeBankCommon.INSTANCE.getDatabaseConfig();

        ChatTableBuilder tableBuilder = new ChatTableBuilder("Cobble Poke Bank Status");

        tableBuilder.addSection("Database");
        tableBuilder.addRow("Database Type", databaseConfig.useMySQL ? "MySQL" : "SQLite");
        tableBuilder.addRow("Database Status", CobblePokeBankCommon.INSTANCE.isDatabaseAvailable() ? "§aConnected" : "§cOffline");

        tableBuilder.addSection("Bank Configuration");
        tableBuilder.addRow("Bank Max Slots", bankConfig.maxSlots <= 0 ? "Unlimited" : String.valueOf(bankConfig.maxSlots));
        tableBuilder.addRow("No Fainted", bankConfig.noFainted ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Held Items", bankConfig.noHeldItems ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Legendaries", bankConfig.noLegendaries ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Mythicals", bankConfig.noMythicals ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Ultra Beasts", bankConfig.noUltraBeasts ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("Official Held Items Only", bankConfig.heldItemRestrictions.officialTaggedOnly ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("Held Item Blacklist Entries", String.valueOf(bankConfig.heldItemRestrictions.blacklist.size()));

        source.sendSystemMessage(tableBuilder.build());
        return 1;
    }

    private int statusBlacklist(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MainConfig.Bank bankConfig = CobblePokeBankCommon.INSTANCE.getConfig().bank;
        var messagesConfig = CobblePokeBankCommon.INSTANCE.getMessagesConfig();

        ChatTableBuilder tableBuilder = new ChatTableBuilder("Cobble Poke Bank Held Item Blacklist");

        if (bankConfig.heldItemRestrictions.blacklist.isEmpty()) {
            source.sendSystemMessage(ChatHelper.buildChatMessage(messagesConfig.commandMessages.noBlacklistedItems));
        } else {
            tableBuilder.addSection("Blacklisted Held Items");
            for (String item : bankConfig.heldItemRestrictions.blacklist) {
                Item decodedItem = ItemDecoder.stringToItem(item, Items.BARRIER);
                tableBuilder.addRow(item, decodedItem.getDefaultInstance().getDisplayName().getString());
            }
        }

        source.sendSystemMessage(tableBuilder.build());
        return 1;
    }
}
