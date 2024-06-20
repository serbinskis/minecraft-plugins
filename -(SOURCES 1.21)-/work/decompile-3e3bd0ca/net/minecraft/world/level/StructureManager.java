package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class StructureManager {

    public final GeneratorAccess level;
    private final WorldOptions worldOptions;
    private final StructureCheck structureCheck;

    public StructureManager(GeneratorAccess generatoraccess, WorldOptions worldoptions, StructureCheck structurecheck) {
        this.level = generatoraccess;
        this.worldOptions = worldoptions;
        this.structureCheck = structurecheck;
    }

    public StructureManager forWorldGenRegion(RegionLimitedWorldAccess regionlimitedworldaccess) {
        if (regionlimitedworldaccess.getLevel() != this.level) {
            String s = String.valueOf(regionlimitedworldaccess.getLevel());

            throw new IllegalStateException("Using invalid structure manager (source level: " + s + ", region: " + String.valueOf(regionlimitedworldaccess));
        } else {
            return new StructureManager(regionlimitedworldaccess, this.worldOptions, this.structureCheck);
        }
    }

    public List<StructureStart> startsForStructure(ChunkCoordIntPair chunkcoordintpair, Predicate<Structure> predicate) {
        Map<Structure, LongSet> map = this.level.getChunk(chunkcoordintpair.x, chunkcoordintpair.z, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
        Builder<StructureStart> builder = ImmutableList.builder();
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Structure, LongSet> entry = (Entry) iterator.next();
            Structure structure = (Structure) entry.getKey();

            if (predicate.test(structure)) {
                LongSet longset = (LongSet) entry.getValue();

                Objects.requireNonNull(builder);
                this.fillStartsForStructure(structure, longset, builder::add);
            }
        }

        return builder.build();
    }

    public List<StructureStart> startsForStructure(SectionPosition sectionposition, Structure structure) {
        LongSet longset = this.level.getChunk(sectionposition.x(), sectionposition.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(structure);
        Builder<StructureStart> builder = ImmutableList.builder();

        Objects.requireNonNull(builder);
        this.fillStartsForStructure(structure, longset, builder::add);
        return builder.build();
    }

    public void fillStartsForStructure(Structure structure, LongSet longset, Consumer<StructureStart> consumer) {
        LongIterator longiterator = longset.iterator();

        while (longiterator.hasNext()) {
            long i = (Long) longiterator.next();
            SectionPosition sectionposition = SectionPosition.of(new ChunkCoordIntPair(i), this.level.getMinSection());
            StructureStart structurestart = this.getStartForStructure(sectionposition, structure, this.level.getChunk(sectionposition.x(), sectionposition.z(), ChunkStatus.STRUCTURE_STARTS));

            if (structurestart != null && structurestart.isValid()) {
                consumer.accept(structurestart);
            }
        }

    }

    @Nullable
    public StructureStart getStartForStructure(SectionPosition sectionposition, Structure structure, StructureAccess structureaccess) {
        return structureaccess.getStartForStructure(structure);
    }

    public void setStartForStructure(SectionPosition sectionposition, Structure structure, StructureStart structurestart, StructureAccess structureaccess) {
        structureaccess.setStartForStructure(structure, structurestart);
    }

    public void addReferenceForStructure(SectionPosition sectionposition, Structure structure, long i, StructureAccess structureaccess) {
        structureaccess.addReferenceForStructure(structure, i);
    }

    public boolean shouldGenerateStructures() {
        return this.worldOptions.generateStructures();
    }

    public StructureStart getStructureAt(BlockPosition blockposition, Structure structure) {
        Iterator iterator = this.startsForStructure(SectionPosition.of(blockposition), structure).iterator();

        StructureStart structurestart;

        do {
            if (!iterator.hasNext()) {
                return StructureStart.INVALID_START;
            }

            structurestart = (StructureStart) iterator.next();
        } while (!structurestart.getBoundingBox().isInside(blockposition));

        return structurestart;
    }

    public StructureStart getStructureWithPieceAt(BlockPosition blockposition, TagKey<Structure> tagkey) {
        return this.getStructureWithPieceAt(blockposition, (holder) -> {
            return holder.is(tagkey);
        });
    }

    public StructureStart getStructureWithPieceAt(BlockPosition blockposition, HolderSet<Structure> holderset) {
        Objects.requireNonNull(holderset);
        return this.getStructureWithPieceAt(blockposition, holderset::contains);
    }

    public StructureStart getStructureWithPieceAt(BlockPosition blockposition, Predicate<Holder<Structure>> predicate) {
        IRegistry<Structure> iregistry = this.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Iterator iterator = this.startsForStructure(new ChunkCoordIntPair(blockposition), (structure) -> {
            Optional optional = iregistry.getHolder(iregistry.getId(structure));

            Objects.requireNonNull(predicate);
            return (Boolean) optional.map(predicate::test).orElse(false);
        }).iterator();

        StructureStart structurestart;

        do {
            if (!iterator.hasNext()) {
                return StructureStart.INVALID_START;
            }

            structurestart = (StructureStart) iterator.next();
        } while (!this.structureHasPieceAt(blockposition, structurestart));

        return structurestart;
    }

    public StructureStart getStructureWithPieceAt(BlockPosition blockposition, Structure structure) {
        Iterator iterator = this.startsForStructure(SectionPosition.of(blockposition), structure).iterator();

        StructureStart structurestart;

        do {
            if (!iterator.hasNext()) {
                return StructureStart.INVALID_START;
            }

            structurestart = (StructureStart) iterator.next();
        } while (!this.structureHasPieceAt(blockposition, structurestart));

        return structurestart;
    }

    public boolean structureHasPieceAt(BlockPosition blockposition, StructureStart structurestart) {
        Iterator iterator = structurestart.getPieces().iterator();

        StructurePiece structurepiece;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            structurepiece = (StructurePiece) iterator.next();
        } while (!structurepiece.getBoundingBox().isInside(blockposition));

        return true;
    }

    public boolean hasAnyStructureAt(BlockPosition blockposition) {
        SectionPosition sectionposition = SectionPosition.of(blockposition);

        return this.level.getChunk(sectionposition.x(), sectionposition.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
    }

    public Map<Structure, LongSet> getAllStructuresAt(BlockPosition blockposition) {
        SectionPosition sectionposition = SectionPosition.of(blockposition);

        return this.level.getChunk(sectionposition.x(), sectionposition.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
    }

    public StructureCheckResult checkStructurePresence(ChunkCoordIntPair chunkcoordintpair, Structure structure, StructurePlacement structureplacement, boolean flag) {
        return this.structureCheck.checkStart(chunkcoordintpair, structure, structureplacement, flag);
    }

    public void addReference(StructureStart structurestart) {
        structurestart.addReference();
        this.structureCheck.incrementReference(structurestart.getChunkPos(), structurestart.getStructure());
    }

    public IRegistryCustom registryAccess() {
        return this.level.registryAccess();
    }
}
