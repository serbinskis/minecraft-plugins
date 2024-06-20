package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.monster.piglin.EntityPiglinAbstract;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.pathfinder.PathType;

public class EntitySkeletonWither extends EntitySkeletonAbstract {

    public EntitySkeletonWither(EntityTypes<? extends EntitySkeletonWither> entitytypes, World world) {
        super(entitytypes, world);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityPiglinAbstract.class, true));
        super.registerGoals();
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.WITHER_SKELETON_DEATH;
    }

    @Override
    SoundEffect getStepSound() {
        return SoundEffects.WITHER_SKELETON_STEP;
    }

    @Override
    protected void dropCustomDeathLoot(WorldServer worldserver, DamageSource damagesource, boolean flag) {
        super.dropCustomDeathLoot(worldserver, damagesource, flag);
        Entity entity = damagesource.getEntity();

        if (entity instanceof EntityCreeper entitycreeper) {
            if (entitycreeper.canDropMobsSkull()) {
                entitycreeper.increaseDroppedSkulls();
                this.spawnAtLocation((IMaterial) Items.WITHER_SKELETON_SKULL);
            }
        }

    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        this.setItemSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(WorldAccess worldaccess, RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {}

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        GroupDataEntity groupdataentity1 = super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, groupdataentity);

        this.getAttribute(GenericAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.reassessWeaponGoal();
        return groupdataentity1;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!super.doHurtTarget(entity)) {
            return false;
        } else {
            if (entity instanceof EntityLiving) {
                ((EntityLiving) entity).addEffect(new MobEffect(MobEffects.WITHER, 200), this);
            }

            return true;
        }
    }

    @Override
    protected EntityArrow getArrow(ItemStack itemstack, float f, @Nullable ItemStack itemstack1) {
        EntityArrow entityarrow = super.getArrow(itemstack, f, itemstack1);

        entityarrow.igniteForSeconds(100.0F);
        return entityarrow;
    }

    @Override
    public boolean canBeAffected(MobEffect mobeffect) {
        return mobeffect.is(MobEffects.WITHER) ? false : super.canBeAffected(mobeffect);
    }
}
