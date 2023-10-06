package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;

public class PacketFlowValidator extends MessageToMessageCodec<Packet<?>, Packet<?>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final AttributeKey<EnumProtocol.a<?>> decoderKey;
    private final AttributeKey<EnumProtocol.a<?>> encoderKey;

    public PacketFlowValidator(AttributeKey<EnumProtocol.a<?>> attributekey, AttributeKey<EnumProtocol.a<?>> attributekey1) {
        this.decoderKey = attributekey;
        this.encoderKey = attributekey1;
    }

    private static void validatePacket(ChannelHandlerContext channelhandlercontext, Packet<?> packet, List<Object> list, AttributeKey<EnumProtocol.a<?>> attributekey) {
        Attribute<EnumProtocol.a<?>> attribute = channelhandlercontext.channel().attr(attributekey);
        EnumProtocol.a<?> enumprotocol_a = (EnumProtocol.a) attribute.get();

        if (!enumprotocol_a.isValidPacketType(packet)) {
            PacketFlowValidator.LOGGER.error("Unrecognized packet in pipeline {}:{} - {}", new Object[]{enumprotocol_a.protocol().id(), enumprotocol_a.flow(), packet});
        }

        ReferenceCountUtil.retain(packet);
        list.add(packet);
        ProtocolSwapHandler.swapProtocolIfNeeded(attribute, packet);
    }

    protected void decode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, List<Object> list) throws Exception {
        validatePacket(channelhandlercontext, packet, list, this.decoderKey);
    }

    protected void encode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, List<Object> list) throws Exception {
        validatePacket(channelhandlercontext, packet, list, this.encoderKey);
    }
}
