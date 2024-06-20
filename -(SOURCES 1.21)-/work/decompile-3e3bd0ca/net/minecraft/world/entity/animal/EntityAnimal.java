package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockLightAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;

public abstract class EntityAnimal extends EntityAgeable {

    protected static final int PARENT_AGE_AFTER_BREEDING = 6000;
    public int inLove;
    @Nullable
    public UUID loveCause;

    protected EntityAnimal(EntityTypes<? extends EntityAnimal> entitytypes, World world) {
        super(entitytypes, world);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
    }

    @Override
    protected void customServerAiStep() {
        if (this.getAge() != 0) {
            this.inLove = 0;
        }

        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getAge() != 0) {
            this.inLove = 0;
        }

        if (this.inLove > 0) {
            --this.inLove;
            if (this.inLove % 10 == 0) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;

                this.level().addParticle(Particles.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
            }
        }

    }

    @Override
    protected void actuallyHurt(DamageSource damagesource, float f) {
        this.resetLove();
        super.actuallyHurt(damagesource, f);
    }

    @Override
    public float getWalkTargetValue(BlockPosition blockposition, IWorldReader iworldreader) {
        return iworldreader.getBlockState(blockposition.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : iworldreader.getPathfindingCostFromLightLevels(blockposition);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putInt("InLove", this.inLove);
        if (this.loveCause != null) {
            nbttagcompound.putUUID("LoveCause", this.loveCause);
        }

    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.inLove = nbttagcompound.getInt("InLove");
        this.loveCause = nbttagcompound.hasUUID("LoveCause") ? nbttagcompound.getUUID("LoveCause") : null;
    }

    public static boolean checkAnimalSpawnRules(EntityTypes<? extends EntityAnimal> entitytypes, GeneratorAccess generatoraccess, EnumMobSpawn enummobspawn, BlockPosition blockposition, RandomSource randomsource) {
        boolean flag = EnumMobSpawn.ignoresLightRequirements(enummobspawn) || isBrightEnoughToSpawn(generatoraccess, blockposition);

        return generatoraccess.getBlockState(blockposition.below()).is(TagsBlock.ANIMALS_SPAWNABLE_ON) && flag;
    }

    protected static boolean isBrightEnoughToSpawn(IBlockLightAccess iblocklightaccess, BlockPosition blockposition) {
        return iblocklightaccess.getRawBrightness(blockposition, 0) > 8;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public boolean removeWhenFarAway(double d0) {
        return false;
    }

    @Override
    protected int getBaseExperienceReward() {
        return 1 + this.level().random.nextInt(3);
    }

    public abstract boolean isFood(ItemStack itemstack);

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (this.isFood(itemstack)) {
            int i = this.getAge();

            if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(entityhuman, enumhand, itemstack);
                this.setInLove(entityhuman);
                return EnumInteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(entityhuman, enumhand, itemstack);
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.level().isClientSide) {
                return EnumInteractionResult.CONSUME;
            }
        }

        return super.mobInteract(entityhuman, enumhand);
    }

    protected void usePlayerItem(EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
        itemstack.consume(1, entityhuman);
    }

    public boolean canFallInLove() {
        return this.inLove <= 0;
    }

    public void setInLove(@Nullable EntityHuman entityhuman) {
        this.inLove = 600;
        if (entityhuman != null) {
            this.loveCause = entityhuman.getUUID();
        }

        this.level().broadcastEntityEvent(this, (byte) 18);
    }

    public void setInLoveTime(int i) {
        this.inLove = i;
    }

    public int getInLoveTime() {
        return this.inLove;
    }

    @Nullable
    public EntityPlayer getLoveCause() {
        if (this.loveCause == null) {
            return null;
        } else {
            EntityHuman entityhuman = this.level().getPlayerByUUID(this.loveCause);

            return entityhuman instanceof EntityPlayer ? (EntityPlayer) entityhuman : null;
        }
    }

    public boolean isInLove() {
        return this.inLove > 0;
    }

    public void resetLove() {
        this.inLove = 0;
    }

    public boolean canMate(EntityAnimal entityanimal) {
        return entityanimal == this ? false : (entityanimal.getClass() != this.getClass() ? false : this.isInLove() && entityanimal.isInLove());
    }

    public void spawnChildFromBreeding(WorldServer worldserver, EntityAnimal entityanimal) {
        EntityAgeable entityageable = this.getBreedOffspring(worldserver, entityanimal);

        if (entityageable != null) {
            entityageable.setBaby(true);
            entityageable.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
            this.finalizeSpawnChildFromBreeding(worldserver, entityanimal, entityageable);
            worldserver.addFreshEntityWithPassengers(entityageable);
        }
    }

    public void finalizeSpawnChildFromBreeding(WorldServer worldserver, EntityAnimal entityanimal, @Nullable EntityAgeable entityageable) {
        Optional.ofNullable(this.getLoveCause()).or(() -> {
            return Optional.ofNullable(entityanimal.getLoveCause());
        }).ifPresent((entityplayer) -> {
            entityplayer.awardStat(StatisticList.ANIMALS_BRED);
            CriterionTriggers.BRED_ANIMALS.trigger(entityplayer, this, entityanimal, entityageable);
        });
        this.setAge(6000);
        entityanimal.setAge(6000);
        this.resetLove();
        entityanimal.resetLove();
        worldserver.broadcastEntityEvent(this, (byte) 18);
        if (worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            worldserver.addFreshEntity(new EntityExperienceOrb(worldserver, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }

    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 18) {
            for (int i = 0; i < 7; ++i) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;

                this.level().addParticle(Particles.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
            }
        } else {
            super.handleEntityEvent(b0);
        }

    }
}
