package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3D;

public interface EnchantmentLocationBasedEffect {

    Codec<EnchantmentLocationBasedEffect> CODEC = BuiltInRegistries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE.byNameCodec().dispatch(EnchantmentLocationBasedEffect::codec, Function.identity());

    static MapCodec<? extends EnchantmentLocationBasedEffect> bootstrap(IRegistry<MapCodec<? extends EnchantmentLocationBasedEffect>> iregistry) {
        IRegistry.register(iregistry, "all_of", AllOf.b.CODEC);
        IRegistry.register(iregistry, "apply_mob_effect", ApplyMobEffect.CODEC);
        IRegistry.register(iregistry, "attribute", EnchantmentAttributeEffect.CODEC);
        IRegistry.register(iregistry, "damage_entity", DamageEntity.CODEC);
        IRegistry.register(iregistry, "damage_item", DamageItem.CODEC);
        IRegistry.register(iregistry, "explode", ExplodeEffect.CODEC);
        IRegistry.register(iregistry, "ignite", Ignite.CODEC);
        IRegistry.register(iregistry, "play_sound", PlaySoundEffect.CODEC);
        IRegistry.register(iregistry, "replace_block", ReplaceBlock.CODEC);
        IRegistry.register(iregistry, "replace_disk", ReplaceDisk.CODEC);
        IRegistry.register(iregistry, "run_function", RunFunction.CODEC);
        IRegistry.register(iregistry, "set_block_properties", SetBlockProperties.CODEC);
        IRegistry.register(iregistry, "spawn_particles", SpawnParticlesEffect.CODEC);
        return (MapCodec) IRegistry.register(iregistry, "summon_entity", SummonEntityEffect.CODEC);
    }

    void onChangedBlock(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, boolean flag);

    default void onDeactivated(EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, int i) {}

    MapCodec<? extends EnchantmentLocationBasedEffect> codec();
}
