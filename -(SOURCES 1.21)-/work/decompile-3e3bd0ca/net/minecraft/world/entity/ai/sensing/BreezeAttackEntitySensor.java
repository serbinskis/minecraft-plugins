package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.breeze.Breeze;

public class BreezeAttackEntitySensor extends SensorNearestLivingEntities<Breeze> {

    public static final int BREEZE_SENSOR_RADIUS = 24;

    public BreezeAttackEntitySensor() {}

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    protected void doTick(WorldServer worldserver, Breeze breeze) {
        super.doTick(worldserver, breeze);
        breeze.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).stream().flatMap(Collection::stream).filter(IEntitySelector.NO_CREATIVE_OR_SPECTATOR).filter((entityliving) -> {
            return Sensor.isEntityAttackable(breeze, entityliving);
        }).findFirst().ifPresentOrElse((entityliving) -> {
            breeze.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, (Object) entityliving);
        }, () -> {
            breeze.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        });
    }

    @Override
    protected int radiusXZ() {
        return 24;
    }

    @Override
    protected int radiusY() {
        return 24;
    }
}
