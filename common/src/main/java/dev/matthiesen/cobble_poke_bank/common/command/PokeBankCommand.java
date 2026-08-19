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
import net.minecraft.network.chat.Component;
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
            context.getSource().sendSystemMessage(Component.literal("Invalid config type. Valid types are: server"));
            return 0;
        }

        if (configType.equals("server")) {
            String[] availableConfigNames = {
                    "bankMaxSlots", "bankNoFainted", "bankNoHeldItems",
                    "bankNoLegendaries", "bankNoMythicals", "bankNoUltraBeasts",
                    "heldItemOfficialTaggedOnly", "heldItemAutoStrip"
            };

            boolean isValidConfigName = false;
            for (String availableConfigName : availableConfigNames) {
                if (availableConfigName.equalsIgnoreCase(configName)) {
                    isValidConfigName = true;
                    break;
                }
            }
            if (!isValidConfigName) {
                context.getSource().sendSystemMessage(Component.literal("Invalid config name. Valid names are: " + String.join(", ", availableConfigNames)));
                return 0;
            }

            switch (configName) {
                case "bankMaxSlots" -> {
                    int intValue;
                    try {
                        intValue = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for bankMaxSlots. Please provide a valid integer."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.bankMaxSlots.set(intValue);
                    PokeBankConfig.SERVER_CONFIG.bankMaxSlots.save();
                }
                case "bankNoFainted" -> {
                    boolean boolValue;
                    try {
                        boolValue = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for bankNoFainted. Please provide a valid boolean (true/false)."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.bankNoFainted.set(boolValue);
                    PokeBankConfig.SERVER_CONFIG.bankNoFainted.save();
                }
                case "bankNoHeldItems" -> {
                    boolean boolValue;
                    try {
                        boolValue = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for bankNoHeldItems. Please provide a valid boolean (true/false)."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.bankNoHeldItems.set(boolValue);
                    PokeBankConfig.SERVER_CONFIG.bankNoHeldItems.save();
                }
                case "bankNoLegendaries" -> {
                    boolean boolValue;
                    try {
                        boolValue = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for bankNoLegendaries. Please provide a valid boolean (true/false)."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.bankNoLegendaries.set(boolValue);
                    PokeBankConfig.SERVER_CONFIG.bankNoLegendaries.save();
                }
                case "bankNoMythicals" -> {
                    boolean boolValue;
                    try {
                        boolValue = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for bankNoMythicals. Please provide a valid boolean (true/false)."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.bankNoMythicals.set(boolValue);
                    PokeBankConfig.SERVER_CONFIG.bankNoMythicals.save();
                }
                case "bankNoUltraBeasts" -> {
                    boolean boolValue;
                    try {
                        boolValue = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for bankNoUltraBeasts. Please provide a valid boolean (true/false)."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.bankNoUltraBeasts.set(boolValue);
                    PokeBankConfig.SERVER_CONFIG.bankNoUltraBeasts.save();
                }
                case "heldItemOfficialTaggedOnly" -> {
                    boolean boolValue;
                    try {
                        boolValue = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for heldItemOfficialTaggedOnly. Please provide a valid boolean (true/false)."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.heldItemOfficialTaggedOnly.set(boolValue);
                    PokeBankConfig.SERVER_CONFIG.heldItemOfficialTaggedOnly.save();
                }
                case "heldItemAutoStrip" -> {
                    boolean boolValue;
                    try {
                        boolValue = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        context.getSource().sendSystemMessage(Component.literal("Invalid value for heldItemAutoStrip. Please provide a valid boolean (true/false)."));
                        return 0;
                    }
                    PokeBankConfig.SERVER_CONFIG.heldItemAutoStrip.set(boolValue);
                    PokeBankConfig.SERVER_CONFIG.heldItemAutoStrip.save();
                }
            }
            return 1;
        }
        return 0;
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

        ChatTableBuilder tableBuilder = new ChatTableBuilder("Cobble Poke Bank Status");

        tableBuilder.addSection("Database");
        tableBuilder.addRow("Database Type", databaseConfig.useMySQL.getAsBoolean() ? "MySQL" : "SQLite");
        tableBuilder.addRow("Database Status", CobblePokeBankCommon.INSTANCE.isDatabaseAvailable() ? "§aConnected" : "§cOffline");

        tableBuilder.addSection("Bank Configuration");
        tableBuilder.addRow("Bank Max Slots", bankConfig.bankMaxSlots.getAsInt() <= 0 ? "Unlimited" : String.valueOf(bankConfig.bankMaxSlots.getAsInt()));
        tableBuilder.addRow("No Fainted", bankConfig.bankNoFainted.getAsBoolean() ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Held Items", bankConfig.bankNoHeldItems.getAsBoolean() ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Legendaries", bankConfig.bankNoLegendaries.getAsBoolean() ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Mythicals", bankConfig.bankNoMythicals.getAsBoolean() ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("No Ultra Beasts", bankConfig.bankNoUltraBeasts.getAsBoolean() ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("Pokemon Blacklist Entries", String.valueOf(bankConfig.bankPokemonBlacklist.get().size()));
        tableBuilder.addRow("Official Held Items Only", bankConfig.heldItemOfficialTaggedOnly.getAsBoolean() ? "§aEnabled" : "§cDisabled");
        tableBuilder.addRow("Held Item Blacklist Entries", String.valueOf(bankConfig.heldItemBlacklist.get().size()));

        source.sendSystemMessage(tableBuilder.build());
        return 1;
    }

    private int statusPokemonBlacklist(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        var bankConfig = PokeBankConfig.SERVER_CONFIG;

        ChatTableBuilder tableBuilder = new ChatTableBuilder("Cobble Poke Bank Pokemon Blacklist");

        if (bankConfig.bankPokemonBlacklist.get().isEmpty()) {
            source.sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandNoBlacklistedPokemon.get()));
        } else {
            tableBuilder.addSection("Blacklisted Pokemon");
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

        ChatTableBuilder tableBuilder = new ChatTableBuilder("Cobble Poke Bank Held Item Blacklist");

        if (bankConfig.heldItemBlacklist.get().isEmpty()) {
            source.sendSystemMessage(ChatHelper.buildChatMessage(PokeBankConfig.SERVER_CONFIG.messageCommandNoBlacklistedItems.get()));
        } else {
            tableBuilder.addSection("Blacklisted Held Items");
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
