package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.SystemUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.ArraySetSorted;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.LootTables;

public class DesertPyramidStructure extends SinglePieceStructure {

    public static final MapCodec<DesertPyramidStructure> CODEC = simpleCodec(DesertPyramidStructure::new);

    public DesertPyramidStructure(Structure.c structure_c) {
        super(DesertPyramidPiece::new, 21, 21, structure_c);
    }

    @Override
    public void afterPlace(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, StructureBoundingBox structureboundingbox, ChunkCoordIntPair chunkcoordintpair, PiecesContainer piecescontainer) {
        Set<BlockPosition> set = ArraySetSorted.create(BaseBlockPosition::compareTo);
        Iterator iterator = piecescontainer.pieces().iterator();

        while (iterator.hasNext()) {
            StructurePiece structurepiece = (StructurePiece) iterator.next();

            if (structurepiece instanceof DesertPyramidPiece desertpyramidpiece) {
                set.addAll(desertpyramidpiece.getPotentialSuspiciousSandWorldPositions());
                placeSuspiciousSand(structureboundingbox, generatoraccessseed, desertpyramidpiece.getRandomCollapsedRoofPos());
            }
        }

        ObjectArrayList<BlockPosition> objectarraylist = new ObjectArrayList(set.stream().toList());
        RandomSource randomsource1 = RandomSource.create(generatoraccessseed.getSeed()).forkPositional().at(piecescontainer.calculateBoundingBox().getCenter());

        SystemUtils.shuffle(objectarraylist, randomsource1);
        int i = Math.min(set.size(), randomsource1.nextInt(5, 8));
        ObjectListIterator objectlistiterator = objectarraylist.iterator();

        while (objectlistiterator.hasNext()) {
            BlockPosition blockposition = (BlockPosition) objectlistiterator.next();

            if (i > 0) {
                --i;
                placeSuspiciousSand(structureboundingbox, generatoraccessseed, blockposition);
            } else if (structureboundingbox.isInside(blockposition)) {
                generatoraccessseed.setBlock(blockposition, Blocks.SAND.defaultBlockState(), 2);
            }
        }

    }

    private static void placeSuspiciousSand(StructureBoundingBox structureboundingbox, GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        if (structureboundingbox.isInside(blockposition)) {
            // CraftBukkit start
            if (generatoraccessseed instanceof org.bukkit.craftbukkit.util.TransformerGeneratorAccess transformerAccess) {
                org.bukkit.craftbukkit.block.CraftBrushableBlock brushableState = (org.bukkit.craftbukkit.block.CraftBrushableBlock) org.bukkit.craftbukkit.block.CraftBlockStates.getBlockState(generatoraccessseed, blockposition, Blocks.SUSPICIOUS_SAND.defaultBlockState(), null);
                brushableState.setLootTable(org.bukkit.craftbukkit.CraftLootTable.minecraftToBukkit(LootTables.DESERT_PYRAMID_ARCHAEOLOGY));
                brushableState.setSeed(blockposition.asLong());
                transformerAccess.setCraftBlock(blockposition, brushableState, 2);
                return;
            }
            // CraftBukkit end
            generatoraccessseed.setBlock(blockposition, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
            generatoraccessseed.getBlockEntity(blockposition, TileEntityTypes.BRUSHABLE_BLOCK).ifPresent((brushableblockentity) -> {
                brushableblockentity.setLootTable(LootTables.DESERT_PYRAMID_ARCHAEOLOGY, blockposition.asLong());
            });
        }

    }

    @Override
    public StructureType<?> type() {
        return StructureType.DESERT_PYRAMID;
    }
}
