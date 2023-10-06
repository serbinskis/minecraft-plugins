package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.IOException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final AttributeKey<EnumProtocol.a<?>> codecKey;

    public PacketEncoder(AttributeKey<EnumProtocol.a<?>> attributekey) {
        this.codecKey = attributekey;
    }

    protected void encode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, ByteBuf bytebuf) throws Exception {
        Attribute<EnumProtocol.a<?>> attribute = channelhandlercontext.channel().attr(this.codecKey);
        EnumProtocol.a<?> enumprotocol_a = (EnumProtocol.a) attribute.get();

        if (enumprotocol_a == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + packet);
        } else {
            int i = enumprotocol_a.packetId(packet);

            if (PacketEncoder.LOGGER.isDebugEnabled()) {
                PacketEncoder.LOGGER.debug(NetworkManager.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", new Object[]{enumprotocol_a.protocol().id(), i, packet.getClass().getName()});
            }

            if (i == -1) {
                throw new IOException("Can't serialize unregistered packet");
            } else {
                PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf);

                packetdataserializer.writeVarInt(i);

                try {
                    int j = packetdataserializer.writerIndex();

                    packet.write(packetdataserializer);
                    int k = packetdataserializer.writerIndex() - j;

                    if (k > 8388608) {
                        throw new IllegalArgumentException("Packet too big (is " + k + ", should be less than 8388608): " + packet);
                    }

                    JvmProfiler.INSTANCE.onPacketSent(enumprotocol_a.protocol(), i, channelhandlercontext.channel().remoteAddress(), k);
                } catch (Throwable throwable) {
                    PacketEncoder.LOGGER.error("Error receiving packet {}", i, throwable);
                    if (packet.isSkippable()) {
                        throw new SkipEncodeException(throwable);
                    }

                    throw throwable;
                } finally {
                    ProtocolSwapHandler.swapProtocolIfNeeded(attribute, packet);
                }

            }
        }
    }
}
