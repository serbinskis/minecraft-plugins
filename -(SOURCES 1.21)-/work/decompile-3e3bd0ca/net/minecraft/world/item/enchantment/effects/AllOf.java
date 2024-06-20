package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3D;

public interface AllOf {

    static <T, A extends T> MapCodec<A> codec(Codec<T> codec, Function<List<T>, A> function, Function<A, List<T>> function1) {
        return RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(codec.listOf().fieldOf("effects").forGetter(function1)).apply(instance, function);
        });
    }

    static AllOf.a entityEffects(EnchantmentEntityEffect... aenchantmententityeffect) {
        return new AllOf.a(List.of(aenchantmententityeffect));
    }

    static AllOf.b locationBasedEffects(EnchantmentLocationBasedEffect... aenchantmentlocationbasedeffect) {
        return new AllOf.b(List.of(aenchantmentlocationbasedeffect));
    }

    static AllOf.c valueEffects(EnchantmentValueEffect... aenchantmentvalueeffect) {
        return new AllOf.c(List.of(aenchantmentvalueeffect));
    }

    public static record a(List<EnchantmentEntityEffect> effects) implements EnchantmentEntityEffect {

        public static final MapCodec<AllOf.a> CODEC = AllOf.codec(EnchantmentEntityEffect.CODEC, AllOf.a::new, AllOf.a::effects);

        @Override
        public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
            Iterator iterator = this.effects.iterator();

            while (iterator.hasNext()) {
                EnchantmentEntityEffect enchantmententityeffect = (EnchantmentEntityEffect) iterator.next();

                enchantmententityeffect.apply(worldserver, i, enchantediteminuse, entity, vec3d);
            }

        }

        @Override
        public MapCodec<AllOf.a> codec() {
            return AllOf.a.CODEC;
        }
    }

    public static record b(List<EnchantmentLocationBasedEffect> effects) implements EnchantmentLocationBasedEffect {

        public static final MapCodec<AllOf.b> CODEC = AllOf.codec(EnchantmentLocationBasedEffect.CODEC, AllOf.b::new, AllOf.b::effects);

        @Override
        public void onChangedBlock(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, boolean flag) {
            Iterator iterator = this.effects.iterator();

            while (iterator.hasNext()) {
                EnchantmentLocationBasedEffect enchantmentlocationbasedeffect = (EnchantmentLocationBasedEffect) iterator.next();

                enchantmentlocationbasedeffect.onChangedBlock(worldserver, i, enchantediteminuse, entity, vec3d, flag);
            }

        }

        @Override
        public void onDeactivated(EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, int i) {
            Iterator iterator = this.effects.iterator();

            while (iterator.hasNext()) {
                EnchantmentLocationBasedEffect enchantmentlocationbasedeffect = (EnchantmentLocationBasedEffect) iterator.next();

                enchantmentlocationbasedeffect.onDeactivated(enchantediteminuse, entity, vec3d, i);
            }

        }

        @Override
        public MapCodec<AllOf.b> codec() {
            return AllOf.b.CODEC;
        }
    }

    public static record c(List<EnchantmentValueEffect> effects) implements EnchantmentValueEffect {

        public static final MapCodec<AllOf.c> CODEC = AllOf.codec(EnchantmentValueEffect.CODEC, AllOf.c::new, AllOf.c::effects);

        @Override
        public float process(int i, RandomSource randomsource, float f) {
            EnchantmentValueEffect enchantmentvalueeffect;

            for (Iterator iterator = this.effects.iterator(); iterator.hasNext(); f = enchantmentvalueeffect.process(i, randomsource, f)) {
                enchantmentvalueeffect = (EnchantmentValueEffect) iterator.next();
            }

            return f;
        }

        @Override
        public MapCodec<AllOf.c> codec() {
            return AllOf.c.CODEC;
        }
    }
}
