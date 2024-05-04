package net.minecraft.network.protocol.configuration;

import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class ConfigurationPacketTypes {

    public static final PacketType<ClientboundFinishConfigurationPacket> CLIENTBOUND_FINISH_CONFIGURATION = createClientbound("finish_configuration");
    public static final PacketType<ClientboundRegistryDataPacket> CLIENTBOUND_REGISTRY_DATA = createClientbound("registry_data");
    public static final PacketType<ClientboundUpdateEnabledFeaturesPacket> CLIENTBOUND_UPDATE_ENABLED_FEATURES = createClientbound("update_enabled_features");
    public static final PacketType<ClientboundSelectKnownPacks> CLIENTBOUND_SELECT_KNOWN_PACKS = createClientbound("select_known_packs");
    public static final PacketType<ClientboundResetChatPacket> CLIENTBOUND_RESET_CHAT = createClientbound("reset_chat");
    public static final PacketType<ServerboundFinishConfigurationPacket> SERVERBOUND_FINISH_CONFIGURATION = createServerbound("finish_configuration");
    public static final PacketType<ServerboundSelectKnownPacks> SERVERBOUND_SELECT_KNOWN_PACKS = createServerbound("select_known_packs");

    public ConfigurationPacketTypes() {}

    private static <T extends Packet<ClientConfigurationPacketListener>> PacketType<T> createClientbound(String s) {
        return new PacketType<>(EnumProtocolDirection.CLIENTBOUND, new MinecraftKey(s));
    }

    private static <T extends Packet<ServerConfigurationPacketListener>> PacketType<T> createServerbound(String s) {
        return new PacketType<>(EnumProtocolDirection.SERVERBOUND, new MinecraftKey(s));
    }
}
