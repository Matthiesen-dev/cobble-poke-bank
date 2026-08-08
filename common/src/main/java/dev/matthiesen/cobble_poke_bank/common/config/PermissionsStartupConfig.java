package dev.matthiesen.cobble_poke_bank.common.config;

import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PermissionsStartupConfig {

    public ModConfigSpec.EnumValue<PermissionLevel> command_pokebank;
    public ModConfigSpec.EnumValue<PermissionLevel> command_pokebank_status;
    public ModConfigSpec.EnumValue<PermissionLevel> command_pokebank_reload;

    public PermissionsStartupConfig(ModConfigSpec.Builder builder) {
        builder.comment("Permissions Configuration")
                .translation("cobble_poke_bank.configuration.permissions")
                .push("permissions");
        builder.comment("Command Permissions")
                .translation("cobble_poke_bank.configuration.permissions.command")
                .push("command");

        command_pokebank = builder.comment("Permission level required to use the '/pokebank' command", "Permission Node: 'cobble_poke_bank.command.pokebank'")
                .translation("cobble_poke_bank.configuration.permissions.command.pokebank")
                .defineEnum("pokebank", PermissionLevel.NONE);
        command_pokebank_status = builder.comment("Permission level required to use the '/pokebank status' command", "Permission Node: 'cobble_poke_bank.command.pokebank.status'")
                .translation("cobble_poke_bank.configuration.permissions.command.pokebank_status")
                .defineEnum("pokebank_status", PermissionLevel.ALL_COMMANDS);
        command_pokebank_reload = builder.comment("Permission level required to use the '/pokebank reload' command", "Permission Node: 'cobble_poke_bank.command.pokebank.reload'")
                .translation("cobble_poke_bank.configuration.permissions.command.pokebank_reload")
                .defineEnum("pokebank_reload", PermissionLevel.ALL_COMMANDS);

        builder.pop(); // Closes "permissions.command"
        builder.pop(); // Closes "permissions"
    }
}
