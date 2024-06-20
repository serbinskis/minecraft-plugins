package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.server.level.EntityPlayer;

public class TransferCommand {

    private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.transfer.error.no_players"));

    public TransferCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("transfer").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("hostname", StringArgumentType.string()).executes((commandcontext) -> {
            return transfer((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "hostname"), 25565, List.of(((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException()));
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("port", IntegerArgumentType.integer(1, 65535)).executes((commandcontext) -> {
            return transfer((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "hostname"), IntegerArgumentType.getInteger(commandcontext, "port"), List.of(((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException()));
        })).then(net.minecraft.commands.CommandDispatcher.argument("players", ArgumentEntity.players()).executes((commandcontext) -> {
            return transfer((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "hostname"), IntegerArgumentType.getInteger(commandcontext, "port"), ArgumentEntity.getPlayers(commandcontext, "players"));
        })))));
    }

    private static int transfer(CommandListenerWrapper commandlistenerwrapper, String s, int i, Collection<EntityPlayer> collection) throws CommandSyntaxException {
        if (collection.isEmpty()) {
            throw TransferCommand.ERROR_NO_PLAYERS.create();
        } else {
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                entityplayer.connection.send(new ClientboundTransferPacket(s, i));
            }

            if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.transfer.success.single", ((EntityPlayer) collection.iterator().next()).getDisplayName(), s, i);
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.transfer.success.multiple", collection.size(), s, i);
                }, true);
            }

            return collection.size();
        }
    }
}
