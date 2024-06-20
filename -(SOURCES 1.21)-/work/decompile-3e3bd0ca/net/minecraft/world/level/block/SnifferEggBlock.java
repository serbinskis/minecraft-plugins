package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class SnifferEggBlock extends Block {

    public static final MapCodec<SnifferEggBlock> CODEC = simpleCodec(SnifferEggBlock::new);
    public static final int MAX_HATCH_LEVEL = 2;
    public static final BlockStateInteger HATCH = BlockProperties.HATCH;
    private static final int REGULAR_HATCH_TIME_TICKS = 24000;
    private static final int BOOSTED_HATCH_TIME_TICKS = 12000;
    private static final int RANDOM_HATCH_OFFSET_TICKS = 300;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 16.0D, 14.0D);

    @Override
    public MapCodec<SnifferEggBlock> codec() {
        return SnifferEggBlock.CODEC;
    }

    public SnifferEggBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(SnifferEggBlock.HATCH, 0));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(SnifferEggBlock.HATCH);
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return SnifferEggBlock.SHAPE;
    }

    public int getHatchLevel(IBlockData iblockdata) {
        return (Integer) iblockdata.getValue(SnifferEggBlock.HATCH);
    }

    private boolean isReadyToHatch(IBlockData iblockdata) {
        return this.getHatchLevel(iblockdata) == 2;
    }

    @Override
    public void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!this.isReadyToHatch(iblockdata)) {
            worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.SNIFFER_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + randomsource.nextFloat() * 0.2F);
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(SnifferEggBlock.HATCH, this.getHatchLevel(iblockdata) + 1), 2);
        } else {
            worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.SNIFFER_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + randomsource.nextFloat() * 0.2F);
            worldserver.destroyBlock(blockposition, false);
            Sniffer sniffer = (Sniffer) EntityTypes.SNIFFER.create(worldserver);

            if (sniffer != null) {
                Vec3D vec3d = blockposition.getCenter();

                sniffer.setBaby(true);
                sniffer.moveTo(vec3d.x(), vec3d.y(), vec3d.z(), MathHelper.wrapDegrees(worldserver.random.nextFloat() * 360.0F), 0.0F);
                worldserver.addFreshEntity(sniffer);
            }

        }
    }

    @Override
    public void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        boolean flag1 = hatchBoost(world, blockposition);

        if (!world.isClientSide() && flag1) {
            world.levelEvent(3009, blockposition, 0);
        }

        int i = flag1 ? 12000 : 24000;
        int j = i / 3;

        world.gameEvent((Holder) GameEvent.BLOCK_PLACE, blockposition, GameEvent.a.of(iblockdata));
        world.scheduleTick(blockposition, (Block) this, j + world.random.nextInt(300));
    }

    @Override
    public boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    public static boolean hatchBoost(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockaccess.getBlockState(blockposition.below()).is(TagsBlock.SNIFFER_EGG_HATCH_BOOST);
    }
}
