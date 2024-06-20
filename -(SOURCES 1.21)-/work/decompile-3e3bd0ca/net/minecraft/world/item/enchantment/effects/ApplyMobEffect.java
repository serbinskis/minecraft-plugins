package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3D;

public record ApplyMobEffect(HolderSet<MobEffectList> toApply, LevelBasedValue minDuration, LevelBasedValue maxDuration, LevelBasedValue minAmplifier, LevelBasedValue maxAmplifier) implements EnchantmentEntityEffect {

    public static final MapCodec<ApplyMobEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyMobEffect::toApply), LevelBasedValue.CODEC.fieldOf("min_duration").forGetter(ApplyMobEffect::minDuration), LevelBasedValue.CODEC.fieldOf("max_duration").forGetter(ApplyMobEffect::maxDuration), LevelBasedValue.CODEC.fieldOf("min_amplifier").forGetter(ApplyMobEffect::minAmplifier), LevelBasedValue.CODEC.fieldOf("max_amplifier").forGetter(ApplyMobEffect::maxAmplifier)).apply(instance, ApplyMobEffect::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        if (entity instanceof EntityLiving entityliving) {
            RandomSource randomsource = entityliving.getRandom();
            Optional<Holder<MobEffectList>> optional = this.toApply.getRandomElement(randomsource);

            if (optional.isPresent()) {
                int j = Math.round(MathHelper.randomBetween(randomsource, this.minDuration.calculate(i), this.maxDuration.calculate(i)) * 20.0F);
                int k = Math.max(0, Math.round(MathHelper.randomBetween(randomsource, this.minAmplifier.calculate(i), this.maxAmplifier.calculate(i))));

                entityliving.addEffect(new MobEffect((Holder) optional.get(), j, k));
            }
        }

    }

    @Override
    public MapCodec<ApplyMobEffect> codec() {
        return ApplyMobEffect.CODEC;
    }
}
