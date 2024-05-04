package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
// CraftBukkit end

public class SculkSensorBlock extends BlockTileEntity implements IBlockWaterlogged {

    public static final MapCodec<SculkSensorBlock> CODEC = simpleCodec(SculkSensorBlock::new);
    public static final int ACTIVE_TICKS = 30;
    public static final int COOLDOWN_TICKS = 10;
    public static final BlockStateEnum<SculkSensorPhase> PHASE = BlockProperties.SCULK_SENSOR_PHASE;
    public static final BlockStateInteger POWER = BlockProperties.POWER;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    private static final float[] RESONANCE_PITCH_BEND = (float[]) SystemUtils.make(new float[16], (afloat) -> {
        int[] aint = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

        for (int i = 0; i < 16; ++i) {
            afloat[i] = BlockNote.getPitchFromNote(aint[i]);
        }

    });

    @Override
    public MapCodec<? extends SculkSensorBlock> codec() {
        return SculkSensorBlock.CODEC;
    }

    public SculkSensorBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(SculkSensorBlock.PHASE, SculkSensorPhase.INACTIVE)).setValue(SculkSensorBlock.POWER, 0)).setValue(SculkSensorBlock.WATERLOGGED, false));
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockposition);

        return (IBlockData) this.defaultBlockState().setValue(SculkSensorBlock.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(SculkSensorBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (getPhase(iblockdata) != SculkSensorPhase.ACTIVE) {
            if (getPhase(iblockdata) == SculkSensorPhase.COOLDOWN) {
                worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(SculkSensorBlock.PHASE, SculkSensorPhase.INACTIVE), 3);
                if (!(Boolean) iblockdata.getValue(SculkSensorBlock.WATERLOGGED)) {
                    worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.SCULK_CLICKING_STOP, SoundCategory.BLOCKS, 1.0F, worldserver.random.nextFloat() * 0.2F + 0.8F);
                }
            }

        } else {
            deactivate(worldserver, blockposition, iblockdata);
        }
    }

    @Override
    public void stepOn(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        if (!world.isClientSide() && canActivate(iblockdata) && entity.getType() != EntityTypes.WARDEN) {
            // CraftBukkit start
            org.bukkit.event.Cancellable cancellable;
            if (entity instanceof EntityHuman) {
                cancellable = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((EntityHuman) entity, org.bukkit.event.block.Action.PHYSICAL, blockposition, null, null, null);
            } else {
                cancellable = new org.bukkit.event.entity.EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
                world.getCraftServer().getPluginManager().callEvent((org.bukkit.event.entity.EntityInteractEvent) cancellable);
            }
            if (cancellable.isCancelled()) {
                return;
            }
            // CraftBukkit end
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof SculkSensorBlockEntity) {
                SculkSensorBlockEntity sculksensorblockentity = (SculkSensorBlockEntity) tileentity;

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    if (sculksensorblockentity.getVibrationUser().canReceiveVibration(worldserver, blockposition, GameEvent.STEP, GameEvent.a.of(iblockdata))) {
                        sculksensorblockentity.getListener().forceScheduleVibration(worldserver, GameEvent.STEP, GameEvent.a.of(entity), entity.position());
                    }
                }
            }
        }

        super.stepOn(world, blockposition, iblockdata, entity);
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!world.isClientSide() && !iblockdata.is(iblockdata1.getBlock())) {
            if ((Integer) iblockdata.getValue(SculkSensorBlock.POWER) > 0 && !world.getBlockTicks().hasScheduledTick(blockposition, this)) {
                world.setBlock(blockposition, (IBlockData) iblockdata.setValue(SculkSensorBlock.POWER, 0), 18);
            }

        }
    }

    @Override
    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata.is(iblockdata1.getBlock())) {
            if (getPhase(iblockdata) == SculkSensorPhase.ACTIVE) {
                updateNeighbours(world, blockposition, iblockdata);
            }

            super.onRemove(iblockdata, world, blockposition, iblockdata1, flag);
        }
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if ((Boolean) iblockdata.getValue(SculkSensorBlock.WATERLOGGED)) {
            generatoraccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(generatoraccess));
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    private static void updateNeighbours(World world, BlockPosition blockposition, IBlockData iblockdata) {
        Block block = iblockdata.getBlock();

        world.updateNeighborsAt(blockposition, block);
        world.updateNeighborsAt(blockposition.below(), block);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new SculkSensorBlockEntity(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return !world.isClientSide ? createTickerHelper(tileentitytypes, TileEntityTypes.SCULK_SENSOR, (world1, blockposition, iblockdata1, sculksensorblockentity) -> {
            VibrationSystem.c.tick(world1, sculksensorblockentity.getVibrationData(), sculksensorblockentity.getVibrationUser());
        }) : null;
    }

    @Override
    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return SculkSensorBlock.SHAPE;
    }

    @Override
    protected boolean isSignalSource(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return (Integer) iblockdata.getValue(SculkSensorBlock.POWER);
    }

    @Override
    public int getDirectSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return enumdirection == EnumDirection.UP ? iblockdata.getSignal(iblockaccess, blockposition, enumdirection) : 0;
    }

    public static SculkSensorPhase getPhase(IBlockData iblockdata) {
        return (SculkSensorPhase) iblockdata.getValue(SculkSensorBlock.PHASE);
    }

    public static boolean canActivate(IBlockData iblockdata) {
        return getPhase(iblockdata) == SculkSensorPhase.INACTIVE;
    }

    public static void deactivate(World world, BlockPosition blockposition, IBlockData iblockdata) {
        // CraftBukkit start
        BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(CraftBlock.at(world, blockposition), iblockdata.getValue(SculkSensorBlock.POWER), 0);
        world.getCraftServer().getPluginManager().callEvent(eventRedstone);

        if (eventRedstone.getNewCurrent() > 0) {
            world.setBlock(blockposition, iblockdata.setValue(SculkSensorBlock.POWER, eventRedstone.getNewCurrent()), 3);
            return;
        }
        // CraftBukkit end
        world.setBlock(blockposition, (IBlockData) ((IBlockData) iblockdata.setValue(SculkSensorBlock.PHASE, SculkSensorPhase.COOLDOWN)).setValue(SculkSensorBlock.POWER, 0), 3);
        world.scheduleTick(blockposition, iblockdata.getBlock(), 10);
        updateNeighbours(world, blockposition, iblockdata);
    }

    @VisibleForTesting
    public int getActiveTicks() {
        return 30;
    }

    public void activate(@Nullable Entity entity, World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        // CraftBukkit start
        BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(CraftBlock.at(world, blockposition), iblockdata.getValue(SculkSensorBlock.POWER), i);
        world.getCraftServer().getPluginManager().callEvent(eventRedstone);

        if (eventRedstone.getNewCurrent() <= 0) {
            return;
        }
        i = eventRedstone.getNewCurrent();
        // CraftBukkit end
        world.setBlock(blockposition, (IBlockData) ((IBlockData) iblockdata.setValue(SculkSensorBlock.PHASE, SculkSensorPhase.ACTIVE)).setValue(SculkSensorBlock.POWER, i), 3);
        world.scheduleTick(blockposition, iblockdata.getBlock(), this.getActiveTicks());
        updateNeighbours(world, blockposition, iblockdata);
        tryResonateVibration(entity, world, blockposition, j);
        world.gameEvent(entity, (Holder) GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, blockposition);
        if (!(Boolean) iblockdata.getValue(SculkSensorBlock.WATERLOGGED)) {
            world.playSound((EntityHuman) null, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, SoundEffects.SCULK_CLICKING, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
        }

    }

    public static void tryResonateVibration(@Nullable Entity entity, World world, BlockPosition blockposition, int i) {
        EnumDirection[] aenumdirection = EnumDirection.values();
        int j = aenumdirection.length;

        for (int k = 0; k < j; ++k) {
            EnumDirection enumdirection = aenumdirection[k];
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            IBlockData iblockdata = world.getBlockState(blockposition1);

            if (iblockdata.is(TagsBlock.VIBRATION_RESONATORS)) {
                world.gameEvent(VibrationSystem.getResonanceEventByFrequency(i), blockposition1, GameEvent.a.of(entity, iblockdata));
                float f = SculkSensorBlock.RESONANCE_PITCH_BEND[i];

                world.playSound((EntityHuman) null, blockposition1, SoundEffects.AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 1.0F, f);
            }
        }

    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (getPhase(iblockdata) == SculkSensorPhase.ACTIVE) {
            EnumDirection enumdirection = EnumDirection.getRandom(randomsource);

            if (enumdirection != EnumDirection.UP && enumdirection != EnumDirection.DOWN) {
                double d0 = (double) blockposition.getX() + 0.5D + (enumdirection.getStepX() == 0 ? 0.5D - randomsource.nextDouble() : (double) enumdirection.getStepX() * 0.6D);
                double d1 = (double) blockposition.getY() + 0.25D;
                double d2 = (double) blockposition.getZ() + 0.5D + (enumdirection.getStepZ() == 0 ? 0.5D - randomsource.nextDouble() : (double) enumdirection.getStepZ() * 0.6D);
                double d3 = (double) randomsource.nextFloat() * 0.04D;

                world.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, 0.0D, d3, 0.0D);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(SculkSensorBlock.PHASE, SculkSensorBlock.POWER, SculkSensorBlock.WATERLOGGED);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof SculkSensorBlockEntity sculksensorblockentity) {
            return getPhase(iblockdata) == SculkSensorPhase.ACTIVE ? sculksensorblockentity.getLastVibrationFrequency() : 0;
        } else {
            return 0;
        }
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    @Override
    protected boolean useShapeForLightOcclusion(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected void spawnAfterBreak(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
        super.spawnAfterBreak(iblockdata, worldserver, blockposition, itemstack, flag);
        // CraftBukkit start - Delegate to getExpDrop
    }

    @Override
    public int getExpDrop(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
        if (flag) {
            return this.tryDropExperience(worldserver, blockposition, itemstack, ConstantInt.of(5));
        }

        return 0;
        // CraftBukkit end
    }
}
