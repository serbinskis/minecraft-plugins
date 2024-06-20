package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.phys.Vec3D;

public class AnimalPanic<E extends EntityCreature> extends Behavior<E> {

    private static final int PANIC_MIN_DURATION = 100;
    private static final int PANIC_MAX_DURATION = 120;
    private static final int PANIC_DISTANCE_HORIZONTAL = 5;
    private static final int PANIC_DISTANCE_VERTICAL = 4;
    private final float speedMultiplier;
    private final Function<EntityCreature, TagKey<DamageType>> panicCausingDamageTypes;

    public AnimalPanic(float f) {
        this(f, (entitycreature) -> {
            return DamageTypeTags.PANIC_CAUSES;
        });
    }

    public AnimalPanic(float f, Function<EntityCreature, TagKey<DamageType>> function) {
        super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED), 100, 120);
        this.speedMultiplier = f;
        this.panicCausingDamageTypes = function;
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, E e0) {
        return (Boolean) e0.getBrain().getMemory(MemoryModuleType.HURT_BY).map((damagesource) -> {
            return damagesource.is((TagKey) this.panicCausingDamageTypes.apply(e0));
        }).orElse(false) || e0.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    protected boolean canStillUse(WorldServer worldserver, E e0, long i) {
        return true;
    }

    protected void start(WorldServer worldserver, E e0, long i) {
        e0.getBrain().setMemory(MemoryModuleType.IS_PANICKING, (Object) true);
        e0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void stop(WorldServer worldserver, E e0, long i) {
        BehaviorController<?> behaviorcontroller = e0.getBrain();

        behaviorcontroller.eraseMemory(MemoryModuleType.IS_PANICKING);
    }

    protected void tick(WorldServer worldserver, E e0, long i) {
        if (e0.getNavigation().isDone()) {
            Vec3D vec3d = this.getPanicPos(e0, worldserver);

            if (vec3d != null) {
                e0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, (Object) (new MemoryTarget(vec3d, this.speedMultiplier, 0)));
            }
        }

    }

    @Nullable
    private Vec3D getPanicPos(E e0, WorldServer worldserver) {
        if (e0.isOnFire()) {
            Optional<Vec3D> optional = this.lookForWater(worldserver, e0).map(Vec3D::atBottomCenterOf);

            if (optional.isPresent()) {
                return (Vec3D) optional.get();
            }
        }

        return LandRandomPos.getPos(e0, 5, 4);
    }

    private Optional<BlockPosition> lookForWater(IBlockAccess iblockaccess, Entity entity) {
        BlockPosition blockposition = entity.blockPosition();

        if (!iblockaccess.getBlockState(blockposition).getCollisionShape(iblockaccess, blockposition).isEmpty()) {
            return Optional.empty();
        } else {
            Predicate predicate;

            if (MathHelper.ceil(entity.getBbWidth()) == 2) {
                predicate = (blockposition1) -> {
                    return BlockPosition.squareOutSouthEast(blockposition1).allMatch((blockposition2) -> {
                        return iblockaccess.getFluidState(blockposition2).is(TagsFluid.WATER);
                    });
                };
            } else {
                predicate = (blockposition1) -> {
                    return iblockaccess.getFluidState(blockposition1).is(TagsFluid.WATER);
                };
            }

            return BlockPosition.findClosestMatch(blockposition, 5, 1, predicate);
        }
    }
}
