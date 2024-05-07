package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockWitherRose extends BlockFlowers {

    public static final MapCodec<BlockWitherRose> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockWitherRose.EFFECTS_FIELD.forGetter(BlockFlowers::getSuspiciousEffects), propertiesCodec()).apply(instance, BlockWitherRose::new);
    });

    @Override
    public MapCodec<BlockWitherRose> codec() {
        return BlockWitherRose.CODEC;
    }

    public BlockWitherRose(Holder<MobEffectList> holder, float f, BlockBase.Info blockbase_info) {
        this(makeEffectList(holder, f), blockbase_info);
    }

    public BlockWitherRose(SuspiciousStewEffects suspicioussteweffects, BlockBase.Info blockbase_info) {
        super(suspicioussteweffects, blockbase_info);
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return super.mayPlaceOn(iblockdata, iblockaccess, blockposition) || iblockdata.is(Blocks.NETHERRACK) || iblockdata.is(Blocks.SOUL_SAND) || iblockdata.is(Blocks.SOUL_SOIL);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        VoxelShape voxelshape = this.getShape(iblockdata, world, blockposition, VoxelShapeCollision.empty());
        Vec3D vec3d = voxelshape.bounds().getCenter();
        double d0 = (double) blockposition.getX() + vec3d.x;
        double d1 = (double) blockposition.getZ() + vec3d.z;

        for (int i = 0; i < 3; ++i) {
            if (randomsource.nextBoolean()) {
                world.addParticle(Particles.SMOKE, d0 + randomsource.nextDouble() / 5.0D, (double) blockposition.getY() + (0.5D - randomsource.nextDouble()), d1 + randomsource.nextDouble() / 5.0D, 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (!world.isClientSide && world.getDifficulty() != EnumDifficulty.PEACEFUL) {
            if (entity instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) entity;

                if (!entityliving.isInvulnerableTo(world.damageSources().wither())) {
                    entityliving.addEffect(new MobEffect(MobEffects.WITHER, 40));
                }
            }

        }
    }
}
