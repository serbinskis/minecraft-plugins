package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3D;

public record DamageEntity(LevelBasedValue minDamage, LevelBasedValue maxDamage, Holder<DamageType> damageType) implements EnchantmentEntityEffect {

    public static final MapCodec<DamageEntity> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LevelBasedValue.CODEC.fieldOf("min_damage").forGetter(DamageEntity::minDamage), LevelBasedValue.CODEC.fieldOf("max_damage").forGetter(DamageEntity::maxDamage), DamageType.CODEC.fieldOf("damage_type").forGetter(DamageEntity::damageType)).apply(instance, DamageEntity::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        float f = MathHelper.randomBetween(entity.getRandom(), this.minDamage.calculate(i), this.maxDamage.calculate(i));

        entity.hurt(new DamageSource(this.damageType, enchantediteminuse.owner()), f);
    }

    @Override
    public MapCodec<DamageEntity> codec() {
        return DamageEntity.CODEC;
    }
}
