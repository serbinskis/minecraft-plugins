package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.IRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

public class EntityVariantPredicate<V> {

    private final Function<Entity, Optional<V>> getter;
    private final EntitySubPredicate.a type;

    public static <V> EntityVariantPredicate<V> create(IRegistry<V> iregistry, Function<Entity, Optional<V>> function) {
        return new EntityVariantPredicate<>(iregistry.byNameCodec(), function);
    }

    public static <V> EntityVariantPredicate<V> create(Codec<V> codec, Function<Entity, Optional<V>> function) {
        return new EntityVariantPredicate<>(codec, function);
    }

    private EntityVariantPredicate(Codec<V> codec, Function<Entity, Optional<V>> function) {
        this.getter = function;
        MapCodec<EntityVariantPredicate.a<V>> mapcodec = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(codec.fieldOf("variant").forGetter(EntityVariantPredicate.a::variant)).apply(instance, this::createPredicate);
        });

        this.type = new EntitySubPredicate.a(mapcodec);
    }

    public EntitySubPredicate.a type() {
        return this.type;
    }

    public EntityVariantPredicate.a<V> createPredicate(V v0) {
        return new EntityVariantPredicate.a<>(this.type, this.getter, v0);
    }

    public static record a<V> (EntitySubPredicate.a type, Function<Entity, Optional<V>> getter, V variant) implements EntitySubPredicate {

        @Override
        public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
            return ((Optional) this.getter.apply(entity)).filter((object) -> {
                return object.equals(this.variant);
            }).isPresent();
        }
    }
}
