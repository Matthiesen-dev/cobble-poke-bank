package dev.matthiesen.cobble_poke_bank.common.utility;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.google.gson.JsonObject;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class PokemonUtility {

    private PokemonUtility() {}

    public static JsonObject pokemonToJson(Pokemon pokemon, RegistryAccess registryAccess) {
        return pokemon.saveToJSON(registryAccess, new JsonObject());
    }

    public static Pokemon pokemonFromJson(JsonObject json, RegistryAccess registryAccess) {
        Pokemon pokemon = new Pokemon();
        return pokemon.loadFromJSON(registryAccess, json);
    }

    public static ItemStack pokemonToItem(Pokemon pokemon) {
        return new ItemBuilder(PokemonItem.from(pokemon, 1))
                .hideAdditional()
                .addLore(loreBuilder(pokemon))
                .setCustomName(customNameBuilder(pokemon))
                .build();
    }

    private static MutableComponent customNameBuilder(Pokemon pokemon) {
        return pokemon.getShiny() ?
                pokemon.getSpecies().getTranslatedName().copy().withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" ★").withStyle(ChatFormatting.GOLD)) :
                pokemon.getSpecies().getTranslatedName().copy().withStyle(ChatFormatting.GRAY);
    }

    private static String parseShowdownGender(Gender gender) {
        return switch (gender) {
            case MALE -> "♂";
            case FEMALE -> "♀";
            case GENDERLESS -> "⚲";
        };
    }

    private static ChatFormatting getGenderColor(Gender gender) {
        return switch (gender) {
            case MALE -> ChatFormatting.BLUE;
            case FEMALE -> ChatFormatting.LIGHT_PURPLE;
            case GENDERLESS -> ChatFormatting.GRAY;
        };
    }

    private static Component[] loreBuilder(Pokemon pokemon) {
        String moveOne = !pokemon.getMoveSet().getMoves().isEmpty() ?
                Objects.requireNonNull(pokemon.getMoveSet().get(0)).getDisplayName().getString() : "Empty";
        String moveTwo = pokemon.getMoveSet().getMoves().size() >= 2 ?
                Objects.requireNonNull(pokemon.getMoveSet().get(1)).getDisplayName().getString() : "Empty";
        String moveThree = pokemon.getMoveSet().getMoves().size() >= 3 ?
                Objects.requireNonNull(pokemon.getMoveSet().get(2)).getDisplayName().getString() : "Empty";
        String moveFour = pokemon.getMoveSet().getMoves().size() >= 4 ?
                Objects.requireNonNull(pokemon.getMoveSet().get(3)).getDisplayName().getString() : "Empty";

        return new Component[]{
                Component.literal(pokemon.getCaughtBall().item().getDefaultInstance().getDisplayName().getString())
                        .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_GRAY)),
                Component.literal("Level: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.valueOf(pokemon.getLevel())).withStyle(ChatFormatting.WHITE)),
                Component.literal("Nickname: ").withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(
                        pokemon.getNickname() != null ? pokemon.getNickname().getString() : "No nickname"
                ).withStyle(ChatFormatting.WHITE)),
                Component.literal("Held Item: ").withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal(
                        pokemon.heldItem().isEmpty() ? "No held item" : pokemon.heldItem().getDisplayName().getString()
                ).withStyle(ChatFormatting.WHITE)),
                Component.literal("Gender: ").withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(parseShowdownGender(pokemon.getGender())
                ).withStyle(getGenderColor(pokemon.getGender()))),
                Component.literal("Nature: ").withStyle(ChatFormatting.YELLOW)
                        .append(LocalizationUtilsKt.lang(pokemon.getNature().getDisplayName().replace("cobblemon.", ""))
                        .withStyle(ChatFormatting.WHITE)),
                Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                        .append(LocalizationUtilsKt.lang(pokemon.getAbility().getDisplayName().replace("cobblemon.", ""))
                        .withStyle(ChatFormatting.WHITE)),
                Component.literal("IVs: ").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal("  HP: ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.HP)))
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("  Atk: ").withStyle(ChatFormatting.BLUE)
                                .append(Component.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.ATTACK)))
                                        .withStyle(ChatFormatting.WHITE)))
                        .append(Component.literal("  Def: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.DEFENCE)))
                                .withStyle(ChatFormatting.WHITE))),
                Component.literal("  SpAtk: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.SPECIAL_ATTACK)))
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("  SpDef: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE)))
                                        .withStyle(ChatFormatting.WHITE)))
                        .append(Component.literal("  Spd: ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.SPEED)))
                                .withStyle(ChatFormatting.WHITE))),
                Component.literal("EVs: ").withStyle(ChatFormatting.DARK_AQUA),
                Component.literal("  HP: ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(String.valueOf(pokemon.getEvs().getOrDefault(Stats.HP)))
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("  Atk: ").withStyle(ChatFormatting.BLUE)
                                .append(Component.literal(String.valueOf(pokemon.getEvs().getOrDefault(Stats.ATTACK)))
                                        .withStyle(ChatFormatting.WHITE)))
                        .append(Component.literal("  Def: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(pokemon.getEvs().getOrDefault(Stats.DEFENCE)))
                                .withStyle(ChatFormatting.WHITE))),
                Component.literal("  SpAtk: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.valueOf(pokemon.getEvs().getOrDefault(Stats.SPECIAL_ATTACK)))
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("  SpDef: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(String.valueOf(pokemon.getEvs().getOrDefault(Stats.SPECIAL_DEFENCE)))
                                        .withStyle(ChatFormatting.WHITE)))
                        .append(Component.literal("  Spd: ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(String.valueOf(pokemon.getEvs().getOrDefault(Stats.SPEED)))
                                .withStyle(ChatFormatting.WHITE))),
                Component.literal("Moves: ").withStyle(ChatFormatting.DARK_GREEN),
                Component.literal(" ").append(Component.literal(moveOne).withStyle(ChatFormatting.WHITE)),
                Component.literal(" ").append(Component.literal(moveTwo).withStyle(ChatFormatting.WHITE)),
                Component.literal(" ").append(Component.literal(moveThree).withStyle(ChatFormatting.WHITE)),
                Component.literal(" ").append(Component.literal(moveFour).withStyle(ChatFormatting.WHITE)),
                Component.literal("Form: ").withStyle(ChatFormatting.GOLD).append(pokemon.getForm().getName())
        };
    }
}
