package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.level.pathfinder.PathEntity;

public class DebugPathCommand {

    private static final SimpleCommandExceptionType ERROR_NOT_MOB = new SimpleCommandExceptionType(IChatBaseComponent.literal("Source is not a mob"));
    private static final SimpleCommandExceptionType ERROR_NO_PATH = new SimpleCommandExceptionType(IChatBaseComponent.literal("Path not found"));
    private static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(IChatBaseComponent.literal("Target not reached"));

    public DebugPathCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("debugpath").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.argument("to", ArgumentPosition.blockPos()).executes((commandcontext) -> {
            return fillBlocks((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "to"));
        })));
    }

    private static int fillBlocks(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition) throws CommandSyntaxException {
        Entity entity = commandlistenerwrapper.getEntity();

        if (!(entity instanceof EntityInsentient entityinsentient)) {
            throw DebugPathCommand.ERROR_NOT_MOB.create();
        } else {
            Navigation navigation = new Navigation(entityinsentient, commandlistenerwrapper.getLevel());
            PathEntity pathentity = navigation.createPath(blockposition, 0);

            PacketDebug.sendPathFindingPacket(commandlistenerwrapper.getLevel(), entityinsentient, pathentity, navigation.getMaxDistanceToWaypoint());
            if (pathentity == null) {
                throw DebugPathCommand.ERROR_NO_PATH.create();
            } else if (!pathentity.canReach()) {
                throw DebugPathCommand.ERROR_NOT_COMPLETE.create();
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.literal("Made path");
                }, true);
                return 1;
            }
        }
    }
}
