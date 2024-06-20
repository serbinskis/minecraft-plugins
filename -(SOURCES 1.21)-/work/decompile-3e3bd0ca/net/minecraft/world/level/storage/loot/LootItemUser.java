package net.minecraft.world.level.storage.loot;

import java.util.Set;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;

public interface LootItemUser {

    default Set<LootContextParameter<?>> getReferencedContextParams() {
        return Set.of();
    }

    default void validate(LootCollector lootcollector) {
        lootcollector.validateUser(this);
    }
}
