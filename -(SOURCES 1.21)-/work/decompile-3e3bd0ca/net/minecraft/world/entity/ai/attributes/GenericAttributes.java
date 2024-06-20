package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;

public class GenericAttributes {

    public static final Holder<AttributeBase> ARMOR = register("generic.armor", (new AttributeRanged("attribute.name.generic.armor", 0.0D, 0.0D, 30.0D)).setSyncable(true));
    public static final Holder<AttributeBase> ARMOR_TOUGHNESS = register("generic.armor_toughness", (new AttributeRanged("attribute.name.generic.armor_toughness", 0.0D, 0.0D, 20.0D)).setSyncable(true));
    public static final Holder<AttributeBase> ATTACK_DAMAGE = register("generic.attack_damage", new AttributeRanged("attribute.name.generic.attack_damage", 2.0D, 0.0D, 2048.0D));
    public static final Holder<AttributeBase> ATTACK_KNOCKBACK = register("generic.attack_knockback", new AttributeRanged("attribute.name.generic.attack_knockback", 0.0D, 0.0D, 5.0D));
    public static final Holder<AttributeBase> ATTACK_SPEED = register("generic.attack_speed", (new AttributeRanged("attribute.name.generic.attack_speed", 4.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> BLOCK_BREAK_SPEED = register("player.block_break_speed", (new AttributeRanged("attribute.name.player.block_break_speed", 1.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> BLOCK_INTERACTION_RANGE = register("player.block_interaction_range", (new AttributeRanged("attribute.name.player.block_interaction_range", 4.5D, 0.0D, 64.0D)).setSyncable(true));
    public static final Holder<AttributeBase> BURNING_TIME = register("generic.burning_time", (new AttributeRanged("attribute.name.generic.burning_time", 1.0D, 0.0D, 1024.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEGATIVE));
    public static final Holder<AttributeBase> EXPLOSION_KNOCKBACK_RESISTANCE = register("generic.explosion_knockback_resistance", (new AttributeRanged("attribute.name.generic.explosion_knockback_resistance", 0.0D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> ENTITY_INTERACTION_RANGE = register("player.entity_interaction_range", (new AttributeRanged("attribute.name.player.entity_interaction_range", 3.0D, 0.0D, 64.0D)).setSyncable(true));
    public static final Holder<AttributeBase> FALL_DAMAGE_MULTIPLIER = register("generic.fall_damage_multiplier", (new AttributeRanged("attribute.name.generic.fall_damage_multiplier", 1.0D, 0.0D, 100.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEGATIVE));
    public static final Holder<AttributeBase> FLYING_SPEED = register("generic.flying_speed", (new AttributeRanged("attribute.name.generic.flying_speed", 0.4D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> FOLLOW_RANGE = register("generic.follow_range", new AttributeRanged("attribute.name.generic.follow_range", 32.0D, 0.0D, 2048.0D));
    public static final Holder<AttributeBase> GRAVITY = register("generic.gravity", (new AttributeRanged("attribute.name.generic.gravity", 0.08D, -1.0D, 1.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEUTRAL));
    public static final Holder<AttributeBase> JUMP_STRENGTH = register("generic.jump_strength", (new AttributeRanged("attribute.name.generic.jump_strength", 0.41999998688697815D, 0.0D, 32.0D)).setSyncable(true));
    public static final Holder<AttributeBase> KNOCKBACK_RESISTANCE = register("generic.knockback_resistance", new AttributeRanged("attribute.name.generic.knockback_resistance", 0.0D, 0.0D, 1.0D));
    public static final Holder<AttributeBase> LUCK = register("generic.luck", (new AttributeRanged("attribute.name.generic.luck", 0.0D, -1024.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MAX_ABSORPTION = register("generic.max_absorption", (new AttributeRanged("attribute.name.generic.max_absorption", 0.0D, 0.0D, 2048.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MAX_HEALTH = register("generic.max_health", (new AttributeRanged("attribute.name.generic.max_health", 20.0D, 1.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MINING_EFFICIENCY = register("player.mining_efficiency", (new AttributeRanged("attribute.name.player.mining_efficiency", 0.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MOVEMENT_EFFICIENCY = register("generic.movement_efficiency", (new AttributeRanged("attribute.name.generic.movement_efficiency", 0.0D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> MOVEMENT_SPEED = register("generic.movement_speed", (new AttributeRanged("attribute.name.generic.movement_speed", 0.7D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> OXYGEN_BONUS = register("generic.oxygen_bonus", (new AttributeRanged("attribute.name.generic.oxygen_bonus", 0.0D, 0.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SAFE_FALL_DISTANCE = register("generic.safe_fall_distance", (new AttributeRanged("attribute.name.generic.safe_fall_distance", 3.0D, -1024.0D, 1024.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SCALE = register("generic.scale", (new AttributeRanged("attribute.name.generic.scale", 1.0D, 0.0625D, 16.0D)).setSyncable(true).setSentiment(AttributeBase.a.NEUTRAL));
    public static final Holder<AttributeBase> SNEAKING_SPEED = register("player.sneaking_speed", (new AttributeRanged("attribute.name.player.sneaking_speed", 0.3D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SPAWN_REINFORCEMENTS_CHANCE = register("zombie.spawn_reinforcements", new AttributeRanged("attribute.name.zombie.spawn_reinforcements", 0.0D, 0.0D, 1.0D));
    public static final Holder<AttributeBase> STEP_HEIGHT = register("generic.step_height", (new AttributeRanged("attribute.name.generic.step_height", 0.6D, 0.0D, 10.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SUBMERGED_MINING_SPEED = register("player.submerged_mining_speed", (new AttributeRanged("attribute.name.player.submerged_mining_speed", 0.2D, 0.0D, 20.0D)).setSyncable(true));
    public static final Holder<AttributeBase> SWEEPING_DAMAGE_RATIO = register("player.sweeping_damage_ratio", (new AttributeRanged("attribute.name.player.sweeping_damage_ratio", 0.0D, 0.0D, 1.0D)).setSyncable(true));
    public static final Holder<AttributeBase> WATER_MOVEMENT_EFFICIENCY = register("generic.water_movement_efficiency", (new AttributeRanged("attribute.name.generic.water_movement_efficiency", 0.0D, 0.0D, 1.0D)).setSyncable(true));

    public GenericAttributes() {}

    private static Holder<AttributeBase> register(String s, AttributeBase attributebase) {
        return IRegistry.registerForHolder(BuiltInRegistries.ATTRIBUTE, MinecraftKey.withDefaultNamespace(s), attributebase);
    }

    public static Holder<AttributeBase> bootstrap(IRegistry<AttributeBase> iregistry) {
        return GenericAttributes.MAX_HEALTH;
    }
}
