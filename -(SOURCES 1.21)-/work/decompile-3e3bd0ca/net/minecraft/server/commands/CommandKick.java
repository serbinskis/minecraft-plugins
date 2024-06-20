package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentChat;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;

public class CommandKick {

    private static final SimpleCommandExceptionType ERROR_KICKING_OWNER = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.kick.owner.failed"));
    private static final SimpleCommandExceptionType ERROR_SINGLEPLAYER = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.kick.singleplayer.failed"));

    public CommandKick() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("kick").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.players()).executes((commandcontext) -> {
            return kickPlayers((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), IChatBaseComponent.translatable("multiplayer.disconnect.kicked"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("reason", ArgumentChat.message()).executes((commandcontext) -> {
            return kickPlayers((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentChat.getMessage(commandcontext, "reason"));
        }))));
    }

    private static int kickPlayers(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, IChatBaseComponent ichatbasecomponent) throws CommandSyntaxException {
        if (!commandlistenerwrapper.getServer().isPublished()) {
            throw CommandKick.ERROR_SINGLEPLAYER.create();
        } else {
            int i = 0;
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                if (!commandlistenerwrapper.getServer().isSingleplayerOwner(entityplayer.getGameProfile())) {
                    entityplayer.connection.disconnect(ichatbasecomponent);
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.kick.success", entityplayer.getDisplayName(), ichatbasecomponent);
                    }, true);
                    ++i;
                }
            }

            if (i == 0) {
                throw CommandKick.ERROR_KICKING_OWNER.create();
            } else {
                return i;
            }
        }
    }
}
