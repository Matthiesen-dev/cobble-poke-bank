package dev.matthiesen.cobble_poke_bank.common.utility;

import com.cobblemon.mod.common.CobblemonItems;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemBuilder;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MenuUtilities {

    private MenuUtilities() {}

    public static Item getFrame() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiFrameItemId.get(), Items.GRAY_STAINED_GLASS_PANE);
    }

    public static Item getPC() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiPcItemId.get(), CobblemonItems.PC);
    }

    public static Item getBank() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiBankItemId.get(), Items.ENDER_CHEST);
    }

    public static Item getInfo() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiInfoItemId.get(), Items.PAPER);
    }

    public static Item getNAV_PREV() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiNavPrevItemId.get(), Items.ARROW);
    }

    public static Item getNAV_NEXT() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiNavNextItemId.get(), Items.ARROW);
    }

    public static Item getBack() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiBackItemId.get(), Items.OAK_SIGN);
    }

    public static Item getConfirm() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiConfirmItemId.get(), Items.LIME_DYE);
    }

    public static Item getCancel() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiCancelItemId.get(), Items.RED_DYE);
    }

    public static Item getInvalid() {
        return ItemDecoder.stringToItem(PokeBankConfig.SERVER_CONFIG.guiInvalidItemId.get(), Items.BARRIER);
    }

    private static ItemStack builder(Item item, Component name) {
        return new ItemBuilder(item)
                .hideAdditional()
                .setCustomName(name)
                .build();
    }

    public static ItemStack getFrameItem() {
        return builder(getFrame(), Component.literal(" "));
    }

    public static ItemStack getPcMenuItem() {
        return builder(getPC(), Component.literal("Open PC").withStyle(ChatFormatting.AQUA));
    }

    public static ItemStack getBankMenuItem() {
        return builder(getBank(), Component.literal("Open Bank").withStyle(ChatFormatting.GOLD));
    }

    public static ItemStack getBackItem() {
        return builder(getBack(), Component.literal("Back").withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getPrevItem() {
        return builder(getNAV_PREV(), Component.literal("Previous").withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getNextItem() {
        return builder(getNAV_NEXT(), Component.literal("Next").withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getConfirmItem() {
        return builder(getConfirm(), Component.literal("Confirm").withStyle(ChatFormatting.GREEN));
    }

    public static ItemStack getCancelItem() {
        return builder(getCancel(), Component.literal("Cancel").withStyle(ChatFormatting.RED));
    }

    public static ItemStack getInfoItem(String label) {
        return builder(getInfo(), Component.literal(label).withStyle(ChatFormatting.YELLOW));
    }

    public static ItemStack getInvalidEntryItem() {
        return builder(getInvalid(), Component.literal("Invalid Pokemon Data").withStyle(ChatFormatting.DARK_RED));
    }
}
