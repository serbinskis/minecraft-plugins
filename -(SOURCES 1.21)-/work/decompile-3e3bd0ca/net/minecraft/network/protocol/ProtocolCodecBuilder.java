package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;

public class ProtocolCodecBuilder<B extends ByteBuf, L extends PacketListener> {

    private final IdDispatchCodec.a<B, Packet<? super L>, PacketType<? extends Packet<? super L>>> dispatchBuilder = IdDispatchCodec.builder(Packet::type);
    private final EnumProtocolDirection flow;

    public ProtocolCodecBuilder(EnumProtocolDirection enumprotocoldirection) {
        this.flow = enumprotocoldirection;
    }

    public <T extends Packet<? super L>> ProtocolCodecBuilder<B, L> add(PacketType<T> packettype, StreamCodec<? super B, T> streamcodec) {
        if (packettype.flow() != this.flow) {
            String s = String.valueOf(packettype);

            throw new IllegalArgumentException("Invalid packet flow for packet " + s + ", expected " + this.flow.name());
        } else {
            this.dispatchBuilder.add(packettype, streamcodec);
            return this;
        }
    }

    public StreamCodec<B, Packet<? super L>> build() {
        return this.dispatchBuilder.build();
    }
}
