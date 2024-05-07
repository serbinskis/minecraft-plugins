package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder<T extends PacketListener> extends ByteToMessageDecoder implements ProtocolSwapHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final ProtocolInfo<T> protocolInfo;

    public PacketDecoder(ProtocolInfo<T> protocolinfo) {
        this.protocolInfo = protocolinfo;
    }

    protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List<Object> list) throws Exception {
        int i = bytebuf.readableBytes();

        if (i != 0) {
            Packet<? super T> packet = (Packet) this.protocolInfo.codec().decode(bytebuf);
            PacketType<? extends Packet<? super T>> packettype = packet.type();

            JvmProfiler.INSTANCE.onPacketReceived(this.protocolInfo.id(), packettype, channelhandlercontext.channel().remoteAddress(), i);
            if (bytebuf.readableBytes() > 0) {
                String s = this.protocolInfo.id().id();

                throw new IOException("Packet " + s + "/" + String.valueOf(packettype) + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + bytebuf.readableBytes() + " bytes extra whilst reading packet " + String.valueOf(packettype));
            } else {
                list.add(packet);
                if (PacketDecoder.LOGGER.isDebugEnabled()) {
                    PacketDecoder.LOGGER.debug(NetworkManager.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {} -> {} bytes", new Object[]{this.protocolInfo.id().id(), packettype, packet.getClass().getName(), i});
                }

                ProtocolSwapHandler.handleInboundTerminalPacket(channelhandlercontext, packet);
            }
        }
    }
}
