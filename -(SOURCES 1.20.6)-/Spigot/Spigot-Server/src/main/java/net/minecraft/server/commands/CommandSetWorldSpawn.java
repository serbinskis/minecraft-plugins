package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentAngle;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.World;

public class CommandSetWorldSpawn {

    public CommandSetWorldSpawn() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("setworldspawn").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).executes((commandcontext) -> {
            return setSpawn((CommandListenerWrapper) commandcontext.getSource(), BlockPosition.containing(((CommandListenerWrapper) commandcontext.getSource()).getPosition()), 0.0F);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).executes((commandcontext) -> {
            return setSpawn((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getSpawnablePos(commandcontext, "pos"), 0.0F);
        })).then(net.minecraft.commands.CommandDispatcher.argument("angle", ArgumentAngle.angle()).executes((commandcontext) -> {
            return setSpawn((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getSpawnablePos(commandcontext, "pos"), ArgumentAngle.getAngle(commandcontext, "angle"));
        }))));
    }

    private static int setSpawn(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, float f) {
        WorldServer worldserver = commandlistenerwrapper.getLevel();

        if (false && worldserver.dimension() != World.OVERWORLD) { // CraftBukkit - SPIGOT-7649: allow in all worlds
            commandlistenerwrapper.sendFailure(IChatBaseComponent.translatable("commands.setworldspawn.failure.not_overworld"));
            return 0;
        } else {
            worldserver.setDefaultSpawnPos(blockposition, f);
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.setworldspawn.success", blockposition.getX(), blockposition.getY(), blockposition.getZ(), f);
            }, true);
            return 1;
        }
    }
}
