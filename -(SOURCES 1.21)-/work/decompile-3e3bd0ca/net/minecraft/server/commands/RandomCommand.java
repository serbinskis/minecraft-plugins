package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentCriterionValue;
import net.minecraft.commands.arguments.ArgumentMinecraftKeyRegistered;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequences;

public class RandomCommand {

    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_LARGE = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.random.error.range_too_large"));
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_SMALL = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.random.error.range_too_small"));

    public RandomCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("random").then(drawRandomValueTree("value", false))).then(drawRandomValueTree("roll", true))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("reset").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("*").executes((commandcontext) -> {
            return resetAllSequences((CommandListenerWrapper) commandcontext.getSource());
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("seed", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return resetAllSequencesAndSetNewDefaults((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "seed"), true, true);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("includeWorldSeed", BoolArgumentType.bool()).executes((commandcontext) -> {
            return resetAllSequencesAndSetNewDefaults((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "seed"), BoolArgumentType.getBool(commandcontext, "includeWorldSeed"), true);
        })).then(net.minecraft.commands.CommandDispatcher.argument("includeSequenceId", BoolArgumentType.bool()).executes((commandcontext) -> {
            return resetAllSequencesAndSetNewDefaults((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "seed"), BoolArgumentType.getBool(commandcontext, "includeWorldSeed"), BoolArgumentType.getBool(commandcontext, "includeSequenceId"));
        })))))).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sequence", ArgumentMinecraftKeyRegistered.id()).suggests(RandomCommand::suggestRandomSequence).executes((commandcontext) -> {
            return resetSequence((CommandListenerWrapper) commandcontext.getSource(), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sequence"));
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("seed", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return resetSequence((CommandListenerWrapper) commandcontext.getSource(), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sequence"), IntegerArgumentType.getInteger(commandcontext, "seed"), true, true);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("includeWorldSeed", BoolArgumentType.bool()).executes((commandcontext) -> {
            return resetSequence((CommandListenerWrapper) commandcontext.getSource(), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sequence"), IntegerArgumentType.getInteger(commandcontext, "seed"), BoolArgumentType.getBool(commandcontext, "includeWorldSeed"), true);
        })).then(net.minecraft.commands.CommandDispatcher.argument("includeSequenceId", BoolArgumentType.bool()).executes((commandcontext) -> {
            return resetSequence((CommandListenerWrapper) commandcontext.getSource(), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sequence"), IntegerArgumentType.getInteger(commandcontext, "seed"), BoolArgumentType.getBool(commandcontext, "includeWorldSeed"), BoolArgumentType.getBool(commandcontext, "includeSequenceId"));
        })))))));
    }

    private static LiteralArgumentBuilder<CommandListenerWrapper> drawRandomValueTree(String s, boolean flag) {
        return (LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal(s).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("range", ArgumentCriterionValue.intRange()).executes((commandcontext) -> {
            return randomSample((CommandListenerWrapper) commandcontext.getSource(), ArgumentCriterionValue.b.getRange(commandcontext, "range"), (MinecraftKey) null, flag);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("sequence", ArgumentMinecraftKeyRegistered.id()).suggests(RandomCommand::suggestRandomSequence).requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).executes((commandcontext) -> {
            return randomSample((CommandListenerWrapper) commandcontext.getSource(), ArgumentCriterionValue.b.getRange(commandcontext, "range"), ArgumentMinecraftKeyRegistered.getId(commandcontext, "sequence"), flag);
        })));
    }

    private static CompletableFuture<Suggestions> suggestRandomSequence(CommandContext<CommandListenerWrapper> commandcontext, SuggestionsBuilder suggestionsbuilder) {
        List<String> list = Lists.newArrayList();

        ((CommandListenerWrapper) commandcontext.getSource()).getLevel().getRandomSequences().forAllSequences((minecraftkey, randomsequence) -> {
            list.add(minecraftkey.toString());
        });
        return ICompletionProvider.suggest((Iterable) list, suggestionsbuilder);
    }

    private static int randomSample(CommandListenerWrapper commandlistenerwrapper, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange, @Nullable MinecraftKey minecraftkey, boolean flag) throws CommandSyntaxException {
        RandomSource randomsource;

        if (minecraftkey != null) {
            randomsource = commandlistenerwrapper.getLevel().getRandomSequence(minecraftkey);
        } else {
            randomsource = commandlistenerwrapper.getLevel().getRandom();
        }

        int i = (Integer) criterionconditionvalue_integerrange.min().orElse(Integer.MIN_VALUE);
        int j = (Integer) criterionconditionvalue_integerrange.max().orElse(Integer.MAX_VALUE);
        long k = (long) j - (long) i;

        if (k == 0L) {
            throw RandomCommand.ERROR_RANGE_TOO_SMALL.create();
        } else if (k >= 2147483647L) {
            throw RandomCommand.ERROR_RANGE_TOO_LARGE.create();
        } else {
            int l = MathHelper.randomBetweenInclusive(randomsource, i, j);

            if (flag) {
                commandlistenerwrapper.getServer().getPlayerList().broadcastSystemMessage(IChatBaseComponent.translatable("commands.random.roll", commandlistenerwrapper.getDisplayName(), l, i, j), false);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.random.sample.success", l);
                }, false);
            }

            return l;
        }
    }

    private static int resetSequence(CommandListenerWrapper commandlistenerwrapper, MinecraftKey minecraftkey) throws CommandSyntaxException {
        commandlistenerwrapper.getLevel().getRandomSequences().reset(minecraftkey);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.random.reset.success", IChatBaseComponent.translationArg(minecraftkey));
        }, false);
        return 1;
    }

    private static int resetSequence(CommandListenerWrapper commandlistenerwrapper, MinecraftKey minecraftkey, int i, boolean flag, boolean flag1) throws CommandSyntaxException {
        commandlistenerwrapper.getLevel().getRandomSequences().reset(minecraftkey, i, flag, flag1);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.random.reset.success", IChatBaseComponent.translationArg(minecraftkey));
        }, false);
        return 1;
    }

    private static int resetAllSequences(CommandListenerWrapper commandlistenerwrapper) {
        int i = commandlistenerwrapper.getLevel().getRandomSequences().clear();

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.random.reset.all.success", i);
        }, false);
        return i;
    }

    private static int resetAllSequencesAndSetNewDefaults(CommandListenerWrapper commandlistenerwrapper, int i, boolean flag, boolean flag1) {
        RandomSequences randomsequences = commandlistenerwrapper.getLevel().getRandomSequences();

        randomsequences.setSeedDefaults(i, flag, flag1);
        int j = randomsequences.clear();

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.random.reset.all.success", j);
        }, false);
        return j;
    }
}
