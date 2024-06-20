package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.codec.StreamCodec;

public class ProtocolInfoBuilder<T extends PacketListener, B extends ByteBuf> {

    final EnumProtocol protocol;
    final EnumProtocolDirection flow;
    private final List<ProtocolInfoBuilder.a<T, ?, B>> codecs = new ArrayList();
    @Nullable
    private BundlerInfo bundlerInfo;

    public ProtocolInfoBuilder(EnumProtocol enumprotocol, EnumProtocolDirection enumprotocoldirection) {
        this.protocol = enumprotocol;
        this.flow = enumprotocoldirection;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B> addPacket(PacketType<P> packettype, StreamCodec<? super B, P> streamcodec) {
        this.codecs.add(new ProtocolInfoBuilder.a<>(packettype, streamcodec));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B> withBundlePacket(PacketType<P> packettype, Function<Iterable<Packet<? super T>>, P> function, D d0) {
        StreamCodec<ByteBuf, D> streamcodec = StreamCodec.unit(d0);
        PacketType<D> packettype1 = d0.type();

        this.codecs.add(new ProtocolInfoBuilder.a<>(packettype1, streamcodec));
        this.bundlerInfo = BundlerInfo.createForPacket(packettype, function, d0);
        return this;
    }

    StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> function, List<ProtocolInfoBuilder.a<T, ?, B>> list) {
        ProtocolCodecBuilder<ByteBuf, T> protocolcodecbuilder = new ProtocolCodecBuilder<>(this.flow);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ProtocolInfoBuilder.a<T, ?, B> protocolinfobuilder_a = (ProtocolInfoBuilder.a) iterator.next();

            protocolinfobuilder_a.addToBuilder(protocolcodecbuilder, function);
        }

        return protocolcodecbuilder.build();
    }

    public ProtocolInfo<T> build(Function<ByteBuf, B> function) {
        return new ProtocolInfoBuilder.b<>(this.protocol, this.flow, this.buildPacketCodec(function, this.codecs), this.bundlerInfo);
    }

    public ProtocolInfo.a<T, B> buildUnbound() {
        final List<ProtocolInfoBuilder.a<T, ?, B>> list = List.copyOf(this.codecs);
        final BundlerInfo bundlerinfo = this.bundlerInfo;

        return new ProtocolInfo.a<T, B>() {
            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> function) {
                return new ProtocolInfoBuilder.b<>(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec(function, list), bundlerinfo);
            }

            @Override
            public EnumProtocol id() {
                return ProtocolInfoBuilder.this.protocol;
            }

            @Override
            public EnumProtocolDirection flow() {
                return ProtocolInfoBuilder.this.flow;
            }

            @Override
            public void listPackets(ProtocolInfo.a.a protocolinfo_a_a) {
                for (int i = 0; i < list.size(); ++i) {
                    ProtocolInfoBuilder.a<T, ?, B> protocolinfobuilder_a = (ProtocolInfoBuilder.a) list.get(i);

                    protocolinfo_a_a.accept(protocolinfobuilder_a.type, i);
                }

            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> ProtocolInfo.a<L, B> protocol(EnumProtocol enumprotocol, EnumProtocolDirection enumprotocoldirection, Consumer<ProtocolInfoBuilder<L, B>> consumer) {
        ProtocolInfoBuilder<L, B> protocolinfobuilder = new ProtocolInfoBuilder<>(enumprotocol, enumprotocoldirection);

        consumer.accept(protocolinfobuilder);
        return protocolinfobuilder.buildUnbound();
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf> ProtocolInfo.a<T, B> serverboundProtocol(EnumProtocol enumprotocol, Consumer<ProtocolInfoBuilder<T, B>> consumer) {
        return protocol(enumprotocol, EnumProtocolDirection.SERVERBOUND, consumer);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf> ProtocolInfo.a<T, B> clientboundProtocol(EnumProtocol enumprotocol, Consumer<ProtocolInfoBuilder<T, B>> consumer) {
        return protocol(enumprotocol, EnumProtocolDirection.CLIENTBOUND, consumer);
    }

    private static record a<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf>(PacketType<P> type, StreamCodec<? super B, P> serializer) {

        public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> protocolcodecbuilder, Function<ByteBuf, B> function) {
            StreamCodec<ByteBuf, P> streamcodec = this.serializer.mapStream(function);

            protocolcodecbuilder.add(this.type, streamcodec);
        }
    }

    private static record b<L extends PacketListener>(EnumProtocol id, EnumProtocolDirection flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo) implements ProtocolInfo<L> {

    }
}
