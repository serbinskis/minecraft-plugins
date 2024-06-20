package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.MinecraftKey;

public interface CustomPacketPayload {

    CustomPacketPayload.b<? extends CustomPacketPayload> type();

    static <B extends ByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> streammemberencoder, StreamDecoder<B, T> streamdecoder) {
        return StreamCodec.ofMember(streammemberencoder, streamdecoder);
    }

    static <T extends CustomPacketPayload> CustomPacketPayload.b<T> createType(String s) {
        return new CustomPacketPayload.b<>(MinecraftKey.withDefaultNamespace(s));
    }

    static <B extends PacketDataSerializer> StreamCodec<B, CustomPacketPayload> codec(final CustomPacketPayload.a<B> custompacketpayload_a, List<CustomPacketPayload.c<? super B, ?>> list) {
        final Map<MinecraftKey, StreamCodec<? super B, ? extends CustomPacketPayload>> map = (Map) list.stream().collect(Collectors.toUnmodifiableMap((custompacketpayload_c) -> {
            return custompacketpayload_c.type().id();
        }, CustomPacketPayload.c::codec));

        return new StreamCodec<B, CustomPacketPayload>() {
            private StreamCodec<? super B, ? extends CustomPacketPayload> findCodec(MinecraftKey minecraftkey) {
                StreamCodec<? super B, ? extends CustomPacketPayload> streamcodec = (StreamCodec) map.get(minecraftkey);

                return streamcodec != null ? streamcodec : custompacketpayload_a.create(minecraftkey);
            }

            private <T extends CustomPacketPayload> void writeCap(B b0, CustomPacketPayload.b<T> custompacketpayload_b, CustomPacketPayload custompacketpayload) {
                b0.writeResourceLocation(custompacketpayload_b.id());
                StreamCodec<B, T> streamcodec = this.findCodec(custompacketpayload_b.id);

                streamcodec.encode(b0, custompacketpayload);
            }

            public void encode(B b0, CustomPacketPayload custompacketpayload) {
                this.writeCap(b0, custompacketpayload.type(), custompacketpayload);
            }

            public CustomPacketPayload decode(B b0) {
                MinecraftKey minecraftkey = b0.readResourceLocation();

                return (CustomPacketPayload) this.findCodec(minecraftkey).decode(b0);
            }
        };
    }

    public static record b<T extends CustomPacketPayload>(MinecraftKey id) {

    }

    public interface a<B extends PacketDataSerializer> {

        StreamCodec<B, ? extends CustomPacketPayload> create(MinecraftKey minecraftkey);
    }

    public static record c<B extends PacketDataSerializer, T extends CustomPacketPayload>(CustomPacketPayload.b<T> type, StreamCodec<B, T> codec) {

    }
}
