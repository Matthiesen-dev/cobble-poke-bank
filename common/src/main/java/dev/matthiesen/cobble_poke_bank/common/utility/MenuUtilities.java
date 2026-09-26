package dev.matthiesen.cobble_poke_bank.common.utility;

import com.cobblemon.mod.common.CobblemonItems;
import dev.matthiesen.cobble_poke_bank.common.config.PokeBankConfig;
import dev.matthiesen.cobble_poke_bank.common.config.ServerConfig;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemBuilder;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MenuUtilities {

    private MenuUtilities() {}

    private static ServerConfig getServerConfig() {
        return PokeBankConfig.SERVER_CONFIG;
    }

    public static Item getFrame() {
        return ItemDecoder.stringToItem(getServerConfig().guiFrameItemId.get(), Items.GRAY_STAINED_GLASS_PANE);
    }

    public static Item getPC() {
        return ItemDecoder.stringToItem(getServerConfig().guiPcItemId.get(), CobblemonItems.PC);
    }

    public static Item getBank() {
        return ItemDecoder.stringToItem(getServerConfig().guiBankItemId.get(), Items.ENDER_CHEST);
    }

    public static Item getInfo() {
        return ItemDecoder.stringToItem(getServerConfig().guiInfoItemId.get(), Items.PAPER);
    }

    public static Item getNAV_PREV() {
        return ItemDecoder.stringToItem(getServerConfig().guiNavPrevItemId.get(), Items.ARROW);
    }

    public static Item getNAV_NEXT() {
        return ItemDecoder.stringToItem(getServerConfig().guiNavNextItemId.get(), Items.ARROW);
    }

    public static Item getBack() {
        return ItemDecoder.stringToItem(getServerConfig().guiBackItemId.get(), Items.OAK_SIGN);
    }

    public static Item getConfirm() {
        return ItemDecoder.stringToItem(getServerConfig().guiConfirmItemId.get(), Items.LIME_DYE);
    }

    public static Item getCancel() {
        return ItemDecoder.stringToItem(getServerConfig().guiCancelItemId.get(), Items.RED_DYE);
    }

    public static Item getInvalid() {
        return ItemDecoder.stringToItem(getServerConfig().guiInvalidItemId.get(), Items.BARRIER);
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
        return builder(getPC(), Component.literal(getServerConfig().guiText_buttonOpenPC.get()).withStyle(ChatFormatting.AQUA));
    }

    public static ItemStack getBankMenuItem() {
        return builder(getBank(), Component.literal(getServerConfig().guiText_buttonOpenBank.get()).withStyle(ChatFormatting.GOLD));
    }

    public static ItemStack getBackItem() {
        return builder(getBack(), Component.literal(getServerConfig().guiText_buttonBack.get()).withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getPrevItem() {
        return builder(getNAV_PREV(), Component.literal(getServerConfig().guiText_buttonPreviousPage.get()).withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getNextItem() {
        return builder(getNAV_NEXT(), Component.literal(getServerConfig().guiText_buttonNextPage.get()).withStyle(ChatFormatting.BLUE));
    }

    public static ItemStack getConfirmItem() {
        return builder(getConfirm(), Component.literal(getServerConfig().guiText_buttonConfirm.get()).withStyle(ChatFormatting.GREEN));
    }

    public static ItemStack getCancelItem() {
        return builder(getCancel(), Component.literal(getServerConfig().guiText_buttonCancel.get()).withStyle(ChatFormatting.RED));
    }

    public static ItemStack getInfoItem(String label) {
        return builder(getInfo(), Component.literal(label).withStyle(ChatFormatting.YELLOW));
    }

    public static ItemStack getInvalidEntryItem() {
        return builder(getInvalid(), Component.literal(getServerConfig().guiText_buttonInvalidEntry.get()).withStyle(ChatFormatting.DARK_RED));
    }
}
