package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.players.NameReferencingFileConverter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.scores.ScoreboardTeam;

public abstract class EntityTameableAnimal extends EntityAnimal implements OwnableEntity {

    public static final int TELEPORT_WHEN_DISTANCE_IS_SQ = 144;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 1;
    protected static final DataWatcherObject<Byte> DATA_FLAGS_ID = DataWatcher.defineId(EntityTameableAnimal.class, DataWatcherRegistry.BYTE);
    protected static final DataWatcherObject<Optional<UUID>> DATA_OWNERUUID_ID = DataWatcher.defineId(EntityTameableAnimal.class, DataWatcherRegistry.OPTIONAL_UUID);
    private boolean orderedToSit;

    protected EntityTameableAnimal(EntityTypes<? extends EntityTameableAnimal> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityTameableAnimal.DATA_FLAGS_ID, (byte) 0);
        datawatcher_a.define(EntityTameableAnimal.DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        if (this.getOwnerUUID() != null) {
            nbttagcompound.putUUID("Owner", this.getOwnerUUID());
        }

        nbttagcompound.putBoolean("Sitting", this.orderedToSit);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        UUID uuid;

        if (nbttagcompound.hasUUID("Owner")) {
            uuid = nbttagcompound.getUUID("Owner");
        } else {
            String s = nbttagcompound.getString("Owner");

            uuid = NameReferencingFileConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
                this.setTame(true, false);
            } catch (Throwable throwable) {
                this.setTame(false, true);
            }
        }

        this.orderedToSit = nbttagcompound.getBoolean("Sitting");
        this.setInSittingPose(this.orderedToSit);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public boolean handleLeashAtDistance(Entity entity, float f) {
        if (this.isInSittingPose()) {
            if (f > 10.0F) {
                this.dropLeash(true, true);
            }

            return false;
        } else {
            return super.handleLeashAtDistance(entity, f);
        }
    }

    protected void spawnTamingParticles(boolean flag) {
        ParticleType particletype = Particles.HEART;

        if (!flag) {
            particletype = Particles.SMOKE;
        }

        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;

            this.level().addParticle(particletype, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }

    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 7) {
            this.spawnTamingParticles(true);
        } else if (b0 == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(b0);
        }

    }

    public boolean isTame() {
        return ((Byte) this.entityData.get(EntityTameableAnimal.DATA_FLAGS_ID) & 4) != 0;
    }

    public void setTame(boolean flag, boolean flag1) {
        byte b0 = (Byte) this.entityData.get(EntityTameableAnimal.DATA_FLAGS_ID);

        if (flag) {
            this.entityData.set(EntityTameableAnimal.DATA_FLAGS_ID, (byte) (b0 | 4));
        } else {
            this.entityData.set(EntityTameableAnimal.DATA_FLAGS_ID, (byte) (b0 & -5));
        }

        if (flag1) {
            this.applyTamingSideEffects();
        }

    }

    protected void applyTamingSideEffects() {}

    public boolean isInSittingPose() {
        return ((Byte) this.entityData.get(EntityTameableAnimal.DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean flag) {
        byte b0 = (Byte) this.entityData.get(EntityTameableAnimal.DATA_FLAGS_ID);

        if (flag) {
            this.entityData.set(EntityTameableAnimal.DATA_FLAGS_ID, (byte) (b0 | 1));
        } else {
            this.entityData.set(EntityTameableAnimal.DATA_FLAGS_ID, (byte) (b0 & -2));
        }

    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return (UUID) ((Optional) this.entityData.get(EntityTameableAnimal.DATA_OWNERUUID_ID)).orElse((Object) null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(EntityTameableAnimal.DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
    }

    public void tame(EntityHuman entityhuman) {
        this.setTame(true, true);
        this.setOwnerUUID(entityhuman.getUUID());
        if (entityhuman instanceof EntityPlayer entityplayer) {
            CriterionTriggers.TAME_ANIMAL.trigger(entityplayer, (EntityAnimal) this);
        }

    }

    @Override
    public boolean canAttack(EntityLiving entityliving) {
        return this.isOwnedBy(entityliving) ? false : super.canAttack(entityliving);
    }

    public boolean isOwnedBy(EntityLiving entityliving) {
        return entityliving == this.getOwner();
    }

    public boolean wantsToAttack(EntityLiving entityliving, EntityLiving entityliving1) {
        return true;
    }

    @Override
    public ScoreboardTeam getTeam() {
        if (this.isTame()) {
            EntityLiving entityliving = this.getOwner();

            if (entityliving != null) {
                return entityliving.getTeam();
            }
        }

        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (this.isTame()) {
            EntityLiving entityliving = this.getOwner();

            if (entity == entityliving) {
                return true;
            }

            if (entityliving != null) {
                return entityliving.isAlliedTo(entity);
            }
        }

        return super.isAlliedTo(entity);
    }

    @Override
    public void die(DamageSource damagesource) {
        if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof EntityPlayer) {
            this.getOwner().sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }

        super.die(damagesource);
    }

    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean flag) {
        this.orderedToSit = flag;
    }

    public void tryToTeleportToOwner() {
        EntityLiving entityliving = this.getOwner();

        if (entityliving != null) {
            this.teleportToAroundBlockPos(entityliving.blockPosition());
        }

    }

    public boolean shouldTryTeleportToOwner() {
        EntityLiving entityliving = this.getOwner();

        return entityliving != null && this.distanceToSqr((Entity) this.getOwner()) >= 144.0D;
    }

    private void teleportToAroundBlockPos(BlockPosition blockposition) {
        for (int i = 0; i < 10; ++i) {
            int j = this.random.nextIntBetweenInclusive(-3, 3);
            int k = this.random.nextIntBetweenInclusive(-3, 3);

            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextIntBetweenInclusive(-1, 1);

                if (this.maybeTeleportTo(blockposition.getX() + j, blockposition.getY() + l, blockposition.getZ() + k)) {
                    return;
                }
            }
        }

    }

    private boolean maybeTeleportTo(int i, int j, int k) {
        if (!this.canTeleportTo(new BlockPosition(i, j, k))) {
            return false;
        } else {
            this.moveTo((double) i + 0.5D, (double) j, (double) k + 0.5D, this.getYRot(), this.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPosition blockposition) {
        PathType pathtype = PathfinderNormal.getPathTypeStatic((EntityInsentient) this, blockposition);

        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            IBlockData iblockdata = this.level().getBlockState(blockposition.below());

            if (!this.canFlyToOwner() && iblockdata.getBlock() instanceof BlockLeaves) {
                return false;
            } else {
                BlockPosition blockposition1 = blockposition.subtract(this.blockPosition());

                return this.level().noCollision(this, this.getBoundingBox().move(blockposition1));
            }
        }
    }

    public final boolean unableToMoveToOwner() {
        return this.isOrderedToSit() || this.isPassenger() || this.mayBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canFlyToOwner() {
        return false;
    }

    public class a extends PathfinderGoalPanic {

        public a(final double d0, final TagKey tagkey) {
            super(EntityTameableAnimal.this, d0, tagkey);
        }

        public a(final double d0) {
            super(EntityTameableAnimal.this, d0);
        }

        @Override
        public void tick() {
            if (!EntityTameableAnimal.this.unableToMoveToOwner() && EntityTameableAnimal.this.shouldTryTeleportToOwner()) {
                EntityTameableAnimal.this.tryToTeleportToOwner();
            }

            super.tick();
        }
    }
}
