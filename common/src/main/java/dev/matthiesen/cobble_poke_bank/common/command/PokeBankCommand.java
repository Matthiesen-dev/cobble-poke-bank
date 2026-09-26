package dev.matthiesen.cobble_poke_bank.common.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.cobble_poke_bank.common.menu.MainMenuScreen;
import dev.matthiesen.cobble_poke_bank.common.utility.ChatHelper;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import dev.matthiesen.matthiesen_core.common.api.events.server.ServerEvent;
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

        // /pokebank status blacklist - shows held item blacklist entries
        var blacklistCMD = CommandBuilder.create("blacklist")
                .requires(requirePredicate(permissions.POKEBANK_STATUS_PERMISSION))
                .executes(this::statusBlacklist);

        // /pokebank status pokemonblacklist - shows Pokemon blacklist entries
        var pokemonBlacklistCMD = CommandBuilder.create("pokemonblacklist")
                .requires(requirePredicate(permissions.POKEBANK_STATUS_PERMISSION))
                .executes(this::statusPokemonBlacklist);

        // /pokebank status - Shows mod status
        var statusCMD = CommandBuilder.create("status")
                .requires(requirePredicate(permissions.POKEBANK_STATUS_PERMISSION))
                .executes(this::status)
                .then(blacklistCMD)
                .then(pokemonBlacklistCMD);

        // /pokebank reload - Reloads the config
        var reloadCMD = CommandBuilder.create("reload")
                .requires(requirePredicate(permissions.POKEBANK_RELOAD_PERMISSION))
                .executes(this::reload);

        // /pokebank configure
        var configureCMD = CommandBuilder.create("configure")
                .argument("configType", StringArgumentType.word(), builder -> builder
                            .suggests((ctx, suggestionsBuilder) -> {
                                suggestionsBuilder.suggest("server");
                                return suggestionsBuilder.buildFuture();
                            })
                            .then(Commands.argument("configName", StringArgumentType.word())
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .executes(this::configure)
                                        )
                            )
                )
                .build();

        // /pokebank - Opens the bank menu
        var pokeBankCMD = CommandBuilder.create("pokebank")
                .requires(requirePredicate(permissions.POKEBANK_PERMISSION))
                .executes(this::action)
                .then(statusCMD)
                .then(reloadCMD)
                .then(configureCMD);

        dispatcher.register(pokeBankCMD.build());
    }

    private int configure(CommandContext<CommandSourceStack> context) {
        String configType = StringArgumentType.getString(context, "configType");
        String configName = StringArgumentType.getString(context, "configName");
        String value = StringArgumentType.getString(context, "value");

        if (!configType.equalsIgnoreCase("server")) {
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(
                    PokeBankConfig.SERVER_CONFIG.messageCommandInvalidConfigType.get().replace("%s", "server")));
            return 0;
        }

        String[] availableConfigNames = {
                "bankMaxSlots", "bankNoFainted", "bankNoHeldItems",
                "bankNoLegendaries", "bankNoMythicals", "bankNoUltraBeasts",
                "heldItemOfficialTaggedOnly", "heldItemAutoStrip"
        };

        String resolvedConfigName = null;
        for (String availableConfigName : availableConfigNames) {
            if (availableConfigName.equalsIgnoreCase(configName)) {
                resolvedConfigName = availableConfigName;
                break;
            }
        }
        if (resolvedConfigName == null) {
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(
                    PokeBankConfig.SERVER_CONFIG.messageCommandInvalidConfigName.get()
                            .replace("%s", String.join(", ", availableConfigNames))));
            return 0;
        }

        var serverConfig = PokeBankConfig.SERVER_CONFIG;

        if (resolvedConfigName.equals("bankMaxSlots")) {
            int intValue;
            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(
                        serverConfig.messageCommandInvalidIntegerValue.get().replace("%s", resolvedConfigName)));
                return 0;
            }
            serverConfig.bankMaxSlots.set(intValue);
            serverConfig.bankMaxSlots.save();
        } else {
            Boolean boolValue = parseBoolean(value);
            if (boolValue == null) {
                context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(
                        serverConfig.messageCommandInvalidBooleanValue.get().replace("%s", resolvedConfigName)));
                return 0;
            }

            switch (resolvedConfigName) {
                case "bankNoFainted" -> {
                    serverConfig.bankNoFainted.set(boolValue);
                    serverConfig.bankNoFainted.save();
                }
                case "bankNoHeldItems" -> {
                    serverConfig.bankNoHeldItems.set(boolValue);
                    serverConfig.bankNoHeldItems.save();
                }
                case "bankNoLegendaries" -> {
                    serverConfig.bankNoLegendaries.set(boolValue);
                    serverConfig.bankNoLegendaries.save();
                }
                case "bankNoMythicals" -> {
                    serverConfig.bankNoMythicals.set(boolValue);
                    serverConfig.bankNoMythicals.save();
                }
                case "bankNoUltraBeasts" -> {
                    serverConfig.bankNoUltraBeasts.set(boolValue);
                    serverConfig.bankNoUltraBeasts.save();
                }
                case "heldItemOfficialTaggedOnly" -> {
                    serverConfig.heldItemOfficialTaggedOnly.set(boolValue);
                    serverConfig.heldItemOfficialTaggedOnly.save();
                }
                case "heldItemAutoStrip" -> {
                    serverConfig.heldItemAutoStrip.set(boolValue);
                    serverConfig.heldItemAutoStrip.save();
                }
            }
        }

        context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(
                serverConfig.messageCommandConfigUpdated.get().replace("%s", resolvedConfigName)));
        return 1;
    }

    private static Boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        return null;
    }

    private int action(CommandContext<CommandSourceStack> context) {
        if (!CobblePokeBankCommon.INSTANCE.isDatabaseAvailable()) {
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandDatabaseUnavailable.get()));
            return 0;
        }

        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException exception) {
            CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to find executing player for pokebank command", exception);
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandPlayerNotFound.get()));
            return 0;
        }

        if (PlayerExtensionsKt.isInBattle(player)) {
            context.getSource().sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandInBattle.get()));
            return 0;
        }

        UIManager.openUIForcefully(player, new MainMenuScreen(player).getPage());
        return 1;
    }

    private int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        var bankConfig = PokeBankConfig.SERVER_CONFIG;
        var databaseConfig = PokeBankConfig.DATABASE_CONFIG;

        String enabled = bankConfig.statusValueEnabled.get();
        String disabled = bankConfig.statusValueDisabled.get();

        ChatTableBuilder tableBuilder = new ChatTableBuilder(bankConfig.statusTitle.get());

        tableBuilder.addSection(bankConfig.statusSectionDatabase.get());
        tableBuilder.addRow(bankConfig.statusLabelDatabaseType.get(), databaseConfig.useMySQL.getAsBoolean() ? bankConfig.statusValueMySQL.get() : bankConfig.statusValueSQLite.get());
        tableBuilder.addRow(bankConfig.statusLabelDatabaseStatus.get(), CobblePokeBankCommon.INSTANCE.isDatabaseAvailable() ? bankConfig.statusValueConnected.get() : bankConfig.statusValueOffline.get());

        tableBuilder.addSection(bankConfig.statusSectionBankConfiguration.get());
        tableBuilder.addRow(bankConfig.statusLabelBankMaxSlots.get(), bankConfig.bankMaxSlots.getAsInt() <= 0 ? bankConfig.statusValueUnlimited.get() : String.valueOf(bankConfig.bankMaxSlots.getAsInt()));
        tableBuilder.addRow(bankConfig.statusLabelNoFainted.get(), bankConfig.bankNoFainted.getAsBoolean() ? enabled : disabled);
        tableBuilder.addRow(bankConfig.statusLabelNoHeldItems.get(), bankConfig.bankNoHeldItems.getAsBoolean() ? enabled : disabled);
        tableBuilder.addRow(bankConfig.statusLabelNoLegendaries.get(), bankConfig.bankNoLegendaries.getAsBoolean() ? enabled : disabled);
        tableBuilder.addRow(bankConfig.statusLabelNoMythicals.get(), bankConfig.bankNoMythicals.getAsBoolean() ? enabled : disabled);
        tableBuilder.addRow(bankConfig.statusLabelNoUltraBeasts.get(), bankConfig.bankNoUltraBeasts.getAsBoolean() ? enabled : disabled);
        tableBuilder.addRow(bankConfig.statusLabelPokemonBlacklistEntries.get(), String.valueOf(bankConfig.bankPokemonBlacklist.get().size()));
        tableBuilder.addRow(bankConfig.statusLabelOfficialHeldItemsOnly.get(), bankConfig.heldItemOfficialTaggedOnly.getAsBoolean() ? enabled : disabled);
        tableBuilder.addRow(bankConfig.statusLabelHeldItemBlacklistEntries.get(), String.valueOf(bankConfig.heldItemBlacklist.get().size()));

        source.sendSystemMessage(tableBuilder.build());
        return 1;
    }

    private int statusPokemonBlacklist(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        var bankConfig = PokeBankConfig.SERVER_CONFIG;

        ChatTableBuilder tableBuilder = new ChatTableBuilder(bankConfig.statusPokemonBlacklistTitle.get());

        if (bankConfig.bankPokemonBlacklist.get().isEmpty()) {
            source.sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandNoBlacklistedPokemon.get()));
        } else {
            tableBuilder.addSection(bankConfig.statusSectionBlacklistedPokemon.get());
            for (String pokemon : bankConfig.bankPokemonBlacklist.get()) {
                tableBuilder.addRow(pokemon, pokemon);
            }
        }

        source.sendSystemMessage(tableBuilder.build());
        return 1;
    }

    private int statusBlacklist(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        var bankConfig = PokeBankConfig.SERVER_CONFIG;

        ChatTableBuilder tableBuilder = new ChatTableBuilder(bankConfig.statusHeldItemBlacklistTitle.get());

        if (bankConfig.heldItemBlacklist.get().isEmpty()) {
            source.sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandNoBlacklistedItems.get()));
        } else {
            tableBuilder.addSection(bankConfig.statusSectionBlacklistedHeldItems.get());
            for (String item : bankConfig.heldItemBlacklist.get()) {
                Item decodedItem = ItemDecoder.stringToItem(item, Items.BARRIER);
                tableBuilder.addRow(item, decodedItem.getDefaultInstance().getDisplayName().getString());
            }
        }

        source.sendSystemMessage(tableBuilder.build());
        return 1;
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CobblePokeBankCommon.INSTANCE.reloadSystem(new ServerEvent.Reload());
        source.sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandConfigsReloaded.get()));
        return 1;
    }
}
