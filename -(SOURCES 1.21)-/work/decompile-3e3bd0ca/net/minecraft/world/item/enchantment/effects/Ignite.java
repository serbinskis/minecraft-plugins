package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3D;

public record Ignite(LevelBasedValue duration) implements EnchantmentEntityEffect {

    public static final MapCodec<Ignite> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LevelBasedValue.CODEC.fieldOf("duration").forGetter((ignite) -> {
            return ignite.duration;
        })).apply(instance, Ignite::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        entity.igniteForSeconds(this.duration.calculate(i));
    }

    @Override
    public MapCodec<Ignite> codec() {
        return Ignite.CODEC;
    }
}
