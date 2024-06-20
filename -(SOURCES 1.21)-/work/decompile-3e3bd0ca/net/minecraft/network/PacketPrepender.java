package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class PacketPrepender extends MessageToByteEncoder<ByteBuf> {

    public static final int MAX_VARINT21_BYTES = 3;

    public PacketPrepender() {}

    protected void encode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, ByteBuf bytebuf1) {
        int i = bytebuf.readableBytes();
        int j = VarInt.getByteSize(i);

        if (j > 3) {
            throw new EncoderException("Packet too large: size " + i + " is over 8");
        } else {
            bytebuf1.ensureWritable(j + i);
            VarInt.write(bytebuf1, i);
            bytebuf1.writeBytes(bytebuf, bytebuf.readerIndex(), i);
        }
    }
}
