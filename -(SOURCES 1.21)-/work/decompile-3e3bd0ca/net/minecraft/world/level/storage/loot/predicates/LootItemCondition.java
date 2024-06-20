package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.storage.loot.LootItemUser;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public interface LootItemCondition extends LootItemUser, Predicate<LootTableInfo> {

    Codec<LootItemCondition> TYPED_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE.byNameCodec().dispatch("condition", LootItemCondition::getType, LootItemConditionType::codec);
    Codec<LootItemCondition> DIRECT_CODEC = Codec.lazyInitialized(() -> {
        return Codec.withAlternative(LootItemCondition.TYPED_CODEC, AllOfCondition.INLINE_CODEC);
    });
    Codec<Holder<LootItemCondition>> CODEC = RegistryFileCodec.create(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);

    LootItemConditionType getType();

    @FunctionalInterface
    public interface a {

        LootItemCondition build();

        default LootItemCondition.a invert() {
            return LootItemConditionInverted.invert(this);
        }

        default AnyOfCondition.a or(LootItemCondition.a lootitemcondition_a) {
            return AnyOfCondition.anyOf(this, lootitemcondition_a);
        }

        default AllOfCondition.a and(LootItemCondition.a lootitemcondition_a) {
            return AllOfCondition.allOf(this, lootitemcondition_a);
        }
    }
}
