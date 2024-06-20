package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentUUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;

public class ServerPackCommand {

    public ServerPackCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("serverpack").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.literal("push").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("url", StringArgumentType.string()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("uuid", ArgumentUUID.uuid()).then(net.minecraft.commands.CommandDispatcher.argument("hash", StringArgumentType.word()).executes((commandcontext) -> {
            return pushPack((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "url"), Optional.of(ArgumentUUID.getUuid(commandcontext, "uuid")), Optional.of(StringArgumentType.getString(commandcontext, "hash")));
        }))).executes((commandcontext) -> {
            return pushPack((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "url"), Optional.of(ArgumentUUID.getUuid(commandcontext, "uuid")), Optional.empty());
        }))).executes((commandcontext) -> {
            return pushPack((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "url"), Optional.empty(), Optional.empty());
        })))).then(net.minecraft.commands.CommandDispatcher.literal("pop").then(net.minecraft.commands.CommandDispatcher.argument("uuid", ArgumentUUID.uuid()).executes((commandcontext) -> {
            return popPack((CommandListenerWrapper) commandcontext.getSource(), ArgumentUUID.getUuid(commandcontext, "uuid"));
        }))));
    }

    private static void sendToAllConnections(CommandListenerWrapper commandlistenerwrapper, Packet<?> packet) {
        commandlistenerwrapper.getServer().getConnection().getConnections().forEach((networkmanager) -> {
            networkmanager.send(packet);
        });
    }

    private static int pushPack(CommandListenerWrapper commandlistenerwrapper, String s, Optional<UUID> optional, Optional<String> optional1) {
        UUID uuid = (UUID) optional.orElseGet(() -> {
            return UUID.nameUUIDFromBytes(s.getBytes(StandardCharsets.UTF_8));
        });
        String s1 = (String) optional1.orElse("");
        ClientboundResourcePackPushPacket clientboundresourcepackpushpacket = new ClientboundResourcePackPushPacket(uuid, s, s1, false, (Optional) null);

        sendToAllConnections(commandlistenerwrapper, clientboundresourcepackpushpacket);
        return 0;
    }

    private static int popPack(CommandListenerWrapper commandlistenerwrapper, UUID uuid) {
        ClientboundResourcePackPopPacket clientboundresourcepackpoppacket = new ClientboundResourcePackPopPacket(Optional.of(uuid));

        sendToAllConnections(commandlistenerwrapper, clientboundresourcepackpoppacket);
        return 0;
    }
}
