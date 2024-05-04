package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.protocol.Packet;

public class UnconfiguredPipelineHandler {

    public UnconfiguredPipelineHandler() {}

    public static <T extends PacketListener> UnconfiguredPipelineHandler.b setupInboundProtocol(ProtocolInfo<T> protocolinfo) {
        return setupInboundHandler(new PacketDecoder<>(protocolinfo));
    }

    private static UnconfiguredPipelineHandler.b setupInboundHandler(ChannelInboundHandler channelinboundhandler) {
        return (channelhandlercontext) -> {
            channelhandlercontext.pipeline().replace(channelhandlercontext.name(), "decoder", channelinboundhandler);
            channelhandlercontext.channel().config().setAutoRead(true);
        };
    }

    public static <T extends PacketListener> UnconfiguredPipelineHandler.d setupOutboundProtocol(ProtocolInfo<T> protocolinfo) {
        return setupOutboundHandler(new PacketEncoder<>(protocolinfo));
    }

    private static UnconfiguredPipelineHandler.d setupOutboundHandler(ChannelOutboundHandler channeloutboundhandler) {
        return (channelhandlercontext) -> {
            channelhandlercontext.pipeline().replace(channelhandlercontext.name(), "encoder", channeloutboundhandler);
        };
    }

    @FunctionalInterface
    public interface b {

        void run(ChannelHandlerContext channelhandlercontext);

        default UnconfiguredPipelineHandler.b andThen(UnconfiguredPipelineHandler.b unconfiguredpipelinehandler_b) {
            return (channelhandlercontext) -> {
                this.run(channelhandlercontext);
                unconfiguredpipelinehandler_b.run(channelhandlercontext);
            };
        }
    }

    @FunctionalInterface
    public interface d {

        void run(ChannelHandlerContext channelhandlercontext);

        default UnconfiguredPipelineHandler.d andThen(UnconfiguredPipelineHandler.d unconfiguredpipelinehandler_d) {
            return (channelhandlercontext) -> {
                this.run(channelhandlercontext);
                unconfiguredpipelinehandler_d.run(channelhandlercontext);
            };
        }
    }

    public static class c extends ChannelOutboundHandlerAdapter {

        public c() {}

        public void write(ChannelHandlerContext channelhandlercontext, Object object, ChannelPromise channelpromise) throws Exception {
            if (object instanceof Packet) {
                ReferenceCountUtil.release(object);
                throw new EncoderException("Pipeline has no outbound protocol configured, can't process packet " + String.valueOf(object));
            } else {
                if (object instanceof UnconfiguredPipelineHandler.d) {
                    UnconfiguredPipelineHandler.d unconfiguredpipelinehandler_d = (UnconfiguredPipelineHandler.d) object;

                    try {
                        unconfiguredpipelinehandler_d.run(channelhandlercontext);
                    } finally {
                        ReferenceCountUtil.release(object);
                    }

                    channelpromise.setSuccess();
                } else {
                    channelhandlercontext.write(object, channelpromise);
                }

            }
        }
    }

    public static class a extends ChannelDuplexHandler {

        public a() {}

        public void channelRead(ChannelHandlerContext channelhandlercontext, Object object) {
            if (!(object instanceof ByteBuf) && !(object instanceof Packet)) {
                channelhandlercontext.fireChannelRead(object);
            } else {
                ReferenceCountUtil.release(object);
                throw new DecoderException("Pipeline has no inbound protocol configured, can't process packet " + String.valueOf(object));
            }
        }

        public void write(ChannelHandlerContext channelhandlercontext, Object object, ChannelPromise channelpromise) throws Exception {
            if (object instanceof UnconfiguredPipelineHandler.b unconfiguredpipelinehandler_b) {
                try {
                    unconfiguredpipelinehandler_b.run(channelhandlercontext);
                } finally {
                    ReferenceCountUtil.release(object);
                }

                channelpromise.setSuccess();
            } else {
                channelhandlercontext.write(object, channelpromise);
            }

        }
    }
}
