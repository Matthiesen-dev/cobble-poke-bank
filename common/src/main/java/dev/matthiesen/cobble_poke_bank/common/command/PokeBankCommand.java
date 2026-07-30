package dev.matthiesen.cobble_poke_bank.common.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.config.MainConfig;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankDatabaseConfig;
import dev.matthiesen.cobble_poke_bank.common.menu.MainMenuScreen;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class PokeBankCommand implements CoreCommand {
    public static final PokeBankCommand CMD = new PokeBankCommand();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        var permissions = CobblePokeBankCommon.INSTANCE.getPermissions();
        dispatcher.register(
                Commands.literal("pokebank")
                        .requires(src -> CobblePokeBankCommon.INSTANCE.checkPermission(src, permissions.POKEBANK_PERMISSION))
                        .executes(this::action)
                        .then(
                                Commands.literal("status")
                                        .requires(src -> CobblePokeBankCommon.INSTANCE.checkPermission(src, permissions.POKEBANK_STATUS_PERMISSION))
                                        .executes(this::status)
                                        .then(
                                                Commands.literal("blacklist")
                                                        .requires(src -> CobblePokeBankCommon.INSTANCE.checkPermission(src, permissions.POKEBANK_STATUS_PERMISSION))
                                                        .executes(this::statusBlacklist)
                                        )
                        )
        );
    }

    private int action(CommandContext<CommandSourceStack> context) {
        if (!CobblePokeBankCommon.INSTANCE.isDatabaseAvailable()) {
            context.getSource().sendFailure(Component.literal("Database is not available. Please try again later."));
            return 0;
        }

        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to find executing player for pokebank command", exception);
            context.getSource().sendFailure(Component.literal("Failed to find executing player."));
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

        ChatTableBuilder tableBuilder = new ChatTableBuilder("Cobble Poke Bank Held Item Blacklist");

        if (bankConfig.heldItemRestrictions.blacklist.isEmpty()) {
            source.sendSystemMessage(Component.literal("No held items are blacklisted."));
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
