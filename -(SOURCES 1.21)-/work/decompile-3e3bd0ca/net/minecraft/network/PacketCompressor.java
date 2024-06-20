package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf> {

    private final byte[] encodeBuf = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public PacketCompressor(int i) {
        this.threshold = i;
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, ByteBuf bytebuf1) {
        int i = bytebuf.readableBytes();

        if (i > 8388608) {
            throw new IllegalArgumentException("Packet too big (is " + i + ", should be less than 8388608)");
        } else {
            if (i < this.threshold) {
                VarInt.write(bytebuf1, 0);
                bytebuf1.writeBytes(bytebuf);
            } else {
                byte[] abyte = new byte[i];

                bytebuf.readBytes(abyte);
                VarInt.write(bytebuf1, abyte.length);
                this.deflater.setInput(abyte, 0, i);
                this.deflater.finish();

                while (!this.deflater.finished()) {
                    int j = this.deflater.deflate(this.encodeBuf);

                    bytebuf1.writeBytes(this.encodeBuf, 0, j);
                }

                this.deflater.reset();
            }

        }
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int i) {
        this.threshold = i;
    }
}
