package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;

public class PacketBundleUnpacker extends MessageToMessageEncoder<Packet<?>> {

    private final BundlerInfo bundlerInfo;

    public PacketBundleUnpacker(BundlerInfo bundlerinfo) {
        this.bundlerInfo = bundlerinfo;
    }

    protected void encode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, List<Object> list) throws Exception {
        BundlerInfo bundlerinfo = this.bundlerInfo;

        Objects.requireNonNull(list);
        bundlerinfo.unbundlePacket(packet, list::add);
        if (packet.isTerminal()) {
            channelhandlercontext.pipeline().remove(channelhandlercontext.name());
        }

    }
}
