package dev.matthiesen.cobble_poke_bank.common.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.menu.MainMenuScreen;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PokeBankCommand implements CoreCommand {
    public static final PokeBankCommand CMD = new PokeBankCommand();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection context) {
        var permissions = CobblePokeBankCommon.INSTANCE.getPermissions();
        dispatcher.register(
                Commands.literal("pokebank")
                        .requires(src -> CobblePokeBankCommon.INSTANCE.checkPermission(src, permissions.POKEBANK_PERMISSION))
                        .executes(this::action)
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
}
