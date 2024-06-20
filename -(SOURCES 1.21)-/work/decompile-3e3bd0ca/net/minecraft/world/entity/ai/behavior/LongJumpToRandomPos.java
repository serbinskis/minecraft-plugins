package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom2;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.phys.Vec3D;

public class LongJumpToRandomPos<E extends EntityInsentient> extends Behavior<E> {

    protected static final int FIND_JUMP_TRIES = 20;
    private static final int PREPARE_JUMP_DURATION = 40;
    protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
    private static final int TIME_OUT_DURATION = 200;
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(new Integer[]{65, 70, 75, 80});
    private final UniformInt timeBetweenLongJumps;
    protected final int maxLongJumpHeight;
    protected final int maxLongJumpWidth;
    protected final float maxJumpVelocityMultiplier;
    protected List<LongJumpToRandomPos.a> jumpCandidates;
    protected Optional<Vec3D> initialPosition;
    @Nullable
    protected Vec3D chosenJump;
    protected int findJumpTries;
    protected long prepareJumpStart;
    private final Function<E, SoundEffect> getJumpSound;
    private final BiPredicate<E, BlockPosition> acceptableLandingSpot;

    public LongJumpToRandomPos(UniformInt uniformint, int i, int j, float f, Function<E, SoundEffect> function) {
        this(uniformint, i, j, f, function, LongJumpToRandomPos::defaultAcceptableLandingSpot);
    }

    public static <E extends EntityInsentient> boolean defaultAcceptableLandingSpot(E e0, BlockPosition blockposition) {
        World world = e0.level();
        BlockPosition blockposition1 = blockposition.below();

        return world.getBlockState(blockposition1).isSolidRender(world, blockposition1) && e0.getPathfindingMalus(PathfinderNormal.getPathTypeStatic(e0, blockposition)) == 0.0F;
    }

