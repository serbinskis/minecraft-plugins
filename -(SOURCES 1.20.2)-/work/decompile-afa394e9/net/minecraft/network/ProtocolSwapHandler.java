package net.minecraft.network;

import io.netty.util.Attribute;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {

    static void swapProtocolIfNeeded(Attribute<EnumProtocol.a<?>> attribute, Packet<?> packet) {
        EnumProtocol enumprotocol = packet.nextProtocol();

        if (enumprotocol != null) {
            EnumProtocol.a<?> enumprotocol_a = (EnumProtocol.a) attribute.get();
            EnumProtocol enumprotocol1 = enumprotocol_a.protocol();

            if (enumprotocol != enumprotocol1) {
                EnumProtocol.a<?> enumprotocol_a1 = enumprotocol.codec(enumprotocol_a.flow());

                attribute.set(enumprotocol_a1);
            }
        }

    }
}
