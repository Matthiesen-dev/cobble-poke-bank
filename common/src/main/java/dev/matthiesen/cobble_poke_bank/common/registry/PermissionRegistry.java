package dev.matthiesen.cobble_poke_bank.common.registry;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import dev.matthiesen.matthiesen_core.common.utility.AbstractPermission;
import net.minecraft.commands.CommandSourceStack;

public final class PermissionRegistry {
    public static Permission POKEBANK_PERMISSION = register(
            "command.pokebank",
            CobblePokeBankCommon.INSTANCE.getConfig().permissionLevels.COMMAND_POKEBANK_PERMISSION_LEVEL
    );

    public static class Permissions {
        public Permission POKEBANK_PERMISSION = PermissionRegistry.POKEBANK_PERMISSION;
    }

    public static Permissions PERMISSIONS;

    public static Permissions getPermissions() {
        if (PERMISSIONS == null) {
            PERMISSIONS = new Permissions();
        }
        return PERMISSIONS;
    }

    public static void init() {}

    public static boolean checkPermission(CommandSourceStack source, Permission permission) {
        return CobblePokeBankCommon.INSTANCE.getPermissionsManager().getPermissionValidator().hasPermission(source, permission);
    }

    @SuppressWarnings("SameParameterValue")
    private static Permission register(String node, int level) {
        var newPermission = modPermission(node, toPermLevel(level));
        CobblePokeBankCommon.INSTANCE.getPermissionsManager().registerPermission(newPermission);
        return newPermission;
    }

    private static Permission modPermission(String node, PermissionLevel level) {
        return new AbstractPermission(node, level) {
            @Override
            protected String getModId() {
                return CobblePokeBankCommon.MOD_ID;
            }

            @Override
            protected String getPermissionNamespace() {
                return "CobblePokeBank";
            }
        };
    }

    private static PermissionLevel toPermLevel(int permLevel) {
        for (PermissionLevel value : PermissionLevel.values()) {
            if (value.ordinal() == permLevel) {
                return value;
            }
        }
        return PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS;
    }
}
