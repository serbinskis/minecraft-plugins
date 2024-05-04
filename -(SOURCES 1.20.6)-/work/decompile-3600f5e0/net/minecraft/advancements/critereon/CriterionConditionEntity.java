package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionEntityProperty;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.scores.ScoreboardTeam;

public record CriterionConditionEntity(Optional<CriterionConditionEntityType> entityType, Optional<CriterionConditionDistance> distanceToPlayer, Optional<CriterionConditionLocation> location, Optional<CriterionConditionLocation> steppingOnLocation, Optional<CriterionConditionMobEffect> effects, Optional<CriterionConditionNBT> nbt, Optional<CriterionConditionEntityFlags> flags, Optional<CriterionConditionEntityEquipment> equipment, Optional<EntitySubPredicate> subPredicate, Optional<CriterionConditionEntity> vehicle, Optional<CriterionConditionEntity> passenger, Optional<CriterionConditionEntity> targetedEntity, Optional<String> team, Optional<SlotsPredicate> slots) {

    public static final Codec<CriterionConditionEntity> CODEC = Codec.recursive("EntityPredicate", (codec) -> {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(CriterionConditionEntityType.CODEC.optionalFieldOf("type").forGetter(CriterionConditionEntity::entityType), CriterionConditionDistance.CODEC.optionalFieldOf("distance").forGetter(CriterionConditionEntity::distanceToPlayer), CriterionConditionLocation.CODEC.optionalFieldOf("location").forGetter(CriterionConditionEntity::location), CriterionConditionLocation.CODEC.optionalFieldOf("stepping_on").forGetter(CriterionConditionEntity::steppingOnLocation), CriterionConditionMobEffect.CODEC.optionalFieldOf("effects").forGetter(CriterionConditionEntity::effects), CriterionConditionNBT.CODEC.optionalFieldOf("nbt").forGetter(CriterionConditionEntity::nbt), CriterionConditionEntityFlags.CODEC.optionalFieldOf("flags").forGetter(CriterionConditionEntity::flags), CriterionConditionEntityEquipment.CODEC.optionalFieldOf("equipment").forGetter(CriterionConditionEntity::equipment), EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(CriterionConditionEntity::subPredicate), codec.optionalFieldOf("vehicle").forGetter(CriterionConditionEntity::vehicle), codec.optionalFieldOf("passenger").forGetter(CriterionConditionEntity::passenger), codec.optionalFieldOf("targeted_entity").forGetter(CriterionConditionEntity::targetedEntity), Codec.STRING.optionalFieldOf("team").forGetter(CriterionConditionEntity::team), SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(CriterionConditionEntity::slots)).apply(instance, CriterionConditionEntity::new);
        });
    });
    public static final Codec<ContextAwarePredicate> ADVANCEMENT_CODEC = Codec.withAlternative(ContextAwarePredicate.CODEC, CriterionConditionEntity.CODEC, CriterionConditionEntity::wrap);

    public static ContextAwarePredicate wrap(CriterionConditionEntity.a criterionconditionentity_a) {
        return wrap(criterionconditionentity_a.build());
    }

    public static Optional<ContextAwarePredicate> wrap(Optional<CriterionConditionEntity> optional) {
        return optional.map(CriterionConditionEntity::wrap);
    }

    public static List<ContextAwarePredicate> wrap(CriterionConditionEntity.a... acriterionconditionentity_a) {
        return Stream.of(acriterionconditionentity_a).map(CriterionConditionEntity::wrap).toList();
    }

    public static ContextAwarePredicate wrap(CriterionConditionEntity criterionconditionentity) {
        LootItemCondition lootitemcondition = LootItemConditionEntityProperty.hasProperties(LootTableInfo.EntityTarget.THIS, criterionconditionentity).build();

        return new ContextAwarePredicate(List.of(lootitemcondition));
    }

    public boolean matches(EntityPlayer entityplayer, @Nullable Entity entity) {
        return this.matches(entityplayer.serverLevel(), entityplayer.position(), entity);
    }

    public boolean matches(WorldServer worldserver, @Nullable Vec3D vec3d, @Nullable Entity entity) {
        if (entity == null) {
            return false;
        } else if (this.entityType.isPresent() && !((CriterionConditionEntityType) this.entityType.get()).matches(entity.getType())) {
            return false;
        } else {
            if (vec3d == null) {
                if (this.distanceToPlayer.isPresent()) {
                    return false;
                }
            } else if (this.distanceToPlayer.isPresent() && !((CriterionConditionDistance) this.distanceToPlayer.get()).matches(vec3d.x, vec3d.y, vec3d.z, entity.getX(), entity.getY(), entity.getZ())) {
                return false;
            }

            if (this.location.isPresent() && !((CriterionConditionLocation) this.location.get()).matches(worldserver, entity.getX(), entity.getY(), entity.getZ())) {
                return false;
            } else {
                if (this.steppingOnLocation.isPresent()) {
                    Vec3D vec3d1 = Vec3D.atCenterOf(entity.getOnPos());

                    if (!((CriterionConditionLocation) this.steppingOnLocation.get()).matches(worldserver, vec3d1.x(), vec3d1.y(), vec3d1.z())) {
                        return false;
                    }
                }

                if (this.effects.isPresent() && !((CriterionConditionMobEffect) this.effects.get()).matches(entity)) {
                    return false;
                } else if (this.flags.isPresent() && !((CriterionConditionEntityFlags) this.flags.get()).matches(entity)) {
                    return false;
                } else if (this.equipment.isPresent() && !((CriterionConditionEntityEquipment) this.equipment.get()).matches(entity)) {
                    return false;
                } else if (this.subPredicate.isPresent() && !((EntitySubPredicate) this.subPredicate.get()).matches(entity, worldserver, vec3d)) {
                    return false;
                } else if (this.vehicle.isPresent() && !((CriterionConditionEntity) this.vehicle.get()).matches(worldserver, vec3d, entity.getVehicle())) {
                    return false;
                } else if (this.passenger.isPresent() && entity.getPassengers().stream().noneMatch((entity1) -> {
                    return ((CriterionConditionEntity) this.passenger.get()).matches(worldserver, vec3d, entity1);
                })) {
                    return false;
                } else if (this.targetedEntity.isPresent() && !((CriterionConditionEntity) this.targetedEntity.get()).matches(worldserver, vec3d, entity instanceof EntityInsentient ? ((EntityInsentient) entity).getTarget() : null)) {
                    return false;
                } else {
                    if (this.team.isPresent()) {
                        ScoreboardTeam scoreboardteam = entity.getTeam();

                        if (scoreboardteam == null || !((String) this.team.get()).equals(scoreboardteam.getName())) {
                            return false;
                        }
                    }

                    return this.slots.isPresent() && !((SlotsPredicate) this.slots.get()).matches(entity) ? false : !this.nbt.isPresent() || ((CriterionConditionNBT) this.nbt.get()).matches(entity);
                }
            }
        }
    }

    public static LootTableInfo createContext(EntityPlayer entityplayer, Entity entity) {
        LootParams lootparams = (new LootParams.a(entityplayer.serverLevel())).withParameter(LootContextParameters.THIS_ENTITY, entity).withParameter(LootContextParameters.ORIGIN, entityplayer.position()).create(LootContextParameterSets.ADVANCEMENT_ENTITY);

        return (new LootTableInfo.Builder(lootparams)).create(Optional.empty());
    }

    public static class a {

        private Optional<CriterionConditionEntityType> entityType = Optional.empty();
        private Optional<CriterionConditionDistance> distanceToPlayer = Optional.empty();
        private Optional<CriterionConditionLocation> location = Optional.empty();
        private Optional<CriterionConditionLocation> steppingOnLocation = Optional.empty();
        private Optional<CriterionConditionMobEffect> effects = Optional.empty();
        private Optional<CriterionConditionNBT> nbt = Optional.empty();
        private Optional<CriterionConditionEntityFlags> flags = Optional.empty();
        private Optional<CriterionConditionEntityEquipment> equipment = Optional.empty();
        private Optional<EntitySubPredicate> subPredicate = Optional.empty();
        private Optional<CriterionConditionEntity> vehicle = Optional.empty();
        private Optional<CriterionConditionEntity> passenger = Optional.empty();
        private Optional<CriterionConditionEntity> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();
        private Optional<SlotsPredicate> slots = Optional.empty();

        public a() {}

        public static CriterionConditionEntity.a entity() {
            return new CriterionConditionEntity.a();
        }

        public CriterionConditionEntity.a of(EntityTypes<?> entitytypes) {
            this.entityType = Optional.of(CriterionConditionEntityType.of(entitytypes));
            return this;
        }

        public CriterionConditionEntity.a of(TagKey<EntityTypes<?>> tagkey) {
            this.entityType = Optional.of(CriterionConditionEntityType.of(tagkey));
            return this;
        }

        public CriterionConditionEntity.a entityType(CriterionConditionEntityType criterionconditionentitytype) {
            this.entityType = Optional.of(criterionconditionentitytype);
            return this;
        }

        public CriterionConditionEntity.a distance(CriterionConditionDistance criterionconditiondistance) {
            this.distanceToPlayer = Optional.of(criterionconditiondistance);
            return this;
        }

        public CriterionConditionEntity.a located(CriterionConditionLocation.a criterionconditionlocation_a) {
            this.location = Optional.of(criterionconditionlocation_a.build());
            return this;
        }

        public CriterionConditionEntity.a steppingOn(CriterionConditionLocation.a criterionconditionlocation_a) {
            this.steppingOnLocation = Optional.of(criterionconditionlocation_a.build());
            return this;
        }

        public CriterionConditionEntity.a effects(CriterionConditionMobEffect.a criterionconditionmobeffect_a) {
            this.effects = criterionconditionmobeffect_a.build();
            return this;
        }

        public CriterionConditionEntity.a nbt(CriterionConditionNBT criterionconditionnbt) {
            this.nbt = Optional.of(criterionconditionnbt);
            return this;
        }

        public CriterionConditionEntity.a flags(CriterionConditionEntityFlags.a criterionconditionentityflags_a) {
            this.flags = Optional.of(criterionconditionentityflags_a.build());
            return this;
        }

        public CriterionConditionEntity.a equipment(CriterionConditionEntityEquipment.a criterionconditionentityequipment_a) {
            this.equipment = Optional.of(criterionconditionentityequipment_a.build());
            return this;
        }

        public CriterionConditionEntity.a equipment(CriterionConditionEntityEquipment criterionconditionentityequipment) {
            this.equipment = Optional.of(criterionconditionentityequipment);
            return this;
        }

        public CriterionConditionEntity.a subPredicate(EntitySubPredicate entitysubpredicate) {
            this.subPredicate = Optional.of(entitysubpredicate);
            return this;
        }

        public CriterionConditionEntity.a vehicle(CriterionConditionEntity.a criterionconditionentity_a) {
            this.vehicle = Optional.of(criterionconditionentity_a.build());
            return this;
        }

        public CriterionConditionEntity.a passenger(CriterionConditionEntity.a criterionconditionentity_a) {
            this.passenger = Optional.of(criterionconditionentity_a.build());
            return this;
        }

        public CriterionConditionEntity.a targetedEntity(CriterionConditionEntity.a criterionconditionentity_a) {
            this.targetedEntity = Optional.of(criterionconditionentity_a.build());
            return this;
        }

        public CriterionConditionEntity.a team(String s) {
            this.team = Optional.of(s);
            return this;
        }

        public CriterionConditionEntity.a slots(SlotsPredicate slotspredicate) {
            this.slots = Optional.of(slotspredicate);
            return this;
        }

        public CriterionConditionEntity build() {
            return new CriterionConditionEntity(this.entityType, this.distanceToPlayer, this.location, this.steppingOnLocation, this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.vehicle, this.passenger, this.targetedEntity, this.team, this.slots);
        }
    }
}
