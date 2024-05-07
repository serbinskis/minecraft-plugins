package net.minecraft.network.protocol.game;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;

public record PacketPlayOutMap(MapId mapId, byte scale, boolean locked, Optional<List<MapIcon>> decorations, Optional<WorldMap.b> colorPatch) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutMap> STREAM_CODEC = StreamCodec.composite(MapId.STREAM_CODEC, PacketPlayOutMap::mapId, ByteBufCodecs.BYTE, PacketPlayOutMap::scale, ByteBufCodecs.BOOL, PacketPlayOutMap::locked, MapIcon.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional), PacketPlayOutMap::decorations, WorldMap.b.STREAM_CODEC, PacketPlayOutMap::colorPatch, PacketPlayOutMap::new);

    public PacketPlayOutMap(MapId mapid, byte b0, boolean flag, @Nullable Collection<MapIcon> collection, @Nullable WorldMap.b worldmap_b) {
        this(mapid, b0, flag, collection != null ? Optional.of(List.copyOf(collection)) : Optional.empty(), Optional.ofNullable(worldmap_b));
    }

    @Override
    public PacketType<PacketPlayOutMap> type() {
        return GamePacketTypes.CLIENTBOUND_MAP_ITEM_DATA;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleMapItemData(this);
    }

    public void applyToMap(WorldMap worldmap) {
        Optional optional = this.decorations;

        Objects.requireNonNull(worldmap);
        optional.ifPresent(worldmap::addClientSideDecorations);
        this.colorPatch.ifPresent((worldmap_b) -> {
            worldmap_b.applyToMap(worldmap);
        });
    }
}
