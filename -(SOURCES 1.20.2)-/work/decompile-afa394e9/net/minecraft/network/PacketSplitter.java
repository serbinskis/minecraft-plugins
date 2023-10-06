package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import javax.annotation.Nullable;

public class PacketSplitter extends ByteToMessageDecoder {

    private static final int MAX_VARINT21_BYTES = 3;
    private final ByteBuf helperBuf = Unpooled.directBuffer(3);
    @Nullable
    private final BandwidthDebugMonitor monitor;

    public PacketSplitter(@Nullable BandwidthDebugMonitor bandwidthdebugmonitor) {
        this.monitor = bandwidthdebugmonitor;
    }

    protected void handlerRemoved0(ChannelHandlerContext channelhandlercontext) {
        this.helperBuf.release();
    }

    private static boolean copyVarint(ByteBuf bytebuf, ByteBuf bytebuf1) {
        for (int i = 0; i < 3; ++i) {
            if (!bytebuf.isReadable()) {
                return false;
            }

            byte b0 = bytebuf.readByte();

            bytebuf1.writeByte(b0);
            if (!VarInt.hasContinuationBit(b0)) {
                return true;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }

    protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List<Object> list) {
        bytebuf.markReaderIndex();
        this.helperBuf.clear();
        if (!copyVarint(bytebuf, this.helperBuf)) {
            bytebuf.resetReaderIndex();
        } else {
            int i = VarInt.read(this.helperBuf);

            if (bytebuf.readableBytes() < i) {
                bytebuf.resetReaderIndex();
            } else {
                if (this.monitor != null) {
                    this.monitor.onReceive(i + VarInt.getByteSize(i));
                }

                list.add(bytebuf.readBytes(i));
            }
        }
    }
}
