package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {

    static void handleInboundTerminalPacket(ChannelHandlerContext channelhandlercontext, Packet<?> packet) {
        if (packet.isTerminal()) {
            channelhandlercontext.channel().config().setAutoRead(false);
            channelhandlercontext.pipeline().addBefore(channelhandlercontext.name(), "inbound_config", new UnconfiguredPipelineHandler.a());
            channelhandlercontext.pipeline().remove(channelhandlercontext.name());
        }

    }

    static void handleOutboundTerminalPacket(ChannelHandlerContext channelhandlercontext, Packet<?> packet) {
        if (packet.isTerminal()) {
            channelhandlercontext.pipeline().addAfter(channelhandlercontext.name(), "outbound_config", new UnconfiguredPipelineHandler.c());
            channelhandlercontext.pipeline().remove(channelhandlercontext.name());
        }

    }
}
