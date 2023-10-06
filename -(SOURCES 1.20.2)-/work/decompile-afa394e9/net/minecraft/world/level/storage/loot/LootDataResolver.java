package net.minecraft.world.level.storage.loot;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.MinecraftKey;

@FunctionalInterface
public interface LootDataResolver {

    @Nullable
    <T> T getElement(LootDataId<T> lootdataid);

    @Nullable
    default <T> T getElement(LootDataType<T> lootdatatype, MinecraftKey minecraftkey) {
        return this.getElement(new LootDataId<>(lootdatatype, minecraftkey));
    }

    default <T> Optional<T> getElementOptional(LootDataId<T> lootdataid) {
        return Optional.ofNullable(this.getElement(lootdataid));
    }

    default <T> Optional<T> getElementOptional(LootDataType<T> lootdatatype, MinecraftKey minecraftkey) {
        return this.getElementOptional(new LootDataId<>(lootdatatype, minecraftkey));
    }

    default LootTable getLootTable(MinecraftKey minecraftkey) {
        return (LootTable) this.getElementOptional(LootDataType.TABLE, minecraftkey).orElse(LootTable.EMPTY);
    }
}
