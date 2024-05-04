package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;

public record LootItemConditionTimeCheck(Optional<Long> period, IntRange value) implements LootItemCondition {

    public static final MapCodec<LootItemConditionTimeCheck> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.LONG.optionalFieldOf("period").forGetter(LootItemConditionTimeCheck::period), IntRange.CODEC.fieldOf("value").forGetter(LootItemConditionTimeCheck::value)).apply(instance, LootItemConditionTimeCheck::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TIME_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    public boolean test(LootTableInfo loottableinfo) {
        WorldServer worldserver = loottableinfo.getLevel();
        long i = worldserver.getDayTime();

        if (this.period.isPresent()) {
            i %= (Long) this.period.get();
        }

        return this.value.test(loottableinfo, (int) i);
    }

    public static LootItemConditionTimeCheck.a time(IntRange intrange) {
        return new LootItemConditionTimeCheck.a(intrange);
    }

    public static class a implements LootItemCondition.a {

        private Optional<Long> period = Optional.empty();
        private final IntRange value;

        public a(IntRange intrange) {
            this.value = intrange;
        }

        public LootItemConditionTimeCheck.a setPeriod(long i) {
            this.period = Optional.of(i);
            return this;
        }

        @Override
        public LootItemConditionTimeCheck build() {
            return new LootItemConditionTimeCheck(this.period, this.value);
        }
    }
}
