package net.minecraft.world.level.levelgen.structure.structures;

import java.util.Iterator;
import java.util.Map;
import net.minecraft.SystemUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.DefinedStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.WorldGenFeatureStructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlockIgnore;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

public class ShipwreckPieces {

    private static final int NUMBER_OF_BLOCKS_ALLOWED_IN_WORLD_GEN_REGION = 32;
    static final BlockPosition PIVOT = new BlockPosition(4, 0, 15);
    private static final MinecraftKey[] STRUCTURE_LOCATION_BEACHED = new MinecraftKey[]{MinecraftKey.withDefaultNamespace("shipwreck/with_mast"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_full"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_fronthalf"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_backhalf"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_full"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_fronthalf"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_backhalf"), MinecraftKey.withDefaultNamespace("shipwreck/with_mast_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_full_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_fronthalf_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_backhalf_degraded")};
    private static final MinecraftKey[] STRUCTURE_LOCATION_OCEAN = new MinecraftKey[]{MinecraftKey.withDefaultNamespace("shipwreck/with_mast"), MinecraftKey.withDefaultNamespace("shipwreck/upsidedown_full"), MinecraftKey.withDefaultNamespace("shipwreck/upsidedown_fronthalf"), MinecraftKey.withDefaultNamespace("shipwreck/upsidedown_backhalf"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_full"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_fronthalf"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_backhalf"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_full"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_fronthalf"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_backhalf"), MinecraftKey.withDefaultNamespace("shipwreck/with_mast_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/upsidedown_full_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/upsidedown_fronthalf_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/upsidedown_backhalf_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_full_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_fronthalf_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/sideways_backhalf_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_full_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_fronthalf_degraded"), MinecraftKey.withDefaultNamespace("shipwreck/rightsideup_backhalf_degraded")};
    static final Map<String, ResourceKey<LootTable>> MARKERS_TO_LOOT = Map.of("map_chest", LootTables.SHIPWRECK_MAP, "treasure_chest", LootTables.SHIPWRECK_TREASURE, "supply_chest", LootTables.SHIPWRECK_SUPPLY);

    public ShipwreckPieces() {}

    public static ShipwreckPieces.a addRandomPiece(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, EnumBlockRotation enumblockrotation, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, boolean flag) {
        MinecraftKey minecraftkey = (MinecraftKey) SystemUtils.getRandom((Object[]) (flag ? ShipwreckPieces.STRUCTURE_LOCATION_BEACHED : ShipwreckPieces.STRUCTURE_LOCATION_OCEAN), randomsource);
        ShipwreckPieces.a shipwreckpieces_a = new ShipwreckPieces.a(structuretemplatemanager, minecraftkey, blockposition, enumblockrotation, flag);

        structurepieceaccessor.addPiece(shipwreckpieces_a);
        return shipwreckpieces_a;
    }

    public static class a extends DefinedStructurePiece {

        private final boolean isBeached;

        public a(StructureTemplateManager structuretemplatemanager, MinecraftKey minecraftkey, BlockPosition blockposition, EnumBlockRotation enumblockrotation, boolean flag) {
            super(WorldGenFeatureStructurePieceType.SHIPWRECK_PIECE, 0, structuretemplatemanager, minecraftkey, minecraftkey.toString(), makeSettings(enumblockrotation), blockposition);
            this.isBeached = flag;
        }

        public a(StructureTemplateManager structuretemplatemanager, NBTTagCompound nbttagcompound) {
            super(WorldGenFeatureStructurePieceType.SHIPWRECK_PIECE, nbttagcompound, structuretemplatemanager, (minecraftkey) -> {
                return makeSettings(EnumBlockRotation.valueOf(nbttagcompound.getString("Rot")));
            });
            this.isBeached = nbttagcompound.getBoolean("isBeached");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound) {
            super.addAdditionalSaveData(structurepieceserializationcontext, nbttagcompound);
            nbttagcompound.putBoolean("isBeached", this.isBeached);
            nbttagcompound.putString("Rot", this.placeSettings.getRotation().name());
        }

        private static DefinedStructureInfo makeSettings(EnumBlockRotation enumblockrotation) {
            return (new DefinedStructureInfo()).setRotation(enumblockrotation).setMirror(EnumBlockMirror.NONE).setRotationPivot(ShipwreckPieces.PIVOT).addProcessor(DefinedStructureProcessorBlockIgnore.STRUCTURE_AND_AIR);
        }

        @Override
        protected void handleDataMarker(String s, BlockPosition blockposition, WorldAccess worldaccess, RandomSource randomsource, StructureBoundingBox structureboundingbox) {
            ResourceKey<LootTable> resourcekey = (ResourceKey) ShipwreckPieces.MARKERS_TO_LOOT.get(s);

            if (resourcekey != null) {
                RandomizableContainer.setBlockEntityLootTable(worldaccess, randomsource, blockposition.below(), resourcekey);
            }

        }

        @Override
        public void postProcess(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, StructureBoundingBox structureboundingbox, ChunkCoordIntPair chunkcoordintpair, BlockPosition blockposition) {
            if (this.isTooBigToFitInWorldGenRegion()) {
                super.postProcess(generatoraccessseed, structuremanager, chunkgenerator, randomsource, structureboundingbox, chunkcoordintpair, blockposition);
            } else {
                int i = generatoraccessseed.getMaxBuildHeight();
                int j = 0;
                BaseBlockPosition baseblockposition = this.template.getSize();
                HeightMap.Type heightmap_type = this.isBeached ? HeightMap.Type.WORLD_SURFACE_WG : HeightMap.Type.OCEAN_FLOOR_WG;
                int k = baseblockposition.getX() * baseblockposition.getZ();

                if (k == 0) {
                    j = generatoraccessseed.getHeight(heightmap_type, this.templatePosition.getX(), this.templatePosition.getZ());
                } else {
                    BlockPosition blockposition1 = this.templatePosition.offset(baseblockposition.getX() - 1, 0, baseblockposition.getZ() - 1);

                    int l;

                    for (Iterator iterator = BlockPosition.betweenClosed(this.templatePosition, blockposition1).iterator(); iterator.hasNext(); i = Math.min(i, l)) {
                        BlockPosition blockposition2 = (BlockPosition) iterator.next();

                        l = generatoraccessseed.getHeight(heightmap_type, blockposition2.getX(), blockposition2.getZ());
                        j += l;
                    }

                    j /= k;
                }

                this.adjustPositionHeight(this.isBeached ? this.calculateBeachedPosition(i, randomsource) : j);
                super.postProcess(generatoraccessseed, structuremanager, chunkgenerator, randomsource, structureboundingbox, chunkcoordintpair, blockposition);
            }
        }

        public boolean isTooBigToFitInWorldGenRegion() {
            BaseBlockPosition baseblockposition = this.template.getSize();

            return baseblockposition.getX() > 32 || baseblockposition.getY() > 32;
        }

        public int calculateBeachedPosition(int i, RandomSource randomsource) {
            return i - this.template.getSize().getY() / 2 - randomsource.nextInt(3);
        }

        public void adjustPositionHeight(int i) {
            this.templatePosition = new BlockPosition(this.templatePosition.getX(), i, this.templatePosition.getZ());
        }
    }
}
