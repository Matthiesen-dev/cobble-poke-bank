package dev.matthiesen.cobble_poke_bank.common.config;

import com.google.gson.annotations.SerializedName;
import dev.matthiesen.matthiesen_core.common.api.text_parsers.BuiltInTextParsers;

public final class MessagesConfig {

    @SerializedName("textParser")
    public String textParser = BuiltInTextParsers.VANILLA.getId();

    @SerializedName("prefix")
    public String prefix = "§f[§6Cobble Poke Bank§f] §r";

    @SerializedName("locationLabels")
    public LocationLabels locationLabels = new LocationLabels();

    @SerializedName("commandMessages")
    public CommandMessages commandMessages = new CommandMessages();

    @SerializedName("databaseMessages")
    public DatabaseMessages databaseMessages = new DatabaseMessages();

    @SerializedName("depositMessages")
    public DepositMessages depositMessages = new DepositMessages();

    @SerializedName("withdrawMessages")
    public WithdrawMessages withdrawMessages = new WithdrawMessages();

    @SerializedName("validationMessages")
    public ValidationMessages validationMessages = new ValidationMessages();

    public static class LocationLabels {

        @SerializedName("deposit")
        public String deposit = "Bank";

        @SerializedName("withdraw")
        public String withdraw = "Server";
    }

    public static class CommandMessages {

        @SerializedName("databaseUnavailable")
        public String databaseUnavailable = "§cDatabase is not available. Please try again later.";

        @SerializedName("playerNotFound")
        public String playerNotFound = "§cFailed to find executing player.";

        @SerializedName("noBlacklistedItems")
        public String noBlacklistedItems = "§eNo held items are blacklisted.";

        @SerializedName("inBattle")
        public String inBattle = "§cYou cannot access the bank while in battle.";

        @SerializedName("configsReloaded")
        public String configsReloaded = "§aConfigs reloaded successfully.";
    }

    public static class DatabaseMessages {

        @SerializedName("loadingData")
        public String loadingData = "§eLoading bank data...";

        @SerializedName("failedToLoadData")
        public String failedToLoadData = "§cFailed to load bank data. Please try again later.";

        @SerializedName("invalidPokemonData")
        public String invalidPokemonData = "§cInvalid Pokemon entry. Check server logs.";
    }

    public static class DepositMessages {

        @SerializedName("pokemonMissing")
        public String pokemonMissing = "§cPokemon is no longer in your PC";

        @SerializedName("failedToSave")
        public String failedToSave = "§cFailed to save Pokemon to bank. Please try again later.";

        @SerializedName("bankFull")
        public String bankFull = "§cYour bank is full. Please remove a Pokemon before depositing another.";

        @SerializedName("pcRemovalFailed")
        public String pcRemovalFailed = "§cFailed to remove Pokemon from PC. Please try again later.";

        @SerializedName("pokemonDeposited")
        public String pokemonDeposited = "§aPokemon has been moved to your Bank.";
    }

    public static class WithdrawMessages {

        @SerializedName("pokemonMissing")
        public String pokemonMissing = "§cPokemon is no longer in your Bank.";

        @SerializedName("failedToReadData")
        public String failedToReadData = "§cFailed to read Pokemon data from bank. Please try again later.";

        @SerializedName("failedToRemoveFromBank")
        public String failedToRemoveFromBank = "§cFailed to remove Pokemon from bank. Please try again later.";

        @SerializedName("pcFull")
        public String pcFull = "§cYour PC is full. Please remove a Pokemon before withdrawing another.";

        @SerializedName("pokemonDeposited")
        public String pokemonDeposited = "§aPokemon has been moved to your PC.";
    }

    public static class ValidationMessages {

        @SerializedName("noHeldItems")
        public String noHeldItems = "§cThis Pokemon is holding an item, which is not allowed in the %s.";

        @SerializedName("officialHeldItemsOnly")
        public String officialHeldItemsOnly = "§cThis Pokemon is holding an item that is not officially tagged, which is not allowed in the %s.";

        @SerializedName("blacklistedHeldItem")
        public String blacklistedHeldItem = "§cThis Pokemon is holding a blacklisted item, which is not allowed in the %s.";

        @SerializedName("noLegendaries")
        public String noLegendaries = "§cThis Pokemon is a legendary, which is not allowed in the %s.";

        @SerializedName("noMythicals")
        public String noMythicals = "§cThis Pokemon is a mythical, which is not allowed in the %s.";

        @SerializedName("noUltraBeasts")
        public String noUltraBeasts = "§cThis Pokemon is an ultra beast, which is not allowed in the %s.";

        @SerializedName("noFainted")
        public String noFainted = "§cThis Pokemon is fainted, which is not allowed in the %s.";

        @SerializedName("autoStrippedHeldItem")
        public String autoStrippedHeldItem = "§eYour Pokemon's held item has been removed as it is not allowed in the %s.";
    }
}
