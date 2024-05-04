package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.EntityTypes;

public interface TagsEntity {

    TagKey<EntityTypes<?>> SKELETONS = create("skeletons");
    TagKey<EntityTypes<?>> ZOMBIES = create("zombies");
    TagKey<EntityTypes<?>> RAIDERS = create("raiders");
    TagKey<EntityTypes<?>> UNDEAD = create("undead");
    TagKey<EntityTypes<?>> BEEHIVE_INHABITORS = create("beehive_inhabitors");
    TagKey<EntityTypes<?>> ARROWS = create("arrows");
    TagKey<EntityTypes<?>> IMPACT_PROJECTILES = create("impact_projectiles");
    TagKey<EntityTypes<?>> POWDER_SNOW_WALKABLE_MOBS = create("powder_snow_walkable_mobs");
    TagKey<EntityTypes<?>> AXOLOTL_ALWAYS_HOSTILES = create("axolotl_always_hostiles");
    TagKey<EntityTypes<?>> AXOLOTL_HUNT_TARGETS = create("axolotl_hunt_targets");
    TagKey<EntityTypes<?>> FREEZE_IMMUNE_ENTITY_TYPES = create("freeze_immune_entity_types");
    TagKey<EntityTypes<?>> FREEZE_HURTS_EXTRA_TYPES = create("freeze_hurts_extra_types");
    TagKey<EntityTypes<?>> CAN_BREATHE_UNDER_WATER = create("can_breathe_under_water");
    TagKey<EntityTypes<?>> FROG_FOOD = create("frog_food");
    TagKey<EntityTypes<?>> FALL_DAMAGE_IMMUNE = create("fall_damage_immune");
    TagKey<EntityTypes<?>> DISMOUNTS_UNDERWATER = create("dismounts_underwater");
    TagKey<EntityTypes<?>> NON_CONTROLLING_RIDER = create("non_controlling_rider");
    TagKey<EntityTypes<?>> DEFLECTS_PROJECTILES = create("deflects_projectiles");
    TagKey<EntityTypes<?>> CAN_TURN_IN_BOATS = create("can_turn_in_boats");
    TagKey<EntityTypes<?>> ILLAGER = create("illager");
    TagKey<EntityTypes<?>> AQUATIC = create("aquatic");
    TagKey<EntityTypes<?>> ARTHROPOD = create("arthropod");
    TagKey<EntityTypes<?>> IGNORES_POISON_AND_REGEN = create("ignores_poison_and_regen");
    TagKey<EntityTypes<?>> INVERTED_HEALING_AND_HARM = create("inverted_healing_and_harm");
    TagKey<EntityTypes<?>> WITHER_FRIENDS = create("wither_friends");
    TagKey<EntityTypes<?>> ILLAGER_FRIENDS = create("illager_friends");
    TagKey<EntityTypes<?>> NOT_SCARY_FOR_PUFFERFISH = create("not_scary_for_pufferfish");
    TagKey<EntityTypes<?>> SENSITIVE_TO_IMPALING = create("sensitive_to_impaling");
    TagKey<EntityTypes<?>> SENSITIVE_TO_BANE_OF_ARTHROPODS = create("sensitive_to_bane_of_arthropods");
    TagKey<EntityTypes<?>> SENSITIVE_TO_SMITE = create("sensitive_to_smite");
    TagKey<EntityTypes<?>> NO_ANGER_FROM_WIND_CHARGE = create("no_anger_from_wind_charge");
    TagKey<EntityTypes<?>> IMMUNE_TO_OOZING = create("immune_to_oozing");
    TagKey<EntityTypes<?>> IMMUNE_TO_INFESTED = create("immune_to_infested");
    TagKey<EntityTypes<?>> REDIRECTABLE_PROJECTILE = create("redirectable_projectile");

    private static TagKey<EntityTypes<?>> create(String s) {
        return TagKey.create(Registries.ENTITY_TYPE, new MinecraftKey(s));
    }
}
