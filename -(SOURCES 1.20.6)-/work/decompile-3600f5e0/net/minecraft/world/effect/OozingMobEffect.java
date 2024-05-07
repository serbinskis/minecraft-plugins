package net.minecraft.world.effect;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;

class OozingMobEffect extends MobEffectList {

    private static final int RADIUS_TO_CHECK_SLIMES = 2;
    public static final int SLIME_SIZE = 2;
    private final ToIntFunction<RandomSource> spawnedCount;

    protected OozingMobEffect(MobEffectInfo mobeffectinfo, int i, ToIntFunction<RandomSource> tointfunction) {
        super(mobeffectinfo, i, Particles.ITEM_SLIME);
        this.spawnedCount = tointfunction;
    }

    @VisibleForTesting
    protected static int numberOfSlimesToSpawn(int i, int j, int k) {
        return MathHelper.clamp(0, i - j, k);
    }

    @Override
    public void onMobRemoved(EntityLiving entityliving, int i, Entity.RemovalReason entity_removalreason) {
        if (entity_removalreason == Entity.RemovalReason.KILLED) {
            int j = this.spawnedCount.applyAsInt(entityliving.getRandom());
            World world = entityliving.level();
            int k = world.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            List<EntitySlime> list = new ArrayList();

            world.getEntities(EntityTypes.SLIME, entityliving.getBoundingBox().inflate(2.0D), (entityslime) -> {
                return entityslime != entityliving;
            }, list, k);
            int l = numberOfSlimesToSpawn(k, list.size(), j);

            for (int i1 = 0; i1 < l; ++i1) {
                this.spawnSlimeOffspring(entityliving.level(), entityliving.getX(), entityliving.getY() + 0.5D, entityliving.getZ());
            }

        }
    }

    private void spawnSlimeOffspring(World world, double d0, double d1, double d2) {
        EntitySlime entityslime = (EntitySlime) EntityTypes.SLIME.create(world);

        if (entityslime != null) {
            entityslime.setSize(2, true);
            entityslime.moveTo(d0, d1, d2, world.getRandom().nextFloat() * 360.0F, 0.0F);
            world.addFreshEntity(entityslime);
        }
    }
}
