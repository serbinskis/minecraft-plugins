package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder<T extends PacketListener> extends MessageToByteEncoder<Packet<T>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final ProtocolInfo<T> protocolInfo;

    public PacketEncoder(ProtocolInfo<T> protocolinfo) {
        this.protocolInfo = protocolinfo;
    }

    protected void encode(ChannelHandlerContext channelhandlercontext, Packet<T> packet, ByteBuf bytebuf) throws Exception {
        PacketType<? extends Packet<? super T>> packettype = packet.type();

        try {
            this.protocolInfo.codec().encode(bytebuf, packet);
            int i = bytebuf.readableBytes();

            if (PacketEncoder.LOGGER.isDebugEnabled()) {
                PacketEncoder.LOGGER.debug(NetworkManager.PACKET_SENT_MARKER, "OUT: [{}:{}] {} -> {} bytes", new Object[]{this.protocolInfo.id().id(), packettype, packet.getClass().getName(), i});
            }

            JvmProfiler.INSTANCE.onPacketSent(this.protocolInfo.id(), packettype, channelhandlercontext.channel().remoteAddress(), i);
        } catch (Throwable throwable) {
            PacketEncoder.LOGGER.error("Error sending packet {}", packettype, throwable);
            if (packet.isSkippable()) {
                throw new SkipEncodeException(throwable);
            }

            throw throwable;
        } finally {
            ProtocolSwapHandler.handleOutboundTerminalPacket(channelhandlercontext, packet);
        }

    }
}
