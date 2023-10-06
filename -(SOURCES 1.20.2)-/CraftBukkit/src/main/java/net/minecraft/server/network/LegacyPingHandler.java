package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.SocketAddress;
import java.util.Locale;
import net.minecraft.server.ServerInfo;
import org.slf4j.Logger;

public class LegacyPingHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerInfo server;

    public LegacyPingHandler(ServerInfo serverinfo) {
        this.server = serverinfo;
    }

    public void channelRead(ChannelHandlerContext channelhandlercontext, Object object) {
        ByteBuf bytebuf = (ByteBuf) object;

        bytebuf.markReaderIndex();
        boolean flag = true;

        try {
            try {
                if (bytebuf.readUnsignedByte() != 254) {
                    return;
                }

                SocketAddress socketaddress = channelhandlercontext.channel().remoteAddress();
                int i = bytebuf.readableBytes();
                String s;
                org.bukkit.event.server.ServerListPingEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callServerListPingEvent(socketaddress, server.getMotd(), server.getPlayerCount(), server.getMaxPlayers()); // CraftBukkit

                if (i == 0) {
                    LegacyPingHandler.LOGGER.debug("Ping: (<1.3.x) from {}", socketaddress);
                    s = createVersion0Response(this.server, event); // CraftBukkit
                    sendFlushAndClose(channelhandlercontext, createLegacyDisconnectPacket(channelhandlercontext.alloc(), s));
                } else {
                    if (bytebuf.readUnsignedByte() != 1) {
                        return;
                    }

                    if (bytebuf.isReadable()) {
                        if (!readCustomPayloadPacket(bytebuf)) {
                            return;
                        }

                        LegacyPingHandler.LOGGER.debug("Ping: (1.6) from {}", socketaddress);
                    } else {
                        LegacyPingHandler.LOGGER.debug("Ping: (1.4-1.5.x) from {}", socketaddress);
                    }

                    s = createVersion1Response(this.server, event); // CraftBukkit
                    sendFlushAndClose(channelhandlercontext, createLegacyDisconnectPacket(channelhandlercontext.alloc(), s));
                }

                bytebuf.release();
                flag = false;
            } catch (RuntimeException runtimeexception) {
                ;
            }

        } finally {
            if (flag) {
                bytebuf.resetReaderIndex();
                channelhandlercontext.channel().pipeline().remove(this);
                channelhandlercontext.fireChannelRead(object);
            }

        }
    }

    private static boolean readCustomPayloadPacket(ByteBuf bytebuf) {
        short short0 = bytebuf.readUnsignedByte();

        if (short0 != 250) {
            return false;
        } else {
            String s = LegacyProtocolUtils.readLegacyString(bytebuf);

            if (!"MC|PingHost".equals(s)) {
                return false;
            } else {
                int i = bytebuf.readUnsignedShort();

                if (bytebuf.readableBytes() != i) {
                    return false;
                } else {
                    short short1 = bytebuf.readUnsignedByte();

                    if (short1 < 73) {
                        return false;
                    } else {
                        String s1 = LegacyProtocolUtils.readLegacyString(bytebuf);
                        int j = bytebuf.readInt();

                        return j <= 65535;
                    }
                }
            }
        }
    }

    // CraftBukkit start
    private static String createVersion0Response(ServerInfo serverinfo, org.bukkit.event.server.ServerListPingEvent event) {
        return String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", event.getMotd(), event.getNumPlayers(), event.getMaxPlayers());
        // CraftBukkit end
    }

    // CraftBukkit start
    private static String createVersion1Response(ServerInfo serverinfo, org.bukkit.event.server.ServerListPingEvent event) {
        return String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, serverinfo.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers());
        // CraftBukkit end
    }

    private static void sendFlushAndClose(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf) {
        channelhandlercontext.pipeline().firstContext().writeAndFlush(bytebuf).addListener(ChannelFutureListener.CLOSE);
    }

    private static ByteBuf createLegacyDisconnectPacket(ByteBufAllocator bytebufallocator, String s) {
        ByteBuf bytebuf = bytebufallocator.buffer();

        bytebuf.writeByte(255);
        LegacyProtocolUtils.writeLegacyString(bytebuf, s);
        return bytebuf;
    }
}
