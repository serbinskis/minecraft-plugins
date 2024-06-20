package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.EntityEnderPearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityEndGateway;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3D;

public class BlockEndGateway extends BlockTileEntity implements Portal {

    public static final MapCodec<BlockEndGateway> CODEC = simpleCodec(BlockEndGateway::new);

    @Override
    public MapCodec<BlockEndGateway> codec() {
        return BlockEndGateway.CODEC;
    }

    protected BlockEndGateway(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityEndGateway(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return createTickerHelper(tileentitytypes, TileEntityTypes.END_GATEWAY, world.isClientSide ? TileEntityEndGateway::beamAnimationTick : TileEntityEndGateway::portalTick);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityEndGateway) {
            int i = ((TileEntityEndGateway) tileentity).getParticleAmount();

            for (int j = 0; j < i; ++j) {
                double d0 = (double) blockposition.getX() + randomsource.nextDouble();
                double d1 = (double) blockposition.getY() + randomsource.nextDouble();
                double d2 = (double) blockposition.getZ() + randomsource.nextDouble();
                double d3 = (randomsource.nextDouble() - 0.5D) * 0.5D;
                double d4 = (randomsource.nextDouble() - 0.5D) * 0.5D;
                double d5 = (randomsource.nextDouble() - 0.5D) * 0.5D;
                int k = randomsource.nextInt(2) * 2 - 1;

                if (randomsource.nextBoolean()) {
                    d2 = (double) blockposition.getZ() + 0.5D + 0.25D * (double) k;
                    d5 = (double) (randomsource.nextFloat() * 2.0F * (float) k);
                } else {
                    d0 = (double) blockposition.getX() + 0.5D + 0.25D * (double) k;
                    d3 = (double) (randomsource.nextFloat() * 2.0F * (float) k);
                }

                world.addParticle(Particles.PORTAL, d0, d1, d2, d3, d4, d5);
            }

        }
    }

    @Override
    public ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBeReplaced(IBlockData iblockdata, FluidType fluidtype) {
        return false;
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (entity.canUsePortal(false)) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (!world.isClientSide && tileentity instanceof TileEntityEndGateway) {
                TileEntityEndGateway tileentityendgateway = (TileEntityEndGateway) tileentity;

                if (!tileentityendgateway.isCoolingDown()) {
                    entity.setAsInsidePortal(this, blockposition);
                    TileEntityEndGateway.triggerCooldown(world, blockposition, iblockdata, tileentityendgateway);
                }
            }
        }

    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(WorldServer worldserver, Entity entity, BlockPosition blockposition) {
        TileEntity tileentity = worldserver.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityEndGateway tileentityendgateway) {
            Vec3D vec3d = tileentityendgateway.getPortalPosition(worldserver, blockposition);

            return vec3d != null ? new DimensionTransition(worldserver, vec3d, calculateExitMovement(entity), entity.getYRot(), entity.getXRot(), DimensionTransition.PLACE_PORTAL_TICKET) : null;
        } else {
            return null;
        }
    }

    private static Vec3D calculateExitMovement(Entity entity) {
        return entity instanceof EntityEnderPearl ? new Vec3D(0.0D, -1.0D, 0.0D) : entity.getDeltaMovement();
    }
}
