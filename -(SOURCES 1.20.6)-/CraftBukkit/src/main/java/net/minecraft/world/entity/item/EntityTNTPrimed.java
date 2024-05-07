package net.minecraft.world.entity.item;

import javax.annotation.Nullable;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

// CraftBukkit start;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
// CraftBukkit end

public class EntityTNTPrimed extends Entity implements TraceableEntity {

    private static final DataWatcherObject<Integer> DATA_FUSE_ID = DataWatcher.defineId(EntityTNTPrimed.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<IBlockData> DATA_BLOCK_STATE_ID = DataWatcher.defineId(EntityTNTPrimed.class, DataWatcherRegistry.BLOCK_STATE);
    private static final int DEFAULT_FUSE_TIME = 80;
    private static final String TAG_BLOCK_STATE = "block_state";
    public static final String TAG_FUSE = "fuse";
    @Nullable
    public EntityLiving owner;
    public float yield = 4; // CraftBukkit - add field
    public boolean isIncendiary = false; // CraftBukkit - add field

    public EntityTNTPrimed(EntityTypes<? extends EntityTNTPrimed> entitytypes, World world) {
        super(entitytypes, world);
        this.blocksBuilding = true;
    }

    public EntityTNTPrimed(World world, double d0, double d1, double d2, @Nullable EntityLiving entityliving) {
        this(EntityTypes.TNT, world);
        this.setPos(d0, d1, d2);
        double d3 = world.random.nextDouble() * 6.2831854820251465D;

        this.setDeltaMovement(-Math.sin(d3) * 0.02D, 0.20000000298023224D, -Math.cos(d3) * 0.02D);
        this.setFuse(80);
        this.xo = d0;
        this.yo = d1;
        this.zo = d2;
        this.owner = entityliving;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityTNTPrimed.DATA_FUSE_ID, 80);
        datawatcher_a.define(EntityTNTPrimed.DATA_BLOCK_STATE_ID, Blocks.TNT.defaultBlockState());
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04D;
    }

    @Override
    public void tick() {
        this.applyGravity();
        this.move(EnumMoveType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        int i = this.getFuse() - 1;

        this.setFuse(i);
        if (i <= 0) {
            // CraftBukkit start - Need to reverse the order of the explosion and the entity death so we have a location for the event
            // this.discard();
            if (!this.level().isClientSide) {
                this.explode();
            }
            this.discard(EntityRemoveEvent.Cause.EXPLODE); // CraftBukkit - add Bukkit remove cause
            // CraftBukkit end
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide) {
                this.level().addParticle(Particles.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    private void explode() {
        // CraftBukkit start
        // float f = 4.0F;
        ExplosionPrimeEvent event = CraftEventFactory.callExplosionPrimeEvent((org.bukkit.entity.Explosive)this.getBukkitEntity());

        if (!event.isCancelled()) {
            this.level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(), event.getRadius(), event.getFire(), World.a.TNT);
        }
        // CraftBukkit end
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.putShort("fuse", (short) this.getFuse());
        nbttagcompound.put("block_state", GameProfileSerializer.writeBlockState(this.getBlockState()));
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.setFuse(nbttagcompound.getShort("fuse"));
        if (nbttagcompound.contains("block_state", 10)) {
            this.setBlockState(GameProfileSerializer.readBlockState(this.level().holderLookup(Registries.BLOCK), nbttagcompound.getCompound("block_state")));
        }

    }

    @Nullable
    @Override
    public EntityLiving getOwner() {
        return this.owner;
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof EntityTNTPrimed entitytntprimed) {
            this.owner = entitytntprimed.owner;
        }

    }

    public void setFuse(int i) {
        this.entityData.set(EntityTNTPrimed.DATA_FUSE_ID, i);
    }

    public int getFuse() {
        return (Integer) this.entityData.get(EntityTNTPrimed.DATA_FUSE_ID);
    }

    public void setBlockState(IBlockData iblockdata) {
        this.entityData.set(EntityTNTPrimed.DATA_BLOCK_STATE_ID, iblockdata);
    }

    public IBlockData getBlockState() {
        return (IBlockData) this.entityData.get(EntityTNTPrimed.DATA_BLOCK_STATE_ID);
    }
}
