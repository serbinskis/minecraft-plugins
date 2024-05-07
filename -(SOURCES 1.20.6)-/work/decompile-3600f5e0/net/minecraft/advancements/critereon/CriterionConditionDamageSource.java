package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3D;

public record CriterionConditionDamageSource(List<TagPredicate<DamageType>> tags, Optional<CriterionConditionEntity> directEntity, Optional<CriterionConditionEntity> sourceEntity) {

    public static final Codec<CriterionConditionDamageSource> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(TagPredicate.codec(Registries.DAMAGE_TYPE).listOf().optionalFieldOf("tags", List.of()).forGetter(CriterionConditionDamageSource::tags), CriterionConditionEntity.CODEC.optionalFieldOf("direct_entity").forGetter(CriterionConditionDamageSource::directEntity), CriterionConditionEntity.CODEC.optionalFieldOf("source_entity").forGetter(CriterionConditionDamageSource::sourceEntity)).apply(instance, CriterionConditionDamageSource::new);
    });

    public boolean matches(EntityPlayer entityplayer, DamageSource damagesource) {
        return this.matches(entityplayer.serverLevel(), entityplayer.position(), damagesource);
    }

    public boolean matches(WorldServer worldserver, Vec3D vec3d, DamageSource damagesource) {
        Iterator iterator = this.tags.iterator();

        TagPredicate tagpredicate;

        do {
            if (!iterator.hasNext()) {
                if (this.directEntity.isPresent() && !((CriterionConditionEntity) this.directEntity.get()).matches(worldserver, vec3d, damagesource.getDirectEntity())) {
                    return false;
                }

                if (this.sourceEntity.isPresent() && !((CriterionConditionEntity) this.sourceEntity.get()).matches(worldserver, vec3d, damagesource.getEntity())) {
                    return false;
                }

                return true;
            }

            tagpredicate = (TagPredicate) iterator.next();
        } while (tagpredicate.matches(damagesource.typeHolder()));

        return false;
    }

    public static class a {

        private final Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
        private Optional<CriterionConditionEntity> directEntity = Optional.empty();
        private Optional<CriterionConditionEntity> sourceEntity = Optional.empty();

        public a() {}

        public static CriterionConditionDamageSource.a damageType() {
            return new CriterionConditionDamageSource.a();
        }

        public CriterionConditionDamageSource.a tag(TagPredicate<DamageType> tagpredicate) {
            this.tags.add(tagpredicate);
            return this;
        }

        public CriterionConditionDamageSource.a direct(CriterionConditionEntity.a criterionconditionentity_a) {
            this.directEntity = Optional.of(criterionconditionentity_a.build());
            return this;
        }

        public CriterionConditionDamageSource.a source(CriterionConditionEntity.a criterionconditionentity_a) {
            this.sourceEntity = Optional.of(criterionconditionentity_a.build());
            return this;
        }

        public CriterionConditionDamageSource build() {
            return new CriterionConditionDamageSource(this.tags.build(), this.directEntity, this.sourceEntity);
        }
    }
}
