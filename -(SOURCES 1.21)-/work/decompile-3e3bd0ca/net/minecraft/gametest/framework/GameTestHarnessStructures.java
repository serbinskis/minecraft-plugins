package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.commands.arguments.blocks.ArgumentTileLocation;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityCommand;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyStructureMode;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class GameTestHarnessStructures {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DEFAULT_Y_SEARCH_RADIUS = 10;
    public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
    public static String testStructuresDir = "gameteststructures";

    public GameTestHarnessStructures() {}

    public static EnumBlockRotation getRotationForRotationSteps(int i) {
        switch (i) {
            case 0:
                return EnumBlockRotation.NONE;
            case 1:
                return EnumBlockRotation.CLOCKWISE_90;
            case 2:
                return EnumBlockRotation.CLOCKWISE_180;
            case 3:
                return EnumBlockRotation.COUNTERCLOCKWISE_90;
            default:
                throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + i);
        }
    }

    public static int getRotationStepsForRotation(EnumBlockRotation enumblockrotation) {
        switch (enumblockrotation) {
            case NONE:
                return 0;
            case CLOCKWISE_90:
                return 1;
            case CLOCKWISE_180:
                return 2;
            case COUNTERCLOCKWISE_90:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + String.valueOf(enumblockrotation));
        }
    }

    public static AxisAlignedBB getStructureBounds(TileEntityStructure tileentitystructure) {
        return AxisAlignedBB.of(getStructureBoundingBox(tileentitystructure));
    }

    public static StructureBoundingBox getStructureBoundingBox(TileEntityStructure tileentitystructure) {
        BlockPosition blockposition = getStructureOrigin(tileentitystructure);
        BlockPosition blockposition1 = getTransformedFarCorner(blockposition, tileentitystructure.getStructureSize(), tileentitystructure.getRotation());

        return StructureBoundingBox.fromCorners(blockposition, blockposition1);
    }

    public static BlockPosition getStructureOrigin(TileEntityStructure tileentitystructure) {
        return tileentitystructure.getBlockPos().offset(tileentitystructure.getStructurePos());
    }

    public static void addCommandBlockAndButtonToStartTest(BlockPosition blockposition, BlockPosition blockposition1, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        BlockPosition blockposition2 = DefinedStructure.transform(blockposition.offset(blockposition1), EnumBlockMirror.NONE, enumblockrotation, blockposition);

        worldserver.setBlockAndUpdate(blockposition2, Blocks.COMMAND_BLOCK.defaultBlockState());
        TileEntityCommand tileentitycommand = (TileEntityCommand) worldserver.getBlockEntity(blockposition2);

        tileentitycommand.getCommandBlock().setCommand("test runclosest");
        BlockPosition blockposition3 = DefinedStructure.transform(blockposition2.offset(0, 0, -1), EnumBlockMirror.NONE, enumblockrotation, blockposition2);

        worldserver.setBlockAndUpdate(blockposition3, Blocks.STONE_BUTTON.defaultBlockState().rotate(enumblockrotation));
    }

    public static void createNewEmptyStructureBlock(String s, BlockPosition blockposition, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        StructureBoundingBox structureboundingbox = getStructureBoundingBox(blockposition.above(), baseblockposition, enumblockrotation);

        clearSpaceForStructure(structureboundingbox, worldserver);
        worldserver.setBlockAndUpdate(blockposition, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

        tileentitystructure.setIgnoreEntities(false);
        tileentitystructure.setStructureName(MinecraftKey.parse(s));
        tileentitystructure.setStructureSize(baseblockposition);
        tileentitystructure.setMode(BlockPropertyStructureMode.SAVE);
        tileentitystructure.setShowBoundingBox(true);
    }

    public static TileEntityStructure prepareTestStructure(GameTestHarnessInfo gametestharnessinfo, BlockPosition blockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        BaseBlockPosition baseblockposition = ((DefinedStructure) worldserver.getStructureManager().get(MinecraftKey.parse(gametestharnessinfo.getStructureName())).orElseThrow(() -> {
            return new IllegalStateException("Missing test structure: " + gametestharnessinfo.getStructureName());
        })).getSize();
        StructureBoundingBox structureboundingbox = getStructureBoundingBox(blockposition, baseblockposition, enumblockrotation);
        BlockPosition blockposition1;

        if (enumblockrotation == EnumBlockRotation.NONE) {
            blockposition1 = blockposition;
        } else if (enumblockrotation == EnumBlockRotation.CLOCKWISE_90) {
            blockposition1 = blockposition.offset(baseblockposition.getZ() - 1, 0, 0);
        } else if (enumblockrotation == EnumBlockRotation.CLOCKWISE_180) {
            blockposition1 = blockposition.offset(baseblockposition.getX() - 1, 0, baseblockposition.getZ() - 1);
        } else {
            if (enumblockrotation != EnumBlockRotation.COUNTERCLOCKWISE_90) {
                throw new IllegalArgumentException("Invalid rotation: " + String.valueOf(enumblockrotation));
            }

            blockposition1 = blockposition.offset(0, 0, baseblockposition.getX() - 1);
        }

        forceLoadChunks(structureboundingbox, worldserver);
        clearSpaceForStructure(structureboundingbox, worldserver);
        return createStructureBlock(gametestharnessinfo, blockposition1.below(), enumblockrotation, worldserver);
    }

    public static void encaseStructure(AxisAlignedBB axisalignedbb, WorldServer worldserver, boolean flag) {
        BlockPosition blockposition = BlockPosition.containing(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).offset(-1, 0, -1);
        BlockPosition blockposition1 = BlockPosition.containing(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);

        BlockPosition.betweenClosedStream(blockposition, blockposition1).forEach((blockposition2) -> {
            boolean flag1 = blockposition2.getX() == blockposition.getX() || blockposition2.getX() == blockposition1.getX() || blockposition2.getZ() == blockposition.getZ() || blockposition2.getZ() == blockposition1.getZ();
            boolean flag2 = blockposition2.getY() == blockposition1.getY();

            if (flag1 || flag2 && flag) {
                worldserver.setBlockAndUpdate(blockposition2, Blocks.BARRIER.defaultBlockState());
            }

        });
    }

    public static void removeBarriers(AxisAlignedBB axisalignedbb, WorldServer worldserver) {
        BlockPosition blockposition = BlockPosition.containing(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).offset(-1, 0, -1);
        BlockPosition blockposition1 = BlockPosition.containing(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);

        BlockPosition.betweenClosedStream(blockposition, blockposition1).forEach((blockposition2) -> {
            boolean flag = blockposition2.getX() == blockposition.getX() || blockposition2.getX() == blockposition1.getX() || blockposition2.getZ() == blockposition.getZ() || blockposition2.getZ() == blockposition1.getZ();
            boolean flag1 = blockposition2.getY() == blockposition1.getY();

            if (worldserver.getBlockState(blockposition2).is(Blocks.BARRIER) && (flag || flag1)) {
                worldserver.setBlockAndUpdate(blockposition2, Blocks.AIR.defaultBlockState());
            }

        });
    }

    private static void forceLoadChunks(StructureBoundingBox structureboundingbox, WorldServer worldserver) {
        structureboundingbox.intersectingChunks().forEach((chunkcoordintpair) -> {
            worldserver.setChunkForced(chunkcoordintpair.x, chunkcoordintpair.z, true);
        });
    }

    public static void clearSpaceForStructure(StructureBoundingBox structureboundingbox, WorldServer worldserver) {
        int i = structureboundingbox.minY() - 1;
        StructureBoundingBox structureboundingbox1 = new StructureBoundingBox(structureboundingbox.minX() - 2, structureboundingbox.minY() - 3, structureboundingbox.minZ() - 3, structureboundingbox.maxX() + 3, structureboundingbox.maxY() + 20, structureboundingbox.maxZ() + 3);

        BlockPosition.betweenClosedStream(structureboundingbox1).forEach((blockposition) -> {
            clearBlock(i, blockposition, worldserver);
        });
        worldserver.getBlockTicks().clearArea(structureboundingbox1);
        worldserver.clearBlockEvents(structureboundingbox1);
        AxisAlignedBB axisalignedbb = AxisAlignedBB.of(structureboundingbox1);
        List<Entity> list = worldserver.getEntitiesOfClass(Entity.class, axisalignedbb, (entity) -> {
            return !(entity instanceof EntityHuman);
        });

        list.forEach(Entity::discard);
    }

    public static BlockPosition getTransformedFarCorner(BlockPosition blockposition, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation) {
        BlockPosition blockposition1 = blockposition.offset(baseblockposition).offset(-1, -1, -1);

        return DefinedStructure.transform(blockposition1, EnumBlockMirror.NONE, enumblockrotation, blockposition);
    }

    public static StructureBoundingBox getStructureBoundingBox(BlockPosition blockposition, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation) {
        BlockPosition blockposition1 = getTransformedFarCorner(blockposition, baseblockposition, enumblockrotation);
        StructureBoundingBox structureboundingbox = StructureBoundingBox.fromCorners(blockposition, blockposition1);
        int i = Math.min(structureboundingbox.minX(), structureboundingbox.maxX());
        int j = Math.min(structureboundingbox.minZ(), structureboundingbox.maxZ());

        return structureboundingbox.move(blockposition.getX() - i, 0, blockposition.getZ() - j);
    }

    public static Optional<BlockPosition> findStructureBlockContainingPos(BlockPosition blockposition, int i, WorldServer worldserver) {
        return findStructureBlocks(blockposition, i, worldserver).filter((blockposition1) -> {
            return doesStructureContain(blockposition1, blockposition, worldserver);
        }).findFirst();
    }

    public static Optional<BlockPosition> findNearestStructureBlock(BlockPosition blockposition, int i, WorldServer worldserver) {
        Comparator<BlockPosition> comparator = Comparator.comparingInt((blockposition1) -> {
            return blockposition1.distManhattan(blockposition);
        });

        return findStructureBlocks(blockposition, i, worldserver).min(comparator);
    }

    public static Stream<BlockPosition> findStructureByTestFunction(BlockPosition blockposition, int i, WorldServer worldserver, String s) {
        return findStructureBlocks(blockposition, i, worldserver).map((blockposition1) -> {
            return (TileEntityStructure) worldserver.getBlockEntity(blockposition1);
        }).filter(Objects::nonNull).filter((tileentitystructure) -> {
            return Objects.equals(tileentitystructure.getStructureName(), s);
        }).map(TileEntity::getBlockPos).map(BlockPosition::immutable);
    }

    public static Stream<BlockPosition> findStructureBlocks(BlockPosition blockposition, int i, WorldServer worldserver) {
        StructureBoundingBox structureboundingbox = getBoundingBoxAtGround(blockposition, i, worldserver);

        return BlockPosition.betweenClosedStream(structureboundingbox).filter((blockposition1) -> {
            return worldserver.getBlockState(blockposition1).is(Blocks.STRUCTURE_BLOCK);
        }).map(BlockPosition::immutable);
    }

    private static TileEntityStructure createStructureBlock(GameTestHarnessInfo gametestharnessinfo, BlockPosition blockposition, EnumBlockRotation enumblockrotation, WorldServer worldserver) {
        worldserver.setBlockAndUpdate(blockposition, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

        tileentitystructure.setMode(BlockPropertyStructureMode.LOAD);
        tileentitystructure.setRotation(enumblockrotation);
        tileentitystructure.setIgnoreEntities(false);
        tileentitystructure.setStructureName(MinecraftKey.parse(gametestharnessinfo.getStructureName()));
        tileentitystructure.setMetaData(gametestharnessinfo.getTestName());
        if (!tileentitystructure.loadStructureInfo(worldserver)) {
            String s = gametestharnessinfo.getTestName();

            throw new RuntimeException("Failed to load structure info for test: " + s + ". Structure name: " + gametestharnessinfo.getStructureName());
        } else {
            return tileentitystructure;
        }
    }

    private static StructureBoundingBox getBoundingBoxAtGround(BlockPosition blockposition, int i, WorldServer worldserver) {
        BlockPosition blockposition1 = BlockPosition.containing((double) blockposition.getX(), (double) worldserver.getHeightmapPos(HeightMap.Type.WORLD_SURFACE, blockposition).getY(), (double) blockposition.getZ());

        return (new StructureBoundingBox(blockposition1)).inflatedBy(i, 10, i);
    }

    public static Stream<BlockPosition> lookedAtStructureBlockPos(BlockPosition blockposition, Entity entity, WorldServer worldserver) {
        boolean flag = true;
        Vec3D vec3d = entity.getEyePosition();
        Vec3D vec3d1 = vec3d.add(entity.getLookAngle().scale(200.0D));
        Stream stream = findStructureBlocks(blockposition, 200, worldserver).map((blockposition1) -> {
            return worldserver.getBlockEntity(blockposition1, TileEntityTypes.STRUCTURE_BLOCK);
        }).flatMap(Optional::stream).filter((tileentitystructure) -> {
            return getStructureBounds(tileentitystructure).clip(vec3d, vec3d1).isPresent();
        }).map(TileEntity::getBlockPos);

        Objects.requireNonNull(blockposition);
        return stream.sorted(Comparator.comparing(blockposition::distSqr)).limit(1L);
    }

    private static void clearBlock(int i, BlockPosition blockposition, WorldServer worldserver) {
        IBlockData iblockdata;

        if (blockposition.getY() < i) {
            iblockdata = Blocks.STONE.defaultBlockState();
        } else {
            iblockdata = Blocks.AIR.defaultBlockState();
        }

        ArgumentTileLocation argumenttilelocation = new ArgumentTileLocation(iblockdata, Collections.emptySet(), (NBTTagCompound) null);

        argumenttilelocation.place(worldserver, blockposition, 2);
        worldserver.blockUpdated(blockposition, iblockdata.getBlock());
    }

    private static boolean doesStructureContain(BlockPosition blockposition, BlockPosition blockposition1, WorldServer worldserver) {
        TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

        return getStructureBoundingBox(tileentitystructure).isInside(blockposition1);
    }
}
