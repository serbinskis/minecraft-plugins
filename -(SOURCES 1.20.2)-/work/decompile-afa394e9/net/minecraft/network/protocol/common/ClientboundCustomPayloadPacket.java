package net.minecraft.network.protocol.common;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventListenerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiAddedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiRemovedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiTicketCountDebugPayload;
import net.minecraft.network.protocol.common.custom.RaidsDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.network.protocol.common.custom.VillageSectionsDebugPayload;
import net.minecraft.network.protocol.common.custom.WorldGenAttemptDebugPayload;
import net.minecraft.resources.MinecraftKey;

public record ClientboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ClientCommonPacketListener> {

    private static final int MAX_PAYLOAD_SIZE = 1048576;
    private static final Map<MinecraftKey, PacketDataSerializer.a<? extends CustomPacketPayload>> KNOWN_TYPES = ImmutableMap.builder().put(BrandPayload.ID, BrandPayload::new).put(BeeDebugPayload.ID, BeeDebugPayload::new).put(BrainDebugPayload.ID, BrainDebugPayload::new).put(GameEventDebugPayload.ID, GameEventDebugPayload::new).put(GameEventListenerDebugPayload.ID, GameEventListenerDebugPayload::new).put(GameTestAddMarkerDebugPayload.ID, GameTestAddMarkerDebugPayload::new).put(GameTestClearMarkersDebugPayload.ID, GameTestClearMarkersDebugPayload::new).put(GoalDebugPayload.ID, GoalDebugPayload::new).put(HiveDebugPayload.ID, HiveDebugPayload::new).put(NeighborUpdatesDebugPayload.ID, NeighborUpdatesDebugPayload::new).put(PathfindingDebugPayload.ID, PathfindingDebugPayload::new).put(PoiAddedDebugPayload.ID, PoiAddedDebugPayload::new).put(PoiRemovedDebugPayload.ID, PoiRemovedDebugPayload::new).put(PoiTicketCountDebugPayload.ID, PoiTicketCountDebugPayload::new).put(RaidsDebugPayload.ID, RaidsDebugPayload::new).put(StructuresDebugPayload.ID, StructuresDebugPayload::new).put(VillageSectionsDebugPayload.ID, VillageSectionsDebugPayload::new).put(WorldGenAttemptDebugPayload.ID, WorldGenAttemptDebugPayload::new).build();

    public ClientboundCustomPayloadPacket(PacketDataSerializer packetdataserializer) {
        this(readPayload(packetdataserializer.readResourceLocation(), packetdataserializer));
    }

    private static CustomPacketPayload readPayload(MinecraftKey minecraftkey, PacketDataSerializer packetdataserializer) {
        PacketDataSerializer.a<? extends CustomPacketPayload> packetdataserializer_a = (PacketDataSerializer.a) ClientboundCustomPayloadPacket.KNOWN_TYPES.get(minecraftkey);

        return (CustomPacketPayload) (packetdataserializer_a != null ? (CustomPacketPayload) packetdataserializer_a.apply(packetdataserializer) : readUnknownPayload(minecraftkey, packetdataserializer));
    }

    private static DiscardedPayload readUnknownPayload(MinecraftKey minecraftkey, PacketDataSerializer packetdataserializer) {
        int i = packetdataserializer.readableBytes();

        if (i >= 0 && i <= 1048576) {
            packetdataserializer.skipBytes(i);
            return new DiscardedPayload(minecraftkey);
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeResourceLocation(this.payload.id());
        this.payload.write(packetdataserializer);
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleCustomPayload(this);
    }
}
