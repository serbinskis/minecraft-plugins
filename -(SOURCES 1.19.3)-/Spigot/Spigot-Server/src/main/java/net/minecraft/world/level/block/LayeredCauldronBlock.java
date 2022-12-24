package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
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

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.event.block.CauldronLevelChangeEvent;
// CraftBukkit end

public class LayeredCauldronBlock extends AbstractCauldronBlock {

    public static final int MIN_FILL_LEVEL = 1;
    public static final int MAX_FILL_LEVEL = 3;
    public static final BlockStateInteger LEVEL = BlockProperties.LEVEL_CAULDRON;
    private static final int BASE_CONTENT_HEIGHT = 6;
    private static final double HEIGHT_PER_LEVEL = 3.0D;
    public static final Predicate<BiomeBase.Precipitation> RAIN = (biomebase_precipitation) -> {
        return biomebase_precipitation == BiomeBase.Precipitation.RAIN;
    };
    public static final Predicate<BiomeBase.Precipitation> SNOW = (biomebase_precipitation) -> {
        return biomebase_precipitation == BiomeBase.Precipitation.SNOW;
    };
    private final Predicate<BiomeBase.Precipitation> fillPredicate;

    public LayeredCauldronBlock(BlockBase.Info blockbase_info, Predicate<BiomeBase.Precipitation> predicate, Map<Item, CauldronInteraction> map) {
        super(blockbase_info, map);
        this.fillPredicate = predicate;
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(LayeredCauldronBlock.LEVEL, 1));
    }

    @Override
    public boolean isFull(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) == 3;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(FluidType fluidtype) {
        return fluidtype == FluidTypes.WATER && this.fillPredicate == LayeredCauldronBlock.RAIN;
    }

    @Override
    protected double getContentHeight(IBlockData iblockdata) {
        return (6.0D + (double) (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) * 3.0D) / 16.0D;
    }

    @Override
    public void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (!world.isClientSide && entity.isOnFire() && this.isEntityInsideContent(iblockdata, blockposition, entity)) {
            // CraftBukkit start
            if (entity.mayInteract(world, blockposition)) {
                if (!lowerFillLevel(iblockdata, world, blockposition, entity, CauldronLevelChangeEvent.ChangeReason.EXTINGUISH)) {
                    return;
                }
            }
            entity.clearFire();
            // CraftBukkit end
        }

    }

    protected void handleEntityOnFireInside(IBlockData iblockdata, World world, BlockPosition blockposition) {
        lowerFillLevel(iblockdata, world, blockposition);
    }

    public static void lowerFillLevel(IBlockData iblockdata, World world, BlockPosition blockposition) {
        // CraftBukkit start
        lowerFillLevel(iblockdata, world, blockposition, null, CauldronLevelChangeEvent.ChangeReason.UNKNOWN);
    }

    public static boolean lowerFillLevel(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity, CauldronLevelChangeEvent.ChangeReason reason) {
        int i = (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) - 1;
        IBlockData iblockdata1 = i == 0 ? Blocks.CAULDRON.defaultBlockState() : (IBlockData) iblockdata.setValue(LayeredCauldronBlock.LEVEL, i);

        return changeLevel(iblockdata, world, blockposition, iblockdata1, entity, reason);
    }

    // CraftBukkit start
    public static boolean changeLevel(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData newBlock, Entity entity, CauldronLevelChangeEvent.ChangeReason reason) {
        CraftBlockState newState = CraftBlockStates.getBlockState(world, blockposition);
        newState.setData(newBlock);

        CauldronLevelChangeEvent event = new CauldronLevelChangeEvent(
                world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()),
                (entity == null) ? null : entity.getBukkitEntity(), reason, newState
        );
        world.getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        newState.update(true);
        world.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(newBlock));
        return true;
    }
    // CraftBukkit end

    @Override
    public void handlePrecipitation(IBlockData iblockdata, World world, BlockPosition blockposition, BiomeBase.Precipitation biomebase_precipitation) {
        if (BlockCauldron.shouldHandlePrecipitation(world, biomebase_precipitation) && (Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) != 3 && this.fillPredicate.test(biomebase_precipitation)) {
            IBlockData iblockdata1 = (IBlockData) iblockdata.cycle(LayeredCauldronBlock.LEVEL);

            changeLevel(iblockdata, world, blockposition, iblockdata1, null, CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL); // CraftBukkit
        }
    }

    @Override
    public int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
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

            // CraftBukkit start
            if (!changeLevel(iblockdata, world, blockposition, iblockdata1, null, CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL)) {
                return;
            }
            // CraftBukkit end
            world.levelEvent(1047, blockposition, 0);
        }
    }
}
