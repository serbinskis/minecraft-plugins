package net.minecraft.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockFlowers extends BlockPlant implements SuspiciousEffectHolder {

    protected static final float AABB_OFFSET = 3.0F;
    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);
    private final List<SuspiciousEffectHolder.a> suspiciousStewEffects;

    public BlockFlowers(MobEffectList mobeffectlist, int i, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        int j;

        if (mobeffectlist.isInstantenous()) {
            j = i;
        } else {
            j = i * 20;
        }

        this.suspiciousStewEffects = List.of(new SuspiciousEffectHolder.a(mobeffectlist, j));
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        Vec3D vec3d = iblockdata.getOffset(iblockaccess, blockposition);

        return BlockFlowers.SHAPE.move(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public List<SuspiciousEffectHolder.a> getSuspiciousEffects() {
        return this.suspiciousStewEffects;
    }
}
