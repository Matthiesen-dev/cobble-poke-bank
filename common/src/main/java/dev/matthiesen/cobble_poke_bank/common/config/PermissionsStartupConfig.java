package dev.matthiesen.cobble_poke_bank.common.config;

import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PermissionsStartupConfig {

    public ModConfigSpec.IntValue command_pokebank;
    public ModConfigSpec.IntValue command_pokebank_status;
    public ModConfigSpec.IntValue command_pokebank_reload;

    public PermissionsStartupConfig(ModConfigSpec.Builder builder) {
        builder.comment("Permissions Configuration").push("permissions");
        builder.comment("Command Permissions").push("command");

        command_pokebank = builder.comment("Permission level required to use the '/pokebank' command", "Permission Node: 'cobble_poke_bank.command.pokebank'")
                .defineInRange("pokebank", PermissionLevel.NONE.getLevel(), 0, 4);
        command_pokebank_status = builder.comment("Permission level required to use the '/pokebank status' command", "Permission Node: 'cobble_poke_bank.command.pokebank.status'")
                .defineInRange("pokebank_status", PermissionLevel.ALL_COMMANDS.getLevel(), 0, 4);
        command_pokebank_reload = builder.comment("Permission level required to use the '/pokebank reload' command", "Permission Node: 'cobble_poke_bank.command.pokebank.reload'")
                .defineInRange("pokebank_reload", PermissionLevel.ALL_COMMANDS.getLevel(), 0, 4);

        builder.pop(); // Closes "permissions.command"
        builder.pop(); // Closes "permissions"
    }
}
