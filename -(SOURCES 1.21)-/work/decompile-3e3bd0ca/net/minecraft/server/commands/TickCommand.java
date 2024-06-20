package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Arrays;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentTime;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.util.TimeRange;

public class TickCommand {

    private static final float MAX_TICKRATE = 10000.0F;
    private static final String DEFAULT_TICKRATE = String.valueOf(20);

    public TickCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("tick").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.literal("query").executes((commandcontext) -> {
            return tickQuery((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("rate").then(net.minecraft.commands.CommandDispatcher.argument("rate", FloatArgumentType.floatArg(1.0F, 10000.0F)).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest(new String[]{TickCommand.DEFAULT_TICKRATE}, suggestionsbuilder);
        }).executes((commandcontext) -> {
            return setTickingRate((CommandListenerWrapper) commandcontext.getSource(), FloatArgumentType.getFloat(commandcontext, "rate"));
        })))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("step").executes((commandcontext) -> {
            return step((CommandListenerWrapper) commandcontext.getSource(), 1);
        })).then(net.minecraft.commands.CommandDispatcher.literal("stop").executes((commandcontext) -> {
            return stopStepping((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.argument("time", ArgumentTime.time(1)).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest(new String[]{"1t", "1s"}, suggestionsbuilder);
        }).executes((commandcontext) -> {
            return step((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "time"));
        })))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("sprint").then(net.minecraft.commands.CommandDispatcher.literal("stop").executes((commandcontext) -> {
            return stopSprinting((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.argument("time", ArgumentTime.time(1)).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest(new String[]{"60s", "1d", "3d"}, suggestionsbuilder);
        }).executes((commandcontext) -> {
            return sprint((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "time"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("unfreeze").executes((commandcontext) -> {
            return setFreeze((CommandListenerWrapper) commandcontext.getSource(), false);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("freeze").executes((commandcontext) -> {
            return setFreeze((CommandListenerWrapper) commandcontext.getSource(), true);
        })));
    }

    private static String nanosToMilisString(long i) {
        return String.format("%.1f", (float) i / (float) TimeRange.NANOSECONDS_PER_MILLISECOND);
    }

    private static int setTickingRate(CommandListenerWrapper commandlistenerwrapper, float f) {
        ServerTickRateManager servertickratemanager = commandlistenerwrapper.getServer().tickRateManager();

        servertickratemanager.setTickRate(f);
        String s = String.format("%.1f", f);

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.tick.rate.success", s);
        }, true);
        return (int) f;
    }

    private static int tickQuery(CommandListenerWrapper commandlistenerwrapper) {
        ServerTickRateManager servertickratemanager = commandlistenerwrapper.getServer().tickRateManager();
        String s = nanosToMilisString(commandlistenerwrapper.getServer().getAverageTickTimeNanos());
        float f = servertickratemanager.tickrate();
        String s1 = String.format("%.1f", f);

        if (servertickratemanager.isSprinting()) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.status.sprinting");
            }, false);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.query.rate.sprinting", s1, s);
            }, false);
        } else {
            if (servertickratemanager.isFrozen()) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.tick.status.frozen");
                }, false);
            } else if (servertickratemanager.nanosecondsPerTick() < commandlistenerwrapper.getServer().getAverageTickTimeNanos()) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.tick.status.lagging");
                }, false);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.tick.status.running");
                }, false);
            }

            String s2 = nanosToMilisString(servertickratemanager.nanosecondsPerTick());

            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.query.rate.running", s1, s, s2);
            }, false);
        }

        long[] along = Arrays.copyOf(commandlistenerwrapper.getServer().getTickTimesNanos(), commandlistenerwrapper.getServer().getTickTimesNanos().length);

        Arrays.sort(along);
        String s3 = nanosToMilisString(along[along.length / 2]);
        String s4 = nanosToMilisString(along[(int) ((double) along.length * 0.95D)]);
        String s5 = nanosToMilisString(along[(int) ((double) along.length * 0.99D)]);

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.tick.query.percentiles", s3, s4, s5, along.length);
        }, false);
        return (int) f;
    }

    private static int sprint(CommandListenerWrapper commandlistenerwrapper, int i) {
        boolean flag = commandlistenerwrapper.getServer().tickRateManager().requestGameToSprint(i);

        if (flag) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.sprint.stop.success");
            }, true);
        }

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.tick.status.sprinting");
        }, true);
        return 1;
    }

    private static int setFreeze(CommandListenerWrapper commandlistenerwrapper, boolean flag) {
        ServerTickRateManager servertickratemanager = commandlistenerwrapper.getServer().tickRateManager();

        if (flag) {
            if (servertickratemanager.isSprinting()) {
                servertickratemanager.stopSprinting();
            }

            if (servertickratemanager.isSteppingForward()) {
                servertickratemanager.stopStepping();
            }
        }

        servertickratemanager.setFrozen(flag);
        if (flag) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.status.frozen");
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.status.running");
            }, true);
        }

        return flag ? 1 : 0;
    }

    private static int step(CommandListenerWrapper commandlistenerwrapper, int i) {
        ServerTickRateManager servertickratemanager = commandlistenerwrapper.getServer().tickRateManager();
        boolean flag = servertickratemanager.stepGameIfPaused(i);

        if (flag) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.step.success", i);
            }, true);
        } else {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.tick.step.fail"));
        }

        return 1;
    }

    private static int stopStepping(CommandListenerWrapper commandlistenerwrapper) {
        ServerTickRateManager servertickratemanager = commandlistenerwrapper.getServer().tickRateManager();
        boolean flag = servertickratemanager.stopStepping();

        if (flag) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.step.stop.success");
            }, true);
            return 1;
        } else {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.tick.step.stop.fail"));
            return 0;
        }
    }

    private static int stopSprinting(CommandListenerWrapper commandlistenerwrapper) {
        ServerTickRateManager servertickratemanager = commandlistenerwrapper.getServer().tickRateManager();
        boolean flag = servertickratemanager.stopSprinting();

        if (flag) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.tick.sprint.stop.success");
            }, true);
            return 1;
        } else {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.tick.sprint.stop.fail"));
            return 0;
        }
    }
}
