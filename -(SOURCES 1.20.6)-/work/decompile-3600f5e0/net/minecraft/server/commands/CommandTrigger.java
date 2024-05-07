package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentScoreboardObjective;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;

public class CommandTrigger {

    private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.trigger.failed.unprimed"));
    private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.trigger.failed.invalid"));

    public CommandTrigger() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("trigger").then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).suggests((commandcontext, suggestionsbuilder) -> {
            return suggestObjectives((CommandListenerWrapper) commandcontext.getSource(), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return simpleTrigger((CommandListenerWrapper) commandcontext.getSource(), ((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"));
        })).then(net.minecraft.commands.CommandDispatcher.literal("add").then(net.minecraft.commands.CommandDispatcher.argument("value", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return addValue((CommandListenerWrapper) commandcontext.getSource(), ((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), IntegerArgumentType.getInteger(commandcontext, "value"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("set").then(net.minecraft.commands.CommandDispatcher.argument("value", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return setValue((CommandListenerWrapper) commandcontext.getSource(), ((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), IntegerArgumentType.getInteger(commandcontext, "value"));
        })))));
    }

    public static CompletableFuture<Suggestions> suggestObjectives(CommandListenerWrapper commandlistenerwrapper, SuggestionsBuilder suggestionsbuilder) {
        Entity entity = commandlistenerwrapper.getEntity();
        List<String> list = Lists.newArrayList();

        if (entity != null) {
            ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
            Iterator iterator = scoreboardserver.getObjectives().iterator();

            while (iterator.hasNext()) {
                ScoreboardObjective scoreboardobjective = (ScoreboardObjective) iterator.next();

                if (scoreboardobjective.getCriteria() == IScoreboardCriteria.TRIGGER) {
                    ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(entity, scoreboardobjective);

                    if (readonlyscoreinfo != null && !readonlyscoreinfo.isLocked()) {
                        list.add(scoreboardobjective.getName());
                    }
                }
            }
        }

        return ICompletionProvider.suggest((Iterable) list, suggestionsbuilder);
    }

    private static int addValue(CommandListenerWrapper commandlistenerwrapper, EntityPlayer entityplayer, ScoreboardObjective scoreboardobjective, int i) throws CommandSyntaxException {
        ScoreAccess scoreaccess = getScore(commandlistenerwrapper.getServer().getScoreboard(), entityplayer, scoreboardobjective);
        int j = scoreaccess.add(i);

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.trigger.add.success", scoreboardobjective.getFormattedDisplayName(), i);
        }, true);
        return j;
    }

    private static int setValue(CommandListenerWrapper commandlistenerwrapper, EntityPlayer entityplayer, ScoreboardObjective scoreboardobjective, int i) throws CommandSyntaxException {
        ScoreAccess scoreaccess = getScore(commandlistenerwrapper.getServer().getScoreboard(), entityplayer, scoreboardobjective);

        scoreaccess.set(i);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.trigger.set.success", scoreboardobjective.getFormattedDisplayName(), i);
        }, true);
        return i;
    }

    private static int simpleTrigger(CommandListenerWrapper commandlistenerwrapper, EntityPlayer entityplayer, ScoreboardObjective scoreboardobjective) throws CommandSyntaxException {
        ScoreAccess scoreaccess = getScore(commandlistenerwrapper.getServer().getScoreboard(), entityplayer, scoreboardobjective);
        int i = scoreaccess.add(1);

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.trigger.simple.success", scoreboardobjective.getFormattedDisplayName());
        }, true);
        return i;
    }

    private static ScoreAccess getScore(Scoreboard scoreboard, ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) throws CommandSyntaxException {
        if (scoreboardobjective.getCriteria() != IScoreboardCriteria.TRIGGER) {
            throw CommandTrigger.ERROR_INVALID_OBJECTIVE.create();
        } else {
            ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(scoreholder, scoreboardobjective);

            if (readonlyscoreinfo != null && !readonlyscoreinfo.isLocked()) {
                ScoreAccess scoreaccess = scoreboard.getOrCreatePlayerScore(scoreholder, scoreboardobjective);

                scoreaccess.lock();
                return scoreaccess;
            } else {
                throw CommandTrigger.ERROR_NOT_PRIMED.create();
            }
        }
    }
}
