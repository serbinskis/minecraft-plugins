package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.VisibleForDebug;

public interface ProtocolInfo<T extends PacketListener> {

    EnumProtocol id();

    EnumProtocolDirection flow();

    StreamCodec<ByteBuf, Packet<? super T>> codec();

    @Nullable
    BundlerInfo bundlerInfo();

    public interface a<T extends PacketListener, B extends ByteBuf> {

        ProtocolInfo<T> bind(Function<ByteBuf, B> function);

        EnumProtocol id();

        EnumProtocolDirection flow();

        @VisibleForDebug
        void listPackets(ProtocolInfo.a.a protocolinfo_a_a);

        @FunctionalInterface
        public interface a {

            void accept(PacketType<?> packettype, int i);
        }
    }
}
