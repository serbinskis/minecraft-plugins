package net.minecraft.advancements.critereon;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.AdvancementDataPlayer;
import net.minecraft.server.AdvancementDataWorld;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.stats.ServerStatisticManager;
import net.minecraft.stats.Statistic;
import net.minecraft.stats.StatisticManager;
import net.minecraft.stats.StatisticWrapper;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileHelper;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionEntity;
import net.minecraft.world.phys.Vec3D;

public record CriterionConditionPlayer(CriterionConditionValue.IntegerRange level, GameTypePredicate gameType, List<CriterionConditionPlayer.e<?>> stats, Object2BooleanMap<MinecraftKey> recipes, Map<MinecraftKey, CriterionConditionPlayer.c> advancements, Optional<CriterionConditionEntity> lookingAt) implements EntitySubPredicate {

    public static final int LOOKING_AT_RANGE = 100;
    public static final MapCodec<CriterionConditionPlayer> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("level", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionPlayer::level), GameTypePredicate.CODEC.optionalFieldOf("gamemode", GameTypePredicate.ANY).forGetter(CriterionConditionPlayer::gameType), CriterionConditionPlayer.e.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(CriterionConditionPlayer::stats), ExtraCodecs.object2BooleanMap(MinecraftKey.CODEC).optionalFieldOf("recipes", Object2BooleanMaps.emptyMap()).forGetter(CriterionConditionPlayer::recipes), Codec.unboundedMap(MinecraftKey.CODEC, CriterionConditionPlayer.c.CODEC).optionalFieldOf("advancements", Map.of()).forGetter(CriterionConditionPlayer::advancements), CriterionConditionEntity.CODEC.optionalFieldOf("looking_at").forGetter(CriterionConditionPlayer::lookingAt)).apply(instance, CriterionConditionPlayer::new);
    });

    @Override
    public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
        if (!(entity instanceof EntityPlayer entityplayer)) {
            return false;
        } else if (!this.level.matches(entityplayer.experienceLevel)) {
            return false;
        } else if (!this.gameType.matches(entityplayer.gameMode.getGameModeForPlayer())) {
            return false;
        } else {
            ServerStatisticManager serverstatisticmanager = entityplayer.getStats();
            Iterator iterator = this.stats.iterator();

            while (iterator.hasNext()) {
                CriterionConditionPlayer.e<?> criterionconditionplayer_e = (CriterionConditionPlayer.e) iterator.next();

                if (!criterionconditionplayer_e.matches(serverstatisticmanager)) {
                    return false;
                }
            }

            RecipeBookServer recipebookserver = entityplayer.getRecipeBook();
            ObjectIterator objectiterator = this.recipes.object2BooleanEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<MinecraftKey> entry = (Entry) objectiterator.next();

                if (recipebookserver.contains((MinecraftKey) entry.getKey()) != entry.getBooleanValue()) {
                    return false;
                }
            }

            if (!this.advancements.isEmpty()) {
                AdvancementDataPlayer advancementdataplayer = entityplayer.getAdvancements();
                AdvancementDataWorld advancementdataworld = entityplayer.getServer().getAdvancements();
                Iterator iterator1 = this.advancements.entrySet().iterator();

                while (iterator1.hasNext()) {
                    java.util.Map.Entry<MinecraftKey, CriterionConditionPlayer.c> java_util_map_entry = (java.util.Map.Entry) iterator1.next();
                    AdvancementHolder advancementholder = advancementdataworld.get((MinecraftKey) java_util_map_entry.getKey());

                    if (advancementholder == null || !((CriterionConditionPlayer.c) java_util_map_entry.getValue()).test(advancementdataplayer.getOrStartProgress(advancementholder))) {
                        return false;
                    }
                }
            }

            if (this.lookingAt.isPresent()) {
                Vec3D vec3d1 = entityplayer.getEyePosition();
                Vec3D vec3d2 = entityplayer.getViewVector(1.0F);
                Vec3D vec3d3 = vec3d1.add(vec3d2.x * 100.0D, vec3d2.y * 100.0D, vec3d2.z * 100.0D);
                MovingObjectPositionEntity movingobjectpositionentity = ProjectileHelper.getEntityHitResult(entityplayer.level(), entityplayer, vec3d1, vec3d3, (new AxisAlignedBB(vec3d1, vec3d3)).inflate(1.0D), (entity1) -> {
                    return !entity1.isSpectator();
                }, 0.0F);

                if (movingobjectpositionentity == null || movingobjectpositionentity.getType() != MovingObjectPosition.EnumMovingObjectType.ENTITY) {
                    return false;
                }

                Entity entity1 = movingobjectpositionentity.getEntity();

                if (!((CriterionConditionEntity) this.lookingAt.get()).matches(entityplayer, entity1) || !entityplayer.hasLineOfSight(entity1)) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public MapCodec<CriterionConditionPlayer> codec() {
        return EntitySubPredicates.PLAYER;
    }

    private static record e<T>(StatisticWrapper<T> type, Holder<T> value, CriterionConditionValue.IntegerRange range, Supplier<Statistic<T>> stat) {

        public static final Codec<CriterionConditionPlayer.e<?>> CODEC = BuiltInRegistries.STAT_TYPE.byNameCodec().dispatch(CriterionConditionPlayer.e::type, CriterionConditionPlayer.e::createTypedCodec);

        public e(StatisticWrapper<T> statisticwrapper, Holder<T> holder, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this(statisticwrapper, holder, criterionconditionvalue_integerrange, Suppliers.memoize(() -> {
                return statisticwrapper.get(holder.value());
            }));
        }

        private static <T> MapCodec<CriterionConditionPlayer.e<T>> createTypedCodec(StatisticWrapper<T> statisticwrapper) {
            return RecordCodecBuilder.mapCodec((instance) -> {
                return instance.group(statisticwrapper.getRegistry().holderByNameCodec().fieldOf("stat").forGetter(CriterionConditionPlayer.e::value), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("value", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionConditionPlayer.e::range)).apply(instance, (holder, criterionconditionvalue_integerrange) -> {
                    return new CriterionConditionPlayer.e<>(statisticwrapper, holder, criterionconditionvalue_integerrange);
                });
            });
        }

        public boolean matches(StatisticManager statisticmanager) {
            return this.range.matches(statisticmanager.getValue((Statistic) this.stat.get()));
        }
    }

    private interface c extends Predicate<AdvancementProgress> {

        Codec<CriterionConditionPlayer.c> CODEC = Codec.either(CriterionConditionPlayer.b.CODEC, CriterionConditionPlayer.a.CODEC).xmap(Either::unwrap, (criterionconditionplayer_c) -> {
            if (criterionconditionplayer_c instanceof CriterionConditionPlayer.b criterionconditionplayer_b) {
                return Either.left(criterionconditionplayer_b);
            } else if (criterionconditionplayer_c instanceof CriterionConditionPlayer.a criterionconditionplayer_a) {
                return Either.right(criterionconditionplayer_a);
            } else {
                throw new UnsupportedOperationException();
            }
        });
    }

    public static class d {

        private CriterionConditionValue.IntegerRange level;
        private GameTypePredicate gameType;
        private final Builder<CriterionConditionPlayer.e<?>> stats;
        private final Object2BooleanMap<MinecraftKey> recipes;
        private final Map<MinecraftKey, CriterionConditionPlayer.c> advancements;
        private Optional<CriterionConditionEntity> lookingAt;

        public d() {
            this.level = CriterionConditionValue.IntegerRange.ANY;
            this.gameType = GameTypePredicate.ANY;
            this.stats = ImmutableList.builder();
            this.recipes = new Object2BooleanOpenHashMap();
            this.advancements = Maps.newHashMap();
            this.lookingAt = Optional.empty();
        }

        public static CriterionConditionPlayer.d player() {
            return new CriterionConditionPlayer.d();
        }

        public CriterionConditionPlayer.d setLevel(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this.level = criterionconditionvalue_integerrange;
            return this;
        }

        public <T> CriterionConditionPlayer.d addStat(StatisticWrapper<T> statisticwrapper, Holder.c<T> holder_c, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            this.stats.add(new CriterionConditionPlayer.e<>(statisticwrapper, holder_c, criterionconditionvalue_integerrange));
            return this;
        }

        public CriterionConditionPlayer.d addRecipe(MinecraftKey minecraftkey, boolean flag) {
            this.recipes.put(minecraftkey, flag);
            return this;
        }

        public CriterionConditionPlayer.d setGameType(GameTypePredicate gametypepredicate) {
            this.gameType = gametypepredicate;
            return this;
        }

        public CriterionConditionPlayer.d setLookingAt(CriterionConditionEntity.a criterionconditionentity_a) {
            this.lookingAt = Optional.of(criterionconditionentity_a.build());
            return this;
        }

        public CriterionConditionPlayer.d checkAdvancementDone(MinecraftKey minecraftkey, boolean flag) {
            this.advancements.put(minecraftkey, new CriterionConditionPlayer.b(flag));
            return this;
        }

        public CriterionConditionPlayer.d checkAdvancementCriterions(MinecraftKey minecraftkey, Map<String, Boolean> map) {
            this.advancements.put(minecraftkey, new CriterionConditionPlayer.a(new Object2BooleanOpenHashMap(map)));
            return this;
        }

        public CriterionConditionPlayer build() {
            return new CriterionConditionPlayer(this.level, this.gameType, this.stats.build(), this.recipes, this.advancements, this.lookingAt);
        }
    }

    private static record a(Object2BooleanMap<String> criterions) implements CriterionConditionPlayer.c {

        public static final Codec<CriterionConditionPlayer.a> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING).xmap(CriterionConditionPlayer.a::new, CriterionConditionPlayer.a::criterions);

        public boolean test(AdvancementProgress advancementprogress) {
            ObjectIterator objectiterator = this.criterions.object2BooleanEntrySet().iterator();

            Entry entry;
            CriterionProgress criterionprogress;

            do {
                if (!objectiterator.hasNext()) {
                    return true;
                }

                entry = (Entry) objectiterator.next();
                criterionprogress = advancementprogress.getCriterion((String) entry.getKey());
            } while (criterionprogress != null && criterionprogress.isDone() == entry.getBooleanValue());

            return false;
        }
    }

    private static record b(boolean state) implements CriterionConditionPlayer.c {

        public static final Codec<CriterionConditionPlayer.b> CODEC = Codec.BOOL.xmap(CriterionConditionPlayer.b::new, CriterionConditionPlayer.b::state);

        public boolean test(AdvancementProgress advancementprogress) {
            return advancementprogress.isDone() == this.state;
        }
    }
}
