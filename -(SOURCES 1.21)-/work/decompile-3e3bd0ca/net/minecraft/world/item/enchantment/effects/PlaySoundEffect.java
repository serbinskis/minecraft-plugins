package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3D;

public record PlaySoundEffect(Holder<SoundEffect> soundEvent, FloatProvider volume, FloatProvider pitch) implements EnchantmentEntityEffect {

    public static final MapCodec<PlaySoundEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(SoundEffect.CODEC.fieldOf("sound").forGetter(PlaySoundEffect::soundEvent), FloatProvider.codec(1.0E-5F, 10.0F).fieldOf("volume").forGetter(PlaySoundEffect::volume), FloatProvider.codec(1.0E-5F, 2.0F).fieldOf("pitch").forGetter(PlaySoundEffect::pitch)).apply(instance, PlaySoundEffect::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        RandomSource randomsource = entity.getRandom();

        if (!entity.isSilent()) {
            worldserver.playSound((EntityHuman) null, vec3d.x(), vec3d.y(), vec3d.z(), this.soundEvent, entity.getSoundSource(), this.volume.sample(randomsource), this.pitch.sample(randomsource));
        }

    }

    @Override
    public MapCodec<PlaySoundEffect> codec() {
        return PlaySoundEffect.CODEC;
    }
}
