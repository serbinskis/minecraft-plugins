package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PacketDecompressor extends ByteToMessageDecoder {

    public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
    public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;
    private final Inflater inflater;
    private int threshold;
    private boolean validateDecompressed;

    public PacketDecompressor(int i, boolean flag) {
        this.threshold = i;
        this.validateDecompressed = flag;
        this.inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List<Object> list) throws Exception {
        if (bytebuf.readableBytes() != 0) {
            int i = VarInt.read(bytebuf);

            if (i == 0) {
                list.add(bytebuf.readBytes(bytebuf.readableBytes()));
            } else {
                if (this.validateDecompressed) {
                    if (i < this.threshold) {
                        throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
                    }

                    if (i > 8388608) {
                        throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 8388608");
                    }
                }

                this.setupInflaterInput(bytebuf);
                ByteBuf bytebuf1 = this.inflate(channelhandlercontext, i);

                this.inflater.reset();
                list.add(bytebuf1);
            }
        }
    }

    private void setupInflaterInput(ByteBuf bytebuf) {
        ByteBuffer bytebuffer;

        if (bytebuf.nioBufferCount() > 0) {
            bytebuffer = bytebuf.nioBuffer();
            bytebuf.skipBytes(bytebuf.readableBytes());
        } else {
            bytebuffer = ByteBuffer.allocateDirect(bytebuf.readableBytes());
            bytebuf.readBytes(bytebuffer);
            bytebuffer.flip();
        }

        this.inflater.setInput(bytebuffer);
    }

    private ByteBuf inflate(ChannelHandlerContext channelhandlercontext, int i) throws DataFormatException {
        ByteBuf bytebuf = channelhandlercontext.alloc().directBuffer(i);

        try {
            ByteBuffer bytebuffer = bytebuf.internalNioBuffer(0, i);
            int j = bytebuffer.position();

            this.inflater.inflate(bytebuffer);
            int k = bytebuffer.position() - j;

            if (k != i) {
                throw new DecoderException("Badly compressed packet - actual length of uncompressed payload " + k + " is does not match declared size " + i);
            } else {
                bytebuf.writerIndex(bytebuf.writerIndex() + k);
                return bytebuf;
            }
        } catch (Exception exception) {
            bytebuf.release();
            throw exception;
        }
    }

    public void setThreshold(int i, boolean flag) {
        this.threshold = i;
        this.validateDecompressed = flag;
    }
}
