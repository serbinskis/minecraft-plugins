package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;

public class Utf8String {

    public Utf8String() {}

    public static String read(ByteBuf bytebuf, int i) {
        int j = ByteBufUtil.utf8MaxBytes(i);
        int k = VarInt.read(bytebuf);

        if (k > j) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
        } else if (k < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            int l = bytebuf.readableBytes();

            if (k > l) {
                throw new DecoderException("Not enough bytes in buffer, expected " + k + ", but got " + l);
            } else {
                String s = bytebuf.toString(bytebuf.readerIndex(), k, StandardCharsets.UTF_8);

                bytebuf.readerIndex(bytebuf.readerIndex() + k);
                if (s.length() > i) {
                    int i1 = s.length();

                    throw new DecoderException("The received string length is longer than maximum allowed (" + i1 + " > " + i + ")");
                } else {
                    return s;
                }
            }
        }
    }

    public static void write(ByteBuf bytebuf, CharSequence charsequence, int i) {
        if (charsequence.length() > i) {
            int j = charsequence.length();

            throw new EncoderException("String too big (was " + j + " characters, max " + i + ")");
        } else {
            int k = ByteBufUtil.utf8MaxBytes(charsequence);
            ByteBuf bytebuf1 = bytebuf.alloc().buffer(k);

            try {
                int l = ByteBufUtil.writeUtf8(bytebuf1, charsequence);
                int i1 = ByteBufUtil.utf8MaxBytes(i);

                if (l > i1) {
                    throw new EncoderException("String too big (was " + l + " bytes encoded, max " + i1 + ")");
                }

                VarInt.write(bytebuf, l);
                bytebuf.writeBytes(bytebuf1);
            } finally {
                bytebuf1.release();
            }

        }
    }
}
