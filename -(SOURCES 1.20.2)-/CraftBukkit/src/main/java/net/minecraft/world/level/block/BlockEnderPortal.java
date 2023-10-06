package net.minecraft.world.level.block;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityEnderPortal;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

// CraftBukkit start
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.dimension.WorldDimension;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
// CraftBukkit end

public class BlockEnderPortal extends BlockTileEntity {

    protected static final VoxelShape SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    protected BlockEnderPortal(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityEnderPortal(blockposition, iblockdata);
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockEnderPortal.SHAPE;
    }

    @Override
    public void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (world instanceof WorldServer && entity.canChangeDimensions() && VoxelShapes.joinIsNotEmpty(VoxelShapes.create(entity.getBoundingBox().move((double) (-blockposition.getX()), (double) (-blockposition.getY()), (double) (-blockposition.getZ()))), iblockdata.getShape(world, blockposition), OperatorBoolean.AND)) {
            ResourceKey<World> resourcekey = world.getTypeKey() == WorldDimension.END ? World.OVERWORLD : World.END; // CraftBukkit - SPIGOT-6152: send back to main overworld in custom ends
            WorldServer worldserver = ((WorldServer) world).getServer().getLevel(resourcekey);

            if (worldserver == null) {
                // return; // CraftBukkit - always fire event in case plugins wish to change it
            }

            // CraftBukkit start - Entity in portal
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(entity.getBukkitEntity(), new org.bukkit.Location(world.getWorld(), blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            world.getCraftServer().getPluginManager().callEvent(event);

            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).changeDimension(worldserver, PlayerTeleportEvent.TeleportCause.END_PORTAL);
                return;
            }
            // CraftBukkit end
            entity.changeDimension(worldserver);
        }

    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        double d0 = (double) blockposition.getX() + randomsource.nextDouble();
        double d1 = (double) blockposition.getY() + 0.8D;
        double d2 = (double) blockposition.getZ() + randomsource.nextDouble();

        world.addParticle(Particles.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public ItemStack getCloneItemStack(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canBeReplaced(IBlockData iblockdata, FluidType fluidtype) {
        return false;
    }
}
