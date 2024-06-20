package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3D;

public record SpawnParticlesEffect(ParticleParam particle, SpawnParticlesEffect.a horizontalPosition, SpawnParticlesEffect.a verticalPosition, SpawnParticlesEffect.c horizontalVelocity, SpawnParticlesEffect.c verticalVelocity, FloatProvider speed) implements EnchantmentEntityEffect {

    public static final MapCodec<SpawnParticlesEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Particles.CODEC.fieldOf("particle").forGetter(SpawnParticlesEffect::particle), SpawnParticlesEffect.a.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticlesEffect::horizontalPosition), SpawnParticlesEffect.a.CODEC.fieldOf("vertical_position").forGetter(SpawnParticlesEffect::verticalPosition), SpawnParticlesEffect.c.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticlesEffect::horizontalVelocity), SpawnParticlesEffect.c.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticlesEffect::verticalVelocity), FloatProvider.CODEC.optionalFieldOf("speed", ConstantFloat.ZERO).forGetter(SpawnParticlesEffect::speed)).apply(instance, SpawnParticlesEffect::new);
    });

    public static SpawnParticlesEffect.a offsetFromEntityPosition(float f) {
        return new SpawnParticlesEffect.a(SpawnParticlesEffect.b.ENTITY_POSITION, f, 1.0F);
    }

    public static SpawnParticlesEffect.a inBoundingBox() {
        return new SpawnParticlesEffect.a(SpawnParticlesEffect.b.BOUNDING_BOX, 0.0F, 1.0F);
    }

    public static SpawnParticlesEffect.c movementScaled(float f) {
        return new SpawnParticlesEffect.c(f, ConstantFloat.ZERO);
    }

    public static SpawnParticlesEffect.c fixedVelocity(FloatProvider floatprovider) {
        return new SpawnParticlesEffect.c(0.0F, floatprovider);
    }

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        RandomSource randomsource = entity.getRandom();
        Vec3D vec3d1 = entity.getKnownMovement();
        float f = entity.getBbWidth();
        float f1 = entity.getBbHeight();

        worldserver.sendParticles(this.particle, this.horizontalPosition.getCoordinate(vec3d.x(), vec3d.x(), f, randomsource), this.verticalPosition.getCoordinate(vec3d.y(), vec3d.y() + (double) (f1 / 2.0F), f1, randomsource), this.horizontalPosition.getCoordinate(vec3d.z(), vec3d.z(), f, randomsource), 0, this.horizontalVelocity.getVelocity(vec3d1.x(), randomsource), this.verticalVelocity.getVelocity(vec3d1.y(), randomsource), this.horizontalVelocity.getVelocity(vec3d1.z(), randomsource), (double) this.speed.sample(randomsource));
    }

    @Override
    public MapCodec<SpawnParticlesEffect> codec() {
        return SpawnParticlesEffect.CODEC;
    }

    public static record a(SpawnParticlesEffect.b type, float offset, float scale) {

        public static final MapCodec<SpawnParticlesEffect.a> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(SpawnParticlesEffect.b.CODEC.fieldOf("type").forGetter(SpawnParticlesEffect.a::type), Codec.FLOAT.optionalFieldOf("offset", 0.0F).forGetter(SpawnParticlesEffect.a::offset), ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1.0F).forGetter(SpawnParticlesEffect.a::scale)).apply(instance, SpawnParticlesEffect.a::new);
        }).validate((spawnparticleseffect_a) -> {
            return spawnparticleseffect_a.type() == SpawnParticlesEffect.b.ENTITY_POSITION && spawnparticleseffect_a.scale() != 1.0F ? DataResult.error(() -> {
                return "Cannot scale an entity position coordinate source";
            }) : DataResult.success(spawnparticleseffect_a);
        });

        public double getCoordinate(double d0, double d1, float f, RandomSource randomsource) {
            return this.type.getCoordinate(d0, d1, f * this.scale, randomsource) + (double) this.offset;
        }
    }

    public static record c(float movementScale, FloatProvider base) {

        public static final MapCodec<SpawnParticlesEffect.c> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.FLOAT.optionalFieldOf("movement_scale", 0.0F).forGetter(SpawnParticlesEffect.c::movementScale), FloatProvider.CODEC.optionalFieldOf("base", ConstantFloat.ZERO).forGetter(SpawnParticlesEffect.c::base)).apply(instance, SpawnParticlesEffect.c::new);
        });

        public double getVelocity(double d0, RandomSource randomsource) {
            return d0 * (double) this.movementScale + (double) this.base.sample(randomsource);
        }
    }

    public static enum b implements INamable {

        ENTITY_POSITION("entity_position", (d0, d1, f, randomsource) -> {
            return d0;
        }), BOUNDING_BOX("in_bounding_box", (d0, d1, f, randomsource) -> {
            return d1 + (randomsource.nextDouble() - 0.5D) * (double) f;
        });

        public static final Codec<SpawnParticlesEffect.b> CODEC = INamable.fromEnum(SpawnParticlesEffect.b::values);
        private final String id;
        private final SpawnParticlesEffect.b.a source;

        private b(final String s, final SpawnParticlesEffect.b.a spawnparticleseffect_b_a) {
            this.id = s;
            this.source = spawnparticleseffect_b_a;
        }

        public double getCoordinate(double d0, double d1, float f, RandomSource randomsource) {
            return this.source.getCoordinate(d0, d1, f, randomsource);
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        @FunctionalInterface
        private interface a {

            double getCoordinate(double d0, double d1, float f, RandomSource randomsource);
        }
    }
}
