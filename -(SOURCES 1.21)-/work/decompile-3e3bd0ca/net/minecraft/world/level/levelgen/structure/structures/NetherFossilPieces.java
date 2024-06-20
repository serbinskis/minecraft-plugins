package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.DefinedStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.WorldGenFeatureStructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlockIgnore;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NetherFossilPieces {

    private static final MinecraftKey[] FOSSILS = new MinecraftKey[]{MinecraftKey.withDefaultNamespace("nether_fossils/fossil_1"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_2"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_3"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_4"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_5"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_6"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_7"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_8"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_9"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_10"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_11"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_12"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_13"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_14")};

    public NetherFossilPieces() {}

    public static void addPieces(StructureTemplateManager structuretemplatemanager, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, BlockPosition blockposition) {
        EnumBlockRotation enumblockrotation = EnumBlockRotation.getRandom(randomsource);

        structurepieceaccessor.addPiece(new NetherFossilPieces.a(structuretemplatemanager, (MinecraftKey) SystemUtils.getRandom((Object[]) NetherFossilPieces.FOSSILS, randomsource), blockposition, enumblockrotation));
    }

    public static class a extends DefinedStructurePiece {

        public a(StructureTemplateManager structuretemplatemanager, MinecraftKey minecraftkey, BlockPosition blockposition, EnumBlockRotation enumblockrotation) {
            super(WorldGenFeatureStructurePieceType.NETHER_FOSSIL, 0, structuretemplatemanager, minecraftkey, minecraftkey.toString(), makeSettings(enumblockrotation), blockposition);
        }

        public a(StructureTemplateManager structuretemplatemanager, NBTTagCompound nbttagcompound) {
            super(WorldGenFeatureStructurePieceType.NETHER_FOSSIL, nbttagcompound, structuretemplatemanager, (minecraftkey) -> {
                return makeSettings(EnumBlockRotation.valueOf(nbttagcompound.getString("Rot")));
            });
        }

        private static DefinedStructureInfo makeSettings(EnumBlockRotation enumblockrotation) {
            return (new DefinedStructureInfo()).setRotation(enumblockrotation).setMirror(EnumBlockMirror.NONE).addProcessor(DefinedStructureProcessorBlockIgnore.STRUCTURE_AND_AIR);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound) {
            super.addAdditionalSaveData(structurepieceserializationcontext, nbttagcompound);
            nbttagcompound.putString("Rot", this.placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(String s, BlockPosition blockposition, WorldAccess worldaccess, RandomSource randomsource, StructureBoundingBox structureboundingbox) {}

        @Override
        public void postProcess(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, StructureBoundingBox structureboundingbox, ChunkCoordIntPair chunkcoordintpair, BlockPosition blockposition) {
            structureboundingbox.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
            super.postProcess(generatoraccessseed, structuremanager, chunkgenerator, randomsource, structureboundingbox, chunkcoordintpair, blockposition);
        }
    }
}
