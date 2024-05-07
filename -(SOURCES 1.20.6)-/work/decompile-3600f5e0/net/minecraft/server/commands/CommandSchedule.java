package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentTime;
import net.minecraft.commands.arguments.item.ArgumentTag;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.CustomFunctionCallback;
import net.minecraft.world.level.timers.CustomFunctionCallbackTag;
import net.minecraft.world.level.timers.CustomFunctionCallbackTimerQueue;

public class CommandSchedule {

    private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.schedule.same_tick"));
    private static final DynamicCommandExceptionType ERROR_CANT_REMOVE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.schedule.cleared.failure", object);
    });
    private static final SuggestionProvider<CommandListenerWrapper> SUGGEST_SCHEDULE = (commandcontext, suggestionsbuilder) -> {
        return ICompletionProvider.suggest((Iterable) ((CommandListenerWrapper) commandcontext.getSource()).getServer().getWorldData().overworldData().getScheduledEvents().getEventsIds(), suggestionsbuilder);
    };

    public CommandSchedule() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("schedule").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.literal("function").then(net.minecraft.commands.CommandDispatcher.argument("function", ArgumentTag.functions()).suggests(CommandFunction.SUGGEST_FUNCTION).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("time", ArgumentTime.time()).executes((commandcontext) -> {
            return schedule((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctionOrTag(commandcontext, "function"), IntegerArgumentType.getInteger(commandcontext, "time"), true);
        })).then(net.minecraft.commands.CommandDispatcher.literal("append").executes((commandcontext) -> {
            return schedule((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctionOrTag(commandcontext, "function"), IntegerArgumentType.getInteger(commandcontext, "time"), false);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("replace").executes((commandcontext) -> {
            return schedule((CommandListenerWrapper) commandcontext.getSource(), ArgumentTag.getFunctionOrTag(commandcontext, "function"), IntegerArgumentType.getInteger(commandcontext, "time"), true);
        })))))).then(net.minecraft.commands.CommandDispatcher.literal("clear").then(net.minecraft.commands.CommandDispatcher.argument("function", StringArgumentType.greedyString()).suggests(CommandSchedule.SUGGEST_SCHEDULE).executes((commandcontext) -> {
            return remove((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "function"));
        }))));
    }

    private static int schedule(CommandListenerWrapper commandlistenerwrapper, Pair<MinecraftKey, Either<net.minecraft.commands.functions.CommandFunction<CommandListenerWrapper>, Collection<net.minecraft.commands.functions.CommandFunction<CommandListenerWrapper>>>> pair, int i, boolean flag) throws CommandSyntaxException {
        if (i == 0) {
            throw CommandSchedule.ERROR_SAME_TICK.create();
        } else {
            long j = commandlistenerwrapper.getLevel().getGameTime() + (long) i;
            MinecraftKey minecraftkey = (MinecraftKey) pair.getFirst();
            CustomFunctionCallbackTimerQueue<MinecraftServer> customfunctioncallbacktimerqueue = commandlistenerwrapper.getServer().getWorldData().overworldData().getScheduledEvents();

            ((Either) pair.getSecond()).ifLeft((net_minecraft_commands_functions_commandfunction) -> {
                String s = minecraftkey.toString();

                if (flag) {
                    customfunctioncallbacktimerqueue.remove(s);
                }

                customfunctioncallbacktimerqueue.schedule(s, j, new CustomFunctionCallback(minecraftkey));
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.schedule.created.function", IChatBaseComponent.translationArg(minecraftkey), i, j);
                }, true);
            }).ifRight((collection) -> {
                String s = "#" + String.valueOf(minecraftkey);

                if (flag) {
                    customfunctioncallbacktimerqueue.remove(s);
                }

                customfunctioncallbacktimerqueue.schedule(s, j, new CustomFunctionCallbackTag(minecraftkey));
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.schedule.created.tag", IChatBaseComponent.translationArg(minecraftkey), i, j);
                }, true);
            });
            return Math.floorMod(j, Integer.MAX_VALUE);
        }
    }

    private static int remove(CommandListenerWrapper commandlistenerwrapper, String s) throws CommandSyntaxException {
        int i = commandlistenerwrapper.getServer().getWorldData().overworldData().getScheduledEvents().remove(s);

        if (i == 0) {
            throw CommandSchedule.ERROR_CANT_REMOVE.create(s);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.schedule.cleared.success", i, s);
            }, true);
            return i;
        }
    }
}
