package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.Particles;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyInstrument;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class BlockNote extends Block {

    public static final MapCodec<BlockNote> CODEC = simpleCodec(BlockNote::new);
    public static final BlockStateEnum<BlockPropertyInstrument> INSTRUMENT = BlockProperties.NOTEBLOCK_INSTRUMENT;
    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;
    public static final BlockStateInteger NOTE = BlockProperties.NOTE;
    public static final int NOTE_VOLUME = 3;

    @Override
    public MapCodec<BlockNote> codec() {
        return BlockNote.CODEC;
    }

    public BlockNote(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockNote.INSTRUMENT, BlockPropertyInstrument.HARP)).setValue(BlockNote.NOTE, 0)).setValue(BlockNote.POWERED, false));
    }

    private IBlockData setInstrument(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata) {
        BlockPropertyInstrument blockpropertyinstrument = generatoraccess.getBlockState(blockposition.above()).instrument();

        if (blockpropertyinstrument.worksAboveNoteBlock()) {
            return (IBlockData) iblockdata.setValue(BlockNote.INSTRUMENT, blockpropertyinstrument);
        } else {
            BlockPropertyInstrument blockpropertyinstrument1 = generatoraccess.getBlockState(blockposition.below()).instrument();
            BlockPropertyInstrument blockpropertyinstrument2 = blockpropertyinstrument1.worksAboveNoteBlock() ? BlockPropertyInstrument.HARP : blockpropertyinstrument1;

            return (IBlockData) iblockdata.setValue(BlockNote.INSTRUMENT, blockpropertyinstrument2);
        }
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return this.setInstrument(blockactioncontext.getLevel(), blockactioncontext.getClickedPos(), this.defaultBlockState());
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        boolean flag = enumdirection.getAxis() == EnumDirection.EnumAxis.Y;

        return flag ? this.setInstrument(generatoraccess, blockposition, iblockdata) : super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        boolean flag1 = world.hasNeighborSignal(blockposition);

        if (flag1 != (Boolean) iblockdata.getValue(BlockNote.POWERED)) {
            if (flag1) {
                this.playNote((Entity) null, iblockdata, world, blockposition);
            }

            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockNote.POWERED, flag1), 3);
        }

    }

    private void playNote(@Nullable Entity entity, IBlockData iblockdata, World world, BlockPosition blockposition) {
        if (((BlockPropertyInstrument) iblockdata.getValue(BlockNote.INSTRUMENT)).worksAboveNoteBlock() || world.getBlockState(blockposition.above()).isAir()) {
            world.blockEvent(blockposition, this, 0, 0);
            world.gameEvent(entity, (Holder) GameEvent.NOTE_BLOCK_PLAY, blockposition);
        }

    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        return itemstack.is(TagsItem.NOTE_BLOCK_TOP_INSTRUMENTS) && movingobjectpositionblock.getDirection() == EnumDirection.UP ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION : super.useItemOn(itemstack, iblockdata, world, blockposition, entityhuman, enumhand, movingobjectpositionblock);
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            iblockdata = (IBlockData) iblockdata.cycle(BlockNote.NOTE);
            world.setBlock(blockposition, iblockdata, 3);
            this.playNote(entityhuman, iblockdata, world, blockposition);
            entityhuman.awardStat(StatisticList.TUNE_NOTEBLOCK);
            return EnumInteractionResult.CONSUME;
        }
    }

    @Override
    protected void attack(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman) {
        if (!world.isClientSide) {
            this.playNote(entityhuman, iblockdata, world, blockposition);
            entityhuman.awardStat(StatisticList.PLAY_NOTEBLOCK);
        }
    }

    public static float getPitchFromNote(int i) {
        return (float) Math.pow(2.0D, (double) (i - 12) / 12.0D);
    }

    @Override
    protected boolean triggerEvent(IBlockData iblockdata, World world, BlockPosition blockposition, int i, int j) {
        BlockPropertyInstrument blockpropertyinstrument = (BlockPropertyInstrument) iblockdata.getValue(BlockNote.INSTRUMENT);
        float f;

        if (blockpropertyinstrument.isTunable()) {
            int k = (Integer) iblockdata.getValue(BlockNote.NOTE);

            f = getPitchFromNote(k);
            world.addParticle(Particles.NOTE, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 1.2D, (double) blockposition.getZ() + 0.5D, (double) k / 24.0D, 0.0D, 0.0D);
        } else {
            f = 1.0F;
        }

        Holder holder;

        if (blockpropertyinstrument.hasCustomSound()) {
            MinecraftKey minecraftkey = this.getCustomSoundId(world, blockposition);

            if (minecraftkey == null) {
                return false;
            }

            holder = Holder.direct(SoundEffect.createVariableRangeEvent(minecraftkey));
        } else {
            holder = blockpropertyinstrument.getSoundEvent();
        }

        world.playSeededSound((EntityHuman) null, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, holder, SoundCategory.RECORDS, 3.0F, f, world.random.nextLong());
        return true;
    }

    @Nullable
    private MinecraftKey getCustomSoundId(World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getBlockEntity(blockposition.above());

        if (tileentity instanceof TileEntitySkull tileentityskull) {
            return tileentityskull.getNoteBlockSound();
        } else {
            return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockNote.INSTRUMENT, BlockNote.POWERED, BlockNote.NOTE);
    }
}
