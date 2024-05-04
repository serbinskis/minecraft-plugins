package net.minecraft.network.protocol.configuration;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;

public interface ServerConfigurationPacketListener extends ServerCommonPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.CONFIGURATION;
    }

    void handleConfigurationFinished(ServerboundFinishConfigurationPacket serverboundfinishconfigurationpacket);

    void handleSelectKnownPacks(ServerboundSelectKnownPacks serverboundselectknownpacks);
}
