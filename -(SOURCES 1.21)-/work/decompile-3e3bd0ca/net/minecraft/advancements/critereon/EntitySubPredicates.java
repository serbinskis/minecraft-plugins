package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.EntityCat;
import net.minecraft.world.entity.animal.EntityFox;
import net.minecraft.world.entity.animal.EntityMushroomCow;
import net.minecraft.world.entity.animal.EntityParrot;
import net.minecraft.world.entity.animal.EntityRabbit;
import net.minecraft.world.entity.animal.EntityTropicalFish;
import net.minecraft.world.entity.animal.EntityWolf;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.EntityHorse;
import net.minecraft.world.entity.animal.horse.EntityLlama;
import net.minecraft.world.entity.animal.horse.HorseColor;
import net.minecraft.world.entity.decoration.EntityPainting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.phys.Vec3D;

public class EntitySubPredicates {

    public static final MapCodec<LightningBoltPredicate> LIGHTNING = register("lightning", LightningBoltPredicate.CODEC);
    public static final MapCodec<CriterionConditionInOpenWater> FISHING_HOOK = register("fishing_hook", CriterionConditionInOpenWater.CODEC);
    public static final MapCodec<CriterionConditionPlayer> PLAYER = register("player", CriterionConditionPlayer.CODEC);
    public static final MapCodec<SlimePredicate> SLIME = register("slime", SlimePredicate.CODEC);
    public static final MapCodec<RaiderPredicate> RAIDER = register("raider", RaiderPredicate.CODEC);
    public static final EntitySubPredicates.b<Axolotl.Variant> AXOLOTL = register("axolotl", EntitySubPredicates.b.create(Axolotl.Variant.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof Axolotl axolotl) {
            optional = Optional.of(axolotl.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<EntityBoat.EnumBoatType> BOAT = register("boat", EntitySubPredicates.b.create((Codec) EntityBoat.EnumBoatType.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityBoat entityboat) {
            optional = Optional.of(entityboat.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<EntityFox.Type> FOX = register("fox", EntitySubPredicates.b.create((Codec) EntityFox.Type.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityFox entityfox) {
            optional = Optional.of(entityfox.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<EntityMushroomCow.Type> MOOSHROOM = register("mooshroom", EntitySubPredicates.b.create((Codec) EntityMushroomCow.Type.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityMushroomCow entitymushroomcow) {
            optional = Optional.of(entitymushroomcow.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<EntityRabbit.Variant> RABBIT = register("rabbit", EntitySubPredicates.b.create(EntityRabbit.Variant.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityRabbit entityrabbit) {
            optional = Optional.of(entityrabbit.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<HorseColor> HORSE = register("horse", EntitySubPredicates.b.create(HorseColor.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityHorse entityhorse) {
            optional = Optional.of(entityhorse.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<EntityLlama.Variant> LLAMA = register("llama", EntitySubPredicates.b.create(EntityLlama.Variant.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityLlama entityllama) {
            optional = Optional.of(entityllama.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<VillagerType> VILLAGER = register("villager", EntitySubPredicates.b.create(BuiltInRegistries.VILLAGER_TYPE.byNameCodec(), (entity) -> {
        Optional optional;

        if (entity instanceof VillagerDataHolder villagerdataholder) {
            optional = Optional.of(villagerdataholder.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<EntityParrot.Variant> PARROT = register("parrot", EntitySubPredicates.b.create(EntityParrot.Variant.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityParrot entityparrot) {
            optional = Optional.of(entityparrot.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.b<EntityTropicalFish.Variant> TROPICAL_FISH = register("tropical_fish", EntitySubPredicates.b.create(EntityTropicalFish.Variant.CODEC, (entity) -> {
        Optional optional;

        if (entity instanceof EntityTropicalFish entitytropicalfish) {
            optional = Optional.of(entitytropicalfish.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.a<PaintingVariant> PAINTING = register("painting", EntitySubPredicates.a.create(Registries.PAINTING_VARIANT, (entity) -> {
        Optional optional;

        if (entity instanceof EntityPainting entitypainting) {
            optional = Optional.of(entitypainting.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.a<CatVariant> CAT = register("cat", EntitySubPredicates.a.create(Registries.CAT_VARIANT, (entity) -> {
        Optional optional;

        if (entity instanceof EntityCat entitycat) {
            optional = Optional.of(entitycat.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.a<FrogVariant> FROG = register("frog", EntitySubPredicates.a.create(Registries.FROG_VARIANT, (entity) -> {
        Optional optional;

        if (entity instanceof Frog frog) {
            optional = Optional.of(frog.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));
    public static final EntitySubPredicates.a<WolfVariant> WOLF = register("wolf", EntitySubPredicates.a.create(Registries.WOLF_VARIANT, (entity) -> {
        Optional optional;

        if (entity instanceof EntityWolf entitywolf) {
            optional = Optional.of(entitywolf.getVariant());
        } else {
            optional = Optional.empty();
        }

        return optional;
    }));

    public EntitySubPredicates() {}

    private static <T extends EntitySubPredicate> MapCodec<T> register(String s, MapCodec<T> mapcodec) {
        return (MapCodec) IRegistry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, s, mapcodec);
    }

    private static <V> EntitySubPredicates.b<V> register(String s, EntitySubPredicates.b<V> entitysubpredicates_b) {
        IRegistry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, s, entitysubpredicates_b.codec);
        return entitysubpredicates_b;
    }

    private static <V> EntitySubPredicates.a<V> register(String s, EntitySubPredicates.a<V> entitysubpredicates_a) {
        IRegistry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, s, entitysubpredicates_a.codec);
        return entitysubpredicates_a;
    }

    public static MapCodec<? extends EntitySubPredicate> bootstrap(IRegistry<MapCodec<? extends EntitySubPredicate>> iregistry) {
        return EntitySubPredicates.LIGHTNING;
    }

    public static EntitySubPredicate catVariant(Holder<CatVariant> holder) {
        return EntitySubPredicates.CAT.createPredicate(HolderSet.direct(holder));
    }

    public static EntitySubPredicate frogVariant(Holder<FrogVariant> holder) {
        return EntitySubPredicates.FROG.createPredicate(HolderSet.direct(holder));
    }

    public static EntitySubPredicate wolfVariant(HolderSet<WolfVariant> holderset) {
        return EntitySubPredicates.WOLF.createPredicate(holderset);
    }

    public static class b<V> {

        final MapCodec<EntitySubPredicates.b<V>.a> codec;
        final Function<Entity, Optional<V>> getter;

        public static <V> EntitySubPredicates.b<V> create(IRegistry<V> iregistry, Function<Entity, Optional<V>> function) {
            return new EntitySubPredicates.b<>(iregistry.byNameCodec(), function);
        }

        public static <V> EntitySubPredicates.b<V> create(Codec<V> codec, Function<Entity, Optional<V>> function) {
            return new EntitySubPredicates.b<>(codec, function);
        }

        public b(Codec<V> codec, Function<Entity, Optional<V>> function) {
            this.getter = function;
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                return instance.group(codec.fieldOf("variant").forGetter((entitysubpredicates_b_a) -> {
                    return entitysubpredicates_b_a.variant;
                })).apply(instance, (object) -> {
                    return new EntitySubPredicates.b.a(object);
                });
            });
        }

        public EntitySubPredicate createPredicate(V v0) {
            return new EntitySubPredicates.b.a(v0);
        }

        private class a implements EntitySubPredicate {

            final V variant;

            a(final Object object) {
                this.variant = object;
            }

            @Override
            public MapCodec<EntitySubPredicates.b<V>.a> codec() {
                return b.this.codec;
            }

            @Override
            public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
                Optional optional = (Optional) b.this.getter.apply(entity);
                Object object = this.variant;

                Objects.requireNonNull(this.variant);
                return optional.filter(object::equals).isPresent();
            }
        }
    }

    public static class a<V> {

        final MapCodec<EntitySubPredicates.a<V>.a> codec;
        final Function<Entity, Optional<Holder<V>>> getter;

        public static <V> EntitySubPredicates.a<V> create(ResourceKey<? extends IRegistry<V>> resourcekey, Function<Entity, Optional<Holder<V>>> function) {
            return new EntitySubPredicates.a<>(resourcekey, function);
        }

        public a(ResourceKey<? extends IRegistry<V>> resourcekey, Function<Entity, Optional<Holder<V>>> function) {
            this.getter = function;
            this.codec = RecordCodecBuilder.mapCodec((instance) -> {
                return instance.group(RegistryCodecs.homogeneousList(resourcekey).fieldOf("variant").forGetter((entitysubpredicates_a_a) -> {
                    return entitysubpredicates_a_a.variants;
                })).apply(instance, (holderset) -> {
                    return new EntitySubPredicates.a.a(holderset);
                });
            });
        }

        public EntitySubPredicate createPredicate(HolderSet<V> holderset) {
            return new EntitySubPredicates.a.a(holderset);
        }

        private class a implements EntitySubPredicate {

            final HolderSet<V> variants;

            a(final HolderSet holderset) {
                this.variants = holderset;
            }

            @Override
            public MapCodec<EntitySubPredicates.a<V>.a> codec() {
                return a.this.codec;
            }

            @Override
            public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
                Optional optional = (Optional) a.this.getter.apply(entity);
                HolderSet holderset = this.variants;

                Objects.requireNonNull(this.variants);
                return optional.filter(holderset::contains).isPresent();
            }
        }
    }
}
