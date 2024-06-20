package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3D;

public record ExplodeEffect(boolean attributeToUser, Optional<Holder<DamageType>> damageType, Optional<LevelBasedValue> knockbackMultiplier, Optional<HolderSet<Block>> immuneBlocks, Vec3D offset, LevelBasedValue radius, boolean createFire, World.a blockInteraction, ParticleParam smallParticle, ParticleParam largeParticle, Holder<SoundEffect> sound) implements EnchantmentEntityEffect {

    public static final MapCodec<ExplodeEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("attribute_to_user", false).forGetter(ExplodeEffect::attributeToUser), DamageType.CODEC.optionalFieldOf("damage_type").forGetter(ExplodeEffect::damageType), LevelBasedValue.CODEC.optionalFieldOf("knockback_multiplier").forGetter(ExplodeEffect::knockbackMultiplier), RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("immune_blocks").forGetter(ExplodeEffect::immuneBlocks), Vec3D.CODEC.optionalFieldOf("offset", Vec3D.ZERO).forGetter(ExplodeEffect::offset), LevelBasedValue.CODEC.fieldOf("radius").forGetter(ExplodeEffect::radius), Codec.BOOL.optionalFieldOf("create_fire", false).forGetter(ExplodeEffect::createFire), World.a.CODEC.fieldOf("block_interaction").forGetter(ExplodeEffect::blockInteraction), Particles.CODEC.fieldOf("small_particle").forGetter(ExplodeEffect::smallParticle), Particles.CODEC.fieldOf("large_particle").forGetter(ExplodeEffect::largeParticle), SoundEffect.CODEC.fieldOf("sound").forGetter(ExplodeEffect::sound)).apply(instance, ExplodeEffect::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        Vec3D vec3d1 = vec3d.add(this.offset);

        worldserver.explode(this.attributeToUser ? entity : null, this.getDamageSource(entity, vec3d1), new SimpleExplosionDamageCalculator(this.blockInteraction != World.a.NONE, this.damageType.isPresent(), this.knockbackMultiplier.map((levelbasedvalue) -> {
            return levelbasedvalue.calculate(i);
        }), this.immuneBlocks), vec3d1.x(), vec3d1.y(), vec3d1.z(), Math.max(this.radius.calculate(i), 0.0F), this.createFire, this.blockInteraction, this.smallParticle, this.largeParticle, this.sound);
    }

    @Nullable
    private DamageSource getDamageSource(Entity entity, Vec3D vec3d) {
        return this.damageType.isEmpty() ? null : (this.attributeToUser ? new DamageSource((Holder) this.damageType.get(), entity) : new DamageSource((Holder) this.damageType.get(), vec3d));
    }

    @Override
    public MapCodec<ExplodeEffect> codec() {
        return ExplodeEffect.CODEC;
    }
}
