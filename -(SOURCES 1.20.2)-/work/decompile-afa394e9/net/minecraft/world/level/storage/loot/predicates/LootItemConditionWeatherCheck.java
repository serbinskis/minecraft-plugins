package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public record LootItemConditionWeatherCheck(Optional<Boolean> isRaining, Optional<Boolean> isThundering) implements LootItemCondition {

    public static final Codec<LootItemConditionWeatherCheck> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(Codec.BOOL, "raining").forGetter(LootItemConditionWeatherCheck::isRaining), ExtraCodecs.strictOptionalField(Codec.BOOL, "thundering").forGetter(LootItemConditionWeatherCheck::isThundering)).apply(instance, LootItemConditionWeatherCheck::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.WEATHER_CHECK;
    }

    public boolean test(LootTableInfo loottableinfo) {
        WorldServer worldserver = loottableinfo.getLevel();

        return this.isRaining.isPresent() && (Boolean) this.isRaining.get() != worldserver.isRaining() ? false : !this.isThundering.isPresent() || (Boolean) this.isThundering.get() == worldserver.isThundering();
    }

    public static LootItemConditionWeatherCheck.a weather() {
        return new LootItemConditionWeatherCheck.a();
    }

    public static class a implements LootItemCondition.a {

        private Optional<Boolean> isRaining = Optional.empty();
        private Optional<Boolean> isThundering = Optional.empty();

        public a() {}

        public LootItemConditionWeatherCheck.a setRaining(boolean flag) {
            this.isRaining = Optional.of(flag);
            return this;
        }

        public LootItemConditionWeatherCheck.a setThundering(boolean flag) {
            this.isThundering = Optional.of(flag);
            return this;
        }

        @Override
        public LootItemConditionWeatherCheck build() {
            return new LootItemConditionWeatherCheck(this.isRaining, this.isThundering);
        }
    }
}
