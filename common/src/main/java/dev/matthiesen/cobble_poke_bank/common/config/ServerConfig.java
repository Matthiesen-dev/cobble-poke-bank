package dev.matthiesen.cobble_poke_bank.common.config;

import dev.matthiesen.matthiesen_core.common.api.text_parsers.BuiltInTextParsers;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ServerConfig {

    // Bank Config options
    public ModConfigSpec.IntValue bankMaxSlots;
    public ModConfigSpec.BooleanValue bankNoFainted;
    public ModConfigSpec.BooleanValue bankNoHeldItems;
    public ModConfigSpec.BooleanValue bankNoLegendaries;
    public ModConfigSpec.BooleanValue bankNoMythicals;
    public ModConfigSpec.BooleanValue bankNoUltraBeasts;

    // Held Item Restrictions
    public ModConfigSpec.BooleanValue heldItemOfficialTaggedOnly;
    public ModConfigSpec.BooleanValue heldItemAutoStrip;
    public ModConfigSpec.ConfigValue<List<? extends String>> heldItemBlacklist;

    // Messages Config
    public ModConfigSpec.EnumValue<BuiltInTextParsers> messageTextParser;
    public ModConfigSpec.ConfigValue<String> messagePrefix;

    // Messages - Location Labels
    public ModConfigSpec.ConfigValue<String> messageLocationDeposit;
    public ModConfigSpec.ConfigValue<String> messageLocationWithdraw;

    // Messages - Command Messages
    public ModConfigSpec.ConfigValue<String> messageCommandDatabaseUnavailable;
    public ModConfigSpec.ConfigValue<String> messageCommandPlayerNotFound;
    public ModConfigSpec.ConfigValue<String> messageCommandNoBlacklistedItems;
    public ModConfigSpec.ConfigValue<String> messageCommandInBattle;
    public ModConfigSpec.ConfigValue<String> messageCommandConfigsReloaded;

    // Messages - Database Messages
    public ModConfigSpec.ConfigValue<String> messageDatabaseLoadingData;
    public ModConfigSpec.ConfigValue<String> messageDatabaseFailedToLoadData;
    public ModConfigSpec.ConfigValue<String> messageDatabaseInvalidPokemonData;

    // Messages - Deposit Messages
    public ModConfigSpec.ConfigValue<String> messageDepositPokemonMissing;
    public ModConfigSpec.ConfigValue<String> messageDepositFailedToSave;
    public ModConfigSpec.ConfigValue<String> messageDepositBankFull;
    public ModConfigSpec.ConfigValue<String> messageDepositPcRemovalFailed;
    public ModConfigSpec.ConfigValue<String> messageDepositPokemonDeposited;

    // Messages - Withdraw Messages
    public ModConfigSpec.ConfigValue<String> messageWithdrawPokemonMissing;
    public ModConfigSpec.ConfigValue<String> messageWithdrawFailedToReadData;
    public ModConfigSpec.ConfigValue<String> messageWithdrawFailedToRemoveFromBank;
    public ModConfigSpec.ConfigValue<String> messageWithdrawPcFull;
    public ModConfigSpec.ConfigValue<String> messageWithdrawPokemonDeposited;

    // Messages - Validation Messages
    public ModConfigSpec.ConfigValue<String> messageValidationNoHeldItems;
    public ModConfigSpec.ConfigValue<String> messageValidationOfficialHeldItemsOnly;
    public ModConfigSpec.ConfigValue<String> messageValidationBlacklistedHeldItem;
    public ModConfigSpec.ConfigValue<String> messageValidationNoLegendaries;
    public ModConfigSpec.ConfigValue<String> messageValidationNoMythicals;
    public ModConfigSpec.ConfigValue<String> messageValidationNoUltraBeasts;
    public ModConfigSpec.ConfigValue<String> messageValidationNoFainted;
    public ModConfigSpec.ConfigValue<String> messageValidationAutoStrippedHeldItem;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Server config").push("server");

        builder.comment("Bank Configuration Options").push("bank");
        bankMaxSlots = builder.comment(
                        "The maximum number of slots a player can have in their bank",
                        "Values <= 0 mean unlimited storage.",
                        "Default: -1"
                )
                .defineInRange("maxSlots", -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        bankNoFainted = builder.comment(
                        "If true, players will not be allowed to store Pokemon that have fainted in the bank.",
                        "If false, fainted Pokemon will be allowed to be stored in the bank.",
                        "Default: false"
                )
                .define("noFainted", false);
        bankNoHeldItems = builder.comment(
                        "If true, players will not be allowed to store Pokemon with held items in the bank.",
                        "If false, Pokemon with held items will be allowed to be stored in the bank.",
                        "Default: false"
                )
                .define("noHeldItems", false);
        bankNoLegendaries = builder.comment(
                        "If true, players will not be allowed to store Legendary Pokemon in the bank.",
                        "If false, Legendary Pokemon will be allowed to be stored in the bank.",
                        "Default: false"
                )
                .define("noLegendaries", false);
        bankNoMythicals = builder.comment(
                        "If true, players will not be allowed to store Mythical Pokemon in the bank.",
                        "If false, Mythical Pokemon will be allowed to be stored in the bank.",
                        "Default: false"
                )
                .define("noMythicals", false);
        bankNoUltraBeasts = builder.comment(
                        "If true, players will not be allowed to store Ultra Beast Pokemon in the bank.",
                        "If false, Ultra Beast Pokemon will be allowed to be stored in the bank.",
                        "Default: false"
                )
                .define("noUltraBeasts", false);
        builder.pop(); // Closes "server.bank"

        builder.comment("Held Item Restrictions").push("heldItemRestrictions");
        heldItemOfficialTaggedOnly = builder.comment(
                        "If true, players will not be allowed to store Pokemon with held items that are not officially tagged.",
                        "If false, Pokemon with held items that are not officially tagged will be allowed to be stored in the bank.",
                        "Default: false"
                )
                .define("officialTaggedOnly", false);
        heldItemAutoStrip = builder.comment(
                        "If true, players will have their Pokemon's held items automatically stripped if they are not allowed in the bank.",
                        "If false, players will not be able to store Pokemon with held items that are not allowed in the bank.",
                        "Default: false"
                )
                .define("autoStrip", false);
        heldItemBlacklist = builder.comment(
                        "A list of held items that are not allowed to be stored in the bank.",
                        "Players will not be able to store Pokemon with these held items in the bank.",
                        "Default: []"
                )
                .defineList("blacklist", List.of(), () -> "", o -> o instanceof String);
        builder.pop(); // Closes "server.heldItemRestrictions"

        builder.comment("Messages Configuration").push("messages");
        messageTextParser = builder.comment(
                "The text parser to use for messages sent to players.",
                "Default: VANILLA"
        ).defineEnum("messagesTextParser", BuiltInTextParsers.VANILLA);
        messagePrefix = builder.comment(
                "The prefix to use for messages sent to players.",
                "Default: §f[§6Cobble Poke Bank§f] §r"
        ).define("messagePrefix", "§f[§6Cobble Poke Bank§f] §r");

        builder.comment("Messages - Location Labels").push("locationLabels");
        messageLocationDeposit = builder.comment(
                        "The label to use for the deposit location in messages sent to players.",
                        "Default: Bank"
                )
                .define("deposit", "Bank");
        messageLocationWithdraw = builder.comment(
                        "The label to use for the withdraw location in messages sent to players.",
                        "Default: Server"
                )
                .define("withdraw", "Server");
        builder.pop(); // Closes "server.messages.locationLabels"

        builder.comment("Messages - Command Messages").push("commandMessages");
        messageCommandDatabaseUnavailable = builder.comment("The message to send to players when the database is unavailable.")
                .define("databaseUnavailable", "§cDatabase is not available. Please try again later.");
        messageCommandPlayerNotFound = builder.comment("The message to send to players when the player is not found.")
                .define("playerNotFound", "§cFailed to find executing player.");
        messageCommandNoBlacklistedItems = builder.comment("The message to send to players when they try to deposit a Pokemon with a blacklisted held item.")
                .define("noBlacklistedItems", "§eNo held items are blacklisted.");
        messageCommandInBattle = builder.comment("The message to send to players when they try to use the bank while in battle.")
                .define("inBattle", "§cYou cannot access the bank while in battle.");
        messageCommandConfigsReloaded = builder.comment("The message to send to players when the configs are reloaded.")
                .define("configsReloaded", "§aConfigs reloaded successfully.");
        builder.pop(); // Closes "server.messages.commandMessages"

        builder.comment("Messages - Database Messages").push("databaseMessages");
        messageDatabaseLoadingData = builder.comment("The message to send to players when the database is loading data.")
                .define("loadingData", "§eLoading bank data...");
        messageDatabaseFailedToLoadData = builder.comment("The message to send to players when the database fails to load data.")
                .define("failedToLoadData", "§cFailed to load bank data. Please try again later.");
        messageDatabaseInvalidPokemonData = builder.comment("The message to send to players when the database has invalid Pokemon data.")
                .define("invalidPokemonData", "§cInvalid Pokemon entry. Check server logs.");
        builder.pop(); // Closes "server.messages.databaseMessages"

        builder.comment("Messages - Deposit Messages").push("depositMessages");
        messageDepositPokemonMissing = builder.comment("The message to send to players when they try to deposit a Pokemon that is missing.")
                .define("pokemonMissing", "§cPokemon is no longer in your PC");
        messageDepositFailedToSave = builder.comment("The message to send to players when the database fails to save the deposited Pokemon.")
                .define("failedToSave", "§cFailed to save Pokemon to bank. Please try again later.");
        messageDepositBankFull = builder.comment("The message to send to players when they try to deposit a Pokemon but the bank is full.")
                .define("bankFull", "§cYour bank is full. Please remove a Pokemon before depositing another.");
        messageDepositPcRemovalFailed = builder.comment("The message to send to players when the database fails to remove the deposited Pokemon from the PC.")
                .define("pcRemovalFailed", "§cFailed to remove Pokemon from PC. Please try again later.");
        messageDepositPokemonDeposited = builder.comment("The message to send to players when they successfully deposit a Pokemon.")
                .define("pokemonDeposited", "§aPokemon has been moved to your Bank.");
        builder.pop(); // Closes "server.messages.depositMessages"

        builder.comment("Messages - Withdraw Messages").push("withdrawMessages");
        messageWithdrawPokemonMissing = builder.comment("The message to send to players when they try to withdraw a Pokemon that is missing.")
                .define("pokemonMissing", "§cPokemon is no longer in your Bank.");
        messageWithdrawFailedToReadData = builder.comment("The message to send to players when the database fails to read the withdrawn Pokemon's data.")
                .define("failedToReadData", "§cFailed to read Pokemon data from bank. Please try again later.");
        messageWithdrawFailedToRemoveFromBank = builder.comment("The message to send to players when the database fails to remove the withdrawn Pokemon from the bank.")
                .define("failedToRemoveFromBank", "§cFailed to remove Pokemon from bank. Please try again later.");
        messageWithdrawPcFull = builder.comment("The message to send to players when they try to withdraw a Pokemon but their PC is full.")
                .define("pcFull", "§cYour PC is full. Please remove a Pokemon before withdrawing another.");
        messageWithdrawPokemonDeposited = builder.comment("The message to send to players when they successfully withdraw a Pokemon.")
                .define("pokemonDeposited", "§aPokemon has been moved to your PC.");
        builder.pop(); // Closes "server.messages.withdrawMessages"

        builder.comment("Messages - Validation Messages").push("validationMessages");
        messageValidationNoHeldItems = builder.comment("The message to send to players when they try to deposit a Pokemon with a held item when held items are not allowed.")
                .define("noHeldItems", "§cThis Pokemon is holding an item, which is not allowed in the %s.");
        messageValidationOfficialHeldItemsOnly = builder.comment("The message to send to players when they try to deposit a Pokemon with a held item that is not officially tagged when only officially tagged held items are allowed.")
                .define("officialHeldItemsOnly", "§cThis Pokemon is holding an item that is not officially tagged, which is not allowed in the %s.");
        messageValidationBlacklistedHeldItem = builder.comment("The message to send to players when they try to deposit a Pokemon with a held item that is blacklisted.")
                .define("blacklistedHeldItem", "§cThis Pokemon is holding a blacklisted item, which is not allowed in the %s.");
        messageValidationNoLegendaries = builder.comment("The message to send to players when they try to deposit a Legendary Pokemon when Legendary Pokemon are not allowed.")
                .define("noLegendaries", "§cThis Pokemon is a legendary, which is not allowed in the %s.");
        messageValidationNoMythicals = builder.comment("The message to send to players when they try to deposit a Mythical Pokemon when Mythical Pokemon are not allowed.")
                .define("noMythicals", "§cThis Pokemon is a mythical, which is not allowed in the %s.");
        messageValidationNoUltraBeasts = builder.comment("The message to send to players when they try to deposit an Ultra Beast Pokemon when Ultra Beast Pokemon are not allowed.")
                .define("noUltraBeasts", "§cThis Pokemon is an Ultra Beast, which is not allowed in the %s.");
        messageValidationNoFainted = builder.comment("The message to send to players when they try to deposit a fainted Pokemon when fainted Pokemon are not allowed.")
                .define("noFainted", "§cThis Pokemon is fainted, which is not allowed in the %s.");
        messageValidationAutoStrippedHeldItem = builder.comment("The message to send to players when they try to deposit a Pokemon with a held item that is not allowed and the held item is automatically stripped.")
                .define("autoStrippedHeldItem", "§eYour Pokemon's held item has been removed as it is not allowed in the %s.");
        builder.pop(); // Closes "server.messages.validationMessages"

        builder.pop(); // Closes "server.messages"

        builder.pop(); // Closes "server"
    }
}
