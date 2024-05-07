package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.util.profiling.GameProfilerFiller;

public class PathfinderGoalSelector {

    private static final PathfinderGoalWrapped NO_GOAL = new PathfinderGoalWrapped(Integer.MAX_VALUE, new PathfinderGoal() {
        @Override
        public boolean canUse() {
            return false;
        }
    }) {
        @Override
        public boolean isRunning() {
            return false;
        }
    };
    private final Map<PathfinderGoal.Type, PathfinderGoalWrapped> lockedFlags = new EnumMap(PathfinderGoal.Type.class);
    private final Set<PathfinderGoalWrapped> availableGoals = new ObjectLinkedOpenHashSet();
    private final Supplier<GameProfilerFiller> profiler;
    private final EnumSet<PathfinderGoal.Type> disabledFlags = EnumSet.noneOf(PathfinderGoal.Type.class);

    public PathfinderGoalSelector(Supplier<GameProfilerFiller> supplier) {
        this.profiler = supplier;
    }

    public void addGoal(int i, PathfinderGoal pathfindergoal) {
        this.availableGoals.add(new PathfinderGoalWrapped(i, pathfindergoal));
    }

    @VisibleForTesting
    public void removeAllGoals(Predicate<PathfinderGoal> predicate) {
        this.availableGoals.removeIf((pathfindergoalwrapped) -> {
            return predicate.test(pathfindergoalwrapped.getGoal());
        });
    }

    public void removeGoal(PathfinderGoal pathfindergoal) {
        Iterator iterator = this.availableGoals.iterator();

        while (iterator.hasNext()) {
            PathfinderGoalWrapped pathfindergoalwrapped = (PathfinderGoalWrapped) iterator.next();

            if (pathfindergoalwrapped.getGoal() == pathfindergoal && pathfindergoalwrapped.isRunning()) {
                pathfindergoalwrapped.stop();
            }
        }

        this.availableGoals.removeIf((pathfindergoalwrapped1) -> {
            return pathfindergoalwrapped1.getGoal() == pathfindergoal;
        });
    }

    private static boolean goalContainsAnyFlags(PathfinderGoalWrapped pathfindergoalwrapped, EnumSet<PathfinderGoal.Type> enumset) {
        Iterator iterator = pathfindergoalwrapped.getFlags().iterator();

        PathfinderGoal.Type pathfindergoal_type;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            pathfindergoal_type = (PathfinderGoal.Type) iterator.next();
        } while (!enumset.contains(pathfindergoal_type));

        return true;
    }

    private static boolean goalCanBeReplacedForAllFlags(PathfinderGoalWrapped pathfindergoalwrapped, Map<PathfinderGoal.Type, PathfinderGoalWrapped> map) {
        Iterator iterator = pathfindergoalwrapped.getFlags().iterator();

        PathfinderGoal.Type pathfindergoal_type;

        do {
            if (!iterator.hasNext()) {
                return true;
            }

            pathfindergoal_type = (PathfinderGoal.Type) iterator.next();
        } while (((PathfinderGoalWrapped) map.getOrDefault(pathfindergoal_type, PathfinderGoalSelector.NO_GOAL)).canBeReplacedBy(pathfindergoalwrapped));

        return false;
    }

    public void tick() {
        GameProfilerFiller gameprofilerfiller = (GameProfilerFiller) this.profiler.get();

        gameprofilerfiller.push("goalCleanup");
        Iterator iterator = this.availableGoals.iterator();

        PathfinderGoalWrapped pathfindergoalwrapped;

        while (iterator.hasNext()) {
            pathfindergoalwrapped = (PathfinderGoalWrapped) iterator.next();
            if (pathfindergoalwrapped.isRunning() && (goalContainsAnyFlags(pathfindergoalwrapped, this.disabledFlags) || !pathfindergoalwrapped.canContinueToUse())) {
                pathfindergoalwrapped.stop();
            }
        }

        this.lockedFlags.entrySet().removeIf((entry) -> {
            return !((PathfinderGoalWrapped) entry.getValue()).isRunning();
        });
        gameprofilerfiller.pop();
        gameprofilerfiller.push("goalUpdate");
        iterator = this.availableGoals.iterator();

        while (iterator.hasNext()) {
            pathfindergoalwrapped = (PathfinderGoalWrapped) iterator.next();
            if (!pathfindergoalwrapped.isRunning() && !goalContainsAnyFlags(pathfindergoalwrapped, this.disabledFlags) && goalCanBeReplacedForAllFlags(pathfindergoalwrapped, this.lockedFlags) && pathfindergoalwrapped.canUse()) {
                Iterator iterator1 = pathfindergoalwrapped.getFlags().iterator();

                while (iterator1.hasNext()) {
                    PathfinderGoal.Type pathfindergoal_type = (PathfinderGoal.Type) iterator1.next();
                    PathfinderGoalWrapped pathfindergoalwrapped1 = (PathfinderGoalWrapped) this.lockedFlags.getOrDefault(pathfindergoal_type, PathfinderGoalSelector.NO_GOAL);

                    pathfindergoalwrapped1.stop();
                    this.lockedFlags.put(pathfindergoal_type, pathfindergoalwrapped);
                }

                pathfindergoalwrapped.start();
            }
        }

        gameprofilerfiller.pop();
        this.tickRunningGoals(true);
    }

    public void tickRunningGoals(boolean flag) {
        GameProfilerFiller gameprofilerfiller = (GameProfilerFiller) this.profiler.get();

        gameprofilerfiller.push("goalTick");
        Iterator iterator = this.availableGoals.iterator();

        while (iterator.hasNext()) {
            PathfinderGoalWrapped pathfindergoalwrapped = (PathfinderGoalWrapped) iterator.next();

            if (pathfindergoalwrapped.isRunning() && (flag || pathfindergoalwrapped.requiresUpdateEveryTick())) {
                pathfindergoalwrapped.tick();
            }
        }

        gameprofilerfiller.pop();
    }

    public Set<PathfinderGoalWrapped> getAvailableGoals() {
        return this.availableGoals;
    }

    public void disableControlFlag(PathfinderGoal.Type pathfindergoal_type) {
        this.disabledFlags.add(pathfindergoal_type);
    }

    public void enableControlFlag(PathfinderGoal.Type pathfindergoal_type) {
        this.disabledFlags.remove(pathfindergoal_type);
    }

    public void setControlFlag(PathfinderGoal.Type pathfindergoal_type, boolean flag) {
        if (flag) {
            this.enableControlFlag(pathfindergoal_type);
        } else {
            this.disableControlFlag(pathfindergoal_type);
        }

    }
}
