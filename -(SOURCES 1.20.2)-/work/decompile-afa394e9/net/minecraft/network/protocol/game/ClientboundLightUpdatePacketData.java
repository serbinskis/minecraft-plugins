package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.chunk.NibbleArray;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacketData {

    private final BitSet skyYMask;
    private final BitSet blockYMask;
    private final BitSet emptySkyYMask;
    private final BitSet emptyBlockYMask;
    private final List<byte[]> skyUpdates;
    private final List<byte[]> blockUpdates;

    public ClientboundLightUpdatePacketData(ChunkCoordIntPair chunkcoordintpair, LevelLightEngine levellightengine, @Nullable BitSet bitset, @Nullable BitSet bitset1) {
        this.skyYMask = new BitSet();
        this.blockYMask = new BitSet();
        this.emptySkyYMask = new BitSet();
        this.emptyBlockYMask = new BitSet();
        this.skyUpdates = Lists.newArrayList();
        this.blockUpdates = Lists.newArrayList();

        for (int i = 0; i < levellightengine.getLightSectionCount(); ++i) {
            if (bitset == null || bitset.get(i)) {
                this.prepareSectionData(chunkcoordintpair, levellightengine, EnumSkyBlock.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
            }

            if (bitset1 == null || bitset1.get(i)) {
                this.prepareSectionData(chunkcoordintpair, levellightengine, EnumSkyBlock.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
            }
        }

    }

    public ClientboundLightUpdatePacketData(PacketDataSerializer packetdataserializer, int i, int j) {
        this.skyYMask = packetdataserializer.readBitSet();
        this.blockYMask = packetdataserializer.readBitSet();
        this.emptySkyYMask = packetdataserializer.readBitSet();
        this.emptyBlockYMask = packetdataserializer.readBitSet();
        this.skyUpdates = packetdataserializer.readList((packetdataserializer1) -> {
            return packetdataserializer1.readByteArray(2048);
        });
        this.blockUpdates = packetdataserializer.readList((packetdataserializer1) -> {
            return packetdataserializer1.readByteArray(2048);
        });
    }

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeBitSet(this.skyYMask);
        packetdataserializer.writeBitSet(this.blockYMask);
        packetdataserializer.writeBitSet(this.emptySkyYMask);
        packetdataserializer.writeBitSet(this.emptyBlockYMask);
        packetdataserializer.writeCollection(this.skyUpdates, PacketDataSerializer::writeByteArray);
        packetdataserializer.writeCollection(this.blockUpdates, PacketDataSerializer::writeByteArray);
    }

    private void prepareSectionData(ChunkCoordIntPair chunkcoordintpair, LevelLightEngine levellightengine, EnumSkyBlock enumskyblock, int i, BitSet bitset, BitSet bitset1, List<byte[]> list) {
        NibbleArray nibblearray = levellightengine.getLayerListener(enumskyblock).getDataLayerData(SectionPosition.of(chunkcoordintpair, levellightengine.getMinLightSection() + i));

        if (nibblearray != null) {
            if (nibblearray.isEmpty()) {
                bitset1.set(i);
            } else {
                bitset.set(i);
                list.add(nibblearray.copy().getData());
            }
        }

    }

    public BitSet getSkyYMask() {
        return this.skyYMask;
    }

    public BitSet getEmptySkyYMask() {
        return this.emptySkyYMask;
    }

    public List<byte[]> getSkyUpdates() {
        return this.skyUpdates;
    }

    public BitSet getBlockYMask() {
        return this.blockYMask;
    }

    public BitSet getEmptyBlockYMask() {
        return this.emptyBlockYMask;
    }

    public List<byte[]> getBlockUpdates() {
        return this.blockUpdates;
    }
}
