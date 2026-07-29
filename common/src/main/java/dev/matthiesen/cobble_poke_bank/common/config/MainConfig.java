package dev.matthiesen.cobble_poke_bank.common.config;

import com.google.gson.annotations.SerializedName;
import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class MainConfig {

    /**
     * Defines the configuration options for the Pokemon bank. This class contains settings that determine the maximum number
     * of Pokemon that can be stored in the bank, as well as restrictions on what types of Pokemon and held items are allowed to be stored.
     */
    @SerializedName("bank")
    public Bank bank = new Bank();

    /**
     * Defines the permission levels required to use specific commands in the mod. Each command has an associated permission
     * level that determines who can execute it.
     */
    @SerializedName("permissionLevels")
    public PermissionLevels permissionLevels = new PermissionLevels();

    /**
     * Defines the configuration options for the Pokemon bank. This class contains settings that determine the maximum number
     * of Pokemon that can be stored in the bank, as well as restrictions on what types of Pokemon and held items are allowed to be stored.
     */
    public static class Bank {
        /**
         * Maximum number of Pokemon that can be stored in the bank.
         * Values <= 0 mean unlimited storage.
         */
        @SerializedName("maxSlots")
        public int maxSlots = -1;

        /**
         * If true, players will not be allowed to store Pokemon that have fainted in the bank. If false, fainted Pokemon
         * will be allowed to be stored in the bank.
         */
        @SerializedName("noFainted")
        public boolean noFainted = false;

        /**
         * If true, players will not be allowed to store Pokemon with held items in the bank. If false, Pokemon with held items
         * will be allowed to be stored in the bank.
         */
        @SerializedName("noHeldItems")
        public boolean noHeldItems = false;

        /**
         * If true, players will not be allowed to store Legendary Pokemon in the bank. If false, Legendary Pokemon will be
         * allowed to be stored in the bank.
         */
        @SerializedName("noLegendaries")
        public boolean noLegendaries = false;

        /**
         * If true, players will not be allowed to store Mythical Pokemon in the bank. If false, Mythical Pokemon will be
         * allowed to be stored in the bank.
         */
        @SerializedName("noMythicals")
        public boolean noMythicals = false;

        /**
         * If true, players will not be allowed to store Ultra Beasts in the bank. If false, Ultra Beasts will be allowed
         * to be stored in the bank.
         */
        @SerializedName("noUltraBeasts")
        public boolean noUltraBeasts = false;

        /**
         * Contains restrictions on what held items are allowed to be stored in the bank.
         */
        @SerializedName("heldItemRestrictions")
        public HeldItemRestrictions heldItemRestrictions = new HeldItemRestrictions();
    }

    /**
     * Defines restrictions on what held items are allowed to be stored in the bank. This class contains settings that determine
     * whether only official held items are allowed and a blacklist of specific held items that are not permitted.
     */
    public static class HeldItemRestrictions {
        /**
         * If true, only official held items (Available via tag) will be allowed in the bank. This is to prevent players from storing custom
         * items that may not be compatible with the mod or other connected servers.
         */
        @SerializedName("officialTaggedOnly")
        public boolean officialTaggedOnly = false;

        /**
         * If true, players will be allowed to store Pokemon with held items that are on the blacklist. If false, Pokemon
         * with blacklisted held items will not be allowed to be stored in the bank.
         */
        @SerializedName("blacklist")
        public List<String> blacklist = List.of();
    }

    /**
     * Defines the permission levels required to use specific commands in the mod. Each command has an associated permission level
     * that determines who can execute it.
     */
    public static class PermissionLevels {

        /**
         * The permission level required to use the /pokebank command. This value is an integer that corresponds to a specific
         * permission level defined in the PermissionLevel enum.
         */
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
