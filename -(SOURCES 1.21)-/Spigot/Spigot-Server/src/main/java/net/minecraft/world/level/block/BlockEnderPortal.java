package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.Particles;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityEnderPortal;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

// CraftBukkit start
import java.util.List;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.dimension.WorldDimension;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.event.CraftPortalEvent;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
// CraftBukkit end

public class BlockEnderPortal extends BlockTileEntity implements Portal {

    public static final MapCodec<BlockEnderPortal> CODEC = simpleCodec(BlockEnderPortal::new);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    @Override
    public MapCodec<BlockEnderPortal> codec() {
        return BlockEnderPortal.CODEC;
    }

    protected BlockEnderPortal(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityEnderPortal(blockposition, iblockdata);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockEnderPortal.SHAPE;
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {
        if (entity.canUsePortal(false) && VoxelShapes.joinIsNotEmpty(VoxelShapes.create(entity.getBoundingBox().move((double) (-blockposition.getX()), (double) (-blockposition.getY()), (double) (-blockposition.getZ()))), iblockdata.getShape(world, blockposition), OperatorBoolean.AND)) {
            // CraftBukkit start - Entity in portal
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(entity.getBukkitEntity(), new org.bukkit.Location(world.getWorld(), blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            world.getCraftServer().getPluginManager().callEvent(event);
            // CraftBukkit end
            if (!world.isClientSide && world.dimension() == World.END && entity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entity;

                if (!entityplayer.seenCredits) {
                    entityplayer.showEndCredits();
                    return;
                }
            }

            entity.setAsInsidePortal(this, blockposition);
        }

    }

    @Override
    public DimensionTransition getPortalDestination(WorldServer worldserver, Entity entity, BlockPosition blockposition) {
        ResourceKey<World> resourcekey = worldserver.getTypeKey() == WorldDimension.END ? World.OVERWORLD : World.END; // CraftBukkit - SPIGOT-6152: send back to main overworld in custom ends
        WorldServer worldserver1 = worldserver.getServer().getLevel(resourcekey);

        if (worldserver1 == null) {
            return new DimensionTransition(PlayerTeleportEvent.TeleportCause.END_PORTAL); // CraftBukkit- always fire event in case plugins wish to change it
        } else {
            boolean flag = resourcekey == World.END;
            BlockPosition blockposition1 = flag ? WorldServer.END_SPAWN_POINT : worldserver1.getSharedSpawnPos();
            Vec3D vec3d = blockposition1.getBottomCenter();
            float f = entity.getYRot();

            if (flag) {
                EndPlatformFeature.createEndPlatform(worldserver1, BlockPosition.containing(vec3d).below(), true, entity); // CraftBukkit
                f = EnumDirection.WEST.toYRot();
                if (entity instanceof EntityPlayer) {
                    vec3d = vec3d.subtract(0.0D, 1.0D, 0.0D);
                }
            } else {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityplayer = (EntityPlayer) entity;

                    return entityplayer.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING, PlayerRespawnEvent.RespawnReason.END_PORTAL); // CraftBukkit
                }

                vec3d = entity.adjustSpawnLocation(worldserver1, blockposition1).getBottomCenter();
            }

            // CraftBukkit start
            CraftPortalEvent event = entity.callPortalEvent(entity, CraftLocation.toBukkit(vec3d, worldserver1.getWorld(), f, entity.getXRot()), PlayerTeleportEvent.TeleportCause.END_PORTAL, 0, 0);
            if (event == null) {
                return null;
            }
            Location to = event.getTo();

            return new DimensionTransition(((CraftWorld) to.getWorld()).getHandle(), CraftLocation.toVec3D(to), entity.getDeltaMovement(), to.getYaw(), to.getPitch(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET), PlayerTeleportEvent.TeleportCause.END_PORTAL);
            // CraftBukkit end
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
    public ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBeReplaced(IBlockData iblockdata, FluidType fluidtype) {
        return false;
    }
}
