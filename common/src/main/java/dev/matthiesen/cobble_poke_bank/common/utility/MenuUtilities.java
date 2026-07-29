package dev.matthiesen.cobble_poke_bank.common.utility;

import com.cobblemon.mod.common.CobblemonItems;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MenuUtilities {
    public static final Item FRAME = Items.GRAY_STAINED_GLASS_PANE;
    public static final Item PC = CobblemonItems.PC;
    public static final Item BANK = Items.ENDER_CHEST;
    public static final Item INFO = Items.PAPER;
    public static final Item NAV = Items.ARROW;
    public static final Item BACK = Items.BARRIER;
    public static final Item CONFIRM = Items.LIME_DYE;
    public static final Item CANCEL = Items.RED_DYE;
    public static final Item INVALID = Items.BARRIER;

    private MenuUtilities() {}

    private static ItemStack builder(Item item, Component name) {
        return new ItemBuilder(item)
                .hideAdditional()
                .setCustomName(name)
                .build();
    }

    public static ItemStack getFrameItem() {
        return builder(FRAME, Component.literal(" "));
    }

    public static ItemStack getPcMenuItem() {
        return builder(PC, Component.literal("Open PC").withStyle(ChatFormatting.AQUA));
    }

    public static ItemStack getBankMenuItem() {
        return builder(BANK, Component.literal("Open Bank").withStyle(ChatFormatting.GOLD));
    }

    public static ItemStack getBackItem() {
        return builder(BACK, Component.literal("Back").withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getPrevItem() {
        return builder(NAV, Component.literal("Previous").withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getNextItem() {
        return builder(NAV, Component.literal("Next").withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getConfirmItem() {
        return builder(CONFIRM, Component.literal("Confirm").withStyle(ChatFormatting.GREEN));
    }

    public static ItemStack getCancelItem() {
        return builder(CANCEL, Component.literal("Cancel").withStyle(ChatFormatting.RED));
    }

    public static ItemStack getInfoItem(String label) {
        return builder(INFO, Component.literal(label).withStyle(ChatFormatting.YELLOW));
    }

    public static ItemStack getInvalidEntryItem() {
        return builder(INVALID, Component.literal("Invalid Pokemon Data").withStyle(ChatFormatting.DARK_RED));
    }
}
