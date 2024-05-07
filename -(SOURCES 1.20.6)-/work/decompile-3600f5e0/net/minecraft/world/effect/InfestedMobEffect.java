package net.minecraft.world.effect;

import java.util.function.ToIntFunction;
import net.minecraft.core.particles.Particles;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntitySilverfish;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.joml.Vector3f;

class InfestedMobEffect extends MobEffectList {

    private final float chanceToSpawn;
    private final ToIntFunction<RandomSource> spawnedCount;

    protected InfestedMobEffect(MobEffectInfo mobeffectinfo, int i, float f, ToIntFunction<RandomSource> tointfunction) {
        super(mobeffectinfo, i, Particles.INFESTED);
        this.chanceToSpawn = f;
        this.spawnedCount = tointfunction;
    }

    @Override
    public void onMobHurt(EntityLiving entityliving, int i, DamageSource damagesource, float f) {
        if (entityliving.getRandom().nextFloat() <= this.chanceToSpawn) {
            int j = this.spawnedCount.applyAsInt(entityliving.getRandom());

            for (int k = 0; k < j; ++k) {
                this.spawnSilverfish(entityliving.level(), entityliving, entityliving.getX(), entityliving.getY() + (double) entityliving.getBbHeight() / 2.0D, entityliving.getZ());
            }
        }

    }

    private void spawnSilverfish(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        EntitySilverfish entitysilverfish = (EntitySilverfish) EntityTypes.SILVERFISH.create(world);

        if (entitysilverfish != null) {
            RandomSource randomsource = entityliving.getRandom();
            float f = 1.5707964F;
            float f1 = MathHelper.randomBetween(randomsource, -1.5707964F, 1.5707964F);
            Vector3f vector3f = entityliving.getLookAngle().toVector3f().mul(0.3F).mul(1.0F, 1.5F, 1.0F).rotateY(f1);

            entitysilverfish.moveTo(d0, d1, d2, world.getRandom().nextFloat() * 360.0F, 0.0F);
            entitysilverfish.setDeltaMovement(new Vec3D(vector3f));
            world.addFreshEntity(entitysilverfish);
            entitysilverfish.playSound(SoundEffects.SILVERFISH_HURT);
        }
    }
}
