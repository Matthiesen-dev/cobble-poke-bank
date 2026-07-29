package dev.matthiesen.cobble_poke_bank.common.config;

import com.google.gson.annotations.SerializedName;
import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

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

        /**
         * If true, players will be allowed to store held items in the bank. If false, held items will prevent the Pokemon from being stored in the bank.
         */
        @SerializedName("allowHeldItems")
        public boolean allowHeldItems = true;

        /**
         * If true, only official held items (Available via tag) will be allowed in the bank. This is to prevent players from storing custom
         * items that may not be compatible with the mod or other connected servers.
         */
        @SerializedName("restrictHeldItemsToOfficialOnly")
        public boolean restrictHeldItemsToOfficialOnly = false;

        /**
         * If true, players will be allowed to store Pokemon with held items that are on the blacklist. If false, Pokemon
         * with blacklisted held items will not be allowed to be stored in the bank.
         */
        @SerializedName("heldItemBlacklist")
        public List<String> heldItemBlacklist = List.of();
    }

    public static class PermissionLevels {
        @SerializedName("command.pokebank")
        public int COMMAND_POKEBANK_PERMISSION_LEVEL = PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS.getLevel();
    }

    /**
     * Parses a list of item strings into a list of Item objects. If an item string is invalid, it will be replaced with the
     * barrier item.
     * @param configList List of item strings to parse.
     * @return List of Item objects corresponding to the item strings. Invalid items will be replaced with the barrier item.
     */
    public static List<Item> parseHeldItemBlacklist(List<String> configList) {
        List<Item> items = new ArrayList<>();

        for (String itemString : configList) {
            Item item = ItemDecoder.stringToItem(itemString, Items.BARRIER);
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }
}
