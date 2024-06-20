package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentChatComponent;
import net.minecraft.commands.arguments.ArgumentMathOperation;
import net.minecraft.commands.arguments.ArgumentScoreboardCriteria;
import net.minecraft.commands.arguments.ArgumentScoreboardObjective;
import net.minecraft.commands.arguments.ArgumentScoreboardSlot;
import net.minecraft.commands.arguments.ArgumentScoreholder;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;

public class CommandScoreboard {

    private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.scoreboard.objectives.add.duplicate"));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.scoreboard.objectives.display.alreadyEmpty"));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.scoreboard.objectives.display.alreadySet"));
    private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.scoreboard.players.enable.failed"));
    private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.scoreboard.players.enable.invalid"));
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("commands.scoreboard.players.get.null", object, object1);
    });

    public CommandScoreboard() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("scoreboard").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("objectives").then(net.minecraft.commands.CommandDispatcher.literal("list").executes((commandcontext) -> {
            return listObjectives((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("add").then(net.minecraft.commands.CommandDispatcher.argument("objective", StringArgumentType.word()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("criteria", ArgumentScoreboardCriteria.criteria()).executes((commandcontext) -> {
            return addObjective((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "objective"), ArgumentScoreboardCriteria.getCriteria(commandcontext, "criteria"), IChatBaseComponent.literal(StringArgumentType.getString(commandcontext, "objective")));
        })).then(net.minecraft.commands.CommandDispatcher.argument("displayName", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return addObjective((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "objective"), ArgumentScoreboardCriteria.getCriteria(commandcontext, "criteria"), ArgumentChatComponent.getComponent(commandcontext, "displayName"));
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("modify").then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).then(net.minecraft.commands.CommandDispatcher.literal("displayname").then(net.minecraft.commands.CommandDispatcher.argument("displayName", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return setDisplayName((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), ArgumentChatComponent.getComponent(commandcontext, "displayName"));
        })))).then(createRenderTypeModify())).then(net.minecraft.commands.CommandDispatcher.literal("displayautoupdate").then(net.minecraft.commands.CommandDispatcher.argument("value", BoolArgumentType.bool()).executes((commandcontext) -> {
            return setDisplayAutoUpdate((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), BoolArgumentType.getBool(commandcontext, "value"));
        })))).then(addNumberFormats(commandbuildcontext, net.minecraft.commands.CommandDispatcher.literal("numberformat"), (commandcontext, numberformat) -> {
            return setObjectiveFormat((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), numberformat);
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("remove").then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).executes((commandcontext) -> {
            return removeObjective((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("setdisplay").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("slot", ArgumentScoreboardSlot.displaySlot()).executes((commandcontext) -> {
            return clearDisplaySlot((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardSlot.getDisplaySlot(commandcontext, "slot"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).executes((commandcontext) -> {
            return setDisplaySlot((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardSlot.getDisplaySlot(commandcontext, "slot"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"));
        })))))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("players").then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("list").executes((commandcontext) -> {
            return listTrackedPlayers((CommandListenerWrapper) commandcontext.getSource());
        })).then(net.minecraft.commands.CommandDispatcher.argument("target", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).executes((commandcontext) -> {
            return listTrackedPlayerScores((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getName(commandcontext, "target"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("set").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).then(net.minecraft.commands.CommandDispatcher.argument("score", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return setScore((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getWritableObjective(commandcontext, "objective"), IntegerArgumentType.getInteger(commandcontext, "score"));
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("get").then(net.minecraft.commands.CommandDispatcher.argument("target", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).executes((commandcontext) -> {
            return getScore((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getName(commandcontext, "target"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("add").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).then(net.minecraft.commands.CommandDispatcher.argument("score", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return addScore((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getWritableObjective(commandcontext, "objective"), IntegerArgumentType.getInteger(commandcontext, "score"));
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("remove").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).then(net.minecraft.commands.CommandDispatcher.argument("score", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return removeScore((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getWritableObjective(commandcontext, "objective"), IntegerArgumentType.getInteger(commandcontext, "score"));
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("reset").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).executes((commandcontext) -> {
            return resetScores((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).executes((commandcontext) -> {
            return resetScore((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("enable").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).suggests((commandcontext, suggestionsbuilder) -> {
            return suggestTriggers((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return enableTrigger((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"));
        }))))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("display").then(net.minecraft.commands.CommandDispatcher.literal("name").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).then(net.minecraft.commands.CommandDispatcher.argument("name", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return setScoreDisplay((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), ArgumentChatComponent.getComponent(commandcontext, "name"));
        }))).executes((commandcontext) -> {
            return setScoreDisplay((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), (IChatBaseComponent) null);
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("numberformat").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(addNumberFormats(commandbuildcontext, net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()), (commandcontext, numberformat) -> {
            return setScoreNumberFormat((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), numberformat);
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("operation").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("targetObjective", ArgumentScoreboardObjective.objective()).then(net.minecraft.commands.CommandDispatcher.argument("operation", ArgumentMathOperation.operation()).then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("sourceObjective", ArgumentScoreboardObjective.objective()).executes((commandcontext) -> {
            return performOperation((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getWritableObjective(commandcontext, "targetObjective"), ArgumentMathOperation.getOperation(commandcontext, "operation"), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "source"), ArgumentScoreboardObjective.getObjective(commandcontext, "sourceObjective"));
        })))))))));
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> addNumberFormats(CommandBuildContext commandbuildcontext, ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, CommandScoreboard.a commandscoreboard_a) {
        return argumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal("blank").executes((commandcontext) -> {
            return commandscoreboard_a.run(commandcontext, BlankFormat.INSTANCE);
        })).then(net.minecraft.commands.CommandDispatcher.literal("fixed").then(net.minecraft.commands.CommandDispatcher.argument("contents", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            IChatBaseComponent ichatbasecomponent = ArgumentChatComponent.getComponent(commandcontext, "contents");

            return commandscoreboard_a.run(commandcontext, new FixedFormat(ichatbasecomponent));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("styled").then(net.minecraft.commands.CommandDispatcher.argument("style", StyleArgument.style(commandbuildcontext)).executes((commandcontext) -> {
            ChatModifier chatmodifier = StyleArgument.getStyle(commandcontext, "style");

            return commandscoreboard_a.run(commandcontext, new StyledFormat(chatmodifier));
        }))).executes((commandcontext) -> {
            return commandscoreboard_a.run(commandcontext, (NumberFormat) null);
        });
    }

    private static LiteralArgumentBuilder<CommandListenerWrapper> createRenderTypeModify() {
        LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder = net.minecraft.commands.CommandDispatcher.literal("rendertype");
        IScoreboardCriteria.EnumScoreboardHealthDisplay[] aiscoreboardcriteria_enumscoreboardhealthdisplay = IScoreboardCriteria.EnumScoreboardHealthDisplay.values();
        int i = aiscoreboardcriteria_enumscoreboardhealthdisplay.length;

        for (int j = 0; j < i; ++j) {
            IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay = aiscoreboardcriteria_enumscoreboardhealthdisplay[j];

            literalargumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal(iscoreboardcriteria_enumscoreboardhealthdisplay.getId()).executes((commandcontext) -> {
                return setRenderType((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), iscoreboardcriteria_enumscoreboardhealthdisplay);
            }));
        }

        return literalargumentbuilder;
    }

    private static CompletableFuture<Suggestions> suggestTriggers(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, SuggestionsBuilder suggestionsbuilder) {
        List<String> list = Lists.newArrayList();
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        Iterator iterator = scoreboardserver.getObjectives().iterator();

        while (iterator.hasNext()) {
            ScoreboardObjective scoreboardobjective = (ScoreboardObjective) iterator.next();

            if (scoreboardobjective.getCriteria() == IScoreboardCriteria.TRIGGER) {
                boolean flag = false;
                Iterator iterator1 = collection.iterator();

                while (true) {
                    if (iterator1.hasNext()) {
                        ScoreHolder scoreholder = (ScoreHolder) iterator1.next();
                        ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(scoreholder, scoreboardobjective);

                        if (readonlyscoreinfo != null && !readonlyscoreinfo.isLocked()) {
                            continue;
                        }

                        flag = true;
                    }

                    if (flag) {
                        list.add(scoreboardobjective.getName());
                    }
                    break;
                }
            }
        }

        return ICompletionProvider.suggest((Iterable) list, suggestionsbuilder);
    }

    private static int getScore(CommandListenerWrapper commandlistenerwrapper, ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) throws CommandSyntaxException {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(scoreholder, scoreboardobjective);

        if (readonlyscoreinfo == null) {
            throw CommandScoreboard.ERROR_NO_VALUE.create(scoreboardobjective.getName(), scoreholder.getFeedbackDisplayName());
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.get.success", scoreholder.getFeedbackDisplayName(), readonlyscoreinfo.value(), scoreboardobjective.getFormattedDisplayName());
            }, false);
            return readonlyscoreinfo.value();
        }
    }

    private static IChatBaseComponent getFirstTargetName(Collection<ScoreHolder> collection) {
        return ((ScoreHolder) collection.iterator().next()).getFeedbackDisplayName();
    }

    private static int performOperation(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective, ArgumentMathOperation.a argumentmathoperation_a, Collection<ScoreHolder> collection1, ScoreboardObjective scoreboardobjective1) throws CommandSyntaxException {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        int i = 0;

        ScoreAccess scoreaccess;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); i += scoreaccess.get()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreaccess = scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective);
            Iterator iterator1 = collection1.iterator();

            while (iterator1.hasNext()) {
                ScoreHolder scoreholder1 = (ScoreHolder) iterator1.next();
                ScoreAccess scoreaccess1 = scoreboardserver.getOrCreatePlayerScore(scoreholder1, scoreboardobjective1);

                argumentmathoperation_a.apply(scoreaccess, scoreaccess1);
            }
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.operation.success.single", scoreboardobjective.getFormattedDisplayName(), getFirstTargetName(collection), i);
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.operation.success.multiple", scoreboardobjective.getFormattedDisplayName(), collection.size());
            }, true);
        }

        return i;
    }

    private static int enableTrigger(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective) throws CommandSyntaxException {
        if (scoreboardobjective.getCriteria() != IScoreboardCriteria.TRIGGER) {
            throw CommandScoreboard.ERROR_NOT_TRIGGER.create();
        } else {
            ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
            int i = 0;
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                ScoreHolder scoreholder = (ScoreHolder) iterator.next();
                ScoreAccess scoreaccess = scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective);

                if (scoreaccess.locked()) {
                    scoreaccess.unlock();
                    ++i;
                }
            }

            if (i == 0) {
                throw CommandScoreboard.ERROR_TRIGGER_ALREADY_ENABLED.create();
            } else {
                if (collection.size() == 1) {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.scoreboard.players.enable.success.single", scoreboardobjective.getFormattedDisplayName(), getFirstTargetName(collection));
                    }, true);
                } else {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.scoreboard.players.enable.success.multiple", scoreboardobjective.getFormattedDisplayName(), collection.size());
                    }, true);
                }

                return i;
            }
        }
    }

    private static int resetScores(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreboardserver.resetAllPlayerScores(scoreholder);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.reset.all.single", getFirstTargetName(collection));
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.reset.all.multiple", collection.size());
            }, true);
        }

        return collection.size();
    }

    private static int resetScore(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreboardserver.resetSinglePlayerScore(scoreholder, scoreboardobjective);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.reset.specific.single", scoreboardobjective.getFormattedDisplayName(), getFirstTargetName(collection));
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.reset.specific.multiple", scoreboardobjective.getFormattedDisplayName(), collection.size());
            }, true);
        }

        return collection.size();
    }

    private static int setScore(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective, int i) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective).set(i);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.set.success.single", scoreboardobjective.getFormattedDisplayName(), getFirstTargetName(collection), i);
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.set.success.multiple", scoreboardobjective.getFormattedDisplayName(), collection.size(), i);
            }, true);
        }

        return i * collection.size();
    }

    private static int setScoreDisplay(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective, @Nullable IChatBaseComponent ichatbasecomponent) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective).display(ichatbasecomponent);
        }

        if (ichatbasecomponent == null) {
            if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.scoreboard.players.display.name.clear.success.single", getFirstTargetName(collection), scoreboardobjective.getFormattedDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.scoreboard.players.display.name.clear.success.multiple", collection.size(), scoreboardobjective.getFormattedDisplayName());
                }, true);
            }
        } else if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.display.name.set.success.single", ichatbasecomponent, getFirstTargetName(collection), scoreboardobjective.getFormattedDisplayName());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.display.name.set.success.multiple", ichatbasecomponent, collection.size(), scoreboardobjective.getFormattedDisplayName());
            }, true);
        }

        return collection.size();
    }

    private static int setScoreNumberFormat(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective, @Nullable NumberFormat numberformat) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective).numberFormatOverride(numberformat);
        }

        if (numberformat == null) {
            if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.scoreboard.players.display.numberFormat.clear.success.single", getFirstTargetName(collection), scoreboardobjective.getFormattedDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.scoreboard.players.display.numberFormat.clear.success.multiple", collection.size(), scoreboardobjective.getFormattedDisplayName());
                }, true);
            }
        } else if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.display.numberFormat.set.success.single", getFirstTargetName(collection), scoreboardobjective.getFormattedDisplayName());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.display.numberFormat.set.success.multiple", collection.size(), scoreboardobjective.getFormattedDisplayName());
            }, true);
        }

        return collection.size();
    }

    private static int addScore(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective, int i) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        int j = 0;

        ScoreAccess scoreaccess;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); j += scoreaccess.get()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreaccess = scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective);
            scoreaccess.set(scoreaccess.get() + i);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.add.success.single", i, scoreboardobjective.getFormattedDisplayName(), getFirstTargetName(collection), j);
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.add.success.multiple", i, scoreboardobjective.getFormattedDisplayName(), collection.size());
            }, true);
        }

        return j;
    }

    private static int removeScore(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective, int i) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();
        int j = 0;

        ScoreAccess scoreaccess;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); j += scoreaccess.get()) {
            ScoreHolder scoreholder = (ScoreHolder) iterator.next();

            scoreaccess = scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective);
            scoreaccess.set(scoreaccess.get() - i);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.remove.success.single", i, scoreboardobjective.getFormattedDisplayName(), getFirstTargetName(collection), j);
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.remove.success.multiple", i, scoreboardobjective.getFormattedDisplayName(), collection.size());
            }, true);
        }

        return j;
    }

    private static int listTrackedPlayers(CommandListenerWrapper commandlistenerwrapper) {
        Collection<ScoreHolder> collection = commandlistenerwrapper.getServer().getScoreboard().getTrackedPlayers();

        if (collection.isEmpty()) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.list.empty");
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.list.success", collection.size(), ChatComponentUtils.formatList(collection, ScoreHolder::getFeedbackDisplayName));
            }, false);
        }

        return collection.size();
    }

    private static int listTrackedPlayerScores(CommandListenerWrapper commandlistenerwrapper, ScoreHolder scoreholder) {
        Object2IntMap<ScoreboardObjective> object2intmap = commandlistenerwrapper.getServer().getScoreboard().listPlayerScores(scoreholder);

        if (object2intmap.isEmpty()) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.list.entity.empty", scoreholder.getFeedbackDisplayName());
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.players.list.entity.success", scoreholder.getFeedbackDisplayName(), object2intmap.size());
            }, false);
            Object2IntMaps.fastForEach(object2intmap, (entry) -> {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.scoreboard.players.list.entity.entry", ((ScoreboardObjective) entry.getKey()).getFormattedDisplayName(), entry.getIntValue());
                }, false);
            });
        }

        return object2intmap.size();
    }

    private static int clearDisplaySlot(CommandListenerWrapper commandlistenerwrapper, DisplaySlot displayslot) throws CommandSyntaxException {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();

        if (scoreboardserver.getDisplayObjective(displayslot) == null) {
            throw CommandScoreboard.ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
        } else {
            scoreboardserver.setDisplayObjective(displayslot, (ScoreboardObjective) null);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.display.cleared", displayslot.getSerializedName());
            }, true);
            return 0;
        }
    }

    private static int setDisplaySlot(CommandListenerWrapper commandlistenerwrapper, DisplaySlot displayslot, ScoreboardObjective scoreboardobjective) throws CommandSyntaxException {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();

        if (scoreboardserver.getDisplayObjective(displayslot) == scoreboardobjective) {
            throw CommandScoreboard.ERROR_DISPLAY_SLOT_ALREADY_SET.create();
        } else {
            scoreboardserver.setDisplayObjective(displayslot, scoreboardobjective);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.display.set", displayslot.getSerializedName(), scoreboardobjective.getDisplayName());
            }, true);
            return 0;
        }
    }

    private static int setDisplayName(CommandListenerWrapper commandlistenerwrapper, ScoreboardObjective scoreboardobjective, IChatBaseComponent ichatbasecomponent) {
        if (!scoreboardobjective.getDisplayName().equals(ichatbasecomponent)) {
            scoreboardobjective.setDisplayName(ichatbasecomponent);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.modify.displayname", scoreboardobjective.getName(), scoreboardobjective.getFormattedDisplayName());
            }, true);
        }

        return 0;
    }

    private static int setDisplayAutoUpdate(CommandListenerWrapper commandlistenerwrapper, ScoreboardObjective scoreboardobjective, boolean flag) {
        if (scoreboardobjective.displayAutoUpdate() != flag) {
            scoreboardobjective.setDisplayAutoUpdate(flag);
            if (flag) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.enable", scoreboardobjective.getName(), scoreboardobjective.getFormattedDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.disable", scoreboardobjective.getName(), scoreboardobjective.getFormattedDisplayName());
                }, true);
            }
        }

        return 0;
    }

    private static int setObjectiveFormat(CommandListenerWrapper commandlistenerwrapper, ScoreboardObjective scoreboardobjective, @Nullable NumberFormat numberformat) {
        scoreboardobjective.setNumberFormat(numberformat);
        if (numberformat != null) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.modify.objectiveFormat.set", scoreboardobjective.getName());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.modify.objectiveFormat.clear", scoreboardobjective.getName());
            }, true);
        }

        return 0;
    }

    private static int setRenderType(CommandListenerWrapper commandlistenerwrapper, ScoreboardObjective scoreboardobjective, IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay) {
        if (scoreboardobjective.getRenderType() != iscoreboardcriteria_enumscoreboardhealthdisplay) {
            scoreboardobjective.setRenderType(iscoreboardcriteria_enumscoreboardhealthdisplay);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.modify.rendertype", scoreboardobjective.getFormattedDisplayName());
            }, true);
        }

        return 0;
    }

    private static int removeObjective(CommandListenerWrapper commandlistenerwrapper, ScoreboardObjective scoreboardobjective) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();

        scoreboardserver.removeObjective(scoreboardobjective);
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.scoreboard.objectives.remove.success", scoreboardobjective.getFormattedDisplayName());
        }, true);
        return scoreboardserver.getObjectives().size();
    }

    private static int addObjective(CommandListenerWrapper commandlistenerwrapper, String s, IScoreboardCriteria iscoreboardcriteria, IChatBaseComponent ichatbasecomponent) throws CommandSyntaxException {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();

        if (scoreboardserver.getObjective(s) != null) {
            throw CommandScoreboard.ERROR_OBJECTIVE_ALREADY_EXISTS.create();
        } else {
            scoreboardserver.addObjective(s, iscoreboardcriteria, ichatbasecomponent, iscoreboardcriteria.getDefaultRenderType(), false, (NumberFormat) null);
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjective(s);

            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.add.success", scoreboardobjective.getFormattedDisplayName());
            }, true);
            return scoreboardserver.getObjectives().size();
        }
    }

    private static int listObjectives(CommandListenerWrapper commandlistenerwrapper) {
        Collection<ScoreboardObjective> collection = commandlistenerwrapper.getServer().getScoreboard().getObjectives();

        if (collection.isEmpty()) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.list.empty");
            }, false);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.scoreboard.objectives.list.success", collection.size(), ChatComponentUtils.formatList(collection, ScoreboardObjective::getFormattedDisplayName));
            }, false);
        }

        return collection.size();
    }

    @FunctionalInterface
    public interface a {

        int run(CommandContext<CommandListenerWrapper> commandcontext, @Nullable NumberFormat numberformat) throws CommandSyntaxException;
    }
}
