package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentUUID;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class DebugConfigCommand {

    public DebugConfigCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("debugconfig").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.literal("config").then(net.minecraft.commands.CommandDispatcher.argument("target", ArgumentEntity.player()).executes((commandcontext) -> {
            return config((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayer(commandcontext, "target"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("unconfig").then(net.minecraft.commands.CommandDispatcher.argument("target", ArgumentUUID.uuid()).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest(getUuidsInConfig(((CommandListenerWrapper) commandcontext.getSource()).getServer()), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return unconfig((CommandListenerWrapper) commandcontext.getSource(), ArgumentUUID.getUuid(commandcontext, "target"));
        }))));
    }

    private static Iterable<String> getUuidsInConfig(MinecraftServer minecraftserver) {
        Set<String> set = new HashSet();
        Iterator iterator = minecraftserver.getConnection().getConnections().iterator();

        while (iterator.hasNext()) {
            NetworkManager networkmanager = (NetworkManager) iterator.next();
            PacketListener packetlistener = networkmanager.getPacketListener();

            if (packetlistener instanceof ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl) {
                set.add(serverconfigurationpacketlistenerimpl.getOwner().getId().toString());
            }
        }

        return set;
    }

    private static int config(CommandListenerWrapper commandlistenerwrapper, EntityPlayer entityplayer) {
        GameProfile gameprofile = entityplayer.getGameProfile();

        entityplayer.connection.switchToConfig();
        commandlistenerwrapper.sendSuccess(() -> {
            String s = gameprofile.getName();

            return IChatBaseComponent.literal("Switched player " + s + "(" + String.valueOf(gameprofile.getId()) + ") to config mode");
        }, false);
        return 1;
    }

    private static int unconfig(CommandListenerWrapper commandlistenerwrapper, UUID uuid) {
        Iterator iterator = commandlistenerwrapper.getServer().getConnection().getConnections().iterator();

        while (iterator.hasNext()) {
            NetworkManager networkmanager = (NetworkManager) iterator.next();
            PacketListener packetlistener = networkmanager.getPacketListener();

            if (packetlistener instanceof ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl) {
                if (serverconfigurationpacketlistenerimpl.getOwner().getId().equals(uuid)) {
                    serverconfigurationpacketlistenerimpl.returnToWorld();
                }
            }
        }

        commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("Can't find player to unconfig"));
        return 0;
    }
}
