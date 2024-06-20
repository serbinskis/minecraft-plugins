package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.EntityHuman;

public class WardenSpawnTrackerCommand {

    public WardenSpawnTrackerCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("warden_spawn_tracker").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.literal("clear").executes((commandcontext) -> {
            return resetTracker((CommandListenerWrapper) commandcontext.getSource(), ImmutableList.of(((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException()));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("set").then(net.minecraft.commands.CommandDispatcher.argument("warning_level", IntegerArgumentType.integer(0, 4)).executes((commandcontext) -> {
            return setWarningLevel((CommandListenerWrapper) commandcontext.getSource(), ImmutableList.of(((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException()), IntegerArgumentType.getInteger(commandcontext, "warning_level"));
        }))));
    }

    private static int setWarningLevel(CommandListenerWrapper commandlistenerwrapper, Collection<? extends EntityHuman> collection, int i) {
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();

            entityhuman.getWardenSpawnTracker().ifPresent((wardenspawntracker) -> {
                wardenspawntracker.setWarningLevel(i);
            });
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.warden_spawn_tracker.set.success.single", ((EntityHuman) collection.iterator().next()).getDisplayName());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.warden_spawn_tracker.set.success.multiple", collection.size());
            }, true);
        }

        return collection.size();
    }

    private static int resetTracker(CommandListenerWrapper commandlistenerwrapper, Collection<? extends EntityHuman> collection) {
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            EntityHuman entityhuman = (EntityHuman) iterator.next();

            entityhuman.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
        }

        if (collection.size() == 1) {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.warden_spawn_tracker.clear.success.single", ((EntityHuman) collection.iterator().next()).getDisplayName());
            }, true);
        } else {
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.warden_spawn_tracker.clear.success.multiple", collection.size());
            }, true);
        }

        return collection.size();
    }
}