    public LongJumpToRandomPos(UniformInt uniformint, int i, int j, float f, Function<E, SoundEffect> function, BiPredicate<E, BlockPosition> bipredicate) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), 200);
        this.jumpCandidates = Lists.newArrayList();
        this.initialPosition = Optional.empty();
        this.timeBetweenLongJumps = uniformint;
        this.maxLongJumpHeight = i;
        this.maxLongJumpWidth = j;
        this.maxJumpVelocityMultiplier = f;
        this.getJumpSound = function;
        this.acceptableLandingSpot = bipredicate;
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, EntityInsentient entityinsentient) {
        boolean flag = entityinsentient.onGround() && !entityinsentient.isInWater() && !entityinsentient.isInLava() && !worldserver.getBlockState(entityinsentient.blockPosition()).is(Blocks.HONEY_BLOCK);

        if (!flag) {
            entityinsentient.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object) (this.timeBetweenLongJumps.sample(worldserver.random) / 2));
        }

        return flag;
    }

    protected boolean canStillUse(WorldServer worldserver, EntityInsentient entityinsentient, long i) {
        boolean flag = this.initialPosition.isPresent() && ((Vec3D) this.initialPosition.get()).equals(entityinsentient.position()) && this.findJumpTries > 0 && !entityinsentient.isInWaterOrBubble() && (this.chosenJump != null || !this.jumpCandidates.isEmpty());

        if (!flag && entityinsentient.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
            entityinsentient.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object) (this.timeBetweenLongJumps.sample(worldserver.random) / 2));
            entityinsentient.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        }

        return flag;
    }

    protected void start(WorldServer worldserver, E e0, long i) {
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.initialPosition = Optional.of(e0.position());
        BlockPosition blockposition = e0.blockPosition();
        int j = blockposition.getX();
        int k = blockposition.getY();
        int l = blockposition.getZ();

        this.jumpCandidates = (List) BlockPosition.betweenClosedStream(j - this.maxLongJumpWidth, k - this.maxLongJumpHeight, l - this.maxLongJumpWidth, j + this.maxLongJumpWidth, k + this.maxLongJumpHeight, l + this.maxLongJumpWidth).filter((blockposition1) -> {
            return !blockposition1.equals(blockposition);
        }).map((blockposition1) -> {
            return new LongJumpToRandomPos.a(blockposition1.immutable(), MathHelper.ceil(blockposition.distSqr(blockposition1)));
        }).collect(Collectors.toCollection(Lists::newArrayList));
    }

    protected void tick(WorldServer worldserver, E e0, long i) {
        if (this.chosenJump != null) {
            if (i - this.prepareJumpStart >= 40L) {
                e0.setYRot(e0.yBodyRot);
                e0.setDiscardFriction(true);
                double d0 = this.chosenJump.length();
                double d1 = d0 + (double) e0.getJumpBoostPower();

                e0.setDeltaMovement(this.chosenJump.scale(d1 / d0));
                e0.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object) true);
                worldserver.playSound((EntityHuman) null, (Entity) e0, (SoundEffect) this.getJumpSound.apply(e0), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
        } else {
            --this.findJumpTries;
            this.pickCandidate(worldserver, e0, i);
        }

    }

    protected void pickCandidate(WorldServer worldserver, E e0, long i) {
        while (true) {
            if (!this.jumpCandidates.isEmpty()) {
                Optional<LongJumpToRandomPos.a> optional = this.getJumpCandidate(worldserver);

                if (optional.isEmpty()) {
                    continue;
                }

                LongJumpToRandomPos.a longjumptorandompos_a = (LongJumpToRandomPos.a) optional.get();
                BlockPosition blockposition = longjumptorandompos_a.getJumpTarget();

                if (!this.isAcceptableLandingPosition(worldserver, e0, blockposition)) {
                    continue;
                }

                Vec3D vec3d = Vec3D.atCenterOf(blockposition);
                Vec3D vec3d1 = this.calculateOptimalJumpVector(e0, vec3d);

                if (vec3d1 == null) {
                    continue;
                }

                e0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, (Object) (new BehaviorTarget(blockposition)));
                NavigationAbstract navigationabstract = e0.getNavigation();
                PathEntity pathentity = navigationabstract.createPath(blockposition, 0, 8);

                if (pathentity != null && pathentity.canReach()) {
                    continue;
                }

                this.chosenJump = vec3d1;
                this.prepareJumpStart = i;
                return;
            }

            return;
        }
    }

    protected Optional<LongJumpToRandomPos.a> getJumpCandidate(WorldServer worldserver) {
        Optional<LongJumpToRandomPos.a> optional = WeightedRandom2.getRandomItem(worldserver.random, this.jumpCandidates);
        List list = this.jumpCandidates;

        Objects.requireNonNull(this.jumpCandidates);
        optional.ifPresent(list::remove);
        return optional;
    }

    private boolean isAcceptableLandingPosition(WorldServer worldserver, E e0, BlockPosition blockposition) {
        BlockPosition blockposition1 = e0.blockPosition();
        int i = blockposition1.getX();
        int j = blockposition1.getZ();

        return i == blockposition.getX() && j == blockposition.getZ() ? false : this.acceptableLandingSpot.test(e0, blockposition);
    }

    @Nullable
    protected Vec3D calculateOptimalJumpVector(EntityInsentient entityinsentient, Vec3D vec3d) {
        List<Integer> list = Lists.newArrayList(LongJumpToRandomPos.ALLOWED_ANGLES);

        Collections.shuffle(list);
        float f = (float) (entityinsentient.getAttributeValue(GenericAttributes.JUMP_STRENGTH) * (double) this.maxJumpVelocityMultiplier);
        Iterator iterator = list.iterator();

        Optional optional;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            int i = (Integer) iterator.next();

            optional = LongJumpUtil.calculateJumpVectorForAngle(entityinsentient, vec3d, f, i, true);
        } while (!optional.isPresent());

        return (Vec3D) optional.get();
    }

    public static class a extends WeightedEntry.a {

        private final BlockPosition jumpTarget;

        public a(BlockPosition blockposition, int i) {
            super(i);
            this.jumpTarget = blockposition;
        }

        public BlockPosition getJumpTarget() {
            return this.jumpTarget;
        }
    }
}
