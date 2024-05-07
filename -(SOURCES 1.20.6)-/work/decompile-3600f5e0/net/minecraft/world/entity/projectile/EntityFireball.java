package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public abstract class EntityFireball extends IProjectile {

    public static final double ATTACK_DEFLECTION_SCALE = 0.1D;
    public static final double BOUNCE_DEFLECTION_SCALE = 0.05D;
    public double xPower;
    public double yPower;
    public double zPower;

    protected EntityFireball(EntityTypes<? extends EntityFireball> entitytypes, World world) {
        super(entitytypes, world);
    }

    protected EntityFireball(EntityTypes<? extends EntityFireball> entitytypes, double d0, double d1, double d2, World world) {
        this(entitytypes, world);
        this.setPos(d0, d1, d2);
    }

    public EntityFireball(EntityTypes<? extends EntityFireball> entitytypes, double d0, double d1, double d2, double d3, double d4, double d5, World world) {
        this(entitytypes, world);
        this.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());
        this.reapplyPosition();
        this.assignPower(d3, d4, d5);
    }

    public EntityFireball(EntityTypes<? extends EntityFireball> entitytypes, EntityLiving entityliving, double d0, double d1, double d2, World world) {
        this(entitytypes, entityliving.getX(), entityliving.getY(), entityliving.getZ(), d0, d1, d2, world);
        this.setOwner(entityliving);
        this.setRot(entityliving.getYRot(), entityliving.getXRot());
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {}

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        double d1 = this.getBoundingBox().getSize() * 4.0D;

        if (Double.isNaN(d1)) {
            d1 = 4.0D;
        }

        d1 *= 64.0D;
        return d0 < d1 * d1;
    }

    protected RayTrace.BlockCollisionOption getClipType() {
        return RayTrace.BlockCollisionOption.COLLIDER;
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();

        if (!this.level().isClientSide && (entity != null && entity.isRemoved() || !this.level().hasChunkAt(this.blockPosition()))) {
            this.discard();
        } else {
            super.tick();
            if (this.shouldBurn()) {
                this.igniteForSeconds(1);
            }

            MovingObjectPosition movingobjectposition = ProjectileHelper.getHitResultOnMoveVector(this, this::canHitEntity, this.getClipType());

            if (movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.MISS) {
                this.hitTargetOrDeflectSelf(movingobjectposition);
            }

            this.checkInsideBlocks();
            Vec3D vec3d = this.getDeltaMovement();
            double d0 = this.getX() + vec3d.x;
            double d1 = this.getY() + vec3d.y;
            double d2 = this.getZ() + vec3d.z;

            ProjectileHelper.rotateTowardsMovement(this, 0.2F);
            float f;

            if (this.isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    float f1 = 0.25F;

                    this.level().addParticle(Particles.BUBBLE, d0 - vec3d.x * 0.25D, d1 - vec3d.y * 0.25D, d2 - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
                }

                f = this.getLiquidInertia();
            } else {
                f = this.getInertia();
            }

            this.setDeltaMovement(vec3d.add(this.xPower, this.yPower, this.zPower).scale((double) f));
            ParticleParam particleparam = this.getTrailParticle();

            if (particleparam != null) {
                this.level().addParticle(particleparam, d0, d1 + 0.5D, d2, 0.0D, 0.0D, 0.0D);
            }

            this.setPos(d0, d1, d2);
        }
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        return !this.isInvulnerableTo(damagesource);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !entity.noPhysics;
    }

    protected boolean shouldBurn() {
        return true;
    }

    @Nullable
    protected ParticleParam getTrailParticle() {
        return Particles.SMOKE;
    }

    protected float getInertia() {
        return 0.95F;
    }

    protected float getLiquidInertia() {
        return 0.8F;
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.put("power", this.newDoubleList(new double[]{this.xPower, this.yPower, this.zPower}));
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        if (nbttagcompound.contains("power", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("power", 6);

            if (nbttaglist.size() == 3) {
                this.xPower = nbttaglist.getDouble(0);
                this.yPower = nbttaglist.getDouble(1);
                this.zPower = nbttaglist.getDouble(2);
            }
        }

    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket() {
        Entity entity = this.getOwner();
        int i = entity == null ? 0 : entity.getId();

        return new PacketPlayOutSpawnEntity(this.getId(), this.getUUID(), this.getX(), this.getY(), this.getZ(), this.getXRot(), this.getYRot(), this.getType(), i, new Vec3D(this.xPower, this.yPower, this.zPower), 0.0D);
    }

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        super.recreateFromPacket(packetplayoutspawnentity);
        double d0 = packetplayoutspawnentity.getXa();
        double d1 = packetplayoutspawnentity.getYa();
        double d2 = packetplayoutspawnentity.getZa();

        this.assignPower(d0, d1, d2);
    }

    public void assignPower(double d0, double d1, double d2) {
        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        if (d3 != 0.0D) {
            this.xPower = d0 / d3 * 0.1D;
            this.yPower = d1 / d3 * 0.1D;
            this.zPower = d2 / d3 * 0.1D;
        }

    }

    @Override
    protected void onDeflection(@Nullable Entity entity, boolean flag) {
        super.onDeflection(entity, flag);
        if (flag) {
            this.xPower = this.getDeltaMovement().x * 0.1D;
            this.yPower = this.getDeltaMovement().y * 0.1D;
            this.zPower = this.getDeltaMovement().z * 0.1D;
        } else {
            this.xPower = this.getDeltaMovement().x * 0.05D;
            this.yPower = this.getDeltaMovement().y * 0.05D;
            this.zPower = this.getDeltaMovement().z * 0.05D;
        }

    }
}
