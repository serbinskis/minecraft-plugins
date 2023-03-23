package net.minecraft.world.level;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;

public interface WorldAccess extends GeneratorAccess {

    WorldServer getLevel();

    default void addFreshEntityWithPassengers(Entity entity) {
        // CraftBukkit start
        this.addFreshEntityWithPassengers(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DEFAULT);
    }

    default void addFreshEntityWithPassengers(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        entity.getSelfAndPassengers().forEach((e) -> this.addFreshEntity(e, reason));
    }

    @Override
    default WorldServer getMinecraftWorld() {
        return getLevel();
    }
    // CraftBukkit end
}
