package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemCrossbow;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;

public interface EnchantmentEffectComponents {

    Codec<DataComponentType<?>> COMPONENT_CODEC = Codec.lazyInitialized(() -> {
        return BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE.byNameCodec();
    });
    Codec<DataComponentMap> CODEC = DataComponentMap.makeCodec(EnchantmentEffectComponents.COMPONENT_CODEC);
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> DAMAGE_PROTECTION = register("damage_protection", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<ConditionalEffect<DamageImmunity>>> DAMAGE_IMMUNITY = register("damage_immunity", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(DamageImmunity.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> DAMAGE = register("damage", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> SMASH_DAMAGE_PER_FALLEN_BLOCK = register("smash_damage_per_fallen_block", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> KNOCKBACK = register("knockback", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> ARMOR_EFFECTIVENESS = register("armor_effectiveness", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<TargetedConditionalEffect<EnchantmentEntityEffect>>> POST_ATTACK = register("post_attack", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(TargetedConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> HIT_BLOCK = register("hit_block", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParameterSets.HIT_BLOCK).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> ITEM_DAMAGE = register("item_damage", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ITEM).listOf());
    });
    DataComponentType<List<EnchantmentAttributeEffect>> ATTRIBUTES = register("attributes", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnchantmentAttributeEffect.CODEC.codec().listOf());
    });
    DataComponentType<List<TargetedConditionalEffect<EnchantmentValueEffect>>> EQUIPMENT_DROPS = register("equipment_drops", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(TargetedConditionalEffect.equipmentDropsCodec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_DAMAGE).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentLocationBasedEffect>>> LOCATION_CHANGED = register("location_changed", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentLocationBasedEffect.CODEC, LootContextParameterSets.ENCHANTED_LOCATION).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> TICK = register("tick", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> AMMO_USE = register("ammo_use", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ITEM).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> PROJECTILE_PIERCING = register("projectile_piercing", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ITEM).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> PROJECTILE_SPAWNED = register("projectile_spawned", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> PROJECTILE_SPREAD = register("projectile_spread", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> PROJECTILE_COUNT = register("projectile_count", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> TRIDENT_RETURN_ACCELERATION = register("trident_return_acceleration", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> FISHING_TIME_REDUCTION = register("fishing_time_reduction", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> FISHING_LUCK_BONUS = register("fishing_luck_bonus", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> BLOCK_EXPERIENCE = register("block_experience", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ITEM).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> MOB_EXPERIENCE = register("mob_experience", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ENTITY).listOf());
    });
    DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> REPAIR_WITH_XP = register("repair_with_xp", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParameterSets.ENCHANTED_ITEM).listOf());
    });
    DataComponentType<EnchantmentValueEffect> CROSSBOW_CHARGE_TIME = register("crossbow_charge_time", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnchantmentValueEffect.CODEC);
    });
    DataComponentType<List<ItemCrossbow.a>> CROSSBOW_CHARGING_SOUNDS = register("crossbow_charging_sounds", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(ItemCrossbow.a.CODEC.listOf());
    });
    DataComponentType<List<Holder<SoundEffect>>> TRIDENT_SOUND = register("trident_sound", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(SoundEffect.CODEC.listOf());
    });
    DataComponentType<Unit> PREVENT_EQUIPMENT_DROP = register("prevent_equipment_drop", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Unit.CODEC);
    });
    DataComponentType<Unit> PREVENT_ARMOR_CHANGE = register("prevent_armor_change", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(Unit.CODEC);
    });
    DataComponentType<EnchantmentValueEffect> TRIDENT_SPIN_ATTACK_STRENGTH = register("trident_spin_attack_strength", (datacomponenttype_a) -> {
        return datacomponenttype_a.persistent(EnchantmentValueEffect.CODEC);
    });

    static DataComponentType<?> bootstrap(IRegistry<DataComponentType<?>> iregistry) {
        return EnchantmentEffectComponents.DAMAGE_PROTECTION;
    }

    private static <T> DataComponentType<T> register(String s, UnaryOperator<DataComponentType.a<T>> unaryoperator) {
        return (DataComponentType) IRegistry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, s, ((DataComponentType.a) unaryoperator.apply(DataComponentType.builder())).build());
    }
}
