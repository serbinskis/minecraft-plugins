package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import java.util.Iterator;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.projectiles.ProjectileSource;
// CraftBukkit end

public abstract class IProjectile extends Entity implements TraceableEntity {

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    private boolean leftOwner;
    private boolean hasBeenShot;

    // CraftBukkit start
    private boolean hitCancelled = false;
    // CraftBukkit end

    IProjectile(EntityTypes<? extends IProjectile> entitytypes, World world) {
        super(entitytypes, world);
    }

    public void setOwner(@Nullable Entity entity) {
        if (entity != null) {
            this.ownerUUID = entity.getUUID();
            this.cachedOwner = entity;
        }
        this.projectileSource = (entity != null && entity.getBukkitEntity() instanceof ProjectileSource) ? (ProjectileSource) entity.getBukkitEntity() : null; // CraftBukkit

    }

    @Nullable
    @Override
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else {
            if (this.ownerUUID != null) {
                World world = this.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    this.cachedOwner = worldserver.getEntity(this.ownerUUID);
                    return this.cachedOwner;
                }
            }

            return null;
        }
    }

    public Entity getEffectSource() {
        return (Entity) MoreObjects.firstNonNull(this.getOwner(), this);
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        if (this.ownerUUID != null) {
            nbttagcompound.putUUID("Owner", this.ownerUUID);
        }

        if (this.leftOwner) {
            nbttagcompound.putBoolean("LeftOwner", true);
        }

        nbttagcompound.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasUUID("Owner")) {
            this.ownerUUID = nbttagcompound.getUUID("Owner");
            this.cachedOwner = null;
        }

        this.leftOwner = nbttagcompound.getBoolean("LeftOwner");
        this.hasBeenShot = nbttagcompound.getBoolean("HasBeenShot");
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof IProjectile iprojectile) {
            this.cachedOwner = iprojectile.cachedOwner;
        }

    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }

        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity entity = this.getOwner();

        if (entity != null) {
            Iterator iterator = this.level().getEntities((Entity) this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (entity1) -> {
                return !entity1.isSpectator() && entity1.isPickable();
            }).iterator();

            while (iterator.hasNext()) {
                Entity entity1 = (Entity) iterator.next();

                if (entity1.getRootVehicle() == entity.getRootVehicle()) {
                    return false;
                }
            }
        }

        return true;
    }

    public Vec3D getMovementToShoot(double d0, double d1, double d2, float f, float f1) {
        return (new Vec3D(d0, d1, d2)).normalize().add(this.random.triangle(0.0D, 0.0172275D * (double) f1), this.random.triangle(0.0D, 0.0172275D * (double) f1), this.random.triangle(0.0D, 0.0172275D * (double) f1)).scale((double) f);
    }

    public void shoot(double d0, double d1, double d2, float f, float f1) {
        Vec3D vec3d = this.getMovementToShoot(d0, d1, d2, f, f1);

        this.setDeltaMovement(vec3d);
        double d3 = vec3d.horizontalDistance();

        this.setYRot((float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D));
        this.setXRot((float) (MathHelper.atan2(vec3d.y, d3) * 57.2957763671875D));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity entity, float f, float f1, float f2, float f3, float f4) {
        float f5 = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);
        float f6 = -MathHelper.sin((f + f2) * 0.017453292F);
        float f7 = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);

        this.shoot((double) f5, (double) f6, (double) f7, f3, f4);
        Vec3D vec3d = entity.getDeltaMovement();

        this.setDeltaMovement(this.getDeltaMovement().add(vec3d.x, entity.onGround() ? 0.0D : vec3d.y, vec3d.z));
    }

    // CraftBukkit start - call projectile hit event
    protected ProjectileDeflection preHitTargetOrDeflectSelf(MovingObjectPosition movingobjectposition) {
        org.bukkit.event.entity.ProjectileHitEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callProjectileHitEvent(this, movingobjectposition);
        this.hitCancelled = event != null && event.isCancelled();
        if (movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.BLOCK || !this.hitCancelled) {
            return this.hitTargetOrDeflectSelf(movingobjectposition);
        }
        return ProjectileDeflection.NONE;
    }
    // CraftBukkit end

    protected ProjectileDeflection hitTargetOrDeflectSelf(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            MovingObjectPositionEntity movingobjectpositionentity = (MovingObjectPositionEntity) movingobjectposition;
            ProjectileDeflection projectiledeflection = movingobjectpositionentity.getEntity().deflection(this);

            if (projectiledeflection != ProjectileDeflection.NONE) {
                this.deflect(projectiledeflection, movingobjectpositionentity.getEntity(), this.getOwner(), false);
                return projectiledeflection;
            }
        }

        this.onHit(movingobjectposition);
        return ProjectileDeflection.NONE;
    }

    public void deflect(ProjectileDeflection projectiledeflection, @Nullable Entity entity, @Nullable Entity entity1, boolean flag) {
        if (!this.level().isClientSide) {
            projectiledeflection.deflect(this, entity, this.random);
            this.setOwner(entity1);
            this.onDeflection(entity, flag);
        }

    }

    protected void onDeflection(@Nullable Entity entity, boolean flag) {}

    protected void onHit(MovingObjectPosition movingobjectposition) {
        MovingObjectPosition.EnumMovingObjectType movingobjectposition_enummovingobjecttype = movingobjectposition.getType();

        if (movingobjectposition_enummovingobjecttype == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            MovingObjectPositionEntity movingobjectpositionentity = (MovingObjectPositionEntity) movingobjectposition;
            Entity entity = movingobjectpositionentity.getEntity();

            if (entity.getType().is(TagsEntity.REDIRECTABLE_PROJECTILE) && entity instanceof IProjectile) {
                IProjectile iprojectile = (IProjectile) entity;

                iprojectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.getOwner(), true);
            }

            this.onHitEntity(movingobjectpositionentity);
            this.level().gameEvent((Holder) GameEvent.PROJECTILE_LAND, movingobjectposition.getLocation(), GameEvent.a.of(this, (IBlockData) null));
        } else if (movingobjectposition_enummovingobjecttype == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock) movingobjectposition;

            this.onHitBlock(movingobjectpositionblock);
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

            this.level().gameEvent((Holder) GameEvent.PROJECTILE_LAND, blockposition, GameEvent.a.of(this, this.level().getBlockState(blockposition)));
        }

    }

    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {}

    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        // CraftBukkit start - cancellable hit event
        if (hitCancelled) {
            return;
        }
        // CraftBukkit end
        IBlockData iblockdata = this.level().getBlockState(movingobjectpositionblock.getBlockPos());

        iblockdata.onProjectileHit(this.level(), iblockdata, movingobjectpositionblock, this);
    }

    @Override
    public void lerpMotion(double d0, double d1, double d2) {
        this.setDeltaMovement(d0, d1, d2);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);

            this.setXRot((float) (MathHelper.atan2(d1, d3) * 57.2957763671875D));
            this.setYRot((float) (MathHelper.atan2(d0, d2) * 57.2957763671875D));
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }

    }

    protected boolean canHitEntity(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        } else {
            Entity entity1 = this.getOwner();

            return entity1 == null || this.leftOwner || !entity1.isPassengerOfSameVehicle(entity);
        }
    }

    protected void updateRotation() {
        Vec3D vec3d = this.getDeltaMovement();
        double d0 = vec3d.horizontalDistance();

        this.setXRot(lerpRotation(this.xRotO, (float) (MathHelper.atan2(vec3d.y, d0) * 57.2957763671875D)));
        this.setYRot(lerpRotation(this.yRotO, (float) (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D)));
    }

    protected static float lerpRotation(float f, float f1) {
        while (f1 - f < -180.0F) {
            f -= 360.0F;
        }

        while (f1 - f >= 180.0F) {
            f += 360.0F;
        }

        return MathHelper.lerp(0.2F, f, f1);
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket() {
        Entity entity = this.getOwner();

        return new PacketPlayOutSpawnEntity(this, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        super.recreateFromPacket(packetplayoutspawnentity);
        Entity entity = this.level().getEntity(packetplayoutspawnentity.getData());

        if (entity != null) {
            this.setOwner(entity);
        }

    }

    @Override
    public boolean mayInteract(World world, BlockPosition blockposition) {
        Entity entity = this.getOwner();

        return entity instanceof EntityHuman ? entity.mayInteract(world, blockposition) : entity == null || world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public boolean mayBreak(World world) {
        return this.getType().is(TagsEntity.IMPACT_PROJECTILES) && world.getGameRules().getBoolean(GameRules.RULE_PROJECTILESCANBREAKBLOCKS);
    }

    @Override
    public boolean isPickable() {
        return this.getType().is(TagsEntity.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getPickRadius() {
        return this.isPickable() ? 1.0F : 0.0F;
    }
}
