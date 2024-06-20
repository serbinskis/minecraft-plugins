package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;

public record ClientboundChunksBiomesPacket(List<ClientboundChunksBiomesPacket.a> chunkBiomeData) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundChunksBiomesPacket> STREAM_CODEC = Packet.codec(ClientboundChunksBiomesPacket::write, ClientboundChunksBiomesPacket::new);
    private static final int TWO_MEGABYTES = 2097152;

    private ClientboundChunksBiomesPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readList(ClientboundChunksBiomesPacket.a::new));
    }

    public static ClientboundChunksBiomesPacket forChunks(List<Chunk> list) {
        return new ClientboundChunksBiomesPacket(list.stream().map(ClientboundChunksBiomesPacket.a::new).toList());
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.chunkBiomeData, (packetdataserializer1, clientboundchunksbiomespacket_a) -> {
            clientboundchunksbiomespacket_a.write(packetdataserializer1);
        });
    }

    @Override
    public PacketType<ClientboundChunksBiomesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CHUNKS_BIOMES;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleChunksBiomes(this);
    }

    public static record a(ChunkCoordIntPair pos, byte[] buffer) {

        public a(Chunk chunk) {
            this(chunk.getPos(), new byte[calculateChunkSize(chunk)]);
            extractChunkData(new PacketDataSerializer(this.getWriteBuffer()), chunk);
        }

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readChunkPos(), packetdataserializer.readByteArray(2097152));
        }

        private static int calculateChunkSize(Chunk chunk) {
            int i = 0;
            ChunkSection[] achunksection = chunk.getSections();
            int j = achunksection.length;

            for (int k = 0; k < j; ++k) {
                ChunkSection chunksection = achunksection[k];

                i += chunksection.getBiomes().getSerializedSize();
            }

            return i;
        }

        public PacketDataSerializer getReadBuffer() {
            return new PacketDataSerializer(Unpooled.wrappedBuffer(this.buffer));
        }

        private ByteBuf getWriteBuffer() {
            ByteBuf bytebuf = Unpooled.wrappedBuffer(this.buffer);

            bytebuf.writerIndex(0);
            return bytebuf;
        }

        public static void extractChunkData(PacketDataSerializer packetdataserializer, Chunk chunk) {
            ChunkSection[] achunksection = chunk.getSections();
            int i = achunksection.length;

            for (int j = 0; j < i; ++j) {
                ChunkSection chunksection = achunksection[j];

                chunksection.getBiomes().write(packetdataserializer);
            }

        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeChunkPos(this.pos);
            packetdataserializer.writeByteArray(this.buffer);
        }
    }
}
