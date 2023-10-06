package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder extends ByteToMessageDecoder implements ProtocolSwapHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final AttributeKey<EnumProtocol.a<?>> codecKey;

    public PacketDecoder(AttributeKey<EnumProtocol.a<?>> attributekey) {
        this.codecKey = attributekey;
    }

    protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List<Object> list) throws Exception {
        int i = bytebuf.readableBytes();

        if (i != 0) {
            Attribute<EnumProtocol.a<?>> attribute = channelhandlercontext.channel().attr(this.codecKey);
            EnumProtocol.a<?> enumprotocol_a = (EnumProtocol.a) attribute.get();
            PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf);
            int j = packetdataserializer.readVarInt();
            Packet<?> packet = enumprotocol_a.createPacket(j, packetdataserializer);

            if (packet == null) {
                throw new IOException("Bad packet id " + j);
            } else {
                JvmProfiler.INSTANCE.onPacketReceived(enumprotocol_a.protocol(), j, channelhandlercontext.channel().remoteAddress(), i);
                if (packetdataserializer.readableBytes() > 0) {
                    String s = enumprotocol_a.protocol().id();

                    throw new IOException("Packet " + s + "/" + j + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + packetdataserializer.readableBytes() + " bytes extra whilst reading packet " + j);
                } else {
                    list.add(packet);
                    if (PacketDecoder.LOGGER.isDebugEnabled()) {
                        PacketDecoder.LOGGER.debug(NetworkManager.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", new Object[]{enumprotocol_a.protocol().id(), j, packet.getClass().getName()});
                    }

                    ProtocolSwapHandler.swapProtocolIfNeeded(attribute, packet);
                }
            }
        }
    }
}
