package net.minecraft.world.entity.ai.sensing;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MobSensor<T extends EntityLiving> extends Sensor<T> {

    private final BiPredicate<T, EntityLiving> mobTest;
    private final Predicate<T> readyTest;
    private final MemoryModuleType<Boolean> toSet;
    private final int memoryTimeToLive;

    public MobSensor(int i, BiPredicate<T, EntityLiving> bipredicate, Predicate<T> predicate, MemoryModuleType<Boolean> memorymoduletype, int j) {
        super(i);
        this.mobTest = bipredicate;
        this.readyTest = predicate;
        this.toSet = memorymoduletype;
        this.memoryTimeToLive = j;
    }

    @Override
    protected void doTick(WorldServer worldserver, T t0) {
        if (!this.readyTest.test(t0)) {
            this.clearMemory(t0);
        } else {
            this.checkForMobsNearby(t0);
        }

    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    public void checkForMobsNearby(T t0) {
        Optional<List<EntityLiving>> optional = t0.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);

        if (!optional.isEmpty()) {
            boolean flag = ((List) optional.get()).stream().anyMatch((entityliving) -> {
                return this.mobTest.test(t0, entityliving);
            });

            if (flag) {
                this.mobDetected(t0);
            }

        }
    }

    public void mobDetected(T t0) {
        t0.getBrain().setMemoryWithExpiry(this.toSet, true, (long) this.memoryTimeToLive);
    }

    public void clearMemory(T t0) {
        t0.getBrain().eraseMemory(this.toSet);
    }
}
