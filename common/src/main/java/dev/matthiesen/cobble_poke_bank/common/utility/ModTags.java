package dev.matthiesen.cobble_poke_bank.common.utility;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> COBBLEMON_HELD_ITEMS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("cobblemon", "held/is_held_item"));
}
