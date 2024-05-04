package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;

public class LayeredCauldronBlock extends AbstractCauldronBlock {

    public static final MapCodec<LayeredCauldronBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BiomeBase.Precipitation.CODEC.fieldOf("precipitation").forGetter((layeredcauldronblock) -> {
            return layeredcauldronblock.precipitationType;
        }), CauldronInteraction.CODEC.fieldOf("interactions").forGetter((layeredcauldronblock) -> {
            return layeredcauldronblock.interactions;
        }), propertiesCodec()).apply(instance, LayeredCauldronBlock::new);
    });
    public static final int MIN_FILL_LEVEL = 1;
    public static final int MAX_FILL_LEVEL = 3;
    public static final BlockStateInteger LEVEL = BlockProperties.LEVEL_CAULDRON;
    private static final int BASE_CONTENT_HEIGHT = 6;
    private static final double HEIGHT_PER_LEVEL = 3.0D;
    private final BiomeBase.Precipitation precipitationType;

    @Override
    public MapCodec<LayeredCauldronBlock> codec() {
        return LayeredCauldronBlock.CODEC;
    }

    public LayeredCauldronBlock(BiomeBase.Precipitation biomebase_precipitation, CauldronInteraction.a cauldroninteraction_a, BlockBase.Info blockbase_info) {
        super(blockbase_info, cauldroninteraction_a);
        this.precipitationType = biomebase_precipitation;
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(LayeredCauldronBlock.LEVEL, 1));
    }

    @Override
    public boolean isFull(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) == 3;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(FluidType fluidtype) {
        return fluidtype == FluidTypes.WATER && this.precipitationType == BiomeBase.Precipitation.RAIN;
    }

    @Override
    protected double getContentHeight(IBlockData iblockdata) {
        return (6.0D + (double) (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) * 3.0D) / 16.0D;
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (!world.isClientSide && entity.isOnFire() && this.isEntityInsideContent(iblockdata, blockposition, entity)) {
            entity.clearFire();
            if (entity.mayInteract(world, blockposition)) {
                this.handleEntityOnFireInside(iblockdata, world, blockposition);
            }
        }

    }

    private void handleEntityOnFireInside(IBlockData iblockdata, World world, BlockPosition blockposition) {
        if (this.precipitationType == BiomeBase.Precipitation.SNOW) {
            lowerFillLevel((IBlockData) Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL)), world, blockposition);
        } else {
            lowerFillLevel(iblockdata, world, blockposition);
        }

    }

    public static void lowerFillLevel(IBlockData iblockdata, World world, BlockPosition blockposition) {
        int i = (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) - 1;
        IBlockData iblockdata1 = i == 0 ? Blocks.CAULDRON.defaultBlockState() : (IBlockData) iblockdata.setValue(LayeredCauldronBlock.LEVEL, i);

        world.setBlockAndUpdate(blockposition, iblockdata1);
        world.gameEvent((Holder) GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(iblockdata1));
    }

    @Override
    public void handlePrecipitation(IBlockData iblockdata, World world, BlockPosition blockposition, BiomeBase.Precipitation biomebase_precipitation) {
        if (BlockCauldron.shouldHandlePrecipitation(world, biomebase_precipitation) && (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) != 3 && biomebase_precipitation == this.precipitationType) {
            IBlockData iblockdata1 = (IBlockData) iblockdata.cycle(LayeredCauldronBlock.LEVEL);

            world.setBlockAndUpdate(blockposition, iblockdata1);
            world.gameEvent((Holder) GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(iblockdata1));
        }
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(LayeredCauldronBlock.LEVEL);
    }

    @Override
    protected void receiveStalactiteDrip(IBlockData iblockdata, World world, BlockPosition blockposition, FluidType fluidtype) {
        if (!this.isFull(iblockdata)) {
            IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(LayeredCauldronBlock.LEVEL, (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) + 1);

            world.setBlockAndUpdate(blockposition, iblockdata1);
            world.gameEvent((Holder) GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(iblockdata1));
            world.levelEvent(1047, blockposition, 0);
        }
    }
}
