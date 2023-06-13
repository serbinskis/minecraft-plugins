package net.minecraft.world.level.block;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;
import org.jetbrains.annotations.Nullable;

public class BrushableBlock extends BlockTileEntity implements Fallable {

    private static final BlockStateInteger DUSTED = BlockProperties.DUSTED;
    public static final int TICK_DELAY = 2;
    private final Block turnsInto;
    private final SoundEffect brushSound;
    private final SoundEffect brushCompletedSound;

    public BrushableBlock(Block block, BlockBase.Info blockbase_info, SoundEffect soundeffect, SoundEffect soundeffect1) {
        super(blockbase_info);
        this.turnsInto = block;
        this.brushSound = soundeffect;
        this.brushCompletedSound = soundeffect1;
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BrushableBlock.DUSTED, 0));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BrushableBlock.DUSTED);
    }

    @Override
    public EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    @Override
    public void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        world.scheduleTick(blockposition, (Block) this, 2);
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        generatoraccess.scheduleTick(blockposition, (Block) this, 2);
        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);

        if (tileentity instanceof BrushableBlockEntity) {
            BrushableBlockEntity brushableblockentity = (BrushableBlockEntity) tileentity;

            brushableblockentity.checkReset();
        }

        if (BlockFalling.isFree(worldserver.getBlockState(blockposition.below())) && blockposition.getY() >= worldserver.getMinBuildHeight()) {
            EntityFallingBlock entityfallingblock = EntityFallingBlock.fall(worldserver, blockposition, iblockdata);

            entityfallingblock.disableDrop();
        }
    }

    @Override
    public void onBrokenAfterFall(World world, BlockPosition blockposition, EntityFallingBlock entityfallingblock) {
        Vec3D vec3d = entityfallingblock.getBoundingBox().getCenter();

        world.levelEvent(2001, BlockPosition.containing(vec3d), Block.getId(entityfallingblock.getBlockState()));
        world.gameEvent((Entity) entityfallingblock, GameEvent.BLOCK_DESTROY, vec3d);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(16) == 0) {
            BlockPosition blockposition1 = blockposition.below();

            if (BlockFalling.isFree(world.getBlockState(blockposition1))) {
                double d0 = (double) blockposition.getX() + randomsource.nextDouble();
                double d1 = (double) blockposition.getY() - 0.05D;
                double d2 = (double) blockposition.getZ() + randomsource.nextDouble();

                world.addParticle(new ParticleParamBlock(Particles.FALLING_DUST, iblockdata), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new BrushableBlockEntity(blockposition, iblockdata);
    }

    public Block getTurnsInto() {
        return this.turnsInto;
    }

    public SoundEffect getBrushSound() {
        return this.brushSound;
    }

    public SoundEffect getBrushCompletedSound() {
        return this.brushCompletedSound;
    }
}
