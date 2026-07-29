package dev.matthiesen.cobble_poke_bank.common.config;

import com.google.gson.annotations.SerializedName;
import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;

public final class MainConfig {
    @SerializedName("bank")
    public Bank bank = new Bank();

    @SerializedName("permissionlevels")
    public PermissionLevels permissionLevels = new PermissionLevels();

    public static class Bank {
        /**
         * Maximum number of Pokemon that can be stored in the bank.
         * Values <= 0 mean unlimited storage.
         */
        @SerializedName("maxSlots")
        public int maxSlots = -1;
    }

    public static class PermissionLevels {
        @SerializedName("command.pokebank")
        public int COMMAND_POKEBANK_PERMISSION_LEVEL = PermissionLevel.NONE.getLevel();
    }
}
