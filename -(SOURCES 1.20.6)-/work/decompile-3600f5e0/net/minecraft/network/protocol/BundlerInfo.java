package net.minecraft.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketListener;

public interface BundlerInfo {

    int BUNDLE_SIZE_LIMIT = 4096;

    static <T extends PacketListener, P extends BundlePacket<? super T>> BundlerInfo createForPacket(final PacketType<P> packettype, final Function<Iterable<Packet<? super T>>, P> function, final BundleDelimiterPacket<? super T> bundledelimiterpacket) {
        return new BundlerInfo() {
            @Override
            public void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
                if (packet.type() == packettype) {
                    P p0 = (BundlePacket) packet;

                    consumer.accept(bundledelimiterpacket);
                    p0.subPackets().forEach(consumer);
                    consumer.accept(bundledelimiterpacket);
                } else {
                    consumer.accept(packet);
                }

            }

            @Nullable
            @Override
            public BundlerInfo.a startPacketBundling(Packet<?> packet) {
                return packet == bundledelimiterpacket ? new BundlerInfo.a() {
                    private final List<Packet<? super T>> bundlePackets = new ArrayList();

                    @Nullable
                    @Override
                    public Packet<?> addPacket(Packet<?> packet1) {
                        if (packet1 == bundledelimiterpacket) {
                            return (Packet) function.apply(this.bundlePackets);
                        } else if (this.bundlePackets.size() >= 4096) {
                            throw new IllegalStateException("Too many packets in a bundle");
                        } else {
                            this.bundlePackets.add(packet1);
                            return null;
                        }
                    }
                } : null;
            }
        };
    }

    void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer);

    @Nullable
    BundlerInfo.a startPacketBundling(Packet<?> packet);

    public interface a {

        @Nullable
        Packet<?> addPacket(Packet<?> packet);
    }
}
