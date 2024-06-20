package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;

public record UniformGenerator(NumberProvider min, NumberProvider max) implements NumberProvider {

    public static final MapCodec<UniformGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(NumberProviders.CODEC.fieldOf("min").forGetter(UniformGenerator::min), NumberProviders.CODEC.fieldOf("max").forGetter(UniformGenerator::max)).apply(instance, UniformGenerator::new);
    });

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.UNIFORM;
    }

    public static UniformGenerator between(float f, float f1) {
        return new UniformGenerator(ConstantValue.exactly(f), ConstantValue.exactly(f1));
    }

    @Override
    public int getInt(LootTableInfo loottableinfo) {
        return MathHelper.nextInt(loottableinfo.getRandom(), this.min.getInt(loottableinfo), this.max.getInt(loottableinfo));
    }

    @Override
    public float getFloat(LootTableInfo loottableinfo) {
        return MathHelper.nextFloat(loottableinfo.getRandom(), this.min.getFloat(loottableinfo), this.max.getFloat(loottableinfo));
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return Sets.union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
    }
}
