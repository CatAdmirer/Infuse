package com.catadmirer.infuseSMP.util;

import java.util.concurrent.CompletableFuture;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

public class InfuseEffectArgumentType implements CustomArgumentType<InfuseEffect,String> {
    private static final DynamicCommandExceptionType ERROR_NO_EFFECT_FOUND = new DynamicCommandExceptionType(s -> new LiteralMessage("\"" + s + "\" is not a registered effect key."));

    @Override
    public InfuseEffect parse(StringReader reader) throws CommandSyntaxException {
        String key = reader.readUnquotedString();

        InfuseEffect effect = InfuseEffect.fromString(key);

        if (effect == null) throw ERROR_NO_EFFECT_FOUND.create(key);

        return effect;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        InfuseEffect.getRegisteredEffects().forEach((i, e) -> {
            builder.suggest(e.getRegularVersion().getKey());
            builder.suggest(e.getAugmentedVersion().getKey());
        });

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}