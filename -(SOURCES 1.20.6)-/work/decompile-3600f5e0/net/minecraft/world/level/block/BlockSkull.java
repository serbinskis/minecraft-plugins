package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.INamable;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockSkull extends BlockSkullAbstract {

    public static final MapCodec<BlockSkull> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockSkull.a.CODEC.fieldOf("kind").forGetter(BlockSkullAbstract::getType), propertiesCodec()).apply(instance, BlockSkull::new);
    });
    public static final int MAX = RotationSegment.getMaxSegmentIndex();
    private static final int ROTATIONS = BlockSkull.MAX + 1;
    public static final BlockStateInteger ROTATION = BlockProperties.ROTATION_16;
    protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);
    protected static final VoxelShape PIGLIN_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);

    @Override
    public MapCodec<? extends BlockSkull> codec() {
        return BlockSkull.CODEC;
    }

    protected BlockSkull(BlockSkull.a blockskull_a, BlockBase.Info blockbase_info) {
        super(blockskull_a, blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(BlockSkull.ROTATION, 0));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return this.getType() == BlockSkull.Type.PIGLIN ? BlockSkull.PIGLIN_SHAPE : BlockSkull.SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return VoxelShapes.empty();
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) super.getStateForPlacement(blockactioncontext).setValue(BlockSkull.ROTATION, RotationSegment.convertToSegment(blockactioncontext.getRotation()));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockSkull.ROTATION, enumblockrotation.rotate((Integer) iblockdata.getValue(BlockSkull.ROTATION), BlockSkull.ROTATIONS));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return (IBlockData) iblockdata.setValue(BlockSkull.ROTATION, enumblockmirror.mirror((Integer) iblockdata.getValue(BlockSkull.ROTATION), BlockSkull.ROTATIONS));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        super.createBlockStateDefinition(blockstatelist_a);
        blockstatelist_a.add(BlockSkull.ROTATION);
    }

    public interface a extends INamable {

        Map<String, BlockSkull.a> TYPES = new Object2ObjectArrayMap();
        Codec<BlockSkull.a> CODEC;

        static {
            Function function = INamable::getSerializedName;
            Map map = BlockSkull.a.TYPES;

            Objects.requireNonNull(map);
            CODEC = Codec.stringResolver(function, map::get);
        }
    }

    public static enum Type implements BlockSkull.a {

        SKELETON("skeleton"), WITHER_SKELETON("wither_skeleton"), PLAYER("player"), ZOMBIE("zombie"), CREEPER("creeper"), PIGLIN("piglin"), DRAGON("dragon");

        private final String name;

        private Type(final String s) {
            this.name = s;
            BlockSkull.Type.TYPES.put(s, this);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
