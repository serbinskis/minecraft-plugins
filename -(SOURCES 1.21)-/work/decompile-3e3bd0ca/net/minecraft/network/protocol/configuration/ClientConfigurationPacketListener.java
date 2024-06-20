package net.minecraft.network.protocol.configuration;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;

public interface ClientConfigurationPacketListener extends ClientCommonPacketListener {

    @Override
    default EnumProtocol protocol() {
        return EnumProtocol.CONFIGURATION;
    }

    void handleConfigurationFinished(ClientboundFinishConfigurationPacket clientboundfinishconfigurationpacket);

    void handleRegistryData(ClientboundRegistryDataPacket clientboundregistrydatapacket);

    void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket clientboundupdateenabledfeaturespacket);

    void handleSelectKnownPacks(ClientboundSelectKnownPacks clientboundselectknownpacks);

    void handleResetChat(ClientboundResetChatPacket clientboundresetchatpacket);
}
