package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;

public class PacketBundlePacker extends MessageToMessageDecoder<Packet<?>> {

    private final BundlerInfo bundlerInfo;
    @Nullable
    private BundlerInfo.a currentBundler;

    public PacketBundlePacker(BundlerInfo bundlerinfo) {
        this.bundlerInfo = bundlerinfo;
    }

    protected void decode(ChannelHandlerContext channelhandlercontext, Packet<?> packet, List<Object> list) throws Exception {
        if (this.currentBundler != null) {
            verifyNonTerminalPacket(packet);
            Packet<?> packet1 = this.currentBundler.addPacket(packet);

            if (packet1 != null) {
                this.currentBundler = null;
                list.add(packet1);
            }
        } else {
            BundlerInfo.a bundlerinfo_a = this.bundlerInfo.startPacketBundling(packet);

            if (bundlerinfo_a != null) {
                verifyNonTerminalPacket(packet);
                this.currentBundler = bundlerinfo_a;
            } else {
                list.add(packet);
                if (packet.isTerminal()) {
                    channelhandlercontext.pipeline().remove(channelhandlercontext.name());
                }
            }
        }

    }

    private static void verifyNonTerminalPacket(Packet<?> packet) {
        if (packet.isTerminal()) {
            throw new DecoderException("Terminal message received in bundle");
        }
    }
}
